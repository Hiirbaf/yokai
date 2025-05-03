package eu.kanade.tachiyomi.ui.setting.controllers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import eu.kanade.presentation.scene.settings.SettingsScene
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.setting.SettingsViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import tachiyomi.domain.connections.ConnectionsPreferences
import tachiyomi.domain.connections.service.ConnectionsManager
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.dialog.Dialog
import tachiyomi.presentation.core.components.material.dialog.LogoutConnectionsDialog
import yokai.presentation.component.preference.Preference
import yokai.presentation.component.preference.PreferenceScreen

@Composable
fun SettingsDiscordScreen() {
    val navigator = LocalNavigator.current
    val settingsViewModel = getViewModel<SettingsViewModel>()
    val connectionsManager = settingsViewModel.connectionsManager
    val enableDRPCPref = settingsViewModel.enableDiscordRPCPref
    val useChapterTitlesPref = settingsViewModel.useChapterTitlesPref
    val discordRPCStatus = settingsViewModel.discordRPCStatusPref

    val enableDRPC by enableDRPCPref.collectAsState()

    var dialog by remember { mutableStateOf<Dialog?>(null) }

    SettingsScene(
        title = stringResource(MR.strings.pref_category_connections),
        onBackPressed = { navigator?.pop() },
    ) {
        PreferenceScreen(
            items = persistentListOf(
                Preference.PreferenceGroup(
                    title = stringResource(MR.strings.connections_discord),
                    preferenceItems = persistentListOf(
                        Preference.PreferenceItem.SwitchPreference(
                            pref = enableDRPCPref,
                            title = stringResource(MR.strings.pref_enable_discord_rpc),
                        ),
                        Preference.PreferenceItem.SwitchPreference(
                            pref = useChapterTitlesPref,
                            enabled = enableDRPC,
                            title = stringResource(MR.strings.show_chapters_titles_title),
                            subtitle = stringResource(MR.strings.show_chapters_titles_subtitle),
                        ),
                        Preference.PreferenceItem.ListPreference(
                            pref = discordRPCStatus,
                            title = stringResource(MR.strings.pref_discord_status),
                            entries = persistentMapOf(
                                -1 to stringResource(MR.strings.pref_discord_dnd),
                                0 to stringResource(MR.strings.pref_discord_idle),
                                1 to stringResource(MR.strings.pref_discord_online),
                            ),
                            enabled = enableDRPC,
                        ),
                    ),
                ),
                Preference.PreferenceGroup(
                    title = stringResource(MR.strings.pref_category_misc),
                    preferenceItems = persistentListOf(
                        Preference.PreferenceItem.TextPreference(
                            title = stringResource(MR.strings.logout),
                            onClick = {
                                dialog = LogoutConnectionsDialog(connectionsManager.discord)
                            },
                        ),
                    ),
                ),
            ),
        )

        dialog?.Content()
    }
}
