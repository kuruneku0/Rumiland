package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val userId: Int = 1,
    val completedLessonsList: List<Int>, // converted via TypeConverters
    val quizScoresMap: Map<Int, Int>,   // converted via TypeConverters
    val currentStreak: Int,
    val lastActiveDate: String // YYYY-MM-DD
)
