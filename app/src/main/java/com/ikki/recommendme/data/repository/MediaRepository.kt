package com.ikki.recommendme.data.repository

import com.ikki.recommendme.data.remote.TmdbApiService
import com.ikki.recommendme.data.remote.TmdbConfig
import com.ikki.recommendme.data.remote.toDetail
import com.ikki.recommendme.data.remote.toDomain
import com.ikki.recommendme.data.sample.SampleData
import com.ikki.recommendme.domain.model.MediaDetail
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Media content source.
 *
 * While TmdbConfig.API_KEY is not set, everything is served from [SampleData].
 * As soon as the key is pasted in, [DefaultMediaRepository] transparently
 * switches to the live TMDB API (with graceful fallback to sample data on
 * network errors).
 */
interface MediaRepository {
    /** true when live TMDB data is being used. */
    val isLive: Boolean

    suspend fun trending(): List<MediaItem>
    suspend fun popularMovies(): List<MediaItem>
    suspend fun popularTv(): List<MediaItem>
    suspend fun topRated(): List<MediaItem>
    suspend fun newReleases(): List<MediaItem>
    suspend fun search(query: String): List<MediaItem>
    suspend fun detail(id: Int, mediaType: MediaType): MediaDetail
    /** Full pool used by the recommendation engine. */
    suspend fun pool(): List<MediaItem>
}

class DefaultMediaRepository(
    private val api: TmdbApiService?
) : MediaRepository {

    override val isLive: Boolean = api != null && TmdbConfig.isConfigured

    private inline fun <T> safeApi(fallback: T, block: () -> T): T =
        if (!isLive) fallback else runCatching { block() }.getOrElse { fallback }

    override suspend fun trending(): List<MediaItem> = withContext(Dispatchers.IO) {
        safeApi(SampleData.trending()) {
            api!!.trending().results.mapNotNull { it.toDomain(MediaType.MOVIE) }
                .ifEmpty { SampleData.trending() }
        }
    }

    override suspend fun popularMovies(): List<MediaItem> = withContext(Dispatchers.IO) {
        safeApi(SampleData.movies) {
            api!!.popularMovies().results.mapNotNull { it.toDomain(MediaType.MOVIE) }
                .ifEmpty { SampleData.movies }
        }
    }

    override suspend fun popularTv(): List<MediaItem> = withContext(Dispatchers.IO) {
        safeApi(SampleData.tvShows) {
            api!!.popularTv().results.mapNotNull { it.toDomain(MediaType.TV) }
                .ifEmpty { SampleData.tvShows }
        }
    }

    override suspend fun topRated(): List<MediaItem> = withContext(Dispatchers.IO) {
        safeApi(SampleData.topRated()) {
            api!!.topRatedMovies().results.mapNotNull { it.toDomain(MediaType.MOVIE) }
                .ifEmpty { SampleData.topRated() }
        }
    }

    override suspend fun newReleases(): List<MediaItem> = withContext(Dispatchers.IO) {
        val client = api
        if (!isLive || client == null) return@withContext SampleData.newReleases()
        runCatching {
            (client.popularMovies().results.mapNotNull { it.toDomain(MediaType.MOVIE) } +
                client.popularTv().results.mapNotNull { it.toDomain(MediaType.TV) })
                .sortedByDescending { it.year ?: 0 }
                .take(12)
                .ifEmpty { SampleData.newReleases() }
        }.getOrElse { SampleData.newReleases() }
    }

    override suspend fun search(query: String): List<MediaItem> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext emptyList()
        if (!isLive) return@withContext SampleData.search(q)
        runCatching {
            api!!.search(q).results
                .mapNotNull { it.toDomain(MediaType.MOVIE) }
                .filter { it.mediaType == MediaType.MOVIE || it.mediaType == MediaType.TV }
        }.getOrElse { SampleData.search(q) }
    }

    override suspend fun detail(id: Int, mediaType: MediaType): MediaDetail =
        withContext(Dispatchers.IO) {
            val client = api
            if (isLive && client != null) {
                runCatching {
                    if (mediaType == MediaType.MOVIE) client.movieDetail(id).toDetail()
                    else client.tvDetail(id).toDetail()
                }.getOrNull()
            } else null
        } ?: SampleData.allItems.find { it.id == id && it.mediaType == mediaType }
            ?.let { SampleData.detailFor(it) }
            ?: run {
                // Unknown id (API error + no sample match): minimal placeholder.
                val stub = MediaItem(
                    id = id, mediaType = mediaType, title = "Details unavailable",
                    overview = "", posterPath = null, backdropPath = null,
                    voteAverage = 0.0, voteCount = 0, releaseDate = null,
                    genres = emptyList(), popularity = 0.0
                )
                MediaDetail(
                    item = stub, tagline = null, status = null, originalLanguage = "en",
                    runtimeMinutes = null, numberOfSeasons = null, numberOfEpisodes = null,
                    cast = emptyList(), videos = emptyList(), similar = emptyList()
                )
            }

    override suspend fun pool(): List<MediaItem> = withContext(Dispatchers.IO) {
        val client = api
        if (!isLive || client == null) return@withContext SampleData.allItems
        runCatching {
            val lists = listOf(
                runCatching { client.trending().results.mapNotNull { it.toDomain(MediaType.MOVIE) } }.getOrDefault(emptyList()),
                runCatching { client.popularMovies().results.mapNotNull { it.toDomain(MediaType.MOVIE) } }.getOrDefault(emptyList()),
                runCatching { client.popularTv().results.mapNotNull { it.toDomain(MediaType.TV) } }.getOrDefault(emptyList()),
                runCatching { client.topRatedMovies().results.mapNotNull { it.toDomain(MediaType.MOVIE) } }.getOrDefault(emptyList())
            )
            lists.flatten().distinctBy { it.key }.ifEmpty { SampleData.allItems }
        }.getOrElse { SampleData.allItems }
    }
}
