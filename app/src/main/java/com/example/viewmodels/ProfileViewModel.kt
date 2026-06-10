package com.example.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.Bookmark
import com.example.data.entities.Lesson
import com.example.data.entities.UserProgress
import com.example.data.repository.AppRepository
import com.example.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AppRepository) : ViewModel() {

    val lessons: StateFlow<List<Lesson>> = repository.allLessons
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userProgress: StateFlow<UserProgress?> = repository.userProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val bookmarks: StateFlow<List<Bookmark>> = repository.allBookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Merge lessons and progress to produce detailed profile stats
    val profileStats: StateFlow<ProfileStats> = combine(lessons, userProgress) { lessonList, progress ->
        val completedCount = lessonList.count { it.isCompleted }
        
        val avgScore = if (progress != null && progress.quizScoresMap.isNotEmpty()) {
            val totalScores = progress.quizScoresMap.values.sum()
            // Each quiz has 5 questions, score is 0 to 5. We calculate percent (score * 20%)
            val avgOutOf5 = totalScores.toFloat() / progress.quizScoresMap.size
            "%.1f از 5 (%.0f٪)".format(avgOutOf5, avgOutOf5 * 20f)
        } else {
            "0.0 از 5 (0٪)"
        }

        val streak = progress?.currentStreak ?: 0

        // Calculate Achievements
        val completedIds = progress?.completedLessonsList ?: emptyList()
        val firstStepAchieved = completedIds.isNotEmpty()
        val loopsAchieved = completedIds.contains(7) || completedIds.contains(8)
        val oopAchieved = completedIds.contains(19) || completedIds.contains(20)
        val ninjaAchieved = completedCount >= 20

        ProfileStats(
            completedLessons = completedCount,
            averageScoreStr = avgScore,
            streakDays = streak,
            badgeFirstStep = firstStepAchieved,
            badgeLoops = loopsAchieved,
            badgeOop = oopAchieved,
            badgeNinja = ninjaAchieved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileStats(0, "0.0", 0, false, false, false, false)
    )

    // Expose bookmarked Python lessons
    val bookmarkedLessons: StateFlow<List<Lesson>> = combine(lessons, bookmarks) { lessonList, bookmarkList ->
        val bookmarkedIds = bookmarkList.filter { it.isBookmarked }.map { it.lessonId }
        lessonList.filter { bookmarkedIds.contains(it.id) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Helper to trigger progress reset (for testing/replay purposes)
    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetProgress()
        }
    }
}

data class ProfileStats(
    val completedLessons: Int,
    val averageScoreStr: String,
    val streakDays: Int,
    val badgeFirstStep: Boolean,
    val badgeLoops: Boolean,
    val badgeOop: Boolean,
    val badgeNinja: Boolean
)
