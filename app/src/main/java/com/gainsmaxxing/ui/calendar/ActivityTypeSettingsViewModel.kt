package com.gainsmaxxing.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gainsmaxxing.data.repository.CalendarRepository
import com.gainsmaxxing.domain.model.CalendarActivityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityTypeDraft(
    val id: Long,
    val name: String,
    val colorPaletteIndex: Int,
    val iconKey: String,
)

data class ActivityTypeSettingsUiState(
    val types: List<ActivityTypeDraft> = emptyList(),
    val expandedTypeId: Long? = null,
    val newTypeName: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ActivityTypeSettingsViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private var persistedTypes: List<CalendarActivityType> = emptyList()
    private var deletedIds: MutableSet<Long> = mutableSetOf()
    private var nextLocalId = -1L

    private val _uiState = MutableStateFlow(ActivityTypeSettingsUiState())
    val uiState: StateFlow<ActivityTypeSettingsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            persistedTypes = calendarRepository.observeActivityTypes().first()
            deletedIds.clear()
            nextLocalId = -1L
            _uiState.value = ActivityTypeSettingsUiState(
                types = persistedTypes.map { it.toDraft() },
            )
        }
    }

    fun setNewTypeName(name: String) {
        _uiState.value = _uiState.value.copy(newTypeName = name, errorMessage = null)
    }

    fun addType(): Boolean {
        val name = _uiState.value.newTypeName.trim()
        if (name.isEmpty()) return false
        val state = _uiState.value
        if (state.types.any { it.name.equals(name, ignoreCase = true) }) {
            _uiState.value = state.copy(errorMessage = "Activity type already exists")
            return false
        }
        _uiState.value = state.copy(
            types = state.types + ActivityTypeDraft(
                id = nextLocalId--,
                name = name,
                colorPaletteIndex = 0,
                iconKey = CalendarIcons.DEFAULT_KEY,
            ),
            newTypeName = "",
            expandedTypeId = nextLocalId + 1,
            errorMessage = null,
        )
        return true
    }

    fun deleteType(id: Long) {
        val state = _uiState.value
        if (id > 0) deletedIds.add(id)
        _uiState.value = state.copy(
            types = state.types.filterNot { it.id == id },
            expandedTypeId = if (state.expandedTypeId == id) null else state.expandedTypeId,
            errorMessage = null,
        )
    }

    fun moveType(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val state = _uiState.value
        if (fromIndex !in state.types.indices || toIndex !in state.types.indices) return
        val types = state.types.toMutableList()
        val item = types.removeAt(fromIndex)
        types.add(toIndex, item)
        _uiState.value = state.copy(types = types, errorMessage = null)
    }

    fun setExpandedType(id: Long?) {
        _uiState.value = _uiState.value.copy(expandedTypeId = id)
    }

    fun updateTypeName(id: Long, name: String) {
        _uiState.value = _uiState.value.copy(
            types = _uiState.value.types.map { draft ->
                if (draft.id == id) draft.copy(name = name) else draft
            },
            errorMessage = null,
        )
    }

    fun updateTypeColor(id: Long, colorIndex: Int) {
        _uiState.value = _uiState.value.copy(
            types = _uiState.value.types.map { draft ->
                if (draft.id == id) draft.copy(colorPaletteIndex = colorIndex) else draft
            },
            errorMessage = null,
        )
    }

    fun updateTypeIcon(id: Long, iconKey: String) {
        _uiState.value = _uiState.value.copy(
            types = _uiState.value.types.map { draft ->
                if (draft.id == id) draft.copy(iconKey = iconKey) else draft
            },
            errorMessage = null,
        )
    }

    fun save(onComplete: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            runCatching {
                deletedIds.forEach { calendarRepository.deleteActivityType(it) }
                val orderedIds = mutableListOf<Long>()
                state.types.forEach { draft ->
                    val trimmed = draft.name.trim()
                    require(trimmed.isNotEmpty()) { "Name cannot be blank" }
                    val id = if (draft.id <= 0) {
                        calendarRepository.addActivityType(
                            name = trimmed,
                            colorPaletteIndex = draft.colorPaletteIndex,
                            iconKey = draft.iconKey,
                        )
                    } else {
                        calendarRepository.updateActivityType(
                            CalendarActivityType(
                                id = draft.id,
                                name = trimmed,
                                colorPaletteIndex = draft.colorPaletteIndex,
                                iconKey = draft.iconKey,
                                sortOrder = 0,
                            ),
                        )
                        draft.id
                    }
                    orderedIds.add(id)
                }
                calendarRepository.saveActivityTypeOrder(orderedIds)
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

    private fun CalendarActivityType.toDraft() = ActivityTypeDraft(
        id = id,
        name = name,
        colorPaletteIndex = colorPaletteIndex,
        iconKey = iconKey,
    )
}
