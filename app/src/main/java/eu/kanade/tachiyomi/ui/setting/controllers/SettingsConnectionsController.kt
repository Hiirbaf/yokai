package eu.kanade.tachiyomi.ui.setting.controllers

import android.app.Activity
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.data.track.TrackPreferences
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController
import eu.kanade.tachiyomi.ui.setting.preferenceCategory
import eu.kanade.tachiyomi.widget.preference.TrackLoginDialog
import eu.kanade.tachiyomi.widget.preference.TrackLogoutDialog
import eu.kanade.tachiyomi.widget.preference.TrackerPreference
import uy.kohesive.injekt.injectLazy
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import yokai.domain.connections.ConnectionsManager
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.openDiscordLoginActivity

class SettingsConnectionsController :
    SettingsLegacyController(),
    TrackLoginDialog.Listener,
    TrackLogoutDialog.Listener {

    private val trackManager: TrackManager by injectLazy()
    private val trackPreferences: TrackPreferences by injectLazy()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        titleRes = MR.strings.tracking

        preferenceCategory {
            titleRes = MR.strings.services

            // Servicios de tracking
            trackPreference(trackManager.myAnimeList) {
                activity?.openInBrowser(MyAnimeListApi.authUrl(), trackManager.myAnimeList.getLogoColor(), true)
            }
            trackPreference(trackManager.shikimori) {
                activity?.openInBrowser(ShikimoriApi.authUrl(), trackManager.shikimori.getLogoColor(), true)
            }
            trackPreference(trackManager.bangumi) {
                activity?.openInBrowser(BangumiApi.authUrl(), trackManager.bangumi.getLogoColor(), true)
            }

            // Servicio de Discord desde Compose (usando ComposePreference)
            add(ComposePreference(context).apply {
                key = "discord_connection"
                title = context.getString(MR.strings.discord)
                setContent {
                    DiscordConnectionsPreference()
                }
            })

            infoPreference(MR.strings.tracking_info)
        }
    }

    private inline fun PreferenceGroup.trackPreference(
        service: TrackService,
        crossinline login: () -> Unit = { },
    ): TrackerPreference {
        return add(
            TrackerPreference(context).apply {
                key = trackPreferences.trackUsername(service).key()
                title = context.getString(service.nameRes())
                iconRes = service.getLogo()
                iconColor = service.getLogoColor()
                onClick {
                    if (service.isLogged) {
                        val dialog = TrackLogoutDialog(service)
                        dialog.targetController = this@SettingsConnectionsController
                        dialog.showDialog(router)
                    } else {
                        login()
                    }
                }
            },
        )
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        updatePreference(trackManager.myAnimeList)
        updatePreference(trackManager.shikimori)
        updatePreference(trackManager.bangumi)
    }

    private fun updatePreference(service: TrackService) {
        val pref = findPreference(trackPreferences.trackUsername(service).key()) as? TrackerPreference
        pref?.notifyChanged()
    }

    override fun trackLoginDialogClosed(service: TrackService) {
        updatePreference(service)
    }

    override fun trackLogoutDialogClosed(service: TrackService) {
        updatePreference(service)
    }
}

@Composable
fun DiscordConnectionsPreference() {
    val context = LocalContext.current
    val connectionsManager = remember { Injekt.get<ConnectionsManager>() }
    val service = connectionsManager.discord

    var showLoginDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        ConnectionsLoginDialog(
            service = service,
            uNameStringRes = MR.strings.username,
            onDismissRequest = { showLoginDialog = false },
        )
    }

    if (showLogoutDialog) {
        ConnectionsLogoutDialog(
            service = service,
            onDismissRequest = { showLogoutDialog = false },
        )
    }

    ConnectionsPreference(
        title = stringResource(service.nameRes()),
        service = service,
        login = { showLoginDialog = true },
        openSettings = {
            if (service.isLogged()) {
                showLogoutDialog = true
            } else {
                context.openDiscordLoginActivity()
            }
        }
    )
}
