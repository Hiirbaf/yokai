package yokai.domain.source

import eu.kanade.tachiyomi.core.preference.PreferenceStore

class SourcePreferences(private val preferenceStore: PreferenceStore) {
    fun extensionRepos() = preferenceStore.getStringSet("extension_repos", emptySet())
    fun trustedExtensions() = preferenceStore.getStringSet("trusted_extensions", emptySet())

    fun externalLocalSource() = preferenceStore.getBoolean("pref_external_local_source", false)
}
