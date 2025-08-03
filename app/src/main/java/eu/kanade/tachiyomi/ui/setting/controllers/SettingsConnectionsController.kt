package eu.kanade.tachiyomi.ui.setting.controllers

import eu.kanade.tachiyomi.ui.setting.SettingsComposeController
import yokai.presentation.settings.ComposableSettings
import eu.kanade.tachiyomi.ui.setting.controllers.SettingsConnectionsScreen

class SettingsConnectionsController : SettingsComposeController() {
    override fun getComposableSettings(): ComposableSettings = SettingsConnectionsScreen
}
