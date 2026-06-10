package com.example.data.database

import androidx.room.*
import com.example.data.entities.QuizQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_questions WHERE lessonId = :lessonId ORDER BY id ASC")
    fun getQuestionsForLesson(lessonId: Int): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions WHERE lessonId = :lessonId ORDER BY id ASC")
    suspend fun getQuestionsForLessonDirect(lessonId: Int): List<QuizQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestion>)

    @Query("SELECT COUNT(*) FROM quiz_questions")
    suspend fun getQuestionCount(): Int
}
