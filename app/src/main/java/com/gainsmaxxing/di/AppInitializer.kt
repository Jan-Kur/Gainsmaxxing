package com.gainsmaxxing.di

import com.gainsmaxxing.data.repository.UserPreferencesRepository
import com.gainsmaxxing.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
) {
    fun initialize() {
        applicationScope.launch {
            userPreferencesRepository.ensureDefaults()
        }
    }
}
