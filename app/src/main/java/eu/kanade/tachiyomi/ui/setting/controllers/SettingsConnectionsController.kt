package eu.kanade.tachiyomi.ui.setting.controllers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.data.connections.ConnectionsService
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import kotlinx.coroutines.launch
import yokai.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import eu.kanade.tachiyomi.util.system.toast
import androidx.compose.ui.platform.LocalContext

class SettingsConnectionsController : BaseComposeController() {

    @ReadOnlyComposable
@Composable
override fun getTitleRes() = MR.strings.pref_category_connections

@Composable
override fun getPreferences(): List<Preference> {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val connectionsManager = remember { Injekt.get<ConnectionsManager>() }

    var dialog by remember { mutableStateOf<Any?>(null) }

    dialog?.run {
        when (this) {
            is LoginConnectionsDialog -> {
                ConnectionsLoginDialog(
                    service = service,
                    uNameStringRes = uNameStringRes,
                    onDismissRequest = { dialog = null },
                )
            }
            is LogoutConnectionsDialog -> {
                ConnectionsLogoutDialog(
                    service = service,
                    onDismissRequest = { dialog = null },
                )
            }
        }
    }

    return listOf(
        Preference.PreferenceGroup(
            title = stringResource(MR.strings.special_services),
            preferenceItems = persistentListOf(
                Preference.PreferenceItem.ConnectionsPreference(
                    title = stringResource(connectionsManager.discord.nameRes()),
                    service = connectionsManager.discord,
                    login = {
                        dialog = LoginConnectionsDialog(
                            service = connectionsManager.discord,
                            uNameStringRes = MR.strings.username, // o uno específico
                        )
                    },
                    openSettings = {
                        navigator.push(SettingsDiscordScreen)
                    },
                )
            )
        )
    )
}
