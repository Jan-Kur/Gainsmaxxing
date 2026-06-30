package com.gainsmaxxing.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gainsmaxxing.data.repository.WorkoutRepository
import com.gainsmaxxing.domain.WeekMath
import com.gainsmaxxing.domain.model.Exercise
import com.gainsmaxxing.domain.model.SplitDay
import com.gainsmaxxing.domain.model.TemplateExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplitEditorExerciseDraft(
    val localId: Long,
    val name: String,
    val targetSets: Int,
    val targetReps: Int,
    val isBodyweight: Boolean,
    val persistedExerciseId: Long? = null,
)

data class SplitEditorDayDraft(
    val dayOfWeek: Int,
    val workoutName: String,
    val isRestDay: Boolean,
    val exercises: List<SplitEditorExerciseDraft>,
)

data class SplitEditorUiState(
    val days: List<SplitEditorDayDraft> = emptyList(),
    val selectedDay: Int = WeekMath.todayDayIndex(),
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
)

@HiltViewModel
class SplitEditorViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitEditorUiState())
    val uiState: StateFlow<SplitEditorUiState> = _uiState.asStateFlow()

    private var nextLocalId = 1L

    init {
        viewModelScope.launch {
            workoutRepository.observeSplit().collect { split ->
                _uiState.value = _uiState.value.copy(
                    days = split.map { day ->
                        SplitEditorDayDraft(
                            dayOfWeek = day.dayOfWeek,
                            workoutName = day.workoutName.orEmpty(),
                            isRestDay = day.isRestDay,
                            exercises = day.exercises.map { template ->
                                SplitEditorExerciseDraft(
                                    localId = nextLocalId++,
                                    name = template.exercise.name,
                                    targetSets = template.targetSets,
                                    targetReps = template.targetReps,
                                    isBodyweight = template.exercise.isBodyweight,
                                    persistedExerciseId = template.exercise.id,
                                )
                            },
                        )
                    }.ifEmpty { defaultEmptyDays() },
                )
            }
        }
    }

    fun selectDay(day: Int) {
        _uiState.value = _uiState.value.copy(selectedDay = day)
    }

    fun setRestDay(isRestDay: Boolean) {
        updateSelectedDay { day ->
            day.copy(
                isRestDay = isRestDay,
                workoutName = if (isRestDay) "" else day.workoutName.ifBlank { "Workout" },
                exercises = if (isRestDay) emptyList() else day.exercises,
            )
        }
    }

    fun setWorkoutName(name: String) {
        updateSelectedDay { it.copy(workoutName = name, isRestDay = false) }
    }

    fun addExercise() {
        updateSelectedDay { day ->
            day.copy(
                isRestDay = false,
                workoutName = day.workoutName.ifBlank { "Workout" },
                exercises = day.exercises + SplitEditorExerciseDraft(
                    localId = nextLocalId++,
                    name = "",
                    targetSets = 3,
                    targetReps = 10,
                    isBodyweight = false,
                ),
            )
        }
    }

    fun updateExercise(localId: Long, transform: (SplitEditorExerciseDraft) -> SplitEditorExerciseDraft) {
        updateSelectedDay { day ->
            day.copy(
                exercises = day.exercises.map { exercise ->
                    if (exercise.localId == localId) transform(exercise) else exercise
                },
            )
        }
    }

    fun removeExercise(localId: Long) {
        updateSelectedDay { day ->
            day.copy(exercises = day.exercises.filterNot { it.localId == localId })
        }
    }

    fun moveExercise(localId: Long, direction: Int) {
        updateSelectedDay { day ->
            val index = day.exercises.indexOfFirst { it.localId == localId }
            if (index < 0) return@updateSelectedDay day
            val target = index + direction
            if (target !in day.exercises.indices) return@updateSelectedDay day
            val mutable = day.exercises.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(target, item)
            day.copy(exercises = mutable)
        }
    }

    fun saveSelectedDay() {
        val dayDraft = _uiState.value.days.getOrNull(_uiState.value.selectedDay) ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveMessage = null)
            runCatching {
                if (dayDraft.isRestDay || dayDraft.exercises.isEmpty()) {
                    workoutRepository.saveSplitDay(
                        SplitDay(
                            dayOfWeek = dayDraft.dayOfWeek,
                            workoutName = null,
                            exercises = emptyList(),
                        ),
                    )
                } else {
                    val exercises = dayDraft.exercises.map { draft ->
                        require(draft.name.isNotBlank()) { "Exercise name is required" }
                        val exercise = workoutRepository.getOrCreateExercise(
                            name = draft.name,
                            isBodyweight = draft.isBodyweight,
                        )
                        TemplateExercise(
                            templateId = 0,
                            exercise = exercise,
                            sortOrder = 0,
                            targetSets = draft.targetSets,
                            targetReps = draft.targetReps,
                            lastReference = null,
                        )
                    }
                    workoutRepository.saveSplitDay(
                        SplitDay(
                            dayOfWeek = dayDraft.dayOfWeek,
                            workoutName = dayDraft.workoutName.trim(),
                            exercises = exercises,
                        ),
                    )
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, saveMessage = "Saved")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveMessage = error.message ?: "Could not save",
                )
            }
        }
    }

    private fun updateSelectedDay(transform: (SplitEditorDayDraft) -> SplitEditorDayDraft) {
        val selected = _uiState.value.selectedDay
        _uiState.value = _uiState.value.copy(
            days = _uiState.value.days.map { day ->
                if (day.dayOfWeek == selected) transform(day) else day
            },
        )
    }

    private fun defaultEmptyDays(): List<SplitEditorDayDraft> =
        (0..6).map { dayOfWeek ->
            SplitEditorDayDraft(
                dayOfWeek = dayOfWeek,
                workoutName = "",
                isRestDay = true,
                exercises = emptyList(),
            )
        }
}
