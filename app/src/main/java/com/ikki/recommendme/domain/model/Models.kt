package com.ikki.recommendme.domain.model

/**
 * Domain models used across the whole app.
 * The TMDB layer maps its DTOs into these classes, so the UI never
 * depends on API-specific shapes.
 */

enum class MediaType { MOVIE, TV }

data class MediaItem(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val overview: String,
    /** TMDB image path (e.g. "/abc123.jpg"). Null -> local placeholder poster. */
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val voteCount: Int,
    /** "yyyy-MM-dd" */
    val releaseDate: String?,
    /** Genre names, already mapped from TMDB genre ids. */
    val genres: List<String>,
    val popularity: Double
) {
    val year: Int? get() = releaseDate?.take(4)?.toIntOrNull()
    /** Stable identifier used by the recommendation engine / watchlist / ratings. */
    val key: String get() = "${mediaType.name.lowercase()}-$id"
}

data class CastMember(
    val id: Int,
    val name: String,
    val character: String?,
    val profilePath: String?
)

data class VideoClip(
    val key: String,
    val name: String,
    val site: String,
    val type: String
) {
    val isYouTube: Boolean get() = site.equals("YouTube", ignoreCase = true)
}

data class MediaDetail(
    val item: MediaItem,
    val tagline: String?,
    val status: String?,
    val originalLanguage: String,
    val runtimeMinutes: Int?,
    val numberOfSeasons: Int?,
    val numberOfEpisodes: Int?,
    val cast: List<CastMember>,
    val videos: List<VideoClip>,
    val similar: List<MediaItem>
)

/** Genres offered in the onboarding questionnaire (subset of TMDB genres). */
enum class AppGenre(val display: String, val emoji: String) {
    ACTION("Action", "💥"),
    ADVENTURE("Adventure", "🧭"),
    ANIMATION("Animation", "🎨"),
    COMEDY("Comedy", "😂"),
    CRIME("Crime", "🔍"),
    DOCUMENTARY("Documentary", "🎥"),
    DRAMA("Drama", "🎭"),
    FANTASY("Fantasy", "🐉"),
    HORROR("Horror", "👻"),
    MYSTERY("Mystery", "🕵️"),
    ROMANCE("Romance", "💕"),
    SCI_FI("Sci-Fi & Fantasy", "🚀"),
    THRILLER("Thriller", "🫣"),
    WAR("War & Politics", "🎖️"),
    FAMILY("Family & Kids", "🧸"),
    MUSIC("Music", "🎵")
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class WatchStatus(val display: String) {
    WATCHING("Watching"),
    RATED("Rated"),
    DROPPED("Dropped")
}

data class UserProfile(
    val email: String,
    val name: String,
    /** SHA-256 hash. Null for Google accounts. Placeholder until Firebase Auth. */
    val passwordHash: String? = null,
    val provider: String = "local",
    val avatarIndex: Int = 0,
    val statusText: String = "New here 🎬",
    /** false until the genre questionnaire is completed (new users only). */
    val onboarded: Boolean = false,
    val likedGenres: List<String> = emptyList(),
    val dislikedGenres: List<String> = emptyList(),
    /** mediaKey -> user score 1..10 (MyAnimeList style). Feeds collaborative filtering. */
    val ratings: Map<String, Int> = emptyMap()
)

data class WatchlistEntry(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val posterPath: String?,
    val year: Int?,
    val genres: List<String>,
    val voteAverage: Double,
    val status: WatchStatus = WatchStatus.WATCHING,
    val userScore: Int? = null,
    val addedAt: Long = System.currentTimeMillis()
) {
    val key: String get() = "${mediaType.name.lowercase()}-$id"

    companion object {
        fun from(item: MediaItem) = WatchlistEntry(
            id = item.id,
            mediaType = item.mediaType,
            title = item.title,
            posterPath = item.posterPath,
            year = item.year,
            genres = item.genres,
            voteAverage = item.voteAverage
        )
    }
}

/** One item produced by the hybrid recommendation engine. */
data class Recommendation(
    val item: MediaItem,
    val score: Double,
    /** Human readable explanation, e.g. "Because you like Sci-Fi". */
    val reason: String
)

sealed class AuthResult {
    data class Success(val profile: UserProfile) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
