// AM -->
package eu.kanade.tachiyomi.ui.setting.controllers

import android.app.Activity
import android.content.Intent
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.data.connections.ConnectionsService
import eu.kanade.tachiyomi.data.preference.PreferenceKeys
import eu.kanade.tachiyomi.ui.base.controller.pushController
import eu.kanade.tachiyomi.ui.setting.connections.DiscordLoginActivity
import eu.kanade.tachiyomi.util.preference.add
import eu.kanade.tachiyomi.util.preference.iconRes
import eu.kanade.tachiyomi.util.preference.onClick
import eu.kanade.tachiyomi.util.preference.titleRes
import eu.kanade.tachiyomi.widget.preference.TrackerPreference
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController
import uy.kohesive.injekt.injectLazy
import eu.kanade.tachiyomi.data.preference.PreferenceKeys as Keys

class SettingsConnectionsController : SettingsLegacyController() {

    private val connectionsManager: ConnectionsManager by injectLazy()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        titleRes = R.string.pref_category_connections

        connectionPreference(connectionsManager.discord, SettingsDiscordController()) {
            val intent = Intent(activity!!, DiscordLoginActivity::class.java)
            startActivityForResult(intent, connectionsManager.discord.id.toInt())
        }
    }

    private inline fun PreferenceGroup.connectionPreference(
        service: ConnectionsService,
        controller: SettingsController,
        crossinline login: () -> Unit,
    ): TrackerPreference {
        return add(
            TrackerPreference(context).apply {
                key = Keys.connectionToken(service.id)
                titleRes = service.nameRes()
                iconRes = service.getLogo()
                iconColor = service.getLogoColor()
                onClick {
                    if (service.isLogged) router.pushController(controller)
                    else login()
                }
            },
        )
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        updatePreference(connectionsManager.discord.id)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                connectionsManager.discord.id.toInt() -> router.pushController(SettingsDiscordController())
            }
        }
    }

    private fun updatePreference(id: Long) {
        val pref = findPreference(PreferenceKeys.connectionToken(id)) as? TrackerPreference
        pref?.notifyChanged()
    }
}
// AM <--
