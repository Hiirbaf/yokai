package eu.kanade.tachiyomi.ui.setting.controllers

import android.content.Context
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.data.connections.ConnectionsService
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.openDiscordLoginActivity
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.withUIContext
import kotlinx.collections.immutable.persistentListOf
import uy.kohesive.injekt.Injekt
import yokai.i18n.MR
import yokai.presentation.component.preference.Preference
import yokai.presentation.settings.ComposableSettings

// sealed class para manejar diálogos
private sealed class ConnectionsDialog {
    data class Login(val service: ConnectionsService, val uNameStringRes: StringResource) : ConnectionsDialog()
    data class Logout(val service: ConnectionsService) : ConnectionsDialog()
}

object SettingsConnectionsScreen : ComposableSettings {

    @Composable
    @ReadOnlyComposable
    override fun getTitleRes() = MR.strings.pref_category_connections

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val connectionsManager = remember { Injekt.get<ConnectionsManager>() }

        // Estado del diálogo
        var dialog by remember { mutableStateOf<ConnectionsDialog?>(null) }
        dialog?.run {
            when (this) {
                is ConnectionsDialog.Login -> ConnectionsLoginDialog(
                    service = service,
                    uNameStringRes = uNameStringRes,
                    onDismissRequest = { dialog = null }
                )
                is ConnectionsDialog.Logout -> ConnectionsLogoutDialog(
                    service = service,
                    onDismissRequest = { dialog = null }
                )
            }
        }

        // Obtenemos Discord desde ConnectionsManager
        val discordService = connectionsManager.getService("discord")

        return listOf(
            Preference.PreferenceGroup(
                title = stringResource(MR.strings.special_services),
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.ConnectionsPreference(
                        title = stringResource(discordService.nameRes()),
                        service = discordService,
                        login = { dialog = ConnectionsDialog.Login(discordService, MR.strings.username) },
                        openSettings = { navigator.push(SettingsDiscordScreen) },
                    ),
                    Preference.PreferenceItem.InfoPreference(stringResource(MR.strings.connections_discord_info)),
                    Preference.PreferenceItem.InfoPreference(stringResource(MR.strings.connections_info)),
                ),
            ),
        )
    }

    @Composable
    private fun ConnectionsLoginDialog(
        service: ConnectionsService,
        uNameStringRes: StringResource,
        onDismissRequest: () -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var username by remember { mutableStateOf(TextFieldValue(service.getUsername())) }
        var password by remember { mutableStateOf(TextFieldValue(service.getPassword())) }
        var processing by remember { mutableStateOf(false) }
        var inputError by remember { mutableStateOf(false) }
        var hidePassword by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(MR.strings.login_title, stringResource(service.nameRes())),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = stringResource(MR.strings.action_close))
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(text = stringResource(uNameStringRes)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true,
                        isError = inputError && username.text.isEmpty(),
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(text = stringResource(MR.strings.password)) },
                        trailingIcon = {
                            IconButton(onClick = { hidePassword = !hidePassword }) {
                                Icon(
                                    imageVector = if (hidePassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (hidePassword) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
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
                    }
                ) {
                    val id = if (processing) MR.strings.loading else MR.strings.login
                    Text(text = stringResource(id))
                }
            }
        )
    }

    private suspend fun checkLogin(
        context: Context,
        service: ConnectionsService,
        username: String,
        password: String
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
}

@Composable
internal fun ConnectionsLogoutDialog(
    service: ConnectionsService,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(MR.strings.logout_title, stringResource(service.nameRes())),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(modifier = Modifier.weight(1f), onClick = onDismissRequest) {
                    Text(text = stringResource(MR.strings.action_cancel))
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
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(text = stringResource(MR.strings.logout))
                }
            }
        }
    )
}
