package com.gainsmaxxing.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val profileName: String,
    val weightUnit: String,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
