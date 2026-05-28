package com.example.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MainViewModel
import com.example.ui.screens.GridCalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JournalEntryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.TimelineScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val JOURNAL_ENTRY = "journal_entry/"
    const val GRID_CALENDAR = "grid_calendar"
    const val TIMELINE = "timeline"
    const val SETTINGS = "settings"
}

@Composable
fun LifeTrackerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel,
    startDestination: String = Routes.ONBOARDING
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                viewModel = viewModel,
                onFinish = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                navigateToEntry = { date ->
                    navController.navigate("${Routes.JOURNAL_ENTRY}$date")
                },
                navigateToGrid = {
                    navController.navigate(Routes.GRID_CALENDAR)
                },
                navigateToTimeline = {
                    navController.navigate(Routes.TIMELINE)
                }
            )
        }
        composable(
            route = "${Routes.JOURNAL_ENTRY}{date}",
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            JournalEntryScreen(
                date = date,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.GRID_CALENDAR) {
            GridCalendarScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.TIMELINE) {
            TimelineScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToEntry = { date ->
                    navController.navigate("${Routes.JOURNAL_ENTRY}$date")
                }
            )
        }
        composable(Routes.SETTINGS) {
            com.example.ui.screens.SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
