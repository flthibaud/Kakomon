package com.fthibaud.learningapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fthibaud.learningapp.permissions.areAllPermissionsGranted
import com.fthibaud.learningapp.screens.AppSelectionScreen
import com.fthibaud.learningapp.screens.HomeScreen
import com.fthibaud.learningapp.screens.OnboardingScreen

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val startDestination = remember {
        if (areAllPermissionsGranted(context)) Home else Onboarding
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Onboarding> {
            OnboardingScreen(
                onNavigateToAppSelection = {
                    navController.navigate(Home) {
                        popUpTo<Onboarding> { inclusive = true }
                    }
                }
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
