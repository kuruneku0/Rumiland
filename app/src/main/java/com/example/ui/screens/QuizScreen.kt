package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.QuizOptionCard
import com.example.ui.components.RumilandButton
import com.example.ui.components.RumilandCard
import com.example.ui.components.RumilandProgressBar
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.LightGreenProgress
import com.example.ui.theme.SoftMintText
import com.example.ui.theme.WarningOrange
import com.example.viewmodels.QuizState
import com.example.viewmodels.QuizViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    lessonId: Int,
    viewModel: QuizViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateBackToLesson: () -> Unit,
    onNavigateToNextLesson: (Int) -> Unit
) {
    // Reload quiz whenever this screen gets composed or lesson changes
    LaunchedEffect(lessonId) {
        viewModel.loadQuiz(lessonId)
    }

    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    val shakeTrigger by viewModel.shakeTrigger.collectAsState()

    val scrollState = rememberScrollState()

    // Shake animation configuration for wrong answer failures
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 250
                        -15f at 50
                        15f at 100
                        -10f at 150
                        10f at 200
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
            viewModel.resetShakeTrigger()
        }
    }

    if (questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("در حال آماده‌سازی آزمون این جلسه...", color = Color.White)
        }
        return
    }

    val currentQuestion = questions[currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "آزمون جلسه $lessonId",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateToHome,
                            modifier = Modifier.testTag("quiz_home_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onNavigateBackToLesson) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (val state = quizState) {
                is QuizState.InProgress -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Progress Row: text and bar
                        val currentCount = currentIndex + 1
                        val totalQuestions = questions.size
                        val progressPercent = currentCount.toFloat() / totalQuestions

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سوال $currentCount از $totalQuestions",
                                color = SoftMintText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "پایتون مقدماتی",
                                color = EmeraldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        RumilandProgressBar(progress = progressPercent)

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. Question Text Box in a Card
                        RumilandCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_question_box"),
                            backgroundColor = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = currentQuestion.questionText,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // 3. Option Selection Cards (4 Options)
                        val labels = listOf("الف", "ب", "ج", "د")
                        val currentSelectedOption = selectedAnswers[currentIndex]

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            currentQuestion.optionsList.forEachIndexed { optIndex, optionString ->
                                QuizOptionCard(
                                    optionText = optionString,
                                    label = labels[optIndex],
                                    isSelected = currentSelectedOption == optIndex,
                                    testTag = "quiz_option_$optIndex",
                                    onClick = {
                                        viewModel.selectOption(currentIndex, optIndex)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // 4. Action Row (Prev / Next or Submit)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Prev button
                            if (currentIndex > 0) {
                                RumilandButton(
                                    text = "قبلی",
                                    isOutlined = true,
                                    testTag = "quiz_prev_button",
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    viewModel.previousQuestion()
                                }
                            } else {
                                Spacer(modifier = Modifier.width(100.dp))
                            }

                            // Next or Submit button
                            val isLast = currentIndex == questions.size - 1
                            val isOptionSelected = currentSelectedOption != null

                            if (isLast) {
                                RumilandButton(
                                    text = "ثبت آزمون 🚀",
                                    isOutlined = false,
                                    testTag = "quiz_submit_button",
                                    modifier = Modifier.width(150.dp)
                                ) {
                                    if (isOptionSelected) {
                                        viewModel.submitQuiz()
                                    }
                                }
                            } else {
                                RumilandButton(
                                    text = "بعدی",
                                    isOutlined = false,
                                    testTag = "quiz_next_button",
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    if (isOptionSelected) {
                                        viewModel.nextQuestion()
                                    }
                                }
                            }
                        }
                    }
                }

                is QuizState.Passed -> {
                    // Celebrate screen (PASS)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Sparkling Icons Row
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Success Star",
                                        tint = if (index == 1) WarningOrange else LightGreenProgress,
                                        modifier = Modifier
                                            .size(if (index == 1) 48.dp else 32.dp)
                                            .padding(horizontal = 2.dp)
                                    )
                                }
                            }

                            // Celebration title badge
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(LightGreenProgress.copy(alpha = 0.2f))
                                    .padding(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Check",
                                    tint = LightGreenProgress,
                                    modifier = Modifier.size(54.dp)
                                )
                            }

                            Text(
                                text = "تبریک! آزمون با موفقیت پاس شد تبریک! 🎉🎉",
                                color = LightGreenProgress,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            // Card results metrics
                            RumilandCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "نمره شما در این آزمون",
                                        color = SoftMintText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${state.score} پاسخ صحیح از ${state.total}",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "درصد موفقیت: ${(state.score * 100) / state.total}٪",
                                        color = LightGreenProgress,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "درس بعدی یادگیری پایتون برای شما باز شد! قفل‌های دانش را یکی پس از دیگری بشکنید.",
                                        color = SoftMintText,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (lessonId < 20) {
                                    RumilandButton(
                                        text = "ورود به درس بعدی 📗",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("next_lesson_button")
                                    ) {
                                        onNavigateToNextLesson(lessonId + 1)
                                    }
                                }
                                
                                RumilandButton(
                                    text = "بازگشت به خانه 🏠",
                                    isOutlined = true,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    onNavigateToHome()
                                }
                            }
                        }
                    }
                }

                is QuizState.Failed -> {
                    // Error Warning screen (FAIL)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(WarningOrange.copy(alpha = 0.2f))
                                    .padding(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Failure",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(54.dp)
                                )
                            }

                            Text(
                                text = "کمی آموزش نیاز داریم! ❌",
                                color = WarningOrange,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            RumilandCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                borderColor = WarningOrange
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "نمره شما در این آزمون",
                                        color = SoftMintText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${state.score} پاسخ صحیح از ${state.total}",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "حداقل نمره برای قبولی: ۴ پاسخ صحیح است.",
                                        color = WarningOrange,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "نگران نباشید! شکست بخشی از فرآیند یادگیری برنامه‌نویسی است. می‌توانید مطالب این جلسه را مرور کرده و بلافاصله مجدداً تلاش کنید.",
                                        color = SoftMintText,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Failure Actions Panel (Retry or Review)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RumilandButton(
                                    text = "تلاش مجدد آزمون 🔄",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("retry_quiz_button")
                                ) {
                                    viewModel.resetQuiz()
                                }

                                RumilandButton(
                                    text = "مرور مباحث این درس 📖",
                                    isOutlined = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("review_lesson_button")
                                ) {
                                    onNavigateBackToLesson()
                                }

                                RumilandButton(
                                    text = "بازگشت به خانه 🏠",
                                    isOutlined = true,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    onNavigateToHome()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
