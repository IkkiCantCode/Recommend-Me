package com.ikki.recommendme.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikki.recommendme.data.repository.UserProfileRepository
import com.ikki.recommendme.data.repository.WatchlistRepository
import com.ikki.recommendme.domain.model.WatchStatus
import com.ikki.recommendme.domain.model.WatchlistEntry
import com.ikki.recommendme.ui.appViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Watchlist page with the 3 MyAnimeList-style tabs: Watching / Rated / Dropped. */
class WatchlistViewModel(
    private val watchlistRepository: WatchlistRepository,
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    val entries: StateFlow<List<WatchlistEntry>> = watchlistRepository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    fun setTab(index: Int) {
        _selectedTab.update { index.coerceIn(0, TABS.lastIndex) }
    }

    fun entriesFor(tab: Int, all: List<WatchlistEntry>): List<WatchlistEntry> =
        all.filter { it.status == tabStatus(tab) }

    fun countFor(tab: Int, all: List<WatchlistEntry>): Int =
        all.count { it.status == tabStatus(tab) }

    private fun tabStatus(tab: Int): WatchStatus = when (tab) {
        0 -> WatchStatus.WATCHING
        1 -> WatchStatus.RATED
        else -> WatchStatus.DROPPED
    }

    /** Moves an entry to another list; RATED requires a score. */
    fun move(entry: WatchlistEntry, status: WatchStatus, score: Int? = null) {
        viewModelScope.launch {
            val previous = entry.status
            watchlistRepository.setStatus(entry.id, entry.mediaType, status, score)
            when {
                status == WatchStatus.RATED && score != null ->
                    profileRepository.rate(entry.key, score)
                previous == WatchStatus.RATED && status != WatchStatus.RATED ->
                    profileRepository.removeRating(entry.key)
            }
        }
    }

    /** Updates only the score (keeps the title in the Rated list). */
    fun setScore(entry: WatchlistEntry, score: Int) {
        viewModelScope.launch {
            watchlistRepository.setScore(entry.id, entry.mediaType, score)
            profileRepository.rate(entry.key, score)
        }
    }

    fun remove(entry: WatchlistEntry) {
        viewModelScope.launch {
            watchlistRepository.remove(entry.id, entry.mediaType)
        }
    }

    companion object {
        val TABS = listOf("Watching", "Rated", "Dropped")
        val Factory = appViewModelFactory {
            WatchlistViewModel(it.watchlistRepository, it.userProfileRepository)
        }
    }
}
