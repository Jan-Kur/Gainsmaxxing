package com.gainsmaxxing.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gainsmaxxing.data.repository.UserPreferencesRepository
import com.gainsmaxxing.data.repository.WorkoutRepository
import com.gainsmaxxing.domain.SetComparison
import com.gainsmaxxing.domain.WeekMath
import com.gainsmaxxing.domain.WeightFormat
import com.gainsmaxxing.domain.model.LoggedSet
import com.gainsmaxxing.domain.model.SplitDay
import com.gainsmaxxing.domain.model.TemplateExercise
import com.gainsmaxxing.domain.model.UserProfile
import com.gainsmaxxing.domain.model.WeightUnit
import com.gainsmaxxing.domain.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class SetUi(
    val weightKg: Float,
    val displayLabel: String,
    val reps: Int,
    val isWarmup: Boolean,
    val isPr: Boolean,
)

data class ExerciseUi(
    val id: Long,
    val name: String,
    val targetSets: Int,
    val targetReps: Int,
    val refSets: Int,
    val refReps: Int,
    val refWeightKg: Float,
    val isBodyweight: Boolean,
    val detailsLine: String,
    val loggedSets: List<SetUi>,
)

data class SplitDayUi(
    val dayOfWeek: Int,
    val workoutName: String,
    val isRestDay: Boolean,
    val exercises: List<ExerciseUi>,
    val typeLabel: String,
)

data class HistorySessionUi(
    val date: LocalDate,
    val dateLabel: String,
    val sets: List<SetUi>,
    val topWeightKg: Float?,
)

