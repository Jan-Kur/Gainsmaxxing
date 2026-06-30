package com.gainsmaxxing.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gainsmaxxing.data.repository.UserPreferencesRepository
import com.gainsmaxxing.domain.model.UserProfile
import com.gainsmaxxing.domain.model.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val profile: StateFlow<UserProfile> = userPreferencesRepository.observeProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferencesRepository.DEFAULT_PROFILE,
        )

    fun toggleWeightUnit() {
        viewModelScope.launch {
            val next = when (profile.value.weightUnit) {
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
}
