package com.mindforge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
    object MemoryMatrix : Screen("game/memory_matrix")
    object TaskPrioritizer : Screen("game/task_prioritizer")
    object NameFace : Screen("game/name_face")
    object MeetingRecall : Screen("game/meeting_recall")
    object ConceptLinker : Screen("game/concept_linker")
    object SpacedReview : Screen("game/spaced_review")
}

/**
 * Main navigation graph for MindForge
 */
@Composable
fun MindForgeNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            // TODO: Add Home screen
        }

        composable(Screen.Profile.route) {
            // TODO: Add Profile screen
        }

        composable(Screen.Progress.route) {
            // TODO: Add Progress screen
        }

        composable(Screen.Settings.route) {
            // TODO: Add Settings screen
        }

        // Game screens
        composable(Screen.MemoryMatrix.route) {
            // TODO: Add Memory Matrix game
        }

        composable(Screen.TaskPrioritizer.route) {
            // TODO: Add Task Prioritizer game
        }

        composable(Screen.NameFace.route) {
            // TODO: Add Name-Face game
        }

        composable(Screen.MeetingRecall.route) {
            // TODO: Add Meeting Recall game
        }

        composable(Screen.ConceptLinker.route) {
            // TODO: Add Concept Linker game
        }

        composable(Screen.SpacedReview.route) {
            // TODO: Add Spaced Review
        }
    }
}
