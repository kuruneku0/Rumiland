package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RumilandButton
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenSurface
import com.example.ui.theme.SoftMintText
import com.example.ui.theme.WarningOrange
import com.example.utils.MarkdownBlock
import com.example.utils.MarkdownParser
import com.example.viewmodels.LearnViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    lessonId: Int,
    viewModel: LearnViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    onStartQuiz: (Int) -> Unit,
    // Custom repository interactions triggered via screen scope
    isBookmarked: Boolean,
    onToggleBookmark: suspend (Int) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val lessons by viewModel.lessons.collectAsState()
    val lesson = lessons.find { it.id == lessonId }

    if (lesson == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("در حال بارگذاری درس...", color = Color.White)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "جلسه ${lesson.id}: ${lesson.title}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateToHome,
                            modifier = Modifier.testTag("home_nav_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                onToggleBookmark(lessonId)
                                Toast.makeText(
                                    context,
                                    if (isBookmarked) "درس از ذخیره‌ها حذف شد" else "درس با موفقیت ذخیره شد",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.testTag("bookmark_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) LightGreenProgressFlow else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Main lesson scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Parse markdown content
                val blocks = remember(lesson.contentMarkdown) {
                    MarkdownParser.parse(lesson.contentMarkdown)
                }

                blocks.forEach { block ->
                    when (block) {
                        is MarkdownBlock.Header -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = block.text,
                                color = EmeraldAccent,
                                fontSize = if (block.level == 3) 18.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is MarkdownBlock.Paragraph -> {
                            Text(
                                text = block.text,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is MarkdownBlock.BulletPoint -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = block.text,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "•",
                                    color = EmeraldAccent,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        is MarkdownBlock.QuoteBlock -> {
                            // Rounded callout block quote
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ForestGreenSurface.copy(alpha = 0.6f))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = block.text,
                                    color = SoftMintText,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Tip",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        is MarkdownBlock.CodeBlock -> {
                            // Monospaced syntax highlighted Python block
                            PythonCodeBlock(codeText = block.code, context = context)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Fixed Start Quiz Action Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                RumilandButton(
                    text = "شروع آزمون این جلسه 📝",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_quiz_button"),
                    isOutlined = false
                ) {
                    onStartQuiz(lessonId)
                }
            }
        }
    }
}

@Composable
fun PythonCodeBlock(codeText: String, context: Context) {
    val blockShape = RoundedCornerShape(12.dp)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(blockShape)
            .background(Color(0xFF0F172A)) // Dark slate code banner
            .border(1.dp, Color(0xFF334155), blockShape)
    ) {
        // Upper banner with copy button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Python Code", codeText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "کد کپی شد!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = SoftMintText,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "python_code.py",
                color = SoftMintText,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Code content with tokenizer highlighting
        SyntaxHighlightedPython(code = codeText)
    }
}

@Composable
fun SyntaxHighlightedPython(code: String) {
    val annotatedString = remember(code) {
        buildAnnotatedString {
            val keywords = setOf("def", "class", "print", "if", "else", "elif", "for", "while", "in", "import", "return", "try", "except", "not", "and", "or", "break", "continue", "pass", "True", "False")
            val builtins = setOf("len", "range", "type", "int", "float", "str", "list", "dict", "tuple", "set")
            
            val words = code.split(Regex("(?<=\\W)|(?=\\W)"))
            var commentInAction = false
            
            for (word in words) {
                when {
                    word == "#" -> {
                        pushStyle(SpanStyle(color = Color(0xFF64748B))) // Gray slate comment
                        append(word)
                        commentInAction = true
                    }
                    commentInAction -> {
                        if (word.contains("\n")) {
                            commentInAction = false
                            pop()
                        }
                        append(word)
                    }
                    keywords.contains(word) -> {
                        pushStyle(SpanStyle(color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)) // Vivid rose keywords
                        append(word)
                        pop()
                    }
                    builtins.contains(word) -> {
                        pushStyle(SpanStyle(color = Color(0xFF38BDF8))) // Celestial blue built-ins
                        append(word)
                        pop()
                    }
                    word.startsWith("\"") || word.startsWith("'") || word.endsWith("\"") || word.endsWith("'") -> {
                        pushStyle(SpanStyle(color = Color(0xFF34D399))) // Mint green strings
                        append(word)
                        pop()
                    }
                    word.toIntOrNull() != null -> {
                        pushStyle(SpanStyle(color = Color(0xFFFB923C))) // Orange numbers
                        append(word)
                        pop()
                    }
                    else -> {
                        append(word)
                    }
                }
            }
            if (commentInAction) pop()
        }
    }

    Text(
        text = annotatedString,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        color = Color(0xFFF8F8F2),
        lineHeight = 20.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    )
}

// Global resource color values mapping safely
val LightGreenProgressFlow = Color(0xFF2ECC71)
