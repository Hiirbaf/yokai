package eu.kanade.tachiyomi.network

import android.content.Context
import android.webkit.WebSettings
import eu.kanade.tachiyomi.core.preference.PreferenceStore

class NetworkPreferences(
    private val preferenceStore: PreferenceStore,
    private val verboseLogging: Boolean,
    private val context: Context, // Context necesario para obtener el User-Agent del sistema
) {

    fun verboseLogging() = preferenceStore.getBoolean("verbose_logging", verboseLogging)

    fun dohProvider() = preferenceStore.getInt("doh_provider", -1)

    fun defaultUserAgent(): String {
        val fallback = try {
            WebSettings.getDefaultUserAgent(context)
        } catch (e: Exception) {
            DEFAULT_USER_AGENT
        }
        return preferenceStore.getString("default_user_agent", fallback)
    }

    companion object {
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:132.0) Gecko/20100101 Firefox/132.0"
    }
}
