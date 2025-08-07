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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bluelinelabs.conductor.RouterTransaction
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.connection.ConnectionsManager
import eu.kanade.tachiyomi.data.connection.ConnectionsService
import eu.kanade.tachiyomi.util.compose.LocalRouter
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.openDiscordLoginActivity
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.withUIContext
import kotlinx.collections.immutable.persistentListOf
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.i18n.MR
import yokai.domain.connections.service.ConnectionsPreferences
import yokai.presentation.component.preference.Preference
import yokai.presentation.settings.ComposableSettings

object SettingsConnectionsScreen : ComposableSettings {

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
    val context = LocalContext.current
    val connectionsPreferences = remember { Injekt.get<ConnectionsPreferences>() }
    val connectionsManager = remember { Injekt.get<ConnectionsManager>() }
    val service = connectionsManager.discord

    val enableDRPCPref = connectionsPreferences.enableDiscordRPC()
    val useChapterTitlesPref = connectionsPreferences.useChapterTitles()
    val discordRPCStatus = connectionsPreferences.discordRPCStatus()

    val enableDRPC by enableDRPCPref.collectAsState()

    var dialog by remember { mutableStateOf<Any?>(null) }
    dialog?.run {
        when (this) {
            is LogoutConnectionsDialog -> ConnectionsLogoutDialog(
                service = service,
                onDismissRequest = {
                    dialog = null
                    enableDRPCPref.set(false)
                },
            )

            is LoginConnectionsDialog -> ConnectionsLoginDialog(
                service = service,
                uNameStringRes = uNameStringRes,
                onDismissRequest = { dialog = null },
            )
        }
    }

    return listOf(
        Preference.PreferenceItem.TextPreference(
            title = stringResource(MR.strings.login),
            onClick = { context.openDiscordLoginActivity() },
        ),
        Preference.PreferenceItem.InfoPreference(stringResource(MR.strings.connections_discord_info)),

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
        Preference.PreferenceItem.TextPreference(
            title = stringResource(MR.strings.logout),
            onClick = { dialog = LogoutConnectionsDialog(service) },
        ),
    )
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
                                    imageVector = if (hidePassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                )
                            }
                        },
                        visualTransformation = if (hidePassword) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
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
                    Text(text = stringResource(id))
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
                            navigator.pop()
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

    private data class LoginConnectionsDialog(
        val service: ConnectionsService,
        @StringRes val uNameStringRes: Int,
    )

    private data class LogoutConnectionsDialog(
        val service: ConnectionsService,
    )
}
