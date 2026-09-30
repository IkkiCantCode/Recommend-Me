package com.ikki.recommendme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikki.recommendme.data.repository.MediaRepository
import com.ikki.recommendme.data.repository.UserProfileRepository
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.domain.model.SortOption
import com.ikki.recommendme.domain.model.MediaSorter
import com.ikki.recommendme.domain.model.UserProfile
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

class HomeViewModel(
    private val mediaRepository: MediaRepository,
    private val profileRepository: UserProfileRepository,
    private val recommendationEngine: RecommendationEngine
) : ViewModel() {

    data class HomeUiState(
        val loading: Boolean = true,
        val profile: UserProfile? = null,
        val rows: List<ContentRow> = emptyList(),
        val usingSampleData: Boolean = true,
        val query: String = "",
        val searching: Boolean = false,
        val searchResults: List<MediaItem> = emptyList(),
        val sort: SortOption = SortOption.RELEVANCE,
        val genreFilter: String? = null,
        val availableGenres: List<String> = emptyList()
    ) {
        val isSearching: Boolean get() = query.isNotBlank()
        val displayedResults: List<MediaItem>
            get() = MediaSorter.apply(searchResults, sort, genreFilter)
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            val profile = profileRepository.currentProfile.first()
            val pool = mediaRepository.pool()
            val recommendations = recommendationEngine.recommend(pool, profile)

            val rows = buildList {
                add(
                    ContentRow(
                        title = "Recommended for You",
                        entries = recommendations.take(10).map {
                            RowEntry(it.item, it.reason)
                        }
                    )
                )
                add(ContentRow("Trending Now", mediaRepository.trending().map { RowEntry(it) }))
                add(ContentRow("Popular Movies", mediaRepository.popularMovies().map { RowEntry(it) }))
                add(ContentRow("Popular TV Shows", mediaRepository.popularTv().map { RowEntry(it) }))
                add(ContentRow("Top Rated", mediaRepository.topRated().map { RowEntry(it) }))
                add(ContentRow("New Releases", mediaRepository.newReleases().map { RowEntry(it) }))
            }

            _uiState.update {
                it.copy(
                    loading = false,
                    profile = profile,
                    rows = rows,
                    usingSampleData = !mediaRepository.isLive,
                    availableGenres = MediaSorter.genresIn(pool)
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), searching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.update { it.copy(searching = true) }
            val results = mediaRepository.search(query)
            _uiState.update { it.copy(searchResults = results, searching = false) }
        }
    }

    fun setSort(option: SortOption) {
        _uiState.update { it.copy(sort = option) }
    }

    fun setGenreFilter(genre: String?) {
        _uiState.update { it.copy(genreFilter = genre) }
    }

    companion object {
        val Factory = appViewModelFactory {
            HomeViewModel(it.mediaRepository, it.userProfileRepository, it.recommendationEngine)
        }
    }
}
