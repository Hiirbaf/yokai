package dev.yokai.domain.library.custom.model

import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.data.database.models.MangaImpl

data class CustomMangaInfo(
    var mangaId: Long,
    val title: String? = null,
    val author: String? = null,
    val artist: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val status: Int? = null,
) {
    fun toManga() = MangaImpl().apply {
        id = this@CustomMangaInfo.mangaId
        title = this@CustomMangaInfo.title ?: ""
        author = this@CustomMangaInfo.author
        artist = this@CustomMangaInfo.artist
        description = this@CustomMangaInfo.description
        genre = this@CustomMangaInfo.genre
        status = this@CustomMangaInfo.status ?: -1
    }

    companion object {
        fun Manga.getMangaInfo() =
            CustomMangaInfo(
                mangaId = id!!,
                title = title,
                author = author,
                artist = artist,
                description = description,
                genre = genre,
                status = status,
            )
    }
}
