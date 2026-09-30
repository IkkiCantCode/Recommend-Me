package com.ikki.recommendme.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikki.recommendme.data.repository.MediaRepository
import com.ikki.recommendme.data.repository.UserProfileRepository
import com.ikki.recommendme.data.repository.WatchlistRepository
import com.ikki.recommendme.domain.model.MediaDetail
import com.ikki.recommendme.domain.model.MediaType
import com.ikki.recommendme.domain.model.WatchStatus
import com.ikki.recommendme.domain.model.WatchlistEntry
import com.ikki.recommendme.ui.appViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val id: Int,
    private val mediaType: MediaType,
    private val mediaRepository: MediaRepository,
    private val watchlistRepository: WatchlistRepository,
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    data class DetailUiState(
        val loading: Boolean = true,
        val detail: MediaDetail? = null,
        val inWatchlist: Boolean = false,
        val status: WatchStatus? = null,
        val userScore: Int? = null
    ) {
        val isBookmarked: Boolean get() = inWatchlist
    }

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val detail = mediaRepository.detail(id, mediaType)
            _uiState.update { it.copy(loading = false, detail = detail) }
        }
        viewModelScope.launch {
            watchlistRepository.entry(id, mediaType).collect { entry: WatchlistEntry? ->
                _uiState.update {
                    it.copy(
                        inWatchlist = entry != null,
                        status = entry?.status,
                        userScore = entry?.userScore
                    )
                }
            }
        }
    }

    /** Bookmark button on the detail page. */
    fun toggleBookmark() {
        val state = _uiState.value
        val detail = state.detail ?: return
        viewModelScope.launch {
            if (state.inWatchlist) {
                watchlistRepository.remove(id, mediaType)
                profileRepository.removeRating(detail.item.key)
            } else {
                watchlistRepository.add(detail.item)
            }
        }
    }

    /**
     * Moves the title between watchlist lists. Choosing RATED requires a
     * 1..10 score; leaving RATED clears the score everywhere.
     */
    fun moveTo(status: WatchStatus, score: Int? = null) {
        val detail = _uiState.value.detail ?: return
        viewModelScope.launch {
            if (!_uiState.value.inWatchlist) {
                watchlistRepository.add(detail.item, status)
            }
            watchlistRepository.setStatus(id, mediaType, status, score)
            when {
                status == WatchStatus.RATED && score != null ->
                    profileRepository.rate(detail.item.key, score)
                status != WatchStatus.RATED ->
                    profileRepository.removeRating(detail.item.key)
            }
        }
    }

    companion object {
        fun factory(id: Int, mediaType: MediaType) = appViewModelFactory {
            DetailViewModel(
                id, mediaType,
                it.mediaRepository,
                it.watchlistRepository,
                it.userProfileRepository
            )
        }
    }
}
