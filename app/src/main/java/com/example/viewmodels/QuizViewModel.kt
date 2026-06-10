package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.QuizQuestion
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: AppRepository) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions: StateFlow<List<QuizQuestion>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // Map of questionIndex -> optionIndex (0-3)
    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Int, Int>> = _selectedAnswers.asStateFlow()

    private val _quizState = MutableStateFlow<QuizState>(QuizState.InProgress)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _shakeTrigger = MutableStateFlow(false)
    val shakeTrigger: StateFlow<Boolean> = _shakeTrigger.asStateFlow()

    var activeLessonId: Int = 1
        private set

    fun loadQuiz(lessonId: Int) {
        activeLessonId = lessonId
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _quizState.value = QuizState.InProgress
        _shakeTrigger.value = false
        
        viewModelScope.launch {
            val fetched = repository.getQuestionsForLessonDirect(lessonId)
            _questions.value = fetched
        }
    }

    fun selectOption(questionIndex: Int, optionIndex: Int) {
        if (_quizState.value != QuizState.InProgress) return
        val currentAnswers = _selectedAnswers.value.toMutableMap()
        currentAnswers[questionIndex] = optionIndex
        _selectedAnswers.value = currentAnswers
    }

    fun nextQuestion() {
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < _questions.value.size) {
            _currentQuestionIndex.value = nextIndex
        }
    }

    fun previousQuestion() {
        val prevIndex = _currentQuestionIndex.value - 1
        if (prevIndex >= 0) {
            _currentQuestionIndex.value = prevIndex
        }
    }

    fun submitQuiz() {
        val currentQuestions = _questions.value
        val answers = _selectedAnswers.value
        
        if (currentQuestions.isEmpty()) return

        var correctCount = 0
        val wrongQuestionIndices = mutableSetOf<Int>()

        for (i in currentQuestions.indices) {
            val correctAnswer = currentQuestions[i].correctAnswerIndex
            val userAnswer = answers[i]
            if (userAnswer == correctAnswer) {
                correctCount++
            } else {
                wrongQuestionIndices.add(i)
            }
        }

        val passed = correctCount >= 4 // 4 out of 5 equals 80% (passes the 70% threshold)

        viewModelScope.launch {
            repository.recordQuizScore(activeLessonId, correctCount)
            
            if (passed) {
                _quizState.value = QuizState.Passed(score = correctCount, total = currentQuestions.size)
            } else {
                _quizState.value = QuizState.Failed(score = correctCount, total = currentQuestions.size)
                // Trigger visual shake effects
                _shakeTrigger.value = true
            }
        }
    }

    fun resetShakeTrigger() {
        _shakeTrigger.value = false
    }

    fun resetQuiz() {
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _quizState.value = QuizState.InProgress
        _shakeTrigger.value = false
    }
}

sealed class QuizState {
    object InProgress : QuizState()
    data class Passed(val score: Int, val total: Int) : QuizState()
    data class Failed(val score: Int, val total: Int) : QuizState()
}
