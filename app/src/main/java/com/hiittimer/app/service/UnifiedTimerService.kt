package com.hiittimer.app.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.data.*
import com.hiittimer.app.performance.PerformanceManager
import com.hiittimer.app.timer.UnifiedTimerManager
import com.hiittimer.app.timer.UnifiedTimerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Enhanced foreground service that supports both simple and complex workouts
 * Ensures timer continues running when app is backgrounded or screen is off
 */
class UnifiedTimerService : Service() {

    companion object {
        // Actions for simple timers
        const val ACTION_START_TIMER = "com.hiittimer.app.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.hiittimer.app.PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "com.hiittimer.app.RESUME_TIMER"
        const val ACTION_STOP_TIMER = "com.hiittimer.app.STOP_TIMER"
        const val ACTION_RESET_TIMER = "com.hiittimer.app.RESET_TIMER"
        
        // Actions for complex workouts
        const val ACTION_START_COMPLEX_WORKOUT = "com.hiittimer.app.START_COMPLEX_WORKOUT"
        const val ACTION_MARK_REP_COMPLETED = "com.hiittimer.app.MARK_REP_COMPLETED"
        const val ACTION_SKIP_TO_NEXT = "com.hiittimer.app.SKIP_TO_NEXT"
        
        // Extras
        const val EXTRA_TIMER_CONFIG = "timer_config"
        const val EXTRA_PRESET_ID = "preset_id"
        const val EXTRA_PRESET_NAME = "preset_name"
        const val EXTRA_EXERCISE_NAME = "exercise_name"
        const val EXTRA_WORKOUT_ID = "workout_id"
        const val EXTRA_COMPLEX_WORKOUT = "complex_workout"
        
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_CHANNEL_ID = "hiit_timer_channel"
    }

    private val binder = UnifiedTimerServiceBinder()
    private lateinit var unifiedTimerManager: UnifiedTimerManager
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: UnifiedTimerNotificationManager
    private lateinit var wakeLockManager: WakeLockManager
    private lateinit var performanceManager: PerformanceManager
    private var workoutHistoryRepository: WorkoutHistoryRepository? = null
    private var complexWorkoutRepository: ComplexWorkoutRepository? = null
    private var statusMonitoringJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Binder class for service communication
     */
    inner class UnifiedTimerServiceBinder : Binder() {
        fun getService(): UnifiedTimerService = this@UnifiedTimerService
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize managers
        val preferencesManager = PreferencesManager(this)
        audioManager = AudioManager(this, preferencesManager.audioSettings.value)
        workoutHistoryRepository = InMemoryWorkoutHistoryRepository()
        complexWorkoutRepository = ComplexWorkoutRepository(this)
        performanceManager = PerformanceManager(this)
        
        // Create unified timer manager
        unifiedTimerManager = UnifiedTimerManager(
            audioManager = audioManager,
            workoutHistoryRepository = workoutHistoryRepository,
            complexWorkoutRepository = complexWorkoutRepository,
            performanceManager = performanceManager
        )
        
        notificationManager = UnifiedTimerNotificationManager(this, performanceManager)
        wakeLockManager = WakeLockManager(this)

        // Initialize performance monitoring
        performanceManager.initialize()

        // Create notification channel
        notificationManager.createNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        when (intent?.action) {
            // Simple timer actions
            ACTION_START_TIMER -> {
                val config = getTimerConfig(intent)
                val presetId = intent.getStringExtra(EXTRA_PRESET_ID)
                val presetName = intent.getStringExtra(EXTRA_PRESET_NAME) ?: "Custom Workout"
                val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME)
                
                if (config != null) {
                    startSimpleTimer(config, presetId, presetName, exerciseName)
                }
            }
            
            // Complex workout actions
            ACTION_START_COMPLEX_WORKOUT -> {
                val workoutId = intent.getStringExtra(EXTRA_WORKOUT_ID)
                val workout = getComplexWorkout(intent)
                
                when {
                    workoutId != null -> startComplexWorkoutById(workoutId)
                    workout != null -> startComplexWorkout(workout)
                }
            }
            
            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_RESUME_TIMER -> resumeTimer()
            ACTION_STOP_TIMER -> stopTimer()
            ACTION_RESET_TIMER -> resetTimer()
            ACTION_MARK_REP_COMPLETED -> markRepCompleted()
            ACTION_SKIP_TO_NEXT -> skipToNext()
        }
        
