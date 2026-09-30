package com.ikki.recommendme.ui.questionnaire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikki.recommendme.data.repository.UserProfileRepository
import com.ikki.recommendme.domain.model.AppGenre
import com.ikki.recommendme.ui.appViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Two-step onboarding for new users:
 *  step 1 -> pick 1..5 favourite genres
 *  step 2 -> pick 1..5 disliked genres
 */
class QuestionnaireViewModel(
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    data class QuestionnaireState(
        val step: Int = 1,
        val liked: List<String> = emptyList(),
        val disliked: List<String> = emptyList(),
        val saving: Boolean = false,
        val finished: Boolean = false
    ) {
        val likedCount: Int get() = liked.size
        val dislikedCount: Int get() = disliked.size
        fun canContinue(): Boolean = if (step == 1) liked.isNotEmpty() else disliked.isNotEmpty()
    }

    private val _state = MutableStateFlow(QuestionnaireState())
    val state: StateFlow<QuestionnaireState> = _state.asStateFlow()

    fun toggleLiked(genre: AppGenre) {
        _state.update { s ->
            val name = genre.display
            val next = if (s.liked.contains(name)) s.liked - name
            else if (s.liked.size >= MAX_GENRES) s.liked
            else s.liked + name
            s.copy(
                liked = next,
                disliked = s.disliked - name // a genre cannot be in both lists
            )
        }
    }

    fun toggleDisliked(genre: AppGenre) {
        _state.update { s ->
            val name = genre.display
            val next = if (s.disliked.contains(name)) s.disliked - name
            else if (s.disliked.size >= MAX_GENRES) s.disliked
            else s.disliked + name
            s.copy(
                disliked = next,
                liked = s.liked - name
            )
        }
    }

    fun next() {
        val s = _state.value
        if (!s.canContinue()) return
        if (s.step == 1) {
            _state.update { it.copy(step = 2) }
        } else {
            submit()
        }
    }

    fun back() {
        _state.update { if (it.step > 1) it.copy(step = it.step - 1) else it }
    }

    private fun submit() {
        val s = _state.value
        if (s.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            profileRepository.completeQuestionnaire(s.liked, s.disliked)
            _state.update { it.copy(saving = false, finished = true) }
        }
    }

    companion object {
        const val MAX_GENRES = 5
        val Factory = appViewModelFactory { QuestionnaireViewModel(it.userProfileRepository) }
    }
}
