package com.fixmate.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fixmate.DiagnosisViewModel
import com.fixmate.ui.screens.CaptureScreen
import com.fixmate.ui.screens.GuidedFormScreen
import com.fixmate.ui.screens.HomeScreen
import com.fixmate.ui.screens.ResultsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Capture : Screen("capture")
    data object Form : Screen("form")
    data object Results : Screen("results")
}

@Composable
fun FixMateApp() {
    val navController = rememberNavController()
    // A single ViewModel is shared across all screens so the form survives navigation.
    val viewModel: DiagnosisViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                usingLiveAi = viewModel.usingLiveAi,
                onStart = {
                    viewModel.resetForNewDiagnosis()
                    navController.navigate(Screen.Capture.route)
                }
            )
        }

        composable(Screen.Capture.route) {
            CaptureScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Screen.Form.route) }
            )
        }

        composable(Screen.Form.route) {
            GuidedFormScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSubmit = {
                    viewModel.submit()
                    navController.navigate(Screen.Results.route)
                }
            )
        }

        composable(Screen.Results.route) {
            ResultsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onStartOver = {
                    viewModel.resetForNewDiagnosis()
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}
