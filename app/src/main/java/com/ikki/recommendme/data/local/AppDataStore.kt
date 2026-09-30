package com.ikki.recommendme.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ikki.recommendme.domain.model.ThemeMode
import com.ikki.recommendme.domain.model.UserProfile
import com.ikki.recommendme.domain.model.WatchlistEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private val Context.recommendDataStore: DataStore<Preferences> by preferencesDataStore(name = "recommend_me_prefs")

/**
 * Single local persistence layer (Preferences DataStore + Gson).
 * Everything is kept per user account so switching to Firebase/Firestore
 * later only means changing repository implementations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppDataStore(private val context: Context) {

    private val gson = Gson()

    private object Keys {
        val USERS = stringPreferencesKey("users_json")
        val SESSION_EMAIL = stringPreferencesKey("session_email")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        fun watchlist(email: String) = stringPreferencesKey("watchlist_${email.hashCode()}")
        fun history(email: String) = stringPreferencesKey("history_${email.hashCode()}")
    }

    private inline fun <reified T> decodeList(json: String?): List<T> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<T>>(json, object : TypeToken<List<T>>() {}.type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    /* ---------- users & session ---------- */

    val users: Flow<List<UserProfile>> = context.recommendDataStore.data
        .map { decodeList<UserProfile>(it[Keys.USERS]) }

    val sessionEmail: Flow<String?> = context.recommendDataStore.data
        .map { it[Keys.SESSION_EMAIL] }

    val currentProfile: Flow<UserProfile?> = sessionEmail.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(null)
        else users.map { list -> list.find { it.email == email } }
    }

    suspend fun upsertUser(user: UserProfile) {
        context.recommendDataStore.edit { prefs ->
            val list = decodeList<UserProfile>(prefs[Keys.USERS])
                .filterNot { it.email.equals(user.email, ignoreCase = true) } + user
            prefs[Keys.USERS] = gson.toJson(list)
        }
    }

    suspend fun setSession(email: String?) {
        context.recommendDataStore.edit { prefs ->
            if (email.isNullOrBlank()) prefs.remove(Keys.SESSION_EMAIL)
            else prefs[Keys.SESSION_EMAIL] = email
        }
    }

    suspend fun updateProfile(email: String, transform: (UserProfile) -> UserProfile) {
        context.recommendDataStore.edit { prefs ->
            val list = decodeList<UserProfile>(prefs[Keys.USERS])
                .map { if (it.email.equals(email, ignoreCase = true)) transform(it) else it }
            prefs[Keys.USERS] = gson.toJson(list)
        }
    }

    /* ---------- theme ---------- */

    val themeMode: Flow<ThemeMode> = context.recommendDataStore.data.map { prefs ->
        runCatching { ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: "") }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.recommendDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    /* ---------- watchlist (per account) ---------- */

    fun watchlist(email: String): Flow<List<WatchlistEntry>> =
        context.recommendDataStore.data
            .map { decodeList<WatchlistEntry>(it[Keys.watchlist(email)]) }

    suspend fun setWatchlist(email: String, entries: List<WatchlistEntry>) {
        context.recommendDataStore.edit { prefs ->
            prefs[Keys.watchlist(email)] = gson.toJson(entries)
        }
    }

    /* ---------- search history (per account) ---------- */

    fun searchHistory(email: String): Flow<List<String>> =
        context.recommendDataStore.data
            .map { decodeList<String>(it[Keys.history(email)]) }

    suspend fun addSearchHistory(email: String, query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        context.recommendDataStore.edit { prefs ->
            val key = Keys.history(email)
            val current = decodeList<String>(prefs[key])
            prefs[key] = gson.toJson((listOf(q) + current).distinct().take(10))
        }
    }

    suspend fun clearSearchHistory(email: String) {
        context.recommendDataStore.edit { prefs -> prefs.remove(Keys.history(email)) }
    }
}