        return START_STICKY
    }

    /**
     * Start simple timer
     */
    private fun startSimpleTimer(config: TimerConfig, presetId: String?, presetName: String, exerciseName: String?) {
        com.hiittimer.app.utils.Logger.d(com.hiittimer.app.error.ErrorHandler.ErrorCategory.TIMER_OPERATION, 
            "UnifiedTimerService: Starting simple timer")
        
        // Start timer
        unifiedTimerManager.startSimpleWorkout(config, presetId, presetName, exerciseName)
        
        // Enter foreground mode
        enterForegroundMode()
    }
    
    /**
     * Start complex workout by ID
     */
    private fun startComplexWorkoutById(workoutId: String) {
        serviceScope.launch {
            try {
                unifiedTimerManager.startComplexWorkout(workoutId)
                enterForegroundMode()
            } catch (e: Exception) {
                com.hiittimer.app.utils.Logger.e(com.hiittimer.app.error.ErrorHandler.ErrorCategory.TIMER_OPERATION, 
                    "Failed to start complex workout", e)
                stopSelf()
            }
        }
    }
    
    /**
     * Start complex workout directly
     */
    private fun startComplexWorkout(workout: ComplexWorkout) {
        com.hiittimer.app.utils.Logger.d(com.hiittimer.app.error.ErrorHandler.ErrorCategory.TIMER_OPERATION, 
            "UnifiedTimerService: Starting complex workout: ${workout.name}")
        
        unifiedTimerManager.startComplexWorkoutDirect(workout)
        enterForegroundMode()
    }
    
    /**
     * Enter foreground mode with notification
     */
    private fun enterForegroundMode() {
        val state = unifiedTimerManager.unifiedState.value
        val notification = notificationManager.createTimerNotification(
            state = state,
            workoutMode = unifiedTimerManager.workoutMode.value
        )
        startForeground(NOTIFICATION_ID, notification)
        
        // Acquire wake lock if needed
        if (performanceManager.shouldUseWakeLock()) {
            wakeLockManager.acquireWakeLock()
        }
        
        // Start monitoring timer status
        startTimerStatusMonitoring()
    }

    /**
     * Pause timer
     */
    private fun pauseTimer() {
        com.hiittimer.app.utils.Logger.d(com.hiittimer.app.error.ErrorHandler.ErrorCategory.TIMER_OPERATION, 
            "UnifiedTimerService: Pausing timer")
        
        unifiedTimerManager.pause()
        wakeLockManager.releaseWakeLock()
        updateNotification()
    }

    /**
     * Resume timer
     */
    private fun resumeTimer() {
        com.hiittimer.app.utils.Logger.d(com.hiittimer.app.error.ErrorHandler.ErrorCategory.TIMER_OPERATION, 
            "UnifiedTimerService: Resuming timer")
        
        unifiedTimerManager.resume()
        
        if (performanceManager.shouldUseWakeLock()) {
            wakeLockManager.acquireWakeLock()
        }
        
        updateNotification()
    }

    /**
     * Stop timer and exit foreground mode
     */
    private fun stopTimer() {
        com.hiittimer.app.utils.Logger.d(com.hiittimer.app.error.ErrorHandler.ErrorCategory.TIMER_OPERATION, 
            "UnifiedTimerService: Stopping timer")
        
        unifiedTimerManager.reset()
        exitForegroundMode()
    }

    /**
     * Reset timer
     */
    private fun resetTimer() {
        com.hiittimer.app.utils.Logger.d(com.hiittimer.app.error.ErrorHandler.ErrorCategory.TIMER_OPERATION, 
            "UnifiedTimerService: Resetting timer")
        
        unifiedTimerManager.reset()
        exitForegroundMode()
    }
    
    /**
     * Mark rep completed (for complex workouts)
     */
    private fun markRepCompleted() {
        unifiedTimerManager.markRepCompleted()
        updateNotification()
    }
    
    /**
     * Skip to next exercise (for complex workouts)
     */
    private fun skipToNext() {
        unifiedTimerManager.skipToNext()
        updateNotification()
    }
    
    /**
     * Exit foreground mode
     */
    private fun exitForegroundMode() {
        wakeLockManager.releaseWakeLock()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    /**
     * Start monitoring timer status for notification updates
     */
    private fun startTimerStatusMonitoring() {
        statusMonitoringJob?.cancel()
        statusMonitoringJob = serviceScope.launch {
            unifiedTimerManager.unifiedState.collect { state ->
                updateNotification()

                // Handle timer completion
                if (state.timerState == TimerState.FINISHED) {
                    wakeLockManager.releaseWakeLock()
                    // Keep notification for a few seconds to show completion
                    delay(com.hiittimer.app.utils.Constants.NOTIFICATION_DELAY_AFTER_FINISH_MS)
                    exitForegroundMode()
                }
            }
        }
    }

    /**
     * Update notification with current timer status
     */
    private fun updateNotification() {
        val state = unifiedTimerManager.unifiedState.value
        val notification = notificationManager.createTimerNotification(
            state = state,
            workoutMode = unifiedTimerManager.workoutMode.value
        )
        notificationManager.updateNotification(NOTIFICATION_ID, notification)
    }
    
    /**
     * Extract TimerConfig from intent
     */
    private fun getTimerConfig(intent: Intent): TimerConfig? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_TIMER_CONFIG, TimerConfig::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_TIMER_CONFIG) as? TimerConfig
        }
    }
    
    /**
     * Extract ComplexWorkout from intent
     */
    private fun getComplexWorkout(intent: Intent): ComplexWorkout? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_COMPLEX_WORKOUT, ComplexWorkout::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_COMPLEX_WORKOUT)
        }
    }

    /**
     * Get unified timer state for UI binding
     */
    fun getUnifiedState(): StateFlow<UnifiedTimerState> = unifiedTimerManager.unifiedState
    
    /**
     * Get workout mode for UI binding
     */
    fun getWorkoutMode(): StateFlow<UnifiedTimerManager.WorkoutMode> = unifiedTimerManager.workoutMode

    /**
     * Get audio manager for UI binding
     */
    fun getAudioManager(): AudioManager = audioManager

    /**
     * Get workout history repository for UI binding
     */
    fun getWorkoutHistoryRepository(): WorkoutHistoryRepository? = workoutHistoryRepository
    
    /**
     * Get complex workout repository for UI binding
     */
    fun getComplexWorkoutRepository(): ComplexWorkoutRepository? = complexWorkoutRepository

    /**
     * Update timer configuration
     */
    fun updateConfig(config: TimerConfig) {
        unifiedTimerManager.updateConfig(config)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Clean up resources
        statusMonitoringJob?.cancel()
        serviceScope.cancel()
        wakeLockManager.releaseWakeLock()
        performanceManager.cleanup()
        unifiedTimerManager.cleanup()
        audioManager.cleanup()
    }
}