data class WorkoutUiState(
    val selectedDay: Int = WeekMath.todayDayIndex(),
    val splitDays: List<SplitDayUi> = emptyList(),
    val weightUnit: WeightUnit = WeightUnit.KG,
    val activeSession: WorkoutSession? = null,
    val showActiveWorkout: Boolean = false,
    val historyExerciseId: Long? = null,
    val historyExerciseName: String = "",
    val historySessions: List<HistorySessionUi> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val elapsedMillis: Long = 0L,
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val selectedDay = MutableStateFlow(WeekMath.todayDayIndex())
    private val showActiveWorkout = MutableStateFlow(false)
    private val historyExerciseId = MutableStateFlow<Long?>(null)
    private val historySessions = MutableStateFlow<List<HistorySessionUi>>(emptyList())
    private val isLoadingHistory = MutableStateFlow(false)
    private val historicalBestByExercise = MutableStateFlow<Map<Long, Float>>(emptyMap())
    private val elapsedMillis = MutableStateFlow(0L)

    private val split = MutableStateFlow<List<SplitDay>>(emptyList())
    private val profile = MutableStateFlow(UserProfile(name = "Athlete", weightUnit = WeightUnit.KG))
    private val activeSession = MutableStateFlow<WorkoutSession?>(null)

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private fun refreshUiState() {
        _uiState.value = buildUiState(
            selectedDay = selectedDay.value,
            split = split.value,
            profile = profile.value,
            activeSession = activeSession.value,
            showActiveWorkout = showActiveWorkout.value,
            historyExerciseId = historyExerciseId.value,
            historySessions = historySessions.value,
            isLoadingHistory = isLoadingHistory.value,
            historicalBestByExercise = historicalBestByExercise.value,
            elapsedMillis = elapsedMillis.value,
        )
    }

    init {
        viewModelScope.launch {
            workoutRepository.observeSplit().collect {
                split.value = it
                refreshUiState()
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.observeProfile().collect {
                profile.value = it
                refreshUiState()
            }
        }
        viewModelScope.launch {
            workoutRepository.observeInProgressSession().collect { session ->
                activeSession.value = session
                if (session != null) {
                    prefetchHistoricalBests(session)
                }
                refreshUiState()
            }
        }
        viewModelScope.launch {
            while (true) {
                val session = activeSession.value
                elapsedMillis.value = if (session != null) {
                    System.currentTimeMillis() - session.startedAt.toEpochMilli()
                } else {
                    0L
                }
                refreshUiState()
                kotlinx.coroutines.delay(1_000)
            }
        }
    }

    fun selectDay(day: Int) {
        selectedDay.value = day
        refreshUiState()
    }

    fun startWorkout() {
        val day = _uiState.value.splitDays.getOrNull(_uiState.value.selectedDay) ?: return
        if (day.isRestDay || activeSession.value != null) return

        viewModelScope.launch {
            workoutRepository.startWorkout(day.dayOfWeek, day.workoutName)
            showActiveWorkout.value = true
            refreshUiState()
        }
    }

    fun resumeWorkout() {
        if (activeSession.value == null) return
        showActiveWorkout.value = true
        refreshUiState()
    }

    fun minimizeActiveWorkout() {
        showActiveWorkout.value = false
        refreshUiState()
    }

    fun finishWorkout() {
        val sessionId = activeSession.value?.id ?: return
        viewModelScope.launch {
            workoutRepository.finishWorkout(sessionId)
            showActiveWorkout.value = false
        }
    }

    fun discardWorkout() {
        val sessionId = activeSession.value?.id ?: return
        viewModelScope.launch {
            workoutRepository.discardWorkout(sessionId)
            showActiveWorkout.value = false
        }
    }

    fun logSet(
        exerciseId: Long,
        weightKg: Float,
        reps: Int,
        isWarmup: Boolean,
    ) {
        val sessionId = activeSession.value?.id ?: return
        viewModelScope.launch {
            workoutRepository.logSet(
                sessionId = sessionId,
                exerciseId = exerciseId,
                weightKg = weightKg,
                reps = reps,
                isWarmup = isWarmup,
            )
        }
    }

    fun openHistory(exerciseId: Long) {
        historyExerciseId.value = exerciseId
        isLoadingHistory.value = true
        refreshUiState()
        viewModelScope.launch {
            val unit = profile.value.weightUnit
            val sessions = workoutRepository.getExerciseHistory(exerciseId)
            historySessions.value = sessions.map { session ->
                HistorySessionUi(
                    date = session.date,
                    dateLabel = formatShortDate(session.date),
                    sets = session.sets.map { it.toSetUi(weightUnit = unit, isPr = false) },
                    topWeightKg = SetComparison.topWorkingWeightKg(session.sets),
                )
            }
            isLoadingHistory.value = false
            refreshUiState()
        }
    }

    fun closeHistory() {
        historyExerciseId.value = null
        historySessions.value = emptyList()
        refreshUiState()
    }

private suspend fun prefetchHistoricalBests(session: WorkoutSession) {
    val existing = historicalBestByExercise.value
    val exerciseIds = session.sets.map { it.exerciseId }.distinct()
    val missing = exerciseIds.filterNot { existing.containsKey(it) }
    if (missing.isEmpty()) return

    val fetched = missing.associateWith { exerciseId ->
        workoutRepository.getHistoricalBestWeightKg(
            exerciseId = exerciseId,
            excludeSessionId = session.id,
        )
    }
    historicalBestByExercise.value = existing + fetched
}

    private fun buildUiState(
        selectedDay: Int,
        split: List<SplitDay>,
        profile: UserProfile,
        activeSession: WorkoutSession?,
        showActiveWorkout: Boolean,
        historyExerciseId: Long?,
        historySessions: List<HistorySessionUi>,
        isLoadingHistory: Boolean,
        historicalBestByExercise: Map<Long, Float>,
        elapsedMillis: Long,
    ): WorkoutUiState {
        val splitDays = if (split.isEmpty()) {
            (0..6).map { dayOfWeek ->
                SplitDayUi(
                    dayOfWeek = dayOfWeek,
                    workoutName = "Rest",
                    isRestDay = true,
                    exercises = emptyList(),
                    typeLabel = "",
                )
            }
        } else {
            split.map { splitDay ->
                splitDay.toUi(
                    weightUnit = profile.weightUnit,
                    activeSession = activeSession,
                    historicalBestByExercise = historicalBestByExercise,
                )
            }
        }
        return WorkoutUiState(
            selectedDay = selectedDay,
            splitDays = splitDays,
            weightUnit = profile.weightUnit,
            activeSession = activeSession,
            showActiveWorkout = showActiveWorkout,
            historyExerciseId = historyExerciseId,
            historyExerciseName = split
                .flatMap { it.exercises }
                .firstOrNull { it.exercise.id == historyExerciseId }
                ?.exercise
                ?.name
                .orEmpty(),
            historySessions = historySessions,
            isLoadingHistory = isLoadingHistory,
            elapsedMillis = elapsedMillis,
        )
    }

    private fun SplitDay.toUi(
        weightUnit: WeightUnit,
        activeSession: WorkoutSession?,
        historicalBestByExercise: Map<Long, Float>,
    ): SplitDayUi {
        val workoutLabel = workoutName ?: "Rest"
        val typeLabel = if (isRestDay) {
            ""
        } else {
            (workoutName?.split(" ")?.firstOrNull() ?: workoutName).orEmpty().uppercase()
        }
        return SplitDayUi(
            dayOfWeek = dayOfWeek,
            workoutName = workoutLabel,
            isRestDay = isRestDay,
            typeLabel = typeLabel,
            exercises = exercises.map { template ->
                val logged = activeSession?.setsForExercise(template.exercise.id).orEmpty()
                val historicalBest = historicalBestByExercise[template.exercise.id] ?: 0f
                template.toUi(
                    weightUnit = weightUnit,
                    loggedSets = logged,
                    historicalBestKg = historicalBest,
                )
            },
        )
    }

    private fun TemplateExercise.toUi(
        weightUnit: WeightUnit,
        loggedSets: List<LoggedSet>,
        historicalBestKg: Float,
    ): ExerciseUi {
        val last = lastReference
        val refSets = last?.workingSetCount ?: targetSets
        val refReps = last?.topSetReps ?: targetReps
        val refKg = last?.topSetWeightKg ?: 0f
        val prFlags = SetComparison.prFlagsForOrderedSets(loggedSets, historicalBestKg)
        return ExerciseUi(
            id = exercise.id,
            name = exercise.name,
            targetSets = targetSets,
            targetReps = targetReps,
            refSets = refSets,
            refReps = refReps,
            refWeightKg = refKg,
            isBodyweight = exercise.isBodyweight,
            detailsLine = exerciseDetailsLine(
                sets = refSets,
                reps = refReps,
                refWeightKg = refKg,
                isBodyweight = exercise.isBodyweight,
                weightUnit = weightUnit,
                hasHistory = last != null,
            ),
            loggedSets = loggedSets.mapIndexed { index, set ->
                set.toSetUi(weightUnit = weightUnit, isPr = prFlags.getOrElse(index) { false })
            },
        )
    }

    private fun LoggedSet.toSetUi(weightUnit: WeightUnit, isPr: Boolean): SetUi {
        val weightLabel = WeightFormat.formatWeight(weightKg, weightUnit)
        return SetUi(
            weightKg = weightKg,
            displayLabel = "$weightLabel × $reps",
            reps = reps,
            isWarmup = isWarmup,
            isPr = isPr,
        )
    }

    companion object {
        fun formatElapsed(millis: Long): String {
            val totalSeconds = (millis / 1_000).coerceAtLeast(0)
            val hours = totalSeconds / 3_600
            val minutes = (totalSeconds % 3_600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%02d:%02d".format(minutes, seconds)
            }
        }

        private fun exerciseDetailsLine(
            sets: Int,
            reps: Int,
            refWeightKg: Float,
            isBodyweight: Boolean,
            weightUnit: WeightUnit,
            hasHistory: Boolean,
        ): String {
            val weightPart = when {
                !hasHistory -> if (isBodyweight) "BW" else "—"
                isBodyweight ->
                    "+${WeightFormat.formatWeight(refWeightKg, weightUnit)} ${WeightFormat.unitLabel(weightUnit)} BW"
                else ->
                    "${WeightFormat.formatWeight(refWeightKg, weightUnit)} ${WeightFormat.unitLabel(weightUnit)}"
            }
            return "$sets sets · $reps reps · $weightPart"
        }

        private fun formatShortDate(date: LocalDate): String =
            "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)}"
    }
}
