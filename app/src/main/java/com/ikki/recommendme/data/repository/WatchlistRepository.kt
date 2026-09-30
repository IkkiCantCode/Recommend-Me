package com.ikki.recommendme.data.repository

import com.ikki.recommendme.data.local.AppDataStore
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.domain.model.MediaType
import com.ikki.recommendme.domain.model.WatchStatus
import com.ikki.recommendme.domain.model.WatchlistEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Watchlist of the currently signed-in user (3 MyAnimeList-style lists:
 * Watching / Rated / Dropped).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistRepository(private val store: AppDataStore) {

    val entries: Flow<List<WatchlistEntry>> = store.sessionEmail.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(emptyList())
        else store.watchlist(email)
    }

    private suspend fun currentEmail(): String? = store.sessionEmail.first()

    suspend fun add(item: MediaItem, status: WatchStatus = WatchStatus.WATCHING) {
        val email = currentEmail() ?: return
        val list = store.watchlist(email).first()
        if (list.any { it.key == item.key }) return
        store.setWatchlist(email, list + WatchlistEntry.from(item).copy(status = status))
    }

    suspend fun remove(id: Int, mediaType: MediaType) {
        val email = currentEmail() ?: return
        val key = "${mediaType.name.lowercase()}-$id"
        val list = store.watchlist(email).first()
        store.setWatchlist(email, list.filterNot { it.key == key })
    }

    /**
     * Moves an entry between lists. Moving into [WatchStatus.RATED] keeps the
     * score; moving out of it clears the score so the Rated list stays clean.
     */
    suspend fun setStatus(id: Int, mediaType: MediaType, status: WatchStatus, score: Int? = null) {
        val email = currentEmail() ?: return
        val key = "${mediaType.name.lowercase()}-$id"
        val list = store.watchlist(email).first()
        val updated = list.map { entry ->
            if (entry.key != key) entry
            else entry.copy(
                status = status,
                userScore = when {
                    status == WatchStatus.RATED -> score ?: entry.userScore
                    else -> null
                }
            )
        }
        store.setWatchlist(email, updated)
    }

    suspend fun setScore(id: Int, mediaType: MediaType, score: Int?) {
        val email = currentEmail() ?: return
        val key = "${mediaType.name.lowercase()}-$id"
        val list = store.watchlist(email).first()
        store.setWatchlist(
            email,
            list.map {
                if (it.key == key) it.copy(userScore = score, status = WatchStatus.RATED)
                else it
            }
        )
    }

    fun entry(id: Int, mediaType: MediaType): Flow<WatchlistEntry?> {
        val key = "${mediaType.name.lowercase()}-$id"
        return entries.map { list -> list.find { it.key == key } }
    }
}
