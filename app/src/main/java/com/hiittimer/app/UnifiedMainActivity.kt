package com.hiittimer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hiittimer.app.data.PreferencesManager
import com.hiittimer.app.ui.config.ConfigScreen
import com.hiittimer.app.ui.history.WorkoutHistoryScreen
import com.hiittimer.app.ui.theme.HIITTimerTheme
import com.hiittimer.app.ui.timer.TimerScreen
import com.hiittimer.app.timer.UnifiedTimerManager
import com.hiittimer.app.ui.timer.UnifiedTimerViewModel
import com.hiittimer.app.ui.workouts.WorkoutSelectionScreen
import com.hiittimer.app.ui.workouts.WorkoutPreviewScreen
import com.hiittimer.app.ui.workouts.WorkoutBuilderScreen
import kotlinx.coroutines.runBlocking

/**
 * Enhanced MainActivity that supports complex workouts
 */
class UnifiedMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferencesManager = remember { PreferencesManager(this) }
            val themePreference by preferencesManager.themePreference.collectAsState()

            HIITTimerTheme(themePreference = themePreference) {
                UnifiedHIITTimerApp()
            }
        }
    }
}

@Composable
fun UnifiedHIITTimerApp() {
    val navController = rememberNavController()
    val unifiedViewModel: UnifiedTimerViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "timer",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Timer screen
            composable("timer") {
                TimerScreen(
                    viewModel = unifiedViewModel,
                    onNavigateToHistory = {
                        navController.navigate("history")
                    },
                    onNavigateToWorkouts = {
                        navController.navigate("workouts")
                    }
                )
            }
            
            // Config screen for simple workouts
            composable("config") {
                ConfigScreen(
                    viewModel = unifiedViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Workout history screen
            composable("history") {
                WorkoutHistoryScreen(
                    workoutHistoryRepository = unifiedViewModel.getWorkoutHistoryRepository(),
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Complex workout selection screen
            composable("workouts") {
                WorkoutSelectionScreen(
                    navController = navController,
                    viewModel = unifiedViewModel
                )
            }
            
            // Workout preview screen
            composable(
                route = "workout_preview/{workoutId}",
                arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getString("workoutId") ?: return@composable
                
                // Get workout from repository
                val workouts by unifiedViewModel.complexWorkouts.collectAsState(initial = emptyList())
                val workout = workouts.find { it.id == workoutId }
                
                if (workout != null) {
                    WorkoutPreviewScreen(
                        navController = navController,
                        viewModel = unifiedViewModel,
                        workout = workout
                    )
                }
            }
            
            // Workout builder screen
            composable(
                route = "workout_builder?name={workoutName}",
                arguments = listOf(
                    navArgument("workoutName") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val workoutName = backStackEntry.arguments?.getString("workoutName") ?: "New Workout"
                
                WorkoutBuilderScreen(
                    navController = navController,
                    viewModel = unifiedViewModel,
                    initialWorkoutName = workoutName
                )
            }
            
            // Workout editor screen (for editing existing workouts)
            composable(
                route = "workout_editor/{workoutId}",
                arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getString("workoutId") ?: return@composable
                
                // Get workout from repository
                val workouts by unifiedViewModel.complexWorkouts.collectAsState(initial = emptyList())
                val workout = workouts.find { it.id == workoutId }
                
                if (workout != null) {
                    WorkoutBuilderScreen(
                        navController = navController,
                        viewModel = unifiedViewModel,
                        existingWorkout = workout
                    )
                }
            }
        }
    }
}