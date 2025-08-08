package eu.kanade.tachiyomi.ui.setting.controllers

import eu.kanade.tachiyomi.ui.setting.SettingsComposeController
import yokai.presentation.settings.ComposableSettings
import eu.kanade.tachiyomi.ui.setting.controllers.SettingsDiscordScreen

class SettingsDiscordController : SettingsComposeController() {
    override fun getComposableSettings(): ComposableSettings =
        SettingsDiscordScreen().withRouter(router)
}
