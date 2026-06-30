package com.gainsmaxxing.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gainsmaxxing.data.repository.BodyMetricsRepository
import com.gainsmaxxing.data.repository.StrengthPrRepository
import com.gainsmaxxing.data.repository.UserPreferencesRepository
import com.gainsmaxxing.domain.StrengthPrComparison
import com.gainsmaxxing.domain.WeightFormat
import com.gainsmaxxing.domain.model.BodyweightEntry
import com.gainsmaxxing.domain.model.EnergyTag
import com.gainsmaxxing.domain.model.SleepEntry
import com.gainsmaxxing.domain.model.StrengthPrEntry
import com.gainsmaxxing.domain.model.UserProfile
import com.gainsmaxxing.domain.model.WeightUnit
import com.gainsmaxxing.data.repository.StrengthPrSummary
import com.gainsmaxxing.domain.SleepChartSlots
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class PrCardUi(
    val exerciseName: String,
    val value: String,
    val unit: String,
    val delta: String,
)

data class StrengthPrEntryUi(
    val date: LocalDate,
    val dateLabel: String,
    val oneRmKg: Float,
    val oneRmDisplay: String,
    val unitLabel: String,
    val loggedAtEpochMs: Long,
)

data class HomeUiState(
    val profile: UserProfile = UserPreferencesRepository.DEFAULT_PROFILE,
    val bodyweightEntries: List<BodyweightEntry> = emptyList(),
    val sleepEntries: List<SleepEntry> = emptyList(),
    val strengthPrCards: List<PrCardUi> = emptyList(),
    val detailExerciseName: String? = null,
    val detailEntries: List<StrengthPrEntryUi> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bodyMetricsRepository: BodyMetricsRepository,
    private val strengthPrRepository: StrengthPrRepository,
) : ViewModel() {

    private val detailExercise = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            userPreferencesRepository.observeProfile(),
            bodyMetricsRepository.observeBodyweightWeeks(),
            bodyMetricsRepository.observeSleepChart(),
            strengthPrRepository.observeSummaries(),
            detailExercise,
        ) { profile, bodyweight, sleep, summaries, detailName ->
            Quint(profile, bodyweight, sleep, summaries, detailName)
        },
        detailExercise.flatMapLatest { name ->
            if (name == null) {
                flowOf(emptyList())
            } else {
                strengthPrRepository.observeEntriesForExercise(name)
            }
        },
    ) { quint, detailEntries ->
        val profile = quint.profile
        val unit = profile.weightUnit
        val unitLabel = WeightFormat.unitLabel(unit)
        val cards = quint.summaries.map { summary ->
            val latest = StrengthPrComparison.latestEntry(summary.entries)
            val previous = latest?.let { StrengthPrComparison.previousEntry(summary.entries, it) }
            PrCardUi(
                exerciseName = summary.exerciseName,
                value = latest?.let { WeightFormat.formatWeight(it.oneRmKg, unit) } ?: "—",
                unit = if (latest != null) unitLabel else "",
                delta = latest?.let {
                    StrengthPrComparison.formatDeltaKg(it.oneRmKg, previous?.oneRmKg, unit)
                } ?: "—",
            )
        }
        HomeUiState(
            profile = profile,
            bodyweightEntries = quint.bodyweight,
            sleepEntries = quint.sleep,
            strengthPrCards = cards,
            detailExerciseName = quint.detailName,
            detailEntries = detailEntries.map { it.toUi(unit) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            sleepEntries = SleepChartSlots.build(emptyList()),
        ),
    )

    private val _showStrengthPrSettings = MutableStateFlow(false)
    val showStrengthPrSettings: StateFlow<Boolean> = _showStrengthPrSettings.asStateFlow()

    fun toggleWeightUnit() {
        viewModelScope.launch {
            val next = when (uiState.value.profile.weightUnit) {
                WeightUnit.KG -> WeightUnit.LBS
                WeightUnit.LBS -> WeightUnit.KG
            }
            userPreferencesRepository.setWeightUnit(next)
        }
    }

    fun setProfileName(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.setProfileName(name)
        }
    }

    fun logBodyweight(weightKg: Float, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            bodyMetricsRepository.logBodyweight(date, weightKg)
        }
    }

    fun logSleep(hours: Float, energyTag: EnergyTag, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            bodyMetricsRepository.logSleep(
                SleepEntry(date = date, hours = hours, energyTag = energyTag),
            )
        }
    }

    fun openStrengthPrDetail(exerciseName: String) {
        detailExercise.value = exerciseName
    }

    fun closeStrengthPrDetail() {
        detailExercise.value = null
    }

    fun logStrengthPr(oneRmKg: Float) {
        val name = detailExercise.value ?: return
        viewModelScope.launch {
            strengthPrRepository.logOneRm(name, oneRmKg)
        }
    }

    fun openStrengthPrSettings() {
        _showStrengthPrSettings.value = true
    }

    fun closeStrengthPrSettings() {
        _showStrengthPrSettings.value = false
    }

    private fun StrengthPrEntry.toUi(unit: WeightUnit): StrengthPrEntryUi {
        val date = loggedDate
        return StrengthPrEntryUi(
            date = date,
            dateLabel = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${date.year}",
            oneRmKg = oneRmKg,
            oneRmDisplay = WeightFormat.formatWeight(oneRmKg, unit),
            unitLabel = WeightFormat.unitLabel(unit),
            loggedAtEpochMs = loggedAt.toEpochMilli(),
        )
    }

    private data class Quint(
        val profile: UserProfile,
        val bodyweight: List<BodyweightEntry>,
        val sleep: List<SleepEntry>,
        val summaries: List<StrengthPrSummary>,
        val detailName: String?,
    )
}
