package com.gainsmaxxing.data.repository

import com.gainsmaxxing.data.db.dao.UserPreferencesDao
import com.gainsmaxxing.data.db.entities.UserPreferencesEntity
import com.gainsmaxxing.data.mapper.toDomain
import com.gainsmaxxing.data.mapper.toEntity
import com.gainsmaxxing.domain.model.UserProfile
import com.gainsmaxxing.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao,
) {
    fun observeProfile(): Flow<UserProfile> =
        userPreferencesDao.observe().map { entity ->
            entity?.toDomain() ?: DEFAULT_PROFILE
        }

    suspend fun setProfileName(name: String) {
        val current = userPreferencesDao.get()?.toDomain() ?: DEFAULT_PROFILE
        userPreferencesDao.upsert(current.copy(name = name.trim()).toEntity())
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        val current = userPreferencesDao.get()?.toDomain() ?: DEFAULT_PROFILE
        userPreferencesDao.upsert(current.copy(weightUnit = unit).toEntity())
    }

    suspend fun ensureDefaults() {
        if (userPreferencesDao.get() == null) {
            userPreferencesDao.upsert(DEFAULT_PROFILE.toEntity())
        }
    }

    companion object {
        val DEFAULT_PROFILE = UserProfile(
            name = "Athlete",
            weightUnit = WeightUnit.KG,
        )
    }
}
