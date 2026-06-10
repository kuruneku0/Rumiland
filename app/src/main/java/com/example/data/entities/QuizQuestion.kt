package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_questions")
data class QuizQuestion(
    @PrimaryKey val id: Int,
    val lessonId: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswerIndex: Int // 0-3
) {
    val optionsList: List<String>
        get() = listOf(optionA, optionB, optionC, optionD)
}
