package yokai.domain.history.interactor

import yokai.domain.history.HistoryRepository

class GetHistory(
    private val historyRepository: HistoryRepository
) {
    suspend fun awaitByMangaId(mangaId: Long) = historyRepository.getByMangaId(mangaId)
    suspend fun awaitAllByMangaId(mangaId: Long) = historyRepository.getAllByMangaId(mangaId)
}
