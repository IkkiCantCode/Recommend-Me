package com.ikki.recommendme.data.repository

import com.ikki.recommendme.data.local.AppDataStore
import com.ikki.recommendme.domain.model.ThemeMode
import com.ikki.recommendme.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * User profile, questionnaire results, ratings and appearance settings.
 * All reads/writes go through [AppDataStore]; a Firestore implementation
 * can replace it behind this class later.
 */
class UserProfileRepository(private val store: AppDataStore) {

    val currentProfile: Flow<UserProfile?> = store.currentProfile
    val themeMode: Flow<ThemeMode> = store.themeMode

    private suspend fun email(): String? = store.sessionEmail.first()

    suspend fun completeQuestionnaire(likedGenres: List<String>, dislikedGenres: List<String>) {
        val e = email() ?: return
        store.updateProfile(e) {
            it.copy(
                onboarded = true,
                likedGenres = likedGenres,
                dislikedGenres = dislikedGenres
            )
        }
    }

    suspend fun updateStatusText(text: String) {
        val e = email() ?: return
        store.updateProfile(e) { it.copy(statusText = text.trim().take(80)) }
    }

    suspend fun updateAvatar(index: Int) {
        val e = email() ?: return
        store.updateProfile(e) { it.copy(avatarIndex = index.coerceIn(0, 7)) }
    }

    suspend fun updateName(name: String) {
        val e = email() ?: return
        val n = name.trim()
        if (n.isNotEmpty()) store.updateProfile(e) { it.copy(name = n) }
    }

    /** Stores the user's 1..10 score for a title (feeds collaborative filtering later). */
    suspend fun rate(mediaKey: String, score: Int) {
        val e = email() ?: return
        store.updateProfile(e) {
            it.copy(ratings = it.ratings + (mediaKey to score.coerceIn(1, 10)))
        }
    }

    suspend fun removeRating(mediaKey: String) {
        val e = email() ?: return
        store.updateProfile(e) { p ->
            p.copy(ratings = p.ratings.filterKeys { it != mediaKey })
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)
}
