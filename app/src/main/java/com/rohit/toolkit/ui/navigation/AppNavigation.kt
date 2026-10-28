package com.rohit.toolkit.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rohit.toolkit.data.model.ToolCategoryId
import com.rohit.toolkit.data.model.ToolRegistry
import com.rohit.toolkit.ui.dashboard.HomeScreen
import com.rohit.toolkit.ui.onboarding.PermissionSetupScreen
import com.rohit.toolkit.ui.settings.SettingsScreen
import com.rohit.toolkit.ui.splash.SplashScreen
import com.rohit.toolkit.ui.tools.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val permissionsConfigured = remember { prefs.getBoolean("permissions_configured", false) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // Step 10: 5-second Animated Splash Screen
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    if (permissionsConfigured) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        // Step 11: Permissions Setup Screen
        composable("onboarding") {
            PermissionSetupScreen(
                onPermissionsCompleted = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // Home Dashboard with 11 Categories
        composable("home") {
            HomeScreen(
                onToolSelected = { tool ->
                    navController.navigate("tool/${tool.id}")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // Settings & GitHub In-App Updater
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Unified Tool Dispatcher for all 11 Categories
        composable(
            route = "tool/{toolId}",
            arguments = listOf(navArgument("toolId") { type = NavType.StringType })
        ) { backStackEntry ->
            val toolId = backStackEntry.arguments?.getString("toolId") ?: ""
            val tool = ToolRegistry.getToolById(toolId)

            when (tool?.categoryId) {
                ToolCategoryId.MATHEMATICS -> {
                    MathematicsToolScreen(toolId = toolId, onBack = { navController.popBackStack() })
                }
                ToolCategoryId.ML_KIT -> {
                    MlKitToolsScreen(toolId = toolId, onBack = { navController.popBackStack() })
                }
                ToolCategoryId.ARCHIVE -> {
                    ArchiveToolsScreen(toolId = toolId, onBack = { navController.popBackStack() })
                }
                ToolCategoryId.FILE_MANAGER -> {
                    FileManagerScreen(onBack = { navController.popBackStack() })
                }
                ToolCategoryId.IMAGE_TOOLS -> {
                    ImageToolsScreen(toolId = toolId, onBack = { navController.popBackStack() })
                }
                ToolCategoryId.ORGANIZE_PDF,
                ToolCategoryId.OPTIMIZE_PDF,
                ToolCategoryId.CONVERT_PDF,
                ToolCategoryId.EDIT_PDF,
                ToolCategoryId.PDF_SECURITY,
                ToolCategoryId.PDF_INTELLIGENCE -> {
                    PdfToolsScreen(toolId = toolId, onBack = { navController.popBackStack() })
                }
                null -> {
                    HomeScreen(
                        onToolSelected = { t -> navController.navigate("tool/${t.id}") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
            }
        }
    }
}
