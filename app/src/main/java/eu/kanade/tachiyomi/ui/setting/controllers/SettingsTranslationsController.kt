package eu.kanade.tachiyomi.ui.setting.controllers

import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.data.preference.PreferenceKeys as Keys
import eu.kanade.tachiyomi.ui.setting.SettingsLegacyController
import eu.kanade.tachiyomi.ui.setting.defaultValue
import eu.kanade.tachiyomi.ui.setting.entriesRes
import eu.kanade.tachiyomi.ui.setting.listPreference
import eu.kanade.tachiyomi.ui.setting.switchPreference
import eu.kanade.tachiyomi.ui.setting.titleRes
import yokai.i18n.MR

class SettingsTranslationsController : SettingsLegacyController() {

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        titleRes = MR.strings.translations

        listPreference(activity) {
            key = Keys.sourceLanguage
            titleRes = MR.strings.translate_from
            entriesRes = arrayOf(
                MR.strings.english_or_latin_script,
                MR.strings.chinese,
                MR.strings.japanese,
                MR.strings.korean,
            )
            entryValues = arrayOf("en", "zh", "ja", "ko")
            defaultValue = "en"
        }

        listPreference(activity) {
            key = Keys.targetLanguage
            titleRes = MR.strings.translate_to
            entriesRes = arrayOf(
                MR.strings.afrikaans,
                MR.strings.albanian,
                MR.strings.catalan,
                MR.strings.chinese,
                MR.strings.croatian,
                MR.strings.czech,
                MR.strings.danish,
                MR.strings.dutch,
                MR.strings.english,
                MR.strings.estonian,
                MR.strings.filipino,
                MR.strings.finnish,
                MR.strings.french,
                MR.strings.german,
                MR.strings.hungarian,
                MR.strings.icelandic,
                MR.strings.indonesian,
                MR.strings.italian,
                MR.strings.japanese,
                MR.strings.korean,
                MR.strings.latvian,
                MR.strings.lithuanian,
                MR.strings.malay,
                MR.strings.norwegian,
                MR.strings.polish,
                MR.strings.portuguese,
                MR.strings.romanian,
                MR.strings.serbian_latin,
                MR.strings.slovak,
                MR.strings.slovenian,
                MR.strings.spanish,
                MR.strings.swedish,
                MR.strings.turkish,
                MR.strings.vietnamese,
            )
            entryValues = arrayOf(
                "af", "sq", "ca", "zh", "hr", "cs", "da", "nl", "en", "et", "fil", "fi", "fr", "de",
                "hu", "is", "id", "it", "ja", "ko", "lv", "lt", "ms", "no", "pl", "pt", "ro", "sr_Latn",
                "sk", "sl", "es", "sv", "tr", "vi"
            )
            defaultValue = "en"
        }

        listPreference(activity) {
            key = Keys.translationMethod
            titleRes = MR.strings.translation_models
            entriesRes = arrayOf(
                MR.strings.mlkit_on_device,
                MR.strings.google_translate_web_based,
            )
            entryValues = arrayOf("mlkit", "google")
            defaultValue = "mlkit"
        }

        switchPreference {
            key = Keys.autoTranslate
            titleRes = MR.strings.translate_after_downloading
            defaultValue = false
        }
    }
}
