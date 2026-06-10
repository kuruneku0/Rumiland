package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.RumilandCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.LightGreenProgress
import com.example.ui.theme.SoftMintText
import com.example.viewmodels.LearnViewModel

@Composable
fun LearnScreen(
    viewModel: LearnViewModel,
    onNavigateToLesson: (Int) -> Unit
) {
    val context = LocalContext.current
    val lessons by viewModel.filteredLessons.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp, start = 12.dp, end = 12.dp)
    ) {
        // Search bar at top to filter lessons
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .testTag("lesson_search_input"),
            placeholder = { Text("جستجوی درس‌ها...", color = SoftMintText, fontSize = 14.sp) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = EmeraldAccent
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                focusedBorderColor = EmeraldAccent,
                unfocusedBorderColor = EmeraldAccent.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (lessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "هیچ درسی با این عنوان پیدا نشد!",
                        color = SoftMintText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 90.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(lessons, key = { it.id }) { lesson ->
                    val isLocked = !lesson.isUnlocked
                    
                    val cardBg = if (isLocked) {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }

                    val opacityScale = if (isLocked) 0.55f else 1f

                    RumilandCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .testTag("lesson_card_${lesson.id}")
                            .clickable {
                                if (isLocked) {
                                    Toast.makeText(context, "لطفاً ابتدا درس‌های قبلی را به اتمام برسانید!", Toast.LENGTH_SHORT).show()
                                } else {
                                    onNavigateToLesson(lesson.id)
                                }
                            },
                        backgroundColor = cardBg,
                        borderColor = if (isLocked) EmeraldAccent.copy(alpha = 0.1f) else EmeraldAccent,
                        elevation = if (isLocked) 1.dp else 4.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            // Top Row: Number and Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status indicator icon
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isLocked -> Color.Gray.copy(alpha = 0.1f)
                                                lesson.isCompleted -> LightGreenProgress.copy(alpha = 0.15f)
                                                else -> EmeraldAccent.copy(alpha = 0.15f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isLocked -> Icons.Default.Lock
                                            lesson.isCompleted -> Icons.Default.CheckCircle
                                            else -> Icons.Default.PlayCircle
                                        },
                                        contentDescription = "Lesson Status",
                                        tint = when {
                                            isLocked -> Color.Gray
                                            lesson.isCompleted -> LightGreenProgress
                                            else -> EmeraldAccent
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Lesson Number
                                Text(
                                    text = " جلسه ${lesson.id}# ",
                                    color = SoftMintText.copy(alpha = opacityScale),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Middle title text
                            Text(
                                text = lesson.title,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = opacityScale),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 3,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Bottom check sub-tag if completed or unlocked text
                            Text(
                                text = when {
                                    isLocked -> "قفل شده"
                                    lesson.isCompleted -> "پاس شده"
                                    else -> "شروع یادگیری"
                                },
                                color = when {
                                    isLocked -> Color.Gray
                                    lesson.isCompleted -> LightGreenProgress
                                    else -> EmeraldAccent
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
