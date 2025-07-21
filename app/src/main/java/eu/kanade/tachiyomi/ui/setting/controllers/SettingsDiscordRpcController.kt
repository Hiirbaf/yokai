package eu.kanade.tachiyomi.ui.setting.controllers

import android.content.Context
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController

class SettingsDiscordRpcController : SettingsLegacyController() {

    override fun setupPreferenceScreen(screen: PreferenceScreen): PreferenceScreen = screen.apply {
        title = "Discord RPC"

        val context: Context = preferenceManager.context

        // Preferencia para activar/desactivar el RPC
        val enableRpcPref = SwitchPreferenceCompat(context).apply {
            key = "pref_discord_rpc_enabled"
            title = "Habilitar Discord Rich Presence"
            summary = "Muestra lo que estás leyendo en Discord"

            setDefaultValue(true)
        }

        addPreference(enableRpcPref)

        // Aquí puedes agregar más configuraciones si lo necesitás, como detalles adicionales
    }
}
