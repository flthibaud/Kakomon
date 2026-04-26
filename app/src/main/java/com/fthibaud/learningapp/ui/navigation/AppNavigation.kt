package com.fthibaud.learningapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fthibaud.learningapp.ui.screens.AppSelectionScreen
import com.fthibaud.learningapp.ui.screens.HomeScreen
import com.fthibaud.learningapp.ui.screens.OnboardingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Onboarding
    ) {
        composable<Onboarding> {
            OnboardingScreen(
                onNavigateToAppSelection = { navController.navigate(Home) }
            )
        }
        composable<Home> {
            HomeScreen(
                onNavigateToAppSelection = { navController.navigate(AppSelection) }
            )
        }
        composable<AppSelection> {
            AppSelectionScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}