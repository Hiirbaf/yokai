package eu.kanade.tachiyomi.util.discord

import android.content.Context
import android.util.Log
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Wrapper singleton sobre KizzyRPC para mostrar Discord Rich Presence
 * mientras el usuario está leyendo en Yōkai.
 *
 * Uso:
 *   DiscordRpcManager.getInstance(context).update(manga, chapter, coverUrl)
 *   DiscordRpcManager.getInstance(context).clear()
 */
class DiscordRpcManager private constructor(private val context: Context) : KoinComponent {

    private val preferences: PreferencesHelper by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Instancia de KizzyRPC — se inicializa lazy para no abrir el socket si RPC está desactivado
    private var rpc: KizzyRpc? = null

    // Discord Application ID del developer portal (puede sobreescribirse en ajustes)
    private val appId: String
        get() = preferences.discordRpcAppId().get().ifBlank { DEFAULT_APP_ID }

    /**
     * Actualiza la presencia con los datos del manga/capítulo actual.
     * No-op si el RPC está desactivado en preferencias.
     */
    fun update(
        mangaTitle: String,
        chapterName: String,
        coverUrl: String? = null,
    ) {
        if (!preferences.discordRpcEnabled().get()) return

        scope.launch {
            runCatching {
                val instance = getOrCreateRpc()
                instance.setActivity(
                    name = "Yōkai",
                    details = mangaTitle,
                    state = chapterName,
                    largeImage = coverUrl ?: "yokai_logo",
                    largeText = mangaTitle,
                    smallImage = "reading_icon",
                    smallText = "Leyendo",
                    startTimestamp = System.currentTimeMillis() / 1000L,
                    // Botón opcional — enlaza al tracker si hay MAL/AL ID
                    buttons = emptyList(),
                )
            }.onFailure { e ->
                Log.e(TAG, "Error actualizando Discord RPC", e)
                rpc = null // forzar reconexión en el siguiente intento
            }
        }
    }

    /**
     * Limpia la presencia y cierra el socket WebSocket.
     * Llamar desde ReaderActivity.onDestroy() o cuando el usuario
     * cambie la preferencia a desactivado.
     */
    fun clear() {
        scope.launch {
            runCatching {
                rpc?.closeRPC()
            }
            rpc = null
        }
    }

    /**
     * Libera todos los recursos. Llamar una única vez al destruir el módulo.
     */
    fun destroy() {
        clear()
        scope.cancel()
    }

    // -------------------------------------------------------------------------

    private suspend fun getOrCreateRpc(): KizzyRpc {
        if (rpc == null) {
            rpc = KizzyRpc(appId, context).also { it.connect() }
        }
        return rpc!!
    }

    companion object {
        private const val TAG = "DiscordRpcManager"

        /**
         * Application ID por defecto — reemplaza con el tuyo en el Discord Developer Portal.
         * https://discord.com/developers/applications
         */
        const val DEFAULT_APP_ID = "1234567890123456789"

        @Volatile
        private var instance: DiscordRpcManager? = null

        fun getInstance(context: Context): DiscordRpcManager {
            return instance ?: synchronized(this) {
                instance ?: DiscordRpcManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
