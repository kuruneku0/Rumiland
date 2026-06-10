package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RumilandButton
import com.example.ui.components.RumilandCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenSurface
import com.example.ui.theme.LightGreenProgress
import com.example.ui.theme.SoftMintText
import com.example.ui.theme.WarningOrange
import com.example.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToLesson: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { com.example.utils.PreferencesManager(context) }
    val savedName = preferencesManager.getUserName()
    val initials = remember(savedName) {
        val parts = savedName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (parts.size >= 2) {
            val p1 = parts[0].firstOrNull()?.toString() ?: ""
            val p2 = parts[1].firstOrNull()?.toString() ?: ""
            "$p1$p2"
        } else if (parts.size == 1) {
            parts[0].take(2)
        } else {
            "JD"
        }
    }

    val scrollState = rememberScrollState()
    val completionState by viewModel.completionState.collectAsState()
    val recentActivity by viewModel.recentActivity.collectAsState()
    val progress by viewModel.userProgress.collectAsState()

    // Smooth overall progress bar/arc animator
    val animatedProgressFraction by animateFloatAsState(
        targetValue = completionState.percentage / 100f,
        animationSpec = tween(1200),
        label = "HomeProgressArc"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(top = 16.dp, bottom = 80.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Sophisticated Dark Header Row (Matching Design HTML header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Initials Badge (Left-aligned)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ForestGreenSurface)
                    .border(2.dp, EmeraldAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Brand title and icon (Right-aligned / Persian)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "آکادمی رومی‌لند",
                    color = SoftMintText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ForestGreenSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Academy Logo",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Hero Section Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(EmeraldAccent.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Sparkle",
                    tint = EmeraldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "آموزش گام‌به‌گام ۲۰ درس پایتون همراه با آزمون",
                    color = EmeraldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. Progress Circular Card with 32dp Rounded Corners (Matching Design HTML Progress Card)
        RumilandCard(
            modifier = Modifier.fillMaxWidth().testTag("home_hero_progress_card"),
            backgroundColor = ForestGreenSurface,
            shape = RoundedCornerShape(32.dp),
            borderAlpha = 0.3f,
            elevation = 12.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Circular Progress Arc on Left
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(80.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeWidth = 6.dp,
                        )
                        CircularProgressIndicator(
                            progress = { animatedProgressFraction },
                            modifier = Modifier
                                .size(80.dp)
                                .testTag("overall_progress_ring"),
                            color = EmeraldAccent,
                            strokeWidth = 6.dp,
                        )
                        Text(
                            text = "${completionState.percentage}٪",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Card Title Details on Right
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "آموزش پایتون",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "ادامه مسیر یادگیری",
                            color = SoftMintText.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Right
                        )
                    }
                }

                // Subordinated progress pills (Sessions & Current dynamic score)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dynamic Total XP pill (LightGreen variant)
                    val totalXP = (completionState.completedCount * 50) + (progress?.quizScoresMap?.values?.sum() ?: 0) * 10
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(LightGreenProgress.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$totalXP امتیاز",
                            color = LightGreenProgress,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Session count pill (Emerald variant)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(EmeraldAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${completionState.completedCount} / ${completionState.totalCount} جلسه",
                            color = EmeraldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Continue Button
                val textBtn = if (completionState.percentage >= 100) "مرور مجدد مسیر" else "ادامه یادگیری: جلسه ${completionState.nextLessonToContinue?.id ?: 1}"
                RumilandButton(
                    text = textBtn,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "continue_journey_button"
                ) {
                    val destinationLessonId = completionState.nextLessonToContinue?.id ?: 1
                    onNavigateToLesson(destinationLessonId)
                }
            }
        }

        // Streak Banner (Streak badge of flame if user progress has any streak)
        progress?.let { pr ->
            if (pr.currentStreak > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WarningOrange.copy(alpha = 0.15f))
                        .border(1.dp, WarningOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Flame active",
                                tint = WarningOrange,
                                modifier = Modifier.size(28.dp)
                             )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                modifier = Modifier.testTag("streak_days_display_text"),
                                text = "روزهای متوالی فعالیت: ${pr.currentStreak} روز",
                                color = WarningOrange,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "عالیه! پرانرژی ادامه بده!",
                            color = SoftMintText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. Featured Daily Python Tip Card with 24dp Rounded Corners (Matching Design HTML Note of Day)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "💡 نکته آموزشی امروز پایتون",
                color = SoftMintText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            )
            RumilandCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ForestGreenSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp),
                borderAlpha = 0.1f,
                elevation = 4.dp
            ) {
                Text(
                    text = viewModel.dailyTip,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 5. Recent Activity Horizontal Grid (Matching Design HTML Last Quizzes grid cards)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "🥋 آخرین آزمون‌های شما",
                color = SoftMintText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            )

            if (recentActivity.isEmpty()) {
                RumilandCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = ForestGreenSurface
                ) {
                    Text(
                        text = "هنوز در هیچ آزمونی شرکت نکرده‌اید. با خواندن درس اول اولین محک خود را بزنید!",
                        color = SoftMintText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    recentActivity.forEach { activity ->
                        val isPassed = activity.score >= 4
                        val percentageText = "${(activity.score * 100) / 5}٪"
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ForestGreenSurface)
                                .border(1.dp, EmeraldAccent.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "درس ${activity.lessonId}",
                                    color = SoftMintText.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = percentageText,
                                    color = if (isPassed) LightGreenProgress else WarningOrange,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
