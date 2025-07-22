package eu.kanade.tachiyomi.ui.setting.controllers

import android.content.Context
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.openDiscordLoginActivity
import yokai.i18n.MR
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController

class SettingsDiscordRpcController : SettingsLegacyController() {

    override fun setupPreferenceScreen(screen: PreferenceScreen): PreferenceScreen = screen.apply {
    title = "Discord RPC"

    val context = preferenceManager.context

    val connectionPref = DiscordConnectionPreference(context).apply {
        title = "Discord"
        onLoginClick = {
            context.openDiscordLoginActivity()
        }
        onSettingsClick = {
            //router.pushController(SettingsDiscordScreen().withFadeTransaction())
            context.toast("Configuración avanzada no disponible todavía.")
        }
    }

    addPreference(connectionPref)
    }
}
