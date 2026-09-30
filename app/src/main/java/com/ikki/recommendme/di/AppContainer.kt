package com.ikki.recommendme.di

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.Context
import com.ikki.recommendme.data.local.AppDataStore
import com.ikki.recommendme.data.remote.TmdbApi
import com.ikki.recommendme.data.repository.AuthRepository
import com.ikki.recommendme.data.repository.DefaultMediaRepository
import com.ikki.recommendme.data.repository.LocalAuthRepository
import com.ikki.recommendme.data.repository.MediaRepository
import com.ikki.recommendme.data.repository.SearchHistoryRepository
import com.ikki.recommendme.data.repository.UserProfileRepository
import com.ikki.recommendme.data.repository.WatchlistRepository
import com.ikki.recommendme.domain.recommendation.RecommendationEngine

/**
 * Manual dependency container (lightweight DI).
 * ViewModels receive dependencies through their ViewModelProvider factories.
 * If you later want Hilt/Dagger, this is the single place to replace.
 */
class AppContainer(context: Context) {

    private val isDebug: Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    val dataStore = AppDataStore(context)

    val authRepository: AuthRepository = LocalAuthRepository(dataStore)
    val mediaRepository: MediaRepository =
        DefaultMediaRepository(TmdbApi.createService(isDebug))
    val watchlistRepository = WatchlistRepository(dataStore)
    val searchHistoryRepository = SearchHistoryRepository(dataStore)
    val userProfileRepository = UserProfileRepository(dataStore)
    val recommendationEngine = RecommendationEngine()
}

/** Owns the app-wide [AppContainer]. */
class RecommendApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
