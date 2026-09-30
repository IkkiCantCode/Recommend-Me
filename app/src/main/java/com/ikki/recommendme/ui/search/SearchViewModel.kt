package com.ikki.recommendme.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikki.recommendme.data.repository.MediaRepository
import com.ikki.recommendme.data.repository.SearchHistoryRepository
import com.ikki.recommendme.data.repository.UserProfileRepository
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.domain.model.MediaSorter
import com.ikki.recommendme.domain.model.SortOption
import com.ikki.recommendme.domain.recommendation.RecommendationEngine
import com.ikki.recommendme.ui.appViewModelFactory
import com.ikki.recommendme.ui.model.ContentRow
import com.ikki.recommendme.ui.model.RowEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Netflix-style search page: recommendation rows are already visible below,
 * a search bar + filters on top, and the search history is persisted.
 */
class SearchViewModel(
    private val mediaRepository: MediaRepository,
    private val profileRepository: UserProfileRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val recommendationEngine: RecommendationEngine
) : ViewModel() {

    data class SearchUiState(
        val loading: Boolean = true,
        val query: String = "",
        val searching: Boolean = false,
        val results: List<MediaItem> = emptyList(),
        val sort: SortOption = SortOption.RELEVANCE,
        val genreFilter: String? = null,
        val availableGenres: List<String> = emptyList(),
        val history: List<String> = emptyList(),
        val rows: List<ContentRow> = emptyList()
    ) {
        val isSearching: Boolean get() = query.isNotBlank()
        val displayedResults: List<MediaItem>
            get() = MediaSorter.apply(results, sort, genreFilter)
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            searchHistoryRepository.history.collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val profile = profileRepository.currentProfile.first()
            val pool = mediaRepository.pool()
            val recommendations = recommendationEngine.recommend(pool, profile)

            val rows = listOf(
                ContentRow(
                    "Picked for You",
                    recommendations.take(10).map { RowEntry(it.item, it.reason) }
                ),
                ContentRow("Trending Now", mediaRepository.trending().map { RowEntry(it) }),
                ContentRow("Top Rated", mediaRepository.topRated().map { RowEntry(it) }),
                ContentRow("Popular TV Shows", mediaRepository.popularTv().map { RowEntry(it) })
            )

            _uiState.update {
                it.copy(
                    loading = false,
                    rows = rows,
                    availableGenres = MediaSorter.genresIn(pool)
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), searching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.update { it.copy(searching = true) }
            val found = mediaRepository.search(query)
            _uiState.update { it.copy(results = found, searching = false) }
        }
    }

    /** Called when the user submits a query (IME search) - saves it to history. */
    fun submitQuery() {
        val q = _uiState.value.query.trim()
        if (q.isNotEmpty()) viewModelScope.launch { searchHistoryRepository.add(q) }
    }

    fun searchFromHistory(query: String) {
        onQueryChange(query)
        viewModelScope.launch { searchHistoryRepository.add(query) }
    }

    fun clearHistory() {
        viewModelScope.launch { searchHistoryRepository.clear() }
    }

    fun setSort(option: SortOption) {
        _uiState.update { it.copy(sort = option) }
    }

    fun setGenreFilter(genre: String?) {
        _uiState.update { it.copy(genreFilter = genre) }
    }

    companion object {
        val Factory = appViewModelFactory {
            SearchViewModel(
                it.mediaRepository,
                it.userProfileRepository,
                it.searchHistoryRepository,
                it.recommendationEngine
            )
        }
    }
}
