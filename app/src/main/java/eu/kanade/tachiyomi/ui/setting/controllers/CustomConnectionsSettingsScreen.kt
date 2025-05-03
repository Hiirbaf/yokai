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
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.data.connections.ConnectionsService
import eu.kanade.tachiyomi.util.system.openDiscordLoginActivity
import eu.kanade.tachiyomi.util.system.launchIO
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

@Composable
    private fun ConnectionsLoginDialog(
        service: ConnectionsService,
        @StringRes uNameStringRes: Int,
        onDismissRequest: () -> Unit,
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var username by remember { mutableStateOf(TextFieldValue(service.getUsername())) }
        var password by remember { mutableStateOf(TextFieldValue(service.getPassword())) }
        var processing by remember { mutableStateOf(false) }
        var inputError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(
                            MR.strings.login_title,
                            stringResource(service.nameRes()),
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(MR.strings.action_close),
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(text = stringResourceInt(uNameStringRes)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true,
                        isError = inputError && username.text.isEmpty(),
                    )

                    var hidePassword by remember { mutableStateOf(true) }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(text = stringResource(MR.strings.password)) },
                        trailingIcon = {
                            IconButton(onClick = { hidePassword = !hidePassword }) {
                                Icon(
                                    imageVector = if (hidePassword) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    },
                                    contentDescription = null,
                                )
                            }
                        },
                        visualTransformation = if (hidePassword) {
                            PasswordVisualTransformation()
                        } else {
                            VisualTransformation.None
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        singleLine = true,
                        isError = inputError && password.text.isEmpty(),
                    )
                }
            },
            confirmButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !processing,
                    onClick = {
                        if (username.text.isEmpty() || password.text.isEmpty()) {
                            inputError = true
                            return@Button
                        }
                        scope.launchIO {
                            inputError = false
                            processing = true
                            val result = checkLogin(
                                context = context,
                                service = service,
                                username = username.text,
                                password = password.text,
                            )
                            if (result) onDismissRequest()
                            processing = false
                        }
                    },
                ) {
                    val id = if (processing) MR.strings.loading else MR.strings.login
                    Text(text = stringResource(id))
                }
            },
        )
    }

// Puedes definir esto en el mismo archivo o en otro donde tengas tus dialogs
private data class LoginConnectionsDialog(
    val service: eu.kanade.tachiyomi.data.connections.ConnectionsService,
    @androidx.annotation.StringRes val uNameStringRes: Int,
)
