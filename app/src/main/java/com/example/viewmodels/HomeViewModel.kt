package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.Lesson
import com.example.data.entities.UserProgress
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.preloadIfNeeded()
            repository.triggerDailyActivity()
        }
    }

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

    // Overall completion fraction and percentage
    val completionState: StateFlow<CompletionProgress> = combine(lessons, userProgress) { lessonList, progress ->
        val total = 20
        val completed = lessonList.count { it.isCompleted }
        val percentage = if (total > 0) (completed * 100) / total else 0
        
        // Find next lesson to continue
        val nextToContinue = lessonList.firstOrNull { it.isUnlocked && !it.isCompleted } 
            ?: lessonList.firstOrNull { !it.isCompleted }
            ?: lessonList.lastOrNull() // Default fallback if completed everything

        CompletionProgress(
            completedCount = completed,
            totalCount = total,
            percentage = percentage,
            nextLessonToContinue = nextToContinue
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CompletionProgress(0, 20, 0, null)
    )

    // Daily Tips - 30 items
    private val pythonTips = listOf(
        "هرگز برای مقایسه رشته‌ها در پایتون از is استفاده نکنید؛ چشمانتان را به استفاده از == عادت دهید.",
        "تابع range(start, stop, step) سه آرگومان می‌پذیرد تا با بازه‌های دلخواه گام بردارید.",
        "با استفاده از f-strings کار با متغیرها درون متون را بسیار خواناتر کنید: f'Name: {name}'",
        "دستور print() به طور پیش‌فرض انتهای خط را با newline پر می‌کند. با end='' می‌توانید آن را تغییر دهید.",
        "لیست‌سازها (List Comprehensions) راهی کوتاه برای ساخت لیست جدید هستند: [x * 2 for x in range(5)]",
        "تاپل‌ها (Tuples) از لیست‌ها سریع‌تر هستند و برای مقادیر ثابت عالی می‌باشند.",
        "متد .get() در دیکشنری، در صورت عدم وجود کلید از کرش برنامه جلوگیری می‌کند و مقدار پیش‌فرض برمی‌گرداند.",
        "تابع len() در زمان ثابت (O(1)) طول عناصر مجموعه‌ها را برمی‌گرداند.",
        "برای جابجا کردن دو متغیر نیازی به متغیر سوم ندارید: x, y = y, x",
        "از روش برش (Slicing) برای برعکس کردن یک رشته استفاده کنید: name[::-1]",
        "شرط‌های یک‌خطی (Ternary Operator) کد شما را کوتاه‌تر می‌کنند: x = 10 if condition else 20",
        "دستور with تضمین می‌کند که فایل پس از اتمام کار، حتی در صورت بروز خطا بسته‌ خواهد شد.",
        "برای الحاق تعداد زیادی رشته از یک لیست، متد ''.join(list) از عملگر + بسیار کارآمدتر است.",
        "مجموعه (Set) تکراری‌ها را حذف می‌کند و برای تست عضویت سریع (in) فوق‌العاده است.",
        "می‌توانید در شرط‌ها از زنجیره مقایسه استفاده کنید: 10 < x < 20",
        "تابع enumerate() در حلقه‌ها علاوه بر مقدار، اندیس عنصر را نیز برمی‌گرداند.",
        "تابع zip() به شما اجازه می‌دهد چند لیست را هم‌زمان در یک حلقه پیمایش کنید.",
        "برای مدیریت چندین وضعیت خطا می‌توانید چند لایه except در ساختار try بنویسید.",
        "متد split() یک رشته را بر اساس فاصله یا کاراکتر مدنظر جدا کرده و یک لیست می‌سازد.",
        "در شیء‌گرایی، متد __init__ هنگام ساخت مستقیم یک کپی یا شیء از کلاس اجرا می‌شود.",
        "پایتون کلمه‌ای کلیدی به نام pass دارد که نقش نگهدارنده جایگاه (Placeholder) برای بلوک کدهای خالی بازی می‌کند.",
        "با فراخوانی import sys می‌توانید به اطلاعات سیستم‌عامل در حال اجرا دسترسی داشته باشید.",
        "هر فایل پایتون (*.py) خود یک ماژول است که به راحتی در کدهای دیگر import می‌شود.",
        "در دیکشنری‌ها، متد .keys() لیست کلیدها و .values() لیست مقادیر را می‌دهد.",
        "پایتون یک زبان با تایپ خودکار (Dynamic Tracing) است؛ یعنی خودش نوع متغیر را تخصیص می‌دهد.",
        "تابع standard input() همیشه ورودی کاربر را به عنوان رشته (String) می‌خواند.",
        "با تعریف self در کلاس، به متغیرها و رفتارهای درونی نمونه فعال کاربری اشاره می‌کنید.",
        "برای چک کردن عدم تداخل مقادیر منطقی از دستور not استفاده کنید.",
        "ارث‌بری اجازه می‌دهد کلاس والد به عنوان مرجع رفتاری کلاس فرزند عمل کند.",
        "کتابخانه ریاضی پایتون (import math) به توابع عالی ریاضی مانند جذر و سینوس دسترسی دارد."
    )

    // Gets a stable daily tip based on the date code hash
    val dailyTip: String
        get() {
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val index = Math.abs(dateStr.hashCode()) % pythonTips.size
            return pythonTips[index]
        }

    // Recent activity metrics: last 3 quiz scores
    val recentActivity: StateFlow<List<RecentQuizResult>> = userProgress.combine(lessons) { progress, lessonList ->
        if (progress == null || progress.quizScoresMap.isEmpty()) {
            emptyList()
        } else {
            progress.quizScoresMap.entries
                .sortedByDescending { it.key } // Show newest lessons first
                .take(3)
                .map { entry ->
                    val lessonTitle = lessonList.find { it.id == entry.key }?.title ?: "درس ${entry.key}"
                    RecentQuizResult(
                        lessonId = entry.key,
                        lessonTitle = lessonTitle,
                        score = entry.value
                    )
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

data class CompletionProgress(
    val completedCount: Int,
    val totalCount: Int,
    val percentage: Int,
    val nextLessonToContinue: Lesson?
)

data class RecentQuizResult(
    val lessonId: Int,
    val lessonTitle: String,
    val score: Int
)
