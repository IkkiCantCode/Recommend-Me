package com.ikki.recommendme.data.remote

/**
 * TMDB configuration.
 *
 * ============================================================
 *  PASTE YOUR TMDB API KEY HERE once TMDB approves your GitHub
 *  repo request (https://www.themoviedb.org/settings/api).
 *  Everything else is already wired: as soon as API_KEY is a
 *  real value the app automatically switches from sample data
 *  to live TMDB data. No other code changes required.
 * ============================================================
 */
object TmdbConfig {
    const val API_KEY = "YOUR_TMDB_API_KEY_HERE"

    const val BASE_URL = "https://api.themoviedb.org/3/"
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    const val POSTER_SIZE = "w500"
    const val BACKDROP_SIZE = "w780"
    const val PROFILE_SIZE = "w185"

    val isConfigured: Boolean
        get() = API_KEY.isNotBlank() && API_KEY != "YOUR_TMDB_API_KEY_HERE"

    fun posterUrl(path: String?): String? =
        path?.takeIf { it.isNotBlank() }?.let { "$IMAGE_BASE_URL$POSTER_SIZE$it" }

    fun backdropUrl(path: String?): String? =
        path?.takeIf { it.isNotBlank() }?.let { "$IMAGE_BASE_URL$BACKDROP_SIZE$it" }

    fun profileUrl(path: String?): String? =
        path?.takeIf { it.isNotBlank() }?.let { "$IMAGE_BASE_URL$PROFILE_SIZE$it" }

    /** TMDB genre id -> name (movie + tv ids merged). */
    val GENRE_NAMES: Map<Int, String> = mapOf(
        28 to "Action", 12 to "Adventure", 16 to "Animation", 35 to "Comedy",
        80 to "Crime", 99 to "Documentary", 18 to "Drama", 10751 to "Family",
        14 to "Fantasy", 36 to "History", 27 to "Horror", 10402 to "Music",
        9648 to "Mystery", 10749 to "Romance", 878 to "Science Fiction",
        10770 to "TV Movie", 53 to "Thriller", 10752 to "War", 37 to "Western",
        10759 to "Action & Adventure", 10762 to "Kids", 10763 to "News",
        10764 to "Reality", 10765 to "Sci-Fi & Fantasy", 10766 to "Soap",
        10767 to "Talk", 10768 to "War & Politics"
    )
}
