package com.ikki.recommendme.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/* ---------------- REST endpoints ---------------- */

interface TmdbApiService {

    @GET("trending/all/week")
    suspend fun trending(
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/popular")
    suspend fun popularMovies(
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("tv/popular")
    suspend fun popularTv(
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/top_rated")
    suspend fun topRatedMovies(
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("search/multi")
    suspend fun search(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/{id}")
    suspend fun movieDetail(
        @Path("id") id: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar"
    ): TmdbMovieDetailDto

    @GET("tv/{id}")
    suspend fun tvDetail(
        @Path("id") id: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar"
    ): TmdbTvDetailDto
}

/* ---------------- DTOs ---------------- */

data class TmdbPagedResponse<T>(
    val page: Int = 1,
    val results: List<T> = emptyList(),
    @SerializedName("total_pages") val totalPages: Int = 0,
    @SerializedName("total_results") val totalResults: Int = 0
)

/** Shared shape of a movie/tv item in list responses. */
data class TmdbMediaDto(
    val id: Int = 0,
    val title: String? = null,
    val name: String? = null,
    @SerializedName("original_title") val originalTitle: String? = null,
    @SerializedName("original_name") val originalName: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
    val popularity: Double = 0.0,
    @SerializedName("media_type") val mediaType: String? = null
)

data class TmdbGenreDto(
    val id: Int = 0,
    val name: String = ""
)

data class TmdbCastDto(
    val id: Int = 0,
    val name: String = "",
    val character: String? = null,
    @SerializedName("profile_path") val profilePath: String? = null
)

data class TmdbCreditsDto(
    val cast: List<TmdbCastDto> = emptyList()
)

data class TmdbVideoDto(
    val key: String = "",
    val name: String = "",
    val site: String = "",
    val type: String = ""
)

data class TmdbVideosDto(
    val results: List<TmdbVideoDto> = emptyList()
)

data class TmdbSimilarDto(
    val results: List<TmdbMediaDto> = emptyList()
)

data class TmdbMovieDetailDto(
    val id: Int = 0,
    val title: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("release_date") val releaseDate: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    val popularity: Double = 0.0,
    val tagline: String? = null,
    val status: String? = null,
    @SerializedName("original_language") val originalLanguage: String? = null,
    val runtime: Int? = null,
    val credits: TmdbCreditsDto? = null,
    val videos: TmdbVideosDto? = null,
    val similar: TmdbSimilarDto? = null
)

data class TmdbTvDetailDto(
    val id: Int = 0,
    val name: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    val popularity: Double = 0.0,
    val tagline: String? = null,
    val status: String? = null,
    @SerializedName("original_language") val originalLanguage: String? = null,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int? = null,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int? = null,
    val credits: TmdbCreditsDto? = null,
    val videos: TmdbVideosDto? = null,
    val similar: TmdbSimilarDto? = null
)

/* ---------------- Client ---------------- */

object TmdbApi {

    fun createService(isDebug: Boolean): TmdbApiService? {
        if (!TmdbConfig.isConfigured) return null
        return runCatching {
            val logging = HttpLoggingInterceptor().apply {
                level = if (isDebug) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            }
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val url = chain.request().url.newBuilder()
                        .addQueryParameter("api_key", TmdbConfig.API_KEY)
                        .build()
                    chain.proceed(chain.request().newBuilder().url(url).build())
                }
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            Retrofit.Builder()
                .baseUrl(TmdbConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TmdbApiService::class.java)
        }.getOrNull()
    }
}
