package com.ikki.recommendme.domain.recommendation

import com.ikki.recommendme.domain.model.AppGenre
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.domain.model.Recommendation
import com.ikki.recommendme.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Collaborative filtering half of the hybrid recommender.
 *
 * The current implementation returns no scores because it needs ratings from
 * many users, which will live in Cloud Firestore. The contract below is what
 * the Firestore implementation must fulfil:
 *
 *  1. Read  users/{uid}/ratings  for every user (mediaKey -> score 1..10).
 *  2. Compute similarity between the current user and the others
 *     (e.g. cosine similarity / Pearson correlation over the rating vectors).
 *  3. Return a weighted average of the top-N most similar users' scores for
 *     items the current user has NOT rated yet, normalised to 0..1.
 */
interface CollaborativeFilter {
    suspend fun scoresFor(profile: UserProfile, pool: List<MediaItem>): Map<String, Double>
}

/** Placeholder used until Firestore is connected - forces content-only mode. */
class NoopCollaborativeFilter : CollaborativeFilter {
    override suspend fun scoresFor(
        profile: UserProfile,
        pool: List<MediaItem>
    ): Map<String, Double> = emptyMap()
}

/**
 * Hybrid recommendation engine.
 *
 * finalScore = w1 * contentBased + w2 * collaborative   (blended, 0..1)
 *
 * Content-based part combines:
 *  - genre preferences from the onboarding questionnaire (like / dislike),
 *  - the user's own 1..10 ratings (genres of highly rated titles boost the taste vector),
 *  - a base score from the title's TMDB rating + popularity ("score base tinggi").
 */
class RecommendationEngine(
    private val collaborativeFilter: CollaborativeFilter = NoopCollaborativeFilter()
) {

    companion object {
        private const val LIKE_WEIGHT = 2.0
        private const val DISLIKE_WEIGHT = -2.5
        private const val RATING_INFLUENCE = 0.8
        private const val CONTENT_WEIGHT = 0.7
        private const val COLLAB_WEIGHT = 0.3
    }

    /** Maps raw TMDB genre names (movie + tv variants) to questionnaire genres. */
    private fun appGenresFor(names: List<String>): Set<AppGenre> =
        names.flatMap { name ->
            when (name) {
                "Action & Adventure" -> listOf(AppGenre.ACTION, AppGenre.ADVENTURE)
                "Science Fiction", "Sci-Fi & Fantasy", "Sci-Fi" -> listOf(AppGenre.SCI_FI)
                "Family", "Kids" -> listOf(AppGenre.FAMILY)
                "War", "War & Politics" -> listOf(AppGenre.WAR)
                else -> listOfNotNull(AppGenre.entries.find { it.display == name })
            }
        }.toSet()

    suspend fun recommend(
        pool: List<MediaItem>,
        profile: UserProfile?
    ): List<Recommendation> = withContext(Dispatchers.Default) {
        if (pool.isEmpty()) return@withContext emptyList()

        val liked = profile?.likedGenres.orEmpty().mapNotNull { l ->
            AppGenre.entries.find { it.display == l }
        }.toSet()
        val disliked = profile?.dislikedGenres.orEmpty().mapNotNull { d ->
            AppGenre.entries.find { it.display == d }
        }.toSet()
        val ratings = profile?.ratings.orEmpty()

        /* ---- taste vector ---- */
        val taste = mutableMapOf<AppGenre, Double>()
        liked.forEach { taste.merge(it, LIKE_WEIGHT, Double::plus) }
        disliked.forEach { taste.merge(it, DISLIKE_WEIGHT, Double::plus) }
        ratings.forEach { (key, score) ->
            val item = pool.find { it.key == key } ?: return@forEach
            val delta = (score - 5.5) / 4.5 // -1.0 .. +1.0
            appGenresFor(item.genres).forEach { g ->
                taste.merge(g, delta * RATING_INFLUENCE, Double::plus)
            }
        }

        /* ---- content-based raw scores ---- */
        val contentRaw: Map<String, Double> = pool.associate { item ->
            val genreScore = appGenresFor(item.genres).sumOf { taste[it] ?: 0.0 }
            val baseScore = (item.voteAverage / 10.0) * 2.0 +
                ((item.popularity / 200.0).coerceIn(0.0, 1.0) * 0.3)
            item.key to (genreScore + baseScore)
        }
        val contentNorm = normalize(contentRaw)

        /* ---- collaborative scores (empty until Firestore is ready) ---- */
        val collabRaw = if (profile == null) emptyMap()
        else runCatching { collaborativeFilter.scoresFor(profile, pool) }.getOrDefault(emptyMap())
        val collabNorm = normalize(collabRaw)

        /* ---- blend ---- */
        pool.map { item ->
            val content = contentNorm[item.key] ?: 0.0
            val collab = collabNorm[item.key]
            val finalScore = if (collab != null && collabRaw.isNotEmpty()) {
                CONTENT_WEIGHT * content + COLLAB_WEIGHT * collab
            } else {
                content
            }
            Recommendation(
                item = item,
                score = finalScore,
                reason = reasonFor(item, liked, ratings)
            )
        }.sortedByDescending { it.score }
    }

    private fun reasonFor(item: MediaItem, liked: Set<AppGenre>, ratings: Map<String, Int>): String {
        val matched = appGenresFor(item.genres).firstOrNull { it in liked }
        if (matched != null) return "Because you like ${matched.display}"
        if (ratings.containsKey(item.key)) return "You rated this"
        if (item.voteAverage >= 8.0) return "Critically acclaimed"
        if (item.voteAverage >= 7.0) return "Loved by viewers"
        return "Popular right now"
    }

    private fun normalize(scores: Map<String, Double>): Map<String, Double> {
        if (scores.isEmpty()) return emptyMap()
        val min = scores.values.min()
        val max = scores.values.max()
        val range = max - min
        if (range <= 1e-9) return scores.mapValues { 0.5 }
        return scores.mapValues { (_, v) -> (v - min) / range }
    }
}
