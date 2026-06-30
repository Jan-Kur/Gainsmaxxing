package com.gainsmaxxing.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gainsmaxxing.data.db.entities.ActivityTypeEntity
import com.gainsmaxxing.data.db.entities.CalendarSkipEntity
import com.gainsmaxxing.data.db.entities.CalendarTemplateSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM activity_types ORDER BY sortOrder ASC")
    fun observeActivityTypes(): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM activity_types ORDER BY sortOrder ASC")
    suspend fun getActivityTypes(): List<ActivityTypeEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM activity_types")
    suspend fun maxActivityTypeSortOrder(): Int

    @Insert
    suspend fun insertActivityType(entity: ActivityTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActivityType(entity: ActivityTypeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActivityTypes(entities: List<ActivityTypeEntity>)

    @Query("DELETE FROM activity_types WHERE id = :id")
    suspend fun deleteActivityType(id: Long)

    @Query("UPDATE calendar_template_slots SET activityTypeId = NULL WHERE activityTypeId = :typeId")
    suspend fun clearTemplateSlotsForType(typeId: Long)

    @Transaction
    suspend fun deleteActivityTypeAndClearSlots(id: Long) {
        clearTemplateSlotsForType(id)
        deleteActivityType(id)
    }

    @Query("SELECT * FROM calendar_template_slots")
    fun observeTemplateSlots(): Flow<List<CalendarTemplateSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplateSlot(slot: CalendarTemplateSlotEntity)

    @Query(
        "SELECT * FROM calendar_skip_overrides WHERE date >= :startDate AND date <= :endDate",
    )
    fun observeSkipsInRange(startDate: String, endDate: String): Flow<List<CalendarSkipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkip(entity: CalendarSkipEntity)

    @Query("DELETE FROM calendar_skip_overrides WHERE date = :date AND slot = :slot")
    suspend fun deleteSkip(date: String, slot: String)

    @Query(
        "SELECT COUNT(*) > 0 FROM calendar_skip_overrides WHERE date = :date AND slot = :slot",
    )
    suspend fun isSkipped(date: String, slot: String): Boolean
}
