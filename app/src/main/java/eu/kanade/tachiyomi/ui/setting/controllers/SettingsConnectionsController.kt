package eu.kanade.tachiyomi.ui.setting.connections

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

    @Composable
    override fun ScreenContent() {
        val context = LocalContext.current
        val connectionsManager = remember { Injekt.get<ConnectionsManager>() }
        val coroutineScope = rememberCoroutineScope()

        var showLoginDialog by remember { mutableStateOf(false) }
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var hidePassword by remember { mutableStateOf(true) }
        val discord = connectionsManager.discord

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(MR.strings.pref_category_connections), style = MaterialTheme.typography.titleLarge)

            Button(
                onClick = { showLoginDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(discord.nameRes()))
            }

            if (showLoginDialog) {
                AlertDialog(
                    onDismissRequest = { showLoginDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val result = try {
                                        discord.login(username, password)
                                        context.toast(MR.strings.login_success)
                                        true
                                    } catch (e: Exception) {
                                        discord.logout()
                                        context.toast(e.message ?: "Login failed")
                                        false
                                    }
                                    if (result) showLoginDialog = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(MR.strings.login))
                        }
                    },
                    title = {
                        Text(stringResource(MR.strings.login_title, stringResource(discord.nameRes())))
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text(stringResource(MR.strings.username)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text(stringResource(MR.strings.password)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                visualTransformation = if (hidePassword) PasswordVisualTransformation() else VisualTransformation.None,
                                trailingIcon = {
                                    IconButton(onClick = { hidePassword = !hidePassword }) {
                                        Icon(
                                            imageVector = if (hidePassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
