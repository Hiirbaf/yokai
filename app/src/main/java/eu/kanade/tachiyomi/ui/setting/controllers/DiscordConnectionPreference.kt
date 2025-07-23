package eu.kanade.tachiyomi.ui.setting.controllers

import android.os.Bundle
import androidx.preference.PreferenceScreen
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.ui.setting.connections.ConnectionsLoginDialog
import eu.kanade.tachiyomi.ui.setting.connections.ConnectionsLogoutDialog
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.base.controller.SettingsController
import uy.kohesive.injekt.injectLazy

class SettingsConnectionsController : SettingsController() {

    private val connectionsManager: ConnectionsManager by injectLazy()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = R.string.pref_category_connections

        val category = PreferenceCategory(context).apply {
            title = context.getString(R.string.special_services)
        }
        addPreference(category)

        val discordConnection = connectionsManager.discord

        // Preferencia de login/logout dinámica
        category.addPreference(
            Preference(context).apply {
                title = context.getString(discordConnection.nameRes())
                summary = if (discordConnection.isLogged) {
                    context.getString(R.string.logout)
                } else {
                    context.getString(R.string.login)
                }

                setOnPreferenceClickListener {
                    if (discordConnection.isLogged) {
                        // Mostrar diálogo de logout
                        ConnectionsLogoutDialog(discordConnection).show(
                            router.activity.supportFragmentManager,
                            "logout_dialog"
                        )
                    } else {
                        // Mostrar diálogo de login
                        ConnectionsLoginDialog(discordConnection).show(
                            router.activity.supportFragmentManager,
                            "login_dialog"
                        )
                    }
                    true
                }
            }
        )

        category.addPreference(
            Preference(context).apply {
                summary = context.getString(R.string.connections_discord_info)
                isSelectable = false
            }
        )

        category.addPreference(
            Preference(context).apply {
                summary = context.getString(R.string.connections_info)
                isSelectable = false
            }
        )
    }
}
