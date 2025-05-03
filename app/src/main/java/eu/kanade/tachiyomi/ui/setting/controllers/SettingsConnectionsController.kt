package eu.kanade.tachiyomi.ui.setting.controllers

import android.app.Activity
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.data.track.TrackPreferences
import eu.kanade.tachiyomi.data.track.TrackService
import eu.kanade.tachiyomi.data.track.bangumi.BangumiApi
import eu.kanade.tachiyomi.data.track.myanimelist.MyAnimeListApi
import eu.kanade.tachiyomi.data.track.shikimori.ShikimoriApi
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController
import eu.kanade.tachiyomi.ui.setting.add
import eu.kanade.tachiyomi.ui.setting.iconRes
import eu.kanade.tachiyomi.ui.setting.infoPreference
import eu.kanade.tachiyomi.ui.setting.onClick
import eu.kanade.tachiyomi.ui.setting.preferenceCategory
import eu.kanade.tachiyomi.ui.setting.composePreference
import eu.kanade.tachiyomi.ui.setting.titleMRes as titleRes
import eu.kanade.tachiyomi.util.system.openInBrowser
import eu.kanade.tachiyomi.widget.preference.TrackLoginDialog
import eu.kanade.tachiyomi.widget.preference.TrackLogoutDialog
import eu.kanade.tachiyomi.widget.preference.TrackerPreference
import uy.kohesive.injekt.injectLazy
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource as stringResourceInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.input.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Close
import kotlinx.coroutines.launch
import tachiyomi.presentation.core.components.material.*
import tachiyomi.presentation.core.screens.Preference
import tachiyomi.presentation.core.screens.Preference.PreferenceItem.ConnectionsPreference
import tachiyomi.presentation.core.screens.Preference.PreferenceItem.InfoPreference
import tachiyomi.presentation.core.screens.Preference.PreferenceGroup
import tachiyomi.presentation.core.screens.LocalNavigator
import tachiyomi.presentation.core.toast
import tachiyomi.core.util.lang.withUIContext
import tachiyomi.domain.connections.ConnectionsService
import tachiyomi.domain.connections.ConnectionsManager
import tachiyomi.presentation.core.util.openDiscordLoginActivity

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

            // Servicio de Discord desde Compose
            composePreference {
                DiscordConnectionsPreference()
            }

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
    val navigator = LocalNavigator.currentOrThrow
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
