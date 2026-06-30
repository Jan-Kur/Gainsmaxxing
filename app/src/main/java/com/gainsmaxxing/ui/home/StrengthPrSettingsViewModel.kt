package com.gainsmaxxing.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gainsmaxxing.data.repository.StrengthPrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StrengthPrSettingsUiState(
    val catalog: List<String> = emptyList(),
    val selected: Set<String> = emptySet(),
    val newExerciseName: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class StrengthPrSettingsViewModel @Inject constructor(
    private val strengthPrRepository: StrengthPrRepository,
) : ViewModel() {

    private var persistedCatalog: List<String> = emptyList()

    private val _uiState = MutableStateFlow(StrengthPrSettingsUiState())
    val uiState: StateFlow<StrengthPrSettingsUiState> = _uiState.asStateFlow()

    // No eager load here; the screen calls load() when it becomes visible so it refreshes on reopen.
    // init {
    //     load()
    // }

    fun load() {
        viewModelScope.launch {
            val settings = strengthPrRepository.observeSettings().first()
            persistedCatalog = settings.catalog
            _uiState.value = StrengthPrSettingsUiState(
                catalog = settings.catalog,
                selected = settings.selected.toSet(),
            )
        }
    }

    fun setNewExerciseName(name: String) {
        _uiState.value = _uiState.value.copy(newExerciseName = name, errorMessage = null)
    }

    fun addExercise(): Boolean {
        val name = _uiState.value.newExerciseName.trim()
        if (name.isEmpty()) return false
        val state = _uiState.value
        if (name in state.catalog) {
            _uiState.value = state.copy(errorMessage = "Exercise already exists")
            return false
        }
        _uiState.value = state.copy(
            catalog = state.catalog + name,
            newExerciseName = "",
            errorMessage = null,
        )
        return true
    }

    fun deleteExercise(name: String) {
        val state = _uiState.value
        _uiState.value = state.copy(
            catalog = state.catalog.filterNot { it == name },
            selected = state.selected - name,
            errorMessage = null,
        )
    }

    fun toggleDisplay(name: String) {
        val state = _uiState.value
        val selected = state.selected.toMutableSet()
        if (name in selected) {
            selected.remove(name)
        } else if (selected.size < StrengthPrRepository.MAX_SELECTION) {
            selected.add(name)
        } else {
            return
        }
        _uiState.value = state.copy(selected = selected, errorMessage = null)
    }

    fun save(onComplete: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            runCatching {
                strengthPrRepository.syncCatalog(persistedCatalog, state.catalog)
                val orderedSelection = state.catalog.filter { it in state.selected }
                strengthPrRepository.saveSelection(orderedSelection)
            }.onSuccess {
                onComplete()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Could not save",
                )
            }
        }
    }
}
