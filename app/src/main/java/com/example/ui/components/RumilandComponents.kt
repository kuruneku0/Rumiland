package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenSurface

// Custom physics-based click modifier satisfying:
// "Press effect: Scale down to 0.96 with opacity change"
@Composable
fun Modifier.pressClickEffect(testTag: String = "", onClick: () -> Unit): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "PressScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.75f else 1f,
        label = "PressAlpha"
    )

    return this
        .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
        .testTag(testTag)
        .pointerInput(onClick) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                    onClick()
                }
            )
        }
}

// 1. RumilandButton Composable satisfying the styling requirements
@Composable
fun RumilandButton(
    text: String,
    modifier: Modifier = Modifier,
    isOutlined: Boolean = false,
    testTag: String = "",
    onClick: () -> Unit
) {
    val buttonShape = RoundedCornerShape(20.dp) // "Rounded corners: 20dp corner radius"
    
    if (isOutlined) {
        Box(
            modifier = modifier
                .shadow(elevation = 2.dp, shape = buttonShape)
                .clip(buttonShape)
                .background(Color.Transparent)
                .pressClickEffect(testTag, onClick)
                .border(1.dp, EmeraldAccent, buttonShape)
                .padding(horizontal = 16.dp, vertical = 12.dp), // "Padding 16px horizontal, 12px vertical"
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Box(
            modifier = modifier
                .shadow(elevation = 4.dp, shape = buttonShape) // "All buttons have subtle shadow (elevation 4dp)"
                .clip(buttonShape)
                .background(EmeraldAccent) // "Filled buttons: Background #00A86B, White text"
                .pressClickEffect(testTag, onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 2. RumilandCard satisfying styling requirements
@Composable
fun RumilandCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = EmeraldAccent,
    elevation: androidx.compose.ui.unit.Dp = 6.dp, // "Subtle elevation (6dp)"
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    borderAlpha: Float = 0.3f,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        border = BorderStroke(1.dp, borderColor.copy(alpha = borderAlpha)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(16.dp) // "Inner padding: 16dp"
        ) {
            content()
        }
    }
}

// 3. QuizOptionCard Composable - Selecatable cards for multiple choice
@Composable
fun QuizOptionCard(
    optionText: String,
    label: String, // 'الف', 'ب', 'ج', 'د'
    isSelected: Boolean,
    testTag: String = "",
    onClick: () -> Unit
) {
    val optionShape = RoundedCornerShape(16.dp)
    val cardBg = if (isSelected) EmeraldAccent else MaterialTheme.colorScheme.surface
    val borderStrokeColor = if (isSelected) EmeraldAccent else EmeraldAccent.copy(alpha = 0.4f)
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pressClickEffect(testTag, onClick),
        shape = optionShape,
        border = BorderStroke(1.dp, borderStrokeColor),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.25f) else EmeraldAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else EmeraldAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            
            Text(
                text = optionText,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// 4. ProgressBar Composable satisfying custom designs
@Composable
fun RumilandProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier
) {
    val progressShape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(progressShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(10.dp)
                .clip(progressShape)
                .background(EmeraldAccent)
        )
    }
}
