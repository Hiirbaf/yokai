package eu.kanade.tachiyomi.ui.setting.controllers

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource as stringResourceInt
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.more.settings.widget.ConnectionPreference
import eu.kanade.presentation.more.settings.widget.InfoPreference
import eu.kanade.presentation.more.settings.widget.PreferenceGroupHeader
import eu.kanade.tachiyomi.data.connection.ConnectionsManager
import eu.kanade.tachiyomi.data.connection.ConnectionsService
import eu.kanade.tachiyomi.util.system.openDiscordLoginActivity
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.launch
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.core.common.util.lang.withUIContext
import tachiyomi.i18n.MR
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class SettingsConnectionsController : SettingsComposeController() {

    @Composable
    override fun composeContent() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val connectionsManager = remember { Injekt.get<ConnectionsManager>() }

        var loginDialog by remember { mutableStateOf<LoginDialogData?>(null) }
        var logoutDialog by remember { mutableStateOf<LogoutDialogData?>(null) }

        PreferenceGroupHeader(stringResource(MR.strings.special_services))

        ConnectionPreference(
            title = stringResource(connectionsManager.discord.nameRes()),
            onLogin = { context.openDiscordLoginActivity() },
            onSettings = { navigator.push(SettingsDiscordScreen) },
            service = connectionsManager.discord,
        )

        InfoPreference(stringResource(MR.strings.connections_discord_info))
        InfoPreference(stringResource(MR.strings.connections_info))

        loginDialog?.let {
            ConnectionsLoginDialog(
                service = it.service,
                uNameStringRes = it.uNameStringRes,
                onDismissRequest = { loginDialog = null },
            )
        }

        logoutDialog?.let {
            ConnectionsLogoutDialog(
                service = it.service,
                onDismissRequest = { logoutDialog = null },
            )
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
                        label = { Text(stringResourceInt(uNameStringRes)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true,
                        isError = inputError && username.text.isEmpty(),
                    )

                    var hidePassword by remember { mutableStateOf(true) }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(MR.strings.password)) },
                        trailingIcon = {
                            IconButton(onClick = { hidePassword = !hidePassword }) {
                                Icon(
                                    imageVector = if (hidePassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                )
                            }
                        },
                        visualTransformation = if (hidePassword) PasswordVisualTransformation() else VisualTransformation.None,
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
                            val result = checkLogin(context, service, username.text, password.text)
                            if (result) onDismissRequest()
                            processing = false
                        }
                    },
                ) {
                    val id = if (processing) MR.strings.loading else MR.strings.login
                    Text(stringResource(id))
                }
            },
        )
    }

    @Composable
    private fun ConnectionsLogoutDialog(
        service: ConnectionsService,
        onDismissRequest: () -> Unit,
    ) {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = stringResource(MR.strings.logout_title, stringResource(service.nameRes())),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismissRequest,
                    ) {
                        Text(stringResource(MR.strings.action_cancel))
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            service.logout()
                            onDismissRequest()
                            context.toast(MR.strings.logout_success)
                            navigator.pop()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text(stringResource(MR.strings.logout))
                    }
                }
            },
        )
    }

    private suspend fun checkLogin(
        context: Context,
        service: ConnectionsService,
        username: String,
        password: String,
    ): Boolean {
        return try {
            service.login(username, password)
            withUIContext { context.toast(MR.strings.login_success) }
            true
        } catch (e: Throwable) {
            service.logout()
            withUIContext { context.toast(e.message.toString()) }
            false
        }
    }

    private data class LoginDialogData(
        val service: ConnectionsService,
        @StringRes val uNameStringRes: Int,
    )

    private data class LogoutDialogData(
        val service: ConnectionsService,
    )
}
