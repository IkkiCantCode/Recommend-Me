package com.ikki.recommendme.domain.model

/** Sorting / filtering options shared by the Home and Search screens. */
enum class SortOption(val display: String) {
    RELEVANCE("Relevance"),
    SCORE_HIGH("Score: High to Low"),
    SCORE_LOW("Score: Low to High"),
    YEAR_NEW("Year: Newest"),
    YEAR_OLD("Year: Oldest"),
    TITLE_AZ("Title: A-Z")
}

object MediaSorter {

    /** Applies an optional single-genre filter and then the sort option. */
    fun apply(
        items: List<MediaItem>,
        option: SortOption,
        genre: String? = null
    ): List<MediaItem> {
        val filtered = if (genre.isNullOrBlank()) items
        else items.filter { item ->
            item.genres.any { it.equals(genre, ignoreCase = true) } ||
                genreMatchesLoosely(item, genre)
        }
        return when (option) {
            SortOption.RELEVANCE -> filtered.sortedByDescending { it.popularity }
            SortOption.SCORE_HIGH -> filtered.sortedByDescending { it.voteAverage }
            SortOption.SCORE_LOW -> filtered.sortedBy { it.voteAverage }
            SortOption.YEAR_NEW -> filtered.sortedByDescending { it.year ?: 0 }
            SortOption.YEAR_OLD -> filtered.sortedBy { it.year ?: 0 }
            SortOption.TITLE_AZ -> filtered.sortedBy { it.title.lowercase() }
        }
    }

    /** Matches questionnaire genre labels such as "Sci-Fi & Fantasy" to item genres. */
    private fun genreMatchesLoosely(item: MediaItem, genre: String): Boolean {
        val needle = genre.lowercase()
        return item.genres.any { g ->
            val gg = g.lowercase()
            when {
                needle.contains("sci-fi") -> gg.contains("science fiction") || gg.contains("sci-fi")
                needle.contains("family") -> gg == "family" || gg == "kids"
                needle.contains("war") -> gg.contains("war")
                needle.contains("action") -> gg.contains("action")
                else -> false
            }
        }
    }

    /** Distinct genre labels present in a list of items (for filter chips). */
    fun genresIn(items: List<MediaItem>): List<String> =
        items.flatMap { it.genres }.distinct().sorted()
}
