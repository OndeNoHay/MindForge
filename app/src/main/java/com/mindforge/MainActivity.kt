package com.mindforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindforge.core.domain.model.DifficultyLevel
import com.mindforge.core.ui.theme.MindForgeTheme
import com.mindforge.feature.home.GameType
import com.mindforge.feature.home.HomeScreen
import com.mindforge.game.memorymatrix.ui.MemoryMatrixScreen
import com.mindforge.game.taskprioritizer.ui.TaskPrioritizerScreen
import com.mindforge.game.nameface.ui.NameFaceScreen
import com.mindforge.game.meetingrecall.ui.MeetingRecallScreen
import com.mindforge.game.conceptlinker.ui.ConceptLinkerScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Single Activity Architecture with Jetpack Compose
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindForgeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MindForgeNavigation()
                }
            }
        }
    }
}

@Composable
fun MindForgeNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGame = { gameType ->
                    when (gameType) {
                        GameType.MEMORY_MATRIX -> {
                            navController.navigate(Screen.MemoryMatrix.route)
                        }
                        GameType.TASK_PRIORITIZER -> {
                            navController.navigate(Screen.TaskPrioritizer.route)
                        }
                        GameType.NAME_FACE -> {
                            navController.navigate(Screen.NameFace.route)
                        }
                        GameType.MEETING_RECALL -> {
                            navController.navigate(Screen.MeetingRecall.route)
                        }
                        GameType.CONCEPT_LINKER -> {
                            navController.navigate(Screen.ConceptLinker.route)
                        }
                        else -> {
                            // Other games not implemented yet
                        }
                    }
                }
            )
        }

        composable(Screen.MemoryMatrix.route) {
            MemoryMatrixScreen(
                difficulty = DifficultyLevel.BEGINNER,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TaskPrioritizer.route) {
            TaskPrioritizerScreen(
                difficulty = DifficultyLevel.BEGINNER,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.NameFace.route) {
            NameFaceScreen(
                difficulty = DifficultyLevel.BEGINNER,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.MeetingRecall.route) {
            MeetingRecallScreen(
                difficulty = DifficultyLevel.BEGINNER,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ConceptLinker.route) {
            ConceptLinkerScreen(
                difficulty = DifficultyLevel.BEGINNER,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MemoryMatrix : Screen("memory_matrix")
    object TaskPrioritizer : Screen("task_prioritizer")
    object NameFace : Screen("name_face")
    object MeetingRecall : Screen("meeting_recall")
    object ConceptLinker : Screen("concept_linker")
}
