package eu.kanade.tachiyomi.ui.setting.controllers

import android.app.Activity
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.R
import yokai.i18n.MR
import yokai.util.lang.getString
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.data.preference.changesIn
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.data.track.TrackPreferences
import eu.kanade.tachiyomi.data.track.TrackService
import eu.kanade.tachiyomi.data.track.bangumi.BangumiApi
import eu.kanade.tachiyomi.data.track.myanimelist.MyAnimeListApi
import eu.kanade.tachiyomi.data.track.shikimori.ShikimoriApi
import eu.kanade.tachiyomi.source.SourceManager
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController
import eu.kanade.tachiyomi.ui.setting.add
import eu.kanade.tachiyomi.ui.setting.defaultValue
import eu.kanade.tachiyomi.ui.setting.iconRes
import eu.kanade.tachiyomi.ui.setting.infoPreference
import eu.kanade.tachiyomi.ui.setting.onClick
import eu.kanade.tachiyomi.ui.setting.preference
import eu.kanade.tachiyomi.ui.setting.preferenceCategory
import eu.kanade.tachiyomi.ui.setting.switchPreference
import eu.kanade.tachiyomi.ui.setting.titleMRes as titleRes
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.openInBrowser
import eu.kanade.tachiyomi.util.view.snack
import eu.kanade.tachiyomi.widget.preference.TrackLoginDialog
import eu.kanade.tachiyomi.widget.preference.TrackLogoutDialog
import eu.kanade.tachiyomi.widget.preference.TrackerPreference
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import eu.kanade.tachiyomi.data.preference.PreferenceKeys as Keys

class SettingsConnectionsController :
    SettingsLegacyController(),
    TrackLoginDialog.Listener,
    TrackLogoutDialog.Listener {

    private val trackManager: TrackManager by injectLazy()
    val trackPreferences: TrackPreferences by injectLazy()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        titleRes = MR.strings.tracking

        preferenceCategory {
            titleRes = MR.strings.services

            trackPreference(trackManager.myAnimeList) {
                activity?.openInBrowser(MyAnimeListApi.authUrl(), trackManager.myAnimeList.getLogoColor(), true)
            }
            trackPreference(trackManager.shikimori) {
                activity?.openInBrowser(ShikimoriApi.authUrl(), trackManager.shikimori.getLogoColor(), true)
            }
            trackPreference(trackManager.bangumi) {
                activity?.openInBrowser(BangumiApi.authUrl(), trackManager.bangumi.getLogoColor(), true)
            }
            infoPreference(MR.strings.tracking_info)
        }
    }

    private inline fun PreferenceGroup.trackPreference(
        service: TrackService,
        crossinline login: () -> Unit = { },
    ): TrackerPreference {
        return add(
            TrackerPreference(context).apply {
                key = trackPreferences.trackUsername(service).key()
                title = context.getString(service.nameRes())
                iconRes = service.getLogo()
                iconColor = service.getLogoColor()
                onClick {
                    if (service.isLogged) {
                        if (service is EnhancedTrackService) {
                            service.logout()
                            updatePreference(service)
                        } else {
                            val dialog = TrackLogoutDialog(service)
                            dialog.targetController = this@SettingsConnectionsController
                            dialog.showDialog(router)
                        }
                    } else {
                        if (service is EnhancedTrackService) {
                            service.loginNoop()
                            updatePreference(service)
                        } else {
                            login()
                        }
                    }
                }
            },
        )
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        updatePreference(trackManager.myAnimeList)
        updatePreference(trackManager.aniList)
        updatePreference(trackManager.shikimori)
        updatePreference(trackManager.bangumi)
    }

    private fun updatePreference(service: TrackService) {
        val pref = findPreference(trackPreferences.trackUsername(service).key()) as? TrackerPreference
        pref?.notifyChanged()
    }

    override fun trackLoginDialogClosed(service: TrackService) {
        updatePreference(service)
    }

    override fun trackLogoutDialogClosed(service: TrackService) {
        updatePreference(service)
    }
    }
