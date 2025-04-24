package yokai.presentation.settings.screen

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import yokai.domain.connections.service.ConnectionsPreferences
import yokai.presentation.extension.repo.visualName
import yokai.presentation.component.preference.Preference
import yokai.presentation.component.preference.widget.TriStateListDialog
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.runBlocking
import yokai.domain.category.interactor.GetCategories
import yokai.domain.category.model.Category
import yokai.i18n.MR
import yokai.presentation.settings.ComposableSettings
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import eu.kanade.tachiyomi.core.storage.preference.collectAsState

object SettingsDiscordScreen : ComposableSettings {

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
            getRPCIncognitoGroup(
                connectionsPreferences = connectionsPreferences,
                enabled = enableDRPC,
            ),
            Preference.PreferenceItem.TextPreference(
                title = stringResource(MR.strings.logout),
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
    val allDbCategories by getCategories.subscribe().collectAsState(initial = runBlocking { getCategories.await() })
    val allCategories = allDbCategories.map { it.toDomainModel() }

    val discordRPCIncognitoPref = connectionsPreferences.discordRPCIncognito()
    val discordRPCIncognitoCategoriesPref = connectionsPreferences.discordRPCIncognitoCategories()

    val includedManga by discordRPCIncognitoCategoriesPref.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val dialogTitle = stringResource(MR.strings.general_categories)
    val dialogMessage = stringResource(MR.strings.pref_discord_incognito_categories_details)
    val infoMessage = stringResource(MR.strings.pref_discord_incognito_categories_details)
    val categoryTitle = stringResource(MR.strings.pref_discord_incognito)
    val categorySubtitle = stringResource(MR.strings.pref_discord_incognito_summary)
    val categoryPickerTitle = stringResource(MR.strings.general_categories)
    val categoryPickerSubtitle = getCategoriesLabel(allCategories, includedManga)

    if (showDialog) {
        TriStateListDialog(
            title = dialogTitle,
            message = dialogMessage,
            items = allCategories,
            initialChecked = includedManga.mapNotNull { id -> allCategories.find { it.id.toString() == id } },
            initialInversed = allCategories.filterNot { it.id.toString() in includedManga },
            itemLabel = { Text(it.visualName) },
            onDismissRequest = { showDialog = false },
            onValueChanged = { newIncluded, _ ->
                discordRPCIncognitoCategoriesPref.set(
                    newIncluded.map { it.id.toString() }.toSet(),
                )
                showDialog = false
            }
        )
    }

    return Preference.PreferenceGroup(
        title = dialogTitle,
        preferenceItems = persistentListOf(
            Preference.PreferenceItem.SwitchPreference(
                pref = discordRPCIncognitoPref,
                title = categoryTitle,
                subtitle = categorySubtitle,
            ),
            Preference.PreferenceItem.TextPreference(
                title = categoryPickerTitle,
                subtitle = categoryPickerSubtitle,
                onClick = { showDialog = true },
            ),
            Preference.PreferenceItem.InfoPreference(infoMessage),
        ),
        enabled = enabled,
    )
}

    private fun getCategoriesLabel(
        allCategories: List<Category>,
        included: Set<String>,
    ): String {
        return allCategories
            .filter { it.id.toString() in included }
            .joinToString(", ") { it.visualName }
    }
}
