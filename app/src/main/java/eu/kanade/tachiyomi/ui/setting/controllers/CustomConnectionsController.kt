package eu.kanade.tachiyomi.ui.setting.controllers

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.data.connections.ConnectionsManager
import eu.kanade.tachiyomi.data.connections.ConnectionsService
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController
import eu.kanade.tachiyomi.util.compose.LocalBackPress
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.openDiscordLoginActivity
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.withUIContext
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.i18n.MR

class CustomConnectionsController : BaseComposeController() {

    @Composable
    override fun ScreenContent() {
        val context = LocalContext.current
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
                is LogoutConnectionsDialog -> {
                    ConnectionsLogoutDialog(
                        service = it.service,
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
                        dialog = LoginConnectionsDialog(
                            service = connectionsManager.discord,
                            uNameStringRes = MR.strings.username,
                        )
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            }
        }
    }
}

// --- Reutilizamos los mismos componentes auxiliares ---

@Composable
private fun ConnectionsLoginDialog(
    service: ConnectionsService,
    uNameStringRes: StringResource,
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
                    label = { Text(text = stringResource(uNameStringRes)) },
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

@Composable
internal fun ConnectionsLogoutDialog(
    service: ConnectionsService,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val router = LocalBackPress.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(MR.strings.logout_title, stringResource(service.nameRes())),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDismissRequest,
                ) {
                    Text(text = stringResource(MR.strings.action_cancel))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        service.logout()
                        onDismissRequest()
                        context.toast(MR.strings.logout_success)
                        router?.invoke()  // Usar el operador ?.invoke para llamar a la función de manera segura
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(text = stringResource(MR.strings.logout))
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

private data class LoginConnectionsDialog(
    val service: ConnectionsService,
    val uNameStringRes: StringResource,
)

internal data class LogoutConnectionsDialog(
    val service: ConnectionsService,
)
