package eu.kanade.tachiyomi.ui.setting.controllers

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.util.fastMap
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import yokai.domain.connections.service.ConnectionsPreferences
//import eu.kanade.presentation.category.visualName
import yokai.presentation.component.preference.Preference
import yokai.presentation.component.preference.widget.TriStateListDialog
import eu.kanade.tachiyomi.data.connection.ConnectionsManager
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.runBlocking
import yokai.domain.category.interactor.GetCategories
import yokai.i18n.MR
import eu.kanade.tachiyomi.appwidget.util.stringResource
import eu.kanade.tachiyomi.core.storage.preference.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.presentation.settings.ComposableSettings

object SettingsDiscordScreen : Screen, ComposableSettings {

    @Composable
    override fun Content() {
        super<ComposableSettings>.Content()
    }

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_connections

    @Composable
    override fun RowScope.AppBarAction() {
        val uriHandler = LocalUriHandler.current
        IconButton(onClick = { uriHandler.openUri("https://tachiyomi.org/help/guides/tracking/") }) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = stringResource(MR.strings.tracking_guide),
            )
        }
    }

    @Composable
    override fun getPreferences(): List<Preference> {
        //val navigator = LocalNavigator.currentOrThrow
        val connectionsPreferences = remember { Injekt.get<ConnectionsPreferences>() }
        val connectionsManager = remember { Injekt.get<ConnectionsManager>() }
        val enableDRPCPref = connectionsPreferences.enableDiscordRPC()
        val useChapterTitlesPref = connectionsPreferences.useChapterTitles()
        val discordRPCStatus = connectionsPreferences.discordRPCStatus()

        val enableDRPC by enableDRPCPref.collectAsState()
        val useChapterTitles by useChapterTitlesPref.collectAsState()

        var dialog by remember { mutableStateOf<Any?>(null) }
        dialog?.run {
            when (this) {
                is LogoutConnectionsDialog -> {
                    ConnectionsLogoutDialog(
                        service = service,
                        onDismissRequest = {
                            dialog = null
                            enableDRPCPref.set(false)
                        },
                    )
                }
            }
        }

        return listOf(
            /*Preference.PreferenceItem.TextPreference(
                title = stringResource(MR.strings.discord_accounts),
                onClick = { navigator.push(DiscordAccountsScreen) },
            ),*/
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
            /*getRPCIncognitoGroup(
                connectionsPreferences = connectionsPreferences,
                enabled = enableDRPC,
            ),*/
            Preference.PreferenceItem.TextPreference(
                title = stringResource(MR.strings.logout),
                onClick = { dialog = LogoutConnectionsDialog(connectionsManager.discord) },
            ),
        )
    }

    /*@Composable
    private fun getRPCIncognitoGroup(
        connectionsPreferences: ConnectionsPreferences,
        enabled: Boolean,
    ): Preference.PreferenceGroup {
        val getCategories = remember { Injekt.get<GetCategories>() }
        val allCategories by getCategories.subscribe().collectAsState(initial = runBlocking { getCategories.await() })

        val discordRPCIncognitoPref = connectionsPreferences.discordRPCIncognito()
        val discordRPCIncognitoCategoriesPref = connectionsPreferences.discordRPCIncognitoCategories()

        val includedManga by discordRPCIncognitoCategoriesPref.collectAsState()
        var showDialog by rememberSaveable { mutableStateOf(false) }
        if (showDialog) {
            TriStateListDialog(
                title = stringResource(MR.strings.general_categories),
                message = stringResource(MR.strings.pref_discord_incognito_categories_details),
                items = allCategories,
                initialChecked = includedManga.mapNotNull { id -> allCategories.find { it.id.toString() == id } },
                initialInversed = includedManga.mapNotNull { allCategories.find { false } },
                itemLabel = { it.visualName },
                onDismissRequest = { showDialog = false },
                onValueChanged = { newIncluded, _ ->
                    discordRPCIncognitoCategoriesPref.set(
                        newIncluded.fastMap { it.id.toString() }
                            .toSet(),
                    )
                    showDialog = false
                },
                onlyChecked = true,
            )
        }

        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.general_categories),
            preferenceItems = persistentListOf(
                Preference.PreferenceItem.SwitchPreference(
                    preference = discordRPCIncognitoPref,
                    title = stringResource(MR.strings.pref_discord_incognito),
                    subtitle = stringResource(MR.strings.pref_discord_incognito_summary),
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(MR.strings.general_categories),
                    subtitle = getCategoriesLabel(
                        allCategories = allCategories,
                        included = includedManga,
                    ),
                    onClick = { showDialog = true },
                ),
                Preference.PreferenceItem.InfoPreference(stringResource(MR.strings.pref_discord_incognito_categories_details)),
            ),
            enabled = enabled,
        )
    }*/
}
