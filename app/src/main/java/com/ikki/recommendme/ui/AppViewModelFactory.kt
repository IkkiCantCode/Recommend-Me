package com.ikki.recommendme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ikki.recommendme.di.AppContainer
import com.ikki.recommendme.di.RecommendApplication

/**
 * Central helper for creating ViewModels with manual DI.
 * Usage in a ViewModel companion:
 *
 *     val Factory = appViewModelFactory { MyViewModel(it.authRepository) }
 */
inline fun <reified VM : ViewModel> appViewModelFactory(
    crossinline create: (AppContainer) -> VM
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            as RecommendApplication
        create(app.container)
    }
}
