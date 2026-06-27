package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlantDetailScreen
import com.example.ui.screens.CropMonitoringScreen

@Composable
fun AgrinexusNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { isMacro -> 
                    navController.navigate("home?isMacro=$isMacro") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { isMacro ->
                    navController.navigate("home?isMacro=$isMacro") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home?isMacro={isMacro}") { backStackEntry ->
            val isMacroStr = backStackEntry.arguments?.getString("isMacro") ?: "false"
            val isMacro = isMacroStr.toBoolean()
            com.example.ui.theme.AgrinexusTheme(isMacro = isMacro) {
                HomeScreen(
                    isMacro = isMacro,
                    onNavigateToPlantDetail = { plantId -> navController.navigate("plant_detail/$plantId/$isMacro") },
                    onNavigateToMonitoring = { navController.navigate("monitoring?isMacro=$isMacro") },
                    onNavigateToProfile = { navController.navigate("profile?isMacro=$isMacro") }
                )
            }
        }
        composable("profile?isMacro={isMacro}") { backStackEntry ->
            val isMacro = (backStackEntry.arguments?.getString("isMacro") ?: "false").toBoolean()
            com.example.ui.theme.AgrinexusTheme(isMacro = isMacro) {
                com.example.ui.screens.ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onProfileUpdated = { isMacroUpdated ->
                        // Optional: Navigate back to home and reload if theme changed
                        navController.navigate("home?isMacro=$isMacroUpdated") {
                            popUpTo("home?isMacro=$isMacro") { inclusive = true }
                        }
                    }
                )
            }
        }
        composable("plant_detail/{plantId}/{isMacro}") { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
            val isMacro = (backStackEntry.arguments?.getString("isMacro") ?: "false").toBoolean()
            com.example.ui.theme.AgrinexusTheme(isMacro = isMacro) {
                PlantDetailScreen(
                    plantId = plantId,
                    isMacroTheme = isMacro,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("monitoring?isMacro={isMacro}") { backStackEntry ->
            val isMacro = (backStackEntry.arguments?.getString("isMacro") ?: "false").toBoolean()
            com.example.ui.theme.AgrinexusTheme(isMacro = isMacro) {
                CropMonitoringScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToCalendar = { cropId ->
                        navController.navigate("crop_calendar/$cropId/$isMacro")
                    },
                    onNavigateToVisualCalendar = { cropId ->
                        navController.navigate("visual_calendar/$cropId/$isMacro")
                    }
                )
            }
        }
        composable("crop_calendar/{cropId}/{isMacro}") { backStackEntry ->
            val cropId = backStackEntry.arguments?.getString("cropId") ?: ""
            val isMacro = (backStackEntry.arguments?.getString("isMacro") ?: "false").toBoolean()
            com.example.ui.theme.AgrinexusTheme(isMacro = isMacro) {
                com.example.ui.screens.CropCalendarScreen(
                    cropId = cropId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("visual_calendar/{cropId}/{isMacro}") { backStackEntry ->
            val cropId = backStackEntry.arguments?.getString("cropId") ?: ""
            val isMacro = (backStackEntry.arguments?.getString("isMacro") ?: "false").toBoolean()
            com.example.ui.theme.AgrinexusTheme(isMacro = isMacro) {
                com.example.ui.screens.CropVisualCalendarScreen(
                    cropId = cropId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
