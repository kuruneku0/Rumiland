package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RumilandButton
import com.example.ui.components.RumilandCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.LightGreenProgress
import com.example.ui.theme.SoftMintText
import com.example.ui.theme.WarningOrange
import com.example.utils.PreferencesManager
import com.example.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    preferencesManager: PreferencesManager,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onNavigateToLesson: (Int) -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.profileStats.collectAsState()
    val bookmarkedLessons by viewModel.bookmarkedLessons.collectAsState()

    val scrollState = rememberScrollState()

    // Rename states
    var isEditingName by remember { mutableStateOf(false) }
    var tempNameInput by remember { mutableStateOf(preferencesManager.getUserName()) }
    var currentSavedName by remember { mutableStateOf(preferencesManager.getUserName()) }

    // Dialog state for wipe
    var showResetProgressConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(top = 16.dp, bottom = 90.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Profile Username Header
        RumilandCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Circular Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(EmeraldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Profile Avatar",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Name editing widget inline
                if (isEditingName) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (tempNameInput.isNotBlank()) {
                                    preferencesManager.setUserName(tempNameInput)
                                    currentSavedName = tempNameInput
                                    isEditingName = false
                                    Toast.makeText(context, "نام کاربری با موفقیت تغییر کرد", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("save_name_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Name",
                                tint = EmeraldAccent
                            )
                        }
                        
                        OutlinedTextField(
                            value = tempNameInput,
                            onValueChange = { tempNameInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("username_input_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldAccent,
                                unfocusedBorderColor = EmeraldAccent.copy(alpha = 0.4f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                tempNameInput = currentSavedName
                                isEditingName = true
                            },
                            modifier = Modifier.testTag("edit_name_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = SoftMintText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentSavedName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Text(
                    text = "دانشجوی آکادمی پایتون رومی‌لند",
                    color = SoftMintText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. Metrics Analytics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Streak
            RumilandCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Fire",
                        tint = WarningOrange,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "روزهای متوالی",
                        color = SoftMintText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${stats.streakDays} روز",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quizzes averages
            RumilandCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Avg tests score",
                        tint = LightGreenProgress,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "میانگین آزمون‌ها",
                        color = SoftMintText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stats.averageScoreStr,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 3. Theme switch toggler
        RumilandCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeChanged(it) },
                    modifier = Modifier.testTag("theme_toggle_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldAccent,
                        uncheckedThumbColor = EmeraldAccent,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    thumbContent = {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Theme state",
                            tint = if (isDarkTheme) EmeraldAccent else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "پلاگین قالب کاربری",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isDarkTheme) "حالت تیره (Dark Mode)" else "حالت روشن (Light Mode)",
                        color = SoftMintText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 4. Badges / Achievements Panel
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "🎖️ لوح‌های افتخار و جوایز علمی شما",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            )
            RumilandCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge 1: First Step
                    BadgeView(
                        title = "قدم اول",
                        unlocked = stats.badgeFirstStep,
                        activeColor = EmeraldAccent,
                        tag = "badge_first_step"
                    )

                    // Badge 2: Loops
                    BadgeView(
                        title = "حلقه‌ها",
                        unlocked = stats.badgeLoops,
                        activeColor = WarningOrange,
                        tag = "badge_loops"
                    )

                    // Badge 3: OOP
                    BadgeView(
                        title = "شیء‌گرا",
                        unlocked = stats.badgeOop,
                        activeColor = LightGreenProgress,
                        tag = "badge_oop"
                    )

                    // Badge 4: Ninja
                    BadgeView(
                        title = "نینجا",
                        unlocked = stats.badgeNinja,
                        activeColor = Color(0xFFBD93F9), // Purple premium
                        tag = "badge_ninja"
                    )
                }
            }
        }

        // 5. Bookmarks Feed List
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "📌 درس‌های نشان‌شده برای مطالعه مجدد",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
            )
            RumilandCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                if (bookmarkedLessons.isEmpty()) {
                    Text(
                        text = "لیست نشان‌شده‌های شما خالی است. در بخش مطالعه دروس می‌توانید درس دلخواه خود را ذخیره کنید تا اینجا نشان داده شود.",
                        color = SoftMintText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        bookmarkedLessons.forEach { lesson ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldAccent.copy(alpha = 0.08f))
                                    .clickable {
                                        onNavigateToLesson(lesson.id)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Active bookmark icon",
                                    tint = EmeraldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = " جلسه ${lesson.id}: ${lesson.title} ",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Reset admin progress triggers
        RumilandButton(
            text = "ریست کردن کامل پیشرفت آموزشی ⚠️",
            isOutlined = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_progress_button")
        ) {
            showResetProgressConfirmation = true
        }
    }

    // Confirmation Alert Modal
    if (showResetProgressConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetProgressConfirmation = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllProgress()
                        showResetProgressConfirmation = false
                        Toast.makeText(context, "تمامی اطلاعات یادگیری مجدداً صفر شدند!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("بله، پاک شود", color = WarningOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetProgressConfirmation = false }) {
                    Text("انصراف", color = SoftMintText)
                }
            },
            title = {
                Text(
                    text = "آیا مطمئن هستید؟",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "با تایید این عمل، تمامی مراحل باز شده، نمرات آزمون‌ها، نشان‌شده‌ها و روزهای فعالیت شما کاملاً صفر شده و از ابتدا به جلسه اول بازخواهید گشت.",
                    color = SoftMintText,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun BadgeView(
    title: String,
    unlocked: Boolean,
    activeColor: Color,
    tag: String
) {
    val opacity = if (unlocked) 1f else 0.40f
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(68.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (unlocked) activeColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (unlocked) Icons.Default.AutoAwesome else Icons.Default.Lock,
                contentDescription = "Badge item",
                tint = if (unlocked) activeColor else Color.Gray,
                modifier = Modifier.size(if (unlocked) 24.dp else 18.dp)
            )
        }
        
        Text(
            text = title,
            color = if (unlocked) Color.White else SoftMintText.copy(alpha = opacity),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
