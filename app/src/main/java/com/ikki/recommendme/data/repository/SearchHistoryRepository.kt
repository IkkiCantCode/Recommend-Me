package com.ikki.recommendme.data.repository

import com.ikki.recommendme.data.local.AppDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Search history of the signed-in user (max 10 entries, newest first). */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchHistoryRepository(private val store: AppDataStore) {

    val history: Flow<List<String>> = store.sessionEmail.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(emptyList())
        else store.searchHistory(email)
    }

    suspend fun add(query: String) {
        val email = store.sessionEmail.first() ?: return
        store.addSearchHistory(email, query)
    }

    suspend fun clear() {
        val email = store.sessionEmail.first() ?: return
        store.clearSearchHistory(email)
    }
}
