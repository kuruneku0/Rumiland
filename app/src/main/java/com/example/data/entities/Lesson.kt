package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: Int,
    val title: String,
    val contentMarkdown: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val orderIndex: Int
)
