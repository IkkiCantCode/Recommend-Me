package com.ikki.recommendme.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikki.recommendme.data.repository.AuthRepository
import com.ikki.recommendme.data.repository.UserProfileRepository
import com.ikki.recommendme.domain.model.ThemeMode
import com.ikki.recommendme.domain.model.UserProfile
import com.ikki.recommendme.ui.appViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProfileEvent {
    data object SignedOut : ProfileEvent
}

class ProfileViewModel(
    private val profileRepository: UserProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = profileRepository.currentProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val themeMode: StateFlow<ThemeMode> = profileRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    private val _event = MutableStateFlow<ProfileEvent?>(null)
    val event: StateFlow<ProfileEvent?> = _event.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { profileRepository.setThemeMode(mode) }
    }

    fun updateStatus(text: String) {
        viewModelScope.launch { profileRepository.updateStatusText(text) }
    }

    fun chooseAvatar(index: Int) {
        viewModelScope.launch { profileRepository.updateAvatar(index) }
    }

    fun updateName(name: String) {
        viewModelScope.launch { profileRepository.updateName(name) }
    }

    fun signOut() {
        if (_saving.value) return
        _saving.update { true }
        viewModelScope.launch {
            authRepository.signOut()
            _saving.update { false }
            _event.update { ProfileEvent.SignedOut }
        }
    }

    fun consumeEvent() {
        _event.update { null }
    }

    companion object {
        val Factory = appViewModelFactory {
            ProfileViewModel(it.userProfileRepository, it.authRepository)
        }
    }
}
