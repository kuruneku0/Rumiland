package com.example.data.database

import androidx.room.*
import com.example.data.entities.Lesson
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY orderIndex ASC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    fun getLessonById(id: Int): Flow<Lesson?>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonByIdDirect(id: Int): Lesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Update
    suspend fun updateLesson(lesson: Lesson)

    @Query("UPDATE lessons SET isUnlocked = 1 WHERE id = :id")
    suspend fun unlockLesson(id: Int)

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun getLessonCount(): Int
}
