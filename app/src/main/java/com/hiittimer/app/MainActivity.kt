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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hiittimer.app.data.PreferencesManager
import com.hiittimer.app.ui.config.ConfigScreen
import com.hiittimer.app.ui.history.WorkoutHistoryScreen
import com.hiittimer.app.ui.theme.HIITTimerTheme
import com.hiittimer.app.ui.timer.TimerScreen
import com.hiittimer.app.ui.timer.TimerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferencesManager = remember { PreferencesManager(this) }
            val themePreference by preferencesManager.themePreference.collectAsState()

            HIITTimerTheme(themePreference = themePreference) {
                HIITTimerApp()
            }
        }
    }
}

@Composable
fun HIITTimerApp() {
    val navController = rememberNavController()
    val timerViewModel: TimerViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(LocalContext.current.applicationContext as android.app.Application)
    )
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "timer",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("timer") {
                TimerScreen(
                    viewModel = timerViewModel,
                    onNavigateToHistory = {
                        navController.navigate("history")
                    }
                )
            }
            
            composable("config") {
                ConfigScreen(
                    viewModel = timerViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("history") {
                WorkoutHistoryScreen(
                    workoutHistoryRepository = timerViewModel.getWorkoutHistoryRepository(),
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
