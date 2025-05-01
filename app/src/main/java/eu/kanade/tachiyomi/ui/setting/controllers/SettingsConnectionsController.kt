package eu.kanade.tachiyomi.ui.setting.controllers

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.data.connections.ConnectionsService
import eu.kanade.tachiyomi.ui.base.controller.SettingsController
import kotlinx.coroutines.launch
import yokai.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import eu.kanade.tachiyomi.util.system.toast
import androidx.compose.ui.platform.LocalContext
import tachiyomi.ui.setting.Preference
import tachiyomi.ui.setting.PreferenceScreen
import tachiyomi.ui.setting.SettingsDiscordController

class SettingsConnectionsController : SettingsController() {

    @Composable
    override fun getTitleRes() = MR.strings.pref_category_connections

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        val context = screen.preferenceManager.context
        val navigator = router

        val connectionsManager = Injekt.get<ConnectionsManager>()

        // Agregar preferencias
        screen.addPreference(
            Preference.PreferenceGroup(
                title = stringResource(MR.strings.special_services),
                preferenceItems = listOf(
                    Preference.PreferenceItem.ConnectionsPreference(
                        title = stringResource(connectionsManager.discord.nameRes()),
                        service = connectionsManager.discord,
                        login = {
                            // Muestra el diálogo de login
                            showLoginDialog(context, connectionsManager.discord)
                        },
                        openSettings = {
                            // Abre la pantalla de configuración de Discord
                            navigator.pushController(SettingsDiscordController())
                        }
                    )
                )
            )
        )
    }

    private fun showLoginDialog(context: Context, service: ConnectionsService) {
        // Mostrar diálogo de login (esto puede ser un fragmento o un diálogo completo)
        // Aquí llamarías a la lógica para abrir el `LoginConnectionsDialog`
        val dialog = LoginConnectionsDialog(service, MR.strings.username)
        // Para implementarlo, probablemente necesitarías usar un `Dialog` o algo similar
    }

    // Esta función es un ejemplo de cómo gestionar el login
    private suspend fun checkLogin(
        context: Context,
        service: ConnectionsService,
        username: String,
        password: String
    ): Boolean {
        return try {
            service.login(username, password)
            context.toast(MR.strings.login_success)
            true
        } catch (e: Exception) {
            service.logout()
            context.toast(e.message ?: "Login failed")
            false
        }
    }
}
