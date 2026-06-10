package com.example.data.database

import android.content.Context
import androidx.room.*
import com.example.data.entities.Bookmark
import com.example.data.entities.Lesson
import com.example.data.entities.QuizQuestion
import com.example.data.entities.UserProgress

@Database(
    entities = [
        Lesson::class,
        QuizQuestion::class,
        UserProgress::class,
        Bookmark::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun quizDao(): QuizDao
    abstract fun progressDao(): ProgressDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rumiland_academy_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
