package com.gainsmaxxing.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = SplitDayEntity::class,
            parentColumns = ["dayOfWeek"],
            childColumns = ["dayOfWeek"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("dayOfWeek"),
        Index("exerciseId"),
    ],
)
data class TemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,
    val exerciseId: Long,
    val sortOrder: Int,
    val targetSets: Int,
    val targetReps: Int,
)
