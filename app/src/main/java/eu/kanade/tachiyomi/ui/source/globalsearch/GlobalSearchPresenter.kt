package eu.kanade.tachiyomi.ui.source.globalsearch

import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.database.models.create
import eu.kanade.tachiyomi.data.database.models.removeCover
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.domain.manga.models.Manga
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceManager
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.ui.base.presenter.BaseCoroutinePresenter
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.launchUI
import eu.kanade.tachiyomi.util.system.withUIContext
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.domain.manga.interactor.GetManga
import yokai.domain.manga.interactor.InsertManga
import yokai.domain.manga.interactor.UpdateManga
import yokai.domain.manga.interactor.GetLibraryManga
import yokai.domain.recents.interactor.GetRecents

/**
 * Presenter of [GlobalSearchController]
 * Function calls should be done from here. UI calls should be done from the controller.
 *
 * @param sourceManager manages the different sources.
 * @param preferences manages the preference calls.
 */
open class GlobalSearchPresenter(
    private val initialQuery: String? = "",
    private val initialExtensionFilter: String? = null,
    private val sourcesToUse: List<CatalogueSource>? = null,
    val sourceManager: SourceManager = Injekt.get(),
    private val preferences: PreferencesHelper = Injekt.get(),
    private val coverCache: CoverCache = Injekt.get(),
) : BaseCoroutinePresenter<GlobalSearchController>() {
    private val getManga: GetManga by injectLazy()
    private val insertManga: InsertManga by injectLazy()
    private val updateManga: UpdateManga by injectLazy()
    private val getLibraryManga: GetLibraryManga by injectLazy()
    private val getRecents: GetRecents by injectLazy()
    private val downloadManager: DownloadManager by injectLazy()

    /**
     * Enabled sources.
     */
    val sources by lazy { getSourcesToQuery() }

    private var fetchSourcesJob: Job? = null

    var query = ""

    private val fetchImageFlow = MutableSharedFlow<Pair<List<Manga>, Source>>()

    private var fetchImageJob: Job? = null

    private val extensionManager: ExtensionManager by injectLazy()

    private var extensionFilter: String? = null

    var items: List<GlobalSearchItem> = emptyList()

    private val semaphore = Semaphore(5)

    private val showTopSearchAids = sourcesToUse == null && initialExtensionFilter == null

    override fun onCreate() {
        super.onCreate()

        extensionFilter = initialExtensionFilter

        if (showTopSearchAids) {
            presenterScope.launchIO {
                val topGenres = getTopGenres()
                val searchHistory = getSearchHistory()
                withUIContext {
                    view?.setGenreChips(topGenres)
                    view?.setSearchHistory(searchHistory)
                }
            }
        }

        if (items.isEmpty()) {
            // Perform a search with previous or initial state
            search(initialQuery.orEmpty())
        }
        presenterScope.launchUI {
            view?.setItems(items)
        }
    }

    /**
     * Returns a list of enabled sources ordered by language and name.
     *
     * @return list containing enabled sources.
     */
    protected open fun getEnabledSources(): List<CatalogueSource> {
        val languages = preferences.enabledLanguages().get()
        val hiddenCatalogues = preferences.hiddenSources().get()
        val pinnedCatalogues = preferences.pinnedCatalogues().get()

        val list = sourceManager.getCatalogueSources()
            .filter { it.lang in languages }
            .filterNot { it.id.toString() in hiddenCatalogues }
            .sortedBy { "(${it.lang}) ${it.name}" }

        return if (preferences.onlySearchPinned().get()) {
            list.filter { it.id.toString() in pinnedCatalogues }
        } else {
            list.sortedBy { it.id.toString() !in pinnedCatalogues }
        }
    }

    private fun getSourcesToQuery(): List<CatalogueSource> {
        if (sourcesToUse != null) return sourcesToUse
        val filter = extensionFilter
        val enabledSources = getEnabledSources()
        if (filter.isNullOrEmpty()) {
            return enabledSources
        }

        val languages = preferences.enabledLanguages().get()
        val filterSources = extensionManager.installedExtensionsFlow.value
            .filter { it.pkgName == filter }
            .flatMap { it.sources }
            .filterIsInstance<CatalogueSource>()

        val result = filterSources.filter { it in enabledSources }

        if (result.isEmpty()) {
            return enabledSources
        }

        return result
    }

    /**
     * Creates a catalogue search item
     */
    protected open fun createCatalogueSearchItem(
        source: CatalogueSource,
        results: List<GlobalSearchMangaItem>?,
    ): GlobalSearchItem {
        return GlobalSearchItem(source, results)
    }

    fun confirmDeletion(manga: Manga) {
        manga.removeCover(coverCache)
        val downloadManager: DownloadManager = Injekt.get()
        sourceManager.get(manga.source)?.let { source ->
            downloadManager.deleteManga(manga, source)
        }
    }

    /**
     * Initiates a search for manga per catalogue.
     *
     * @param query query on which to search.
     */
    fun search(query: String) {
        val normalizedQuery = normalizeQuery(query)

        // Return if there's nothing to do
        if (this.query == normalizedQuery) return

        // Update query
        this.query = normalizedQuery

        if (showTopSearchAids) {
            presenterScope.launchIO {
                if (normalizedQuery.isNotBlank()) {
                    saveSearchQuery(normalizedQuery)
                }

                withUIContext {
                    view?.setSearchHistory(getSearchHistory())
                }
            }
        }

        if (normalizedQuery.isBlank()) {
            fetchSourcesJob?.cancel()
            items = emptyList()
            presenterScope.launchUI { view?.setItems(items) }
            return
        }

        // Create image fetch subscription
        initializeFetchImageSubscription()

        // Create items with the initial state
        val sourceItems = sources.map { createCatalogueSearchItem(it, null) }
        items = sourceItems
        presenterScope.launchUI { view?.setItems(sourceItems) }
        val pinnedSourceIds = preferences.pinnedCatalogues().get()

        fetchSourcesJob?.cancel()
        fetchSourcesJob = presenterScope.launch {
            val staticSections = if (showTopSearchAids) {
                getStaticSectionsForQuery(normalizedQuery)
            } else {
                emptyList()
            }
            items = staticSections + sourceItems
            withUIContext { view?.setItems(items) }

            sources.map { source ->
                launch mainLaunch@{
                    semaphore.withPermit {
                        if (this@GlobalSearchPresenter.query != normalizedQuery) return@mainLaunch
                        if (this@GlobalSearchPresenter.items.find { it.source == source }?.results != null) {
                            return@mainLaunch
                        }
                        val mangas = try {
                            source.getSearchManga(1, normalizedQuery, source.getFilterList())
                        } catch (error: Exception) {
                            MangasPage(emptyList(), false)
                        }
                            .mangas.take(10)
                            .mapNotNull { networkToLocalManga(it, source.id) }
                        fetchImage(mangas, source)
                        val result = createCatalogueSearchItem(
                            source,
                            mangas.map {
                                GlobalSearchMangaItem(
                                    it,
                                    getManga.subscribeByUrlAndSource(it.url, it.source),
                                )
                            },
                        )
                        val currentStaticSections = items.filter { it.isStaticSection }
                        val sortedSources = items
                            .filterNot { it.isStaticSection }
                            .map { item -> if (item.source == result.source) result else item }
                            .sortedWith(
                                compareBy(
                                    // Bubble up sources that actually have results
                                    { it.results.isNullOrEmpty() },
                                    // Same as initial sort, i.e. pinned first then alphabetically
                                    { it.source.id.toString() !in pinnedSourceIds },
                                    { "${it.source.name.lowercase(Locale.getDefault())} (${it.source.lang})" },
                                ),
                            )
                        items = currentStaticSections + sortedSources
                        withUIContext { view?.setItems(items) }
                    }
                }
            }
        }
    }

    private suspend fun getStaticSectionsForQuery(query: String): List<GlobalSearchItem> {
        val historyManga = getHistoryMangaForQuery(query)
        val historyMangaIds = historyManga.mapNotNull { it.id }.toSet()
        val localMatches = getLocalMatchesForQuery(query, historyMangaIds)

        return buildList {
            if (historyManga.isNotEmpty()) {
                add(
                    GlobalSearchItem(
                        source = HISTORY_SECTION_SOURCE,
                        results = historyManga.toGlobalSearchMangaItems(),
                        openSourceOnClick = false,
                        showLanguageSubtitle = false,
                        isStaticSection = true,
                    ),
                )
            }

            if (localMatches.isNotEmpty()) {
                add(
                    GlobalSearchItem(
                        source = LOCAL_MATCHES_SECTION_SOURCE,
                        results = localMatches.toGlobalSearchMangaItems(),
                        openSourceOnClick = false,
                        showLanguageSubtitle = false,
                        isStaticSection = true,
                    ),
                )
            }
        }
    }

    private suspend fun getHistoryMangaForQuery(query: String): List<Manga> {
        val recents = getRecents.awaitAll(
            includeRead = true,
            filterScanlators = false,
            isEndless = false,
            isResuming = false,
            search = query,
            offset = 0L,
        )

        return recents
            .sortedByDescending { it.history.last_read }
            .map { it.manga }
            .distinctBy { it.id }
            .take(MAX_STATIC_SECTION_RESULTS)
    }

    private suspend fun getLocalMatchesForQuery(query: String, excludeIds: Set<Long>): List<Manga> {
        data class LocalResult(
            val manga: Manga,
            val isDownloaded: Boolean,
        )

        val normalizedQuery = query.lowercase(Locale.getDefault())

        return getLibraryManga.await()
            .map { it.manga }
            .filter { manga ->
                val mangaId = manga.id
                mangaId == null || mangaId !in excludeIds
            }
            .filter { it.title.lowercase(Locale.getDefault()).contains(normalizedQuery) }
            .map { manga ->
                LocalResult(
                    manga = manga,
                    isDownloaded = downloadManager.getDownloadCount(manga) > 0,
                )
            }
            .filter { it.isDownloaded || it.manga.favorite }
            .sortedWith(
                compareByDescending<LocalResult> { it.isDownloaded }
                    .thenByDescending { it.manga.favorite }
                    .thenBy { it.manga.title.lowercase(Locale.getDefault()) },
            )
            .map { it.manga }
            .take(MAX_STATIC_SECTION_RESULTS)
    }

    private suspend fun getTopGenres(): List<String> {
        val genreMap = linkedMapOf<String, Int>()

        getLibraryManga.await()
            .asSequence()
            .map { it.manga }
            .flatMap { (it.genre ?: "").split(',').asSequence() }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { genre ->
                genreMap[genre] = (genreMap[genre] ?: 0) + 1
            }

        return genreMap.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.lowercase(Locale.getDefault()) })
            .map { it.key }
            .take(MAX_TOP_GENRES)
    }

    private fun getSearchHistory(): List<String> {
        return preferences.globalSearchHistory().get()
            .split(SEARCH_HISTORY_SEPARATOR)
            .map(::normalizeQuery)
            .filter { it.isNotBlank() }
            .distinct()
            .take(MAX_SEARCH_HISTORY)
    }

    private fun saveSearchQuery(query: String) {
        if (preferences.incognitoMode().get()) return

        val normalized = normalizeQuery(query)
        if (normalized.isBlank()) return

        val updated = buildList {
            add(normalized)
            addAll(getSearchHistory().filterNot { it.equals(normalized, true) })
        }.take(MAX_SEARCH_HISTORY)

        preferences.globalSearchHistory().set(updated.joinToString(SEARCH_HISTORY_SEPARATOR))
    }

    private fun normalizeQuery(query: String): String {
        return query.replace(SEARCH_HISTORY_SEPARATOR, " ").trim()
    }

    private fun List<Manga>.toGlobalSearchMangaItems(): List<GlobalSearchMangaItem> {
        return map {
            GlobalSearchMangaItem(
                manga = it,
                mangaFlow = getManga.subscribeByUrlAndSource(it.url, it.source),
            )
        }
    }

    /**
     * Initialize a list of manga.
     *
     * @param manga the list of manga to initialize.
     */
    private fun fetchImage(manga: List<Manga>, source: Source) {
        presenterScope.launch {
            fetchImageFlow.emit(Pair(manga, source))
        }
    }

    /**
     * Subscribes to the initializer of manga details and updates the view if needed.
     */
    private fun initializeFetchImageSubscription() {
        fetchImageJob?.cancel()
        fetchImageJob = fetchImageFlow.onEach { (mangaList, source) ->
            mangaList
                .filter { it.thumbnail_url == null && !it.initialized }
                .forEach {
                    presenterScope.launchIO {
                        try {
                            val manga = getMangaDetails(it, source)
                            withUIContext {
                                view?.onMangaInitialized(source as CatalogueSource, manga)
                            }
                        } catch (_: Exception) {
                            withUIContext {
                                view?.onMangaInitialized(source as CatalogueSource, it)
                            }
                        }
                    }
                }
        }.launchIn(presenterScope)
    }

    /**
     * Initializes the given manga.
     *
     * @param manga the manga to initialize.
     * @return The initialized manga.
     */
    private suspend fun getMangaDetails(manga: Manga, source: Source): Manga {
        val networkManga = source.getMangaDetails(manga.copy())
        manga.copyFrom(networkManga)
        manga.initialized = true
        updateManga.await(manga.toMangaUpdate())
        return manga
    }

    /**
     * Returns a manga from the database for the given manga from network. It creates a new entry
     * if the manga is not yet in the database.
     *
     * @param sManga the manga from the source.
     * @return a manga from the database.
     */
    protected open suspend fun networkToLocalManga(sManga: SManga, sourceId: Long): Manga? {
        var localManga = getManga.awaitByUrlAndSource(sManga.url, sourceId)
        if (localManga == null) {
            val newManga =
                try {
                    Manga.create(sManga.url, sManga.title, sourceId)
                } catch (_: UninitializedPropertyAccessException) {
                    return null
                }
            newManga.copyFrom(sManga)
            newManga.id = insertManga.await(newManga)
            localManga = newManga
        } else if (!localManga.favorite) {
            // if the manga isn't a favorite, set its display title from source
            // if it later becomes a favorite, updated title will go to db
            localManga.title =
                try {
                    sManga.title
                } catch (_: UninitializedPropertyAccessException) {
                    return localManga
                }
        }
        return localManga
    }

    private class StaticSectionSource(
        override val id: Long,
        override val name: String,
    ) : CatalogueSource {
        override val lang: String = ""
        override val supportsLatest: Boolean = false

        override fun getFilterList(): FilterList = FilterList()
    }

    private companion object {
        const val MAX_SEARCH_HISTORY = 10
        const val MAX_TOP_GENRES = 12
        const val MAX_STATIC_SECTION_RESULTS = 12
        const val SEARCH_HISTORY_SEPARATOR = "\n"

        val HISTORY_SECTION_SOURCE = StaticSectionSource(-10_001L, "History")
        val LOCAL_MATCHES_SECTION_SOURCE = StaticSectionSource(-10_002L, "Downloads / Favorites")
    }
}
