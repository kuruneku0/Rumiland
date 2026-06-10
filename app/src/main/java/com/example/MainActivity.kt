package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.database.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.LessonDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.LightGreenProgress
import com.example.ui.theme.RumilandAcademyTheme
import com.example.ui.theme.SoftMintText
import com.example.utils.PreferencesManager
import com.example.viewmodels.HomeViewModel
import com.example.viewmodels.LearnViewModel
import com.example.viewmodels.ProfileViewModel
import com.example.viewmodels.QuizViewModel
import com.example.viewmodels.RumilandViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB and Repository layers
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(applicationContext)
        val preferencesManager = PreferencesManager(applicationContext)

        // 2. Set up ViewModel Providers
        val factory = RumilandViewModelFactory(repository)
        val homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        val learnViewModel = ViewModelProvider(this, factory)[LearnViewModel::class.java]
        val quizViewModel = ViewModelProvider(this, factory)[QuizViewModel::class.java]
        val profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setContent {
            // Live theme toggle subscriber
            var isDarkTheme by remember { mutableStateOf(preferencesManager.isDarkTheme()) }

            RumilandAcademyTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()

                // Track active navigation stack
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                // Last unlocked lesson ID for fluid "Quiz" Tab clicks
                val userProgress by homeViewModel.userProgress.collectAsState()
                val lastUnlockedLessonId = remember(userProgress) {
                    val completed = userProgress?.completedLessonsList ?: emptyList()
                    val nextId = (completed.maxOrNull() ?: 0) + 1
                    if (nextId <= 20) nextId else 20
                }

                // Hide bottom navigation rules on study routes (to preserve focus space)
                val shouldShowBottomBar = remember(currentRoute) {
                    currentRoute == "home" || currentRoute == "learn" || currentRoute == "profile"
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // "On EVERY screen EXCEPT HomeScreen: Show a TopAppBar"
                        if (currentRoute == "learn" || currentRoute == "profile") {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = if (currentRoute == "learn") "آموزش پایتون رومی‌لند" else "پروفایل کاربری رومی‌لند",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { navController.navigate("home") },
                                        modifier = Modifier.testTag("global_home_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = "Go Home",
                                            tint = Color.White
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            androidx.compose.material3.Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                    .border(
                                        androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.2f)),
                                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                    ),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    tonalElevation = 0.dp
                                ) {
                                    // 1. Home Tab
                                    NavigationBarItem(
                                        selected = currentRoute == "home",
                                        onClick = { navController.navigate("home") },
                                        label = { Text("خانه", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                        modifier = Modifier.testTag("nav_tab_home"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = EmeraldAccent,
                                            selectedTextColor = EmeraldAccent,
                                            unselectedIconColor = SoftMintText,
                                            unselectedTextColor = SoftMintText,
                                            indicatorColor = EmeraldAccent.copy(alpha = 0.15f)
                                        )
                                    )

                                    // 2. Learn Tab
                                    NavigationBarItem(
                                        selected = currentRoute == "learn",
                                        onClick = { navController.navigate("learn") },
                                        label = { Text("آموزش", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.School, contentDescription = "Learn") },
                                        modifier = Modifier.testTag("nav_tab_learn"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = EmeraldAccent,
                                            selectedTextColor = EmeraldAccent,
                                            unselectedIconColor = SoftMintText,
                                            unselectedTextColor = SoftMintText,
                                            indicatorColor = EmeraldAccent.copy(alpha = 0.15f)
                                        )
                                    )

                                    // 3. Quiz Tab (Directs to the user's active unlocked session)
                                    NavigationBarItem(
                                        selected = currentRoute.startsWith("quiz"),
                                        onClick = { navController.navigate("quiz/$lastUnlockedLessonId") },
                                        label = { Text("آزمون", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.Quiz, contentDescription = "Quiz") },
                                        modifier = Modifier.testTag("nav_tab_quiz"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = EmeraldAccent,
                                            selectedTextColor = EmeraldAccent,
                                            unselectedIconColor = SoftMintText,
                                            unselectedTextColor = SoftMintText,
                                            indicatorColor = EmeraldAccent.copy(alpha = 0.15f)
                                        )
                                    )

                                    // 4. Profile Tab
                                    NavigationBarItem(
                                        selected = currentRoute == "profile",
                                        onClick = { navController.navigate("profile") },
                                        label = { Text("پروفایل", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                        modifier = Modifier.testTag("nav_tab_profile"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = EmeraldAccent,
                                            selectedTextColor = EmeraldAccent,
                                            unselectedIconColor = SoftMintText,
                                            unselectedTextColor = SoftMintText,
                                            indicatorColor = EmeraldAccent.copy(alpha = 0.15f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Route 1: Home Dashboard Screen
                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigateToLesson = { lessonId ->
                                    navController.navigate("lesson_detail/$lessonId")
                                }
                            )
                        }

                        // Route 2: Learn Course Syllabus Grid
                        composable("learn") {
                            LearnScreen(
                                viewModel = learnViewModel,
                                onNavigateToLesson = { lessonId ->
                                    navController.navigate("lesson_detail/$lessonId")
                                }
                            )
                        }

                        // Route 3: Lesson detail page with parsed Python text
                        composable(
                            route = "lesson_detail/{lessonId}",
                            arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: 1
                            val isBookmarkedState = repository.isBookmarked(lessonId).collectAsState(initial = false)
                            val isBookmarkedValue = isBookmarkedState.value == true

                            LessonDetailScreen(
                                lessonId = lessonId,
                                viewModel = learnViewModel,
                                onNavigateToHome = { navController.navigate("home") },
                                onNavigateBack = { navController.navigateUp() },
                                onStartQuiz = { id -> navController.navigate("quiz/$id") },
                                isBookmarked = isBookmarkedValue,
                                onToggleBookmark = { id ->
                                    repository.toggleBookmark(id)
                                }
                            )
                        }

                        // Route 4: Interactive Quiz Assessment Screen
                        composable(
                            route = "quiz/{lessonId}",
                            arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: 1
                            QuizScreen(
                                lessonId = lessonId,
                                viewModel = quizViewModel,
                                onNavigateToHome = { navController.navigate("home") },
                                onNavigateBackToLesson = {
                                    navController.navigate("lesson_detail/$lessonId")
                                },
                                onNavigateToNextLesson = { nextId ->
                                    navController.navigate("lesson_detail/$nextId")
                                }
                            )
                        }

                        // Route 5: Profile Screen with stats and badges
                        composable("profile") {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                preferencesManager = preferencesManager,
                                isDarkTheme = isDarkTheme,
                                onThemeChanged = { toggleState ->
                                    preferencesManager.setDarkTheme(toggleState)
                                    isDarkTheme = toggleState
                                },
                                onNavigateToLesson = { id ->
                                    navController.navigate("lesson_detail/$id")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
