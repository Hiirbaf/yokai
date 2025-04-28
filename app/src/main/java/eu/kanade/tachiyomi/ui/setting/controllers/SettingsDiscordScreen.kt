package eu.kanade.tachiyomi.ui.setting.controllers

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.resources.compose.stringResource as mokoStringResource
import eu.kanade.presentation.category.visualName
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.data.database.models.Category
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.runBlocking
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.domain.category.interactor.GetCategories
import yokai.domain.connections.service.ConnectionsPreferences
import yokai.i18n.MR
import yokai.presentation.component.preference.Preference
import yokai.presentation.component.preference.widget.TriStateListDialog
import yokai.presentation.settings.ComposableSettings
import kotlinx.coroutines.flow.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import tachiyomi.domain.entries.manga.interactor.fastMap


object SettingsDiscordScreen : Screen, ComposableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_connections

    @Composable
    override fun RowScope.AppBarAction() {
        val uriHandler = LocalUriHandler.current
        IconButton(onClick = { uriHandler.openUri("https://tachiyomi.org/help/guides/tracking/") }) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = mokoStringResource(MR.strings.tracking_guide),
            )
        }
    }

    @Composable
    override fun getPreferences(): List<Preference> {
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
            Preference.PreferenceGroup(
                title = mokoStringResource(MR.strings.connections_discord),
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.SwitchPreference(
                        pref = enableDRPCPref,
                        title = mokoStringResource(MR.strings.pref_enable_discord_rpc),
                    ),
                    Preference.PreferenceItem.SwitchPreference(
                        pref = useChapterTitlesPref,
                        enabled = enableDRPC,
                        title = mokoStringResource(MR.strings.show_chapters_titles_title),
                        subtitle = mokoStringResource(MR.strings.show_chapters_titles_subtitle),
                    ),
                    Preference.PreferenceItem.ListPreference(
                        pref = discordRPCStatus,
                        title = mokoStringResource(MR.strings.pref_discord_status),
                        entries = persistentMapOf(
                            -1 to mokoStringResource(MR.strings.pref_discord_dnd),
                            0 to mokoStringResource(MR.strings.pref_discord_idle),
                            1 to mokoStringResource(MR.strings.pref_discord_online),
                        ),
                        enabled = enableDRPC,
                    ),
                ),
            ),
            getRPCIncognitoGroup(
                connectionsPreferences = connectionsPreferences,
                enabled = enableDRPC,
            ),
            Preference.PreferenceItem.TextPreference(
                title = mokoStringResource(MR.strings.logout),
                onClick = { dialog = LogoutConnectionsDialog(connectionsManager.discord) },
            ),
        )
    }

    @Composable
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
                title = mokoStringResource(MR.strings.general_categories),
                message = mokoStringResource(MR.strings.pref_discord_incognito_categories_details),
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

        val categoriesLabel = remember(allCategories, includedManga) {
            val includedCategories = allCategories.filter { includedManga.contains(it.id.toString()) }
            includedCategories.takeIf { it.isNotEmpty() }
                ?.joinToString { it.visualName }
        }

        return Preference.PreferenceGroup(
            title = mokoStringResource(MR.strings.general_categories),
            preferenceItems = persistentListOf(
                Preference.PreferenceItem.SwitchPreference(
                    pref = discordRPCIncognitoPref,
                    title = mokoStringResource(MR.strings.pref_discord_incognito),
                    subtitle = mokoStringResource(MR.strings.pref_discord_incognito_summary),
                ),
                Preference.PreferenceItem.TextPreference(
                    title = mokoStringResource(MR.strings.general_categories),
                    subtitle = categoriesLabel ?: mokoStringResource(MR.strings.none),
                    onClick = { showDialog = true },
                ),
                Preference.PreferenceItem.InfoPreference(
                    mokoStringResource(MR.strings.pref_discord_incognito_categories_details),
                ),
            ),
            enabled = enabled,
        )
    }
}
