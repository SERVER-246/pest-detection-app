package com.example.intelli_pest.presentation.navigation

/**
 * Navigation destinations for the app
 */
sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Camera : Screen("camera")
    data object Results : Screen("results/{resultId}") {
        fun createRoute(resultId: Long) = "results/$resultId"
    }
    data object Models : Screen("models")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}

