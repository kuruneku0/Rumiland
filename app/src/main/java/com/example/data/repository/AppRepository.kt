package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.entities.Bookmark
import com.example.data.entities.Lesson
import com.example.data.entities.QuizQuestion
import com.example.data.entities.UserProgress
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val lessonDao = db.lessonDao()
    private val quizDao = db.quizDao()
    private val progressDao = db.progressDao()
    private val bookmarkDao = db.bookmarkDao()

    val allLessons: Flow<List<Lesson>> = lessonDao.getAllLessons()
    val userProgress: Flow<UserProgress?> = progressDao.getUserProgress()
    val allBookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    // Preload database on first run
    suspend fun preloadIfNeeded() = withContext(Dispatchers.IO) {
        try {
            // 1. Preload Lessons
            val lessonCount = lessonDao.getLessonCount()
            if (lessonCount == 0) {
                val lessonsJson = readAssetJson("lessons.json")
                if (lessonsJson != null) {
                    val listType = Types.newParameterizedType(List::class.java, Lesson::class.java)
                    val adapter = moshi.adapter<List<Lesson>>(listType)
                    val lessons = adapter.fromJson(lessonsJson)
                    if (lessons != null) {
                        lessonDao.insertLessons(lessons)
                        Log.d("AppRepository", "Successfully preloaded ${lessons.size} lessons.")
                    }
                }
            }

            // 2. Preload Quizzes
            val quizCount = quizDao.getQuestionCount()
            if (quizCount == 0) {
                val quizzesJson = readAssetJson("quizzes.json")
                if (quizzesJson != null) {
                    val listType = Types.newParameterizedType(List::class.java, QuizQuestion::class.java)
                    val adapter = moshi.adapter<List<QuizQuestion>>(listType)
                    val questions = adapter.fromJson(quizzesJson)
                    if (questions != null) {
                        quizDao.insertQuestions(questions)
                        Log.d("AppRepository", "Successfully preloaded ${questions.size} quiz questions.")
                    }
                }
            }

            // 3. Initialize default UserProgress if empty
            val currentProgress = progressDao.getUserProgressDirect()
            if (currentProgress == null) {
                val initialProgress = UserProgress(
                    userId = 1,
                    completedLessonsList = emptyList(),
                    quizScoresMap = emptyMap(),
                    currentStreak = 0,
                    lastActiveDate = ""
                )
                progressDao.insertOrUpdateProgress(initialProgress)
                Log.d("AppRepository", "Successfully initialized default user progress.")
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Error preloading assets: ${e.message}", e)
        }
    }

    private fun readAssetJson(fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            reader.close()
            stringBuilder.toString()
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to read asset $fileName: ${e.message}", e)
            null
        }
    }

    fun getLessonById(id: Int): Flow<Lesson?> = lessonDao.getLessonById(id)

    suspend fun getLessonByIdDirect(id: Int): Lesson? = lessonDao.getLessonByIdDirect(id)

    fun getQuestionsForLesson(lessonId: Int): Flow<List<QuizQuestion>> =
        quizDao.getQuestionsForLesson(lessonId)

    suspend fun getQuestionsForLessonDirect(lessonId: Int): List<QuizQuestion> =
        quizDao.getQuestionsForLessonDirect(lessonId)

    fun isBookmarked(lessonId: Int): Flow<Boolean?> = bookmarkDao.isBookmarked(lessonId)

    suspend fun toggleBookmark(lessonId: Int) = withContext(Dispatchers.IO) {
        val current = bookmarkDao.isBookmarkedDirect(lessonId) ?: false
        bookmarkDao.insertBookmark(Bookmark(lessonId = lessonId, isBookmarked = !current))
    }

    // Records quiz results and processes smart feature unlocks & streaks
    suspend fun recordQuizScore(lessonId: Int, score: Int) = withContext(Dispatchers.IO) {
        val progress = progressDao.getUserProgressDirect() ?: UserProgress(
            userId = 1,
            completedLessonsList = emptyList(),
            quizScoresMap = emptyMap(),
            currentStreak = 0,
            lastActiveDate = ""
        )

        val updatedScoresMap = progress.quizScoresMap.toMutableMap()
        val previousHighScore = updatedScoresMap[lessonId] ?: 0
        if (score > previousHighScore) {
            updatedScoresMap[lessonId] = score
        }

        val updatedCompletedList = progress.completedLessonsList.toMutableList()
        val passed = score >= 4 // 4 out of 5 equals 80% (passes the 70% threshold)
        
        if (passed) {
            if (!updatedCompletedList.contains(lessonId)) {
                updatedCompletedList.add(lessonId)
            }
            
            // Mark current lesson as completed in lessons table
            val lesson = lessonDao.getLessonByIdDirect(lessonId)
            if (lesson != null) {
                lessonDao.updateLesson(lesson.copy(isCompleted = true))
            }

            // Unlock next lesson
            val nextLessonId = lessonId + 1
            if (nextLessonId <= 20) {
                val nextLesson = lessonDao.getLessonByIdDirect(nextLessonId)
                if (nextLesson != null && !nextLesson.isUnlocked) {
                    lessonDao.updateLesson(nextLesson.copy(isUnlocked = true))
                }
            }
        }

        // Apply daily streak computations
        val (newStreak, todayStr) = calculateStreak(progress.currentStreak, progress.lastActiveDate)

        val updatedProgress = progress.copy(
            completedLessonsList = updatedCompletedList,
            quizScoresMap = updatedScoresMap,
            currentStreak = newStreak,
            lastActiveDate = todayStr
        )

        progressDao.insertOrUpdateProgress(updatedProgress)
    }

    // Increments streak on lesson study or home view
    suspend fun triggerDailyActivity() = withContext(Dispatchers.IO) {
        val progress = progressDao.getUserProgressDirect() ?: return@withContext
        val (newStreak, todayStr) = calculateStreak(progress.currentStreak, progress.lastActiveDate)
        if (newStreak != progress.currentStreak || todayStr != progress.lastActiveDate) {
            progressDao.insertOrUpdateProgress(
                progress.copy(
                    currentStreak = newStreak,
                    lastActiveDate = todayStr
                )
            )
        }
    }

    private fun calculateStreak(currentStreak: Int, lastActiveStr: String): Pair<Int, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date())

        if (lastActiveStr == todayStr) {
            // Already active today, streak stays same
            return Pair(currentStreak, todayStr)
        }

        if (lastActiveStr.isEmpty()) {
            return Pair(1, todayStr)
        }

        return try {
            val lastActiveDate = dateFormat.parse(lastActiveStr)
            val todayDate = dateFormat.parse(todayStr)

            if (lastActiveDate != null && todayDate != null) {
                val calLast = Calendar.getInstance().apply { time = lastActiveDate }
                val calToday = Calendar.getInstance().apply { time = todayDate }

                // Check if lastActive was yesterday
                val calYesterday = Calendar.getInstance().apply {
                    time = todayDate
                    add(Calendar.DAY_OF_YEAR, -1)
                }

                val isYesterday = calLast.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
                        calLast.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)

                if (isYesterday) {
                    Pair(currentStreak + 1, todayStr)
                } else {
                    // Missed a day (last login before yesterday), reset streak to 1
                    Pair(1, todayStr)
                }
            } else {
                Pair(1, todayStr)
            }
        } catch (e: Exception) {
            Pair(1, todayStr)
        }
    }

    // Helper to completely reset user progress for testing/replayability
    suspend fun resetProgress() = withContext(Dispatchers.IO) {
        val initialProgress = UserProgress(
            userId = 1,
            completedLessonsList = emptyList(),
            quizScoresMap = emptyMap(),
            currentStreak = 0,
            lastActiveDate = ""
        )
        progressDao.insertOrUpdateProgress(initialProgress)

        // Lock all lessons except the first
        val lessons = lessonDao.getAllLessons().firstOrNull() ?: emptyList()
        lessons.forEach { lesson ->
            val shouldUnlock = lesson.id == 1
            lessonDao.updateLesson(lesson.copy(isUnlocked = shouldUnlock, isCompleted = false))
        }
    }
}
