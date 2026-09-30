package com.ikki.recommendme.data.remote

import com.ikki.recommendme.domain.model.CastMember
import com.ikki.recommendme.domain.model.MediaDetail
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.domain.model.MediaType
import com.ikki.recommendme.domain.model.VideoClip

/** Maps TMDB DTOs into domain models. */

fun TmdbMediaDto.toDomain(defaultType: MediaType): MediaItem? {
    if (id <= 0) return null
    val type = when (mediaType?.lowercase()) {
        "movie" -> MediaType.MOVIE
        "tv" -> MediaType.TV
        "person" -> return null
        else -> defaultType
    }
    return MediaItem(
        id = id,
        mediaType = type,
        title = title ?: name ?: originalTitle ?: originalName ?: "Untitled",
        overview = overview.orEmpty(),
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        voteCount = voteCount,
        releaseDate = releaseDate ?: firstAirDate,
        genres = genreIds.mapNotNull { TmdbConfig.GENRE_NAMES[it] },
        popularity = popularity
    )
}

fun TmdbMovieDetailDto.toDomain(): MediaItem = MediaItem(
    id = id,
    mediaType = MediaType.MOVIE,
    title = title ?: "Untitled",
    overview = overview.orEmpty(),
    posterPath = posterPath,
    backdropPath = backdropPath,
    voteAverage = voteAverage,
    voteCount = voteCount,
    releaseDate = releaseDate,
    genres = genres.map { it.name },
    popularity = popularity
)

fun TmdbTvDetailDto.toDomain(): MediaItem = MediaItem(
    id = id,
    mediaType = MediaType.TV,
    title = name ?: "Untitled",
    overview = overview.orEmpty(),
    posterPath = posterPath,
    backdropPath = backdropPath,
    voteAverage = voteAverage,
    voteCount = voteCount,
    releaseDate = firstAirDate,
    genres = genres.map { it.name },
    popularity = popularity
)

fun TmdbMovieDetailDto.toDetail(): MediaDetail = MediaDetail(
    item = toDomain(),
    tagline = tagline,
    status = status,
    originalLanguage = originalLanguage ?: "en",
    runtimeMinutes = runtime,
    numberOfSeasons = null,
    numberOfEpisodes = null,
    cast = credits?.cast.orEmpty().map {
        CastMember(it.id, it.name, it.character, it.profilePath)
    },
    videos = videos?.results.orEmpty()
        .filter { it.key.isNotBlank() }
        .map { VideoClip(it.key, it.name, it.site, it.type) },
    similar = similar?.results.orEmpty().mapNotNull { it.toDomain(MediaType.MOVIE) }
)

fun TmdbTvDetailDto.toDetail(): MediaDetail = MediaDetail(
    item = toDomain(),
    tagline = tagline,
    status = status,
    originalLanguage = originalLanguage ?: "en",
    runtimeMinutes = null,
    numberOfSeasons = numberOfSeasons,
    numberOfEpisodes = numberOfEpisodes,
    cast = credits?.cast.orEmpty().map {
        CastMember(it.id, it.name, it.character, it.profilePath)
    },
    videos = videos?.results.orEmpty()
        .filter { it.key.isNotBlank() }
        .map { VideoClip(it.key, it.name, it.site, it.type) },
    similar = similar?.results.orEmpty().mapNotNull { it.toDomain(MediaType.TV) }
)
