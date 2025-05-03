// CustomConnectionsSettingsScreen.kt
package eu.kanade.tachiyomi.setting.controllers

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.util.system.openDiscordLoginActivity
import yokai.i18n.MR
import dev.icerock.moko.resources.compose.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object CustomConnectionsSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val connectionsManager = remember { Injekt.get<ConnectionsManager>() }

        var dialog by remember { mutableStateOf<Any?>(null) }

        dialog?.let {
            when (it) {
                is LoginConnectionsDialog -> {
                    ConnectionsLoginDialog(
                        service = it.service,
                        uNameStringRes = it.uNameStringRes,
                        onDismissRequest = { dialog = null },
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(MR.strings.pref_category_connections),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { context.openDiscordLoginActivity() },
                elevation = CardDefaults.cardElevation(4.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(connectionsManager.discord.nameRes()),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = {
                        navigator.push(SettingsDiscordScreen)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            }
        }
    }
}

// Puedes definir esto en el mismo archivo o en otro donde tengas tus dialogs
private data class LoginConnectionsDialog(
    val service: eu.kanade.tachiyomi.data.connections.ConnectionsService,
    @androidx.annotation.StringRes val uNameStringRes: Int,
)
