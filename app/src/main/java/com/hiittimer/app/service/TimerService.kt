package com.hiittimer.app.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.data.*
import com.hiittimer.app.performance.PerformanceManager
import com.hiittimer.app.timer.TimerManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Foreground service for background timer execution (TS-005: Platform Integration)
 * Ensures timer continues running when app is backgrounded or screen is off
 */
class TimerService : Service() {

    companion object {
        const val ACTION_START_TIMER = "com.hiittimer.app.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.hiittimer.app.PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "com.hiittimer.app.RESUME_TIMER"
        const val ACTION_STOP_TIMER = "com.hiittimer.app.STOP_TIMER"
        const val ACTION_RESET_TIMER = "com.hiittimer.app.RESET_TIMER"
        
        const val EXTRA_TIMER_CONFIG = "timer_config"
        const val EXTRA_PRESET_ID = "preset_id"
        const val EXTRA_PRESET_NAME = "preset_name"
        const val EXTRA_EXERCISE_NAME = "exercise_name"
        
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_CHANNEL_ID = "hiit_timer_channel"
    }

    private val binder = TimerServiceBinder()
    private lateinit var timerManager: TimerManager
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: TimerNotificationManager
    private lateinit var wakeLockManager: WakeLockManager
    private lateinit var performanceManager: PerformanceManager
    private var workoutHistoryRepository: WorkoutHistoryRepository? = null
    private var statusMonitoringJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Binder class for service communication
     */
    inner class TimerServiceBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize managers with current audio settings
        val preferencesManager = com.hiittimer.app.data.PreferencesManager(this)
        audioManager = AudioManager(this, preferencesManager.audioSettings.value)
        workoutHistoryRepository = InMemoryWorkoutHistoryRepository()
        performanceManager = PerformanceManager(this)
        timerManager = TimerManager(audioManager, workoutHistoryRepository, performanceManager)
        notificationManager = TimerNotificationManager(this, performanceManager)
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
            ACTION_START_TIMER -> {
                val config = intent.getSerializableExtra(EXTRA_TIMER_CONFIG) as? TimerConfig
                val presetId = intent.getStringExtra(EXTRA_PRESET_ID)
                val presetName = intent.getStringExtra(EXTRA_PRESET_NAME) ?: "Custom Workout"
                val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME)
                
                if (config != null) {
                    startTimer(config, presetId, presetName, exerciseName)
                }
            }
            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_RESUME_TIMER -> resumeTimer()
            ACTION_STOP_TIMER -> stopTimer()
            ACTION_RESET_TIMER -> resetTimer()
        }
        
        return START_STICKY // Restart service if killed by system
    }

    /**
     * Start timer and enter foreground mode
     */
    private fun startTimer(config: TimerConfig, presetId: String?, presetName: String, exerciseName: String?) {
        // Start foreground service with notification
        val notification = notificationManager.createTimerNotification(
            timerStatus = timerManager.timerStatus.value,
            isRunning = true
        )
        startForeground(NOTIFICATION_ID, notification)
        
        // Acquire wake lock to keep screen on (only if performance allows)
        if (performanceManager.shouldUseWakeLock()) {
            wakeLockManager.acquireWakeLock()
        }
        
        // Start timer
        timerManager.start(config, presetId, presetName, exerciseName)
        
        // Start monitoring timer status for notification updates
        startTimerStatusMonitoring()
    }

    /**
     * Pause timer
     */
    private fun pauseTimer() {
        timerManager.pause()
        
        // Release wake lock when paused
        wakeLockManager.releaseWakeLock()
        
        // Update notification
        updateNotification()
    }

    /**
     * Resume timer
     */
    private fun resumeTimer() {
        timerManager.resume()
        
        // Re-acquire wake lock (only if performance allows)
        if (performanceManager.shouldUseWakeLock()) {
            wakeLockManager.acquireWakeLock()
        }
        
        // Update notification
        updateNotification()
    }

    /**
     * Stop timer and exit foreground mode
     */
    private fun stopTimer() {
        timerManager.reset()
        wakeLockManager.releaseWakeLock()
        stopForeground(true)
        stopSelf()
    }

    /**
     * Reset timer
     */
    private fun resetTimer() {
        timerManager.reset()
        wakeLockManager.releaseWakeLock()
        stopForeground(true)
        stopSelf()
    }

    /**
     * Start monitoring timer status for notification updates
     */
    private fun startTimerStatusMonitoring() {
        statusMonitoringJob?.cancel()
        statusMonitoringJob = serviceScope.launch {
            timerManager.timerStatus.collect { status ->
                updateNotification()

                // Handle timer completion
                if (status.state == TimerState.FINISHED) {
                    wakeLockManager.releaseWakeLock()
                    // Keep notification for a few seconds to show completion
                    delay(5000)
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
    }

    /**
     * Update notification with current timer status
     */
    private fun updateNotification() {
        val notification = notificationManager.createTimerNotification(
            timerStatus = timerManager.timerStatus.value,
            isRunning = timerManager.timerStatus.value.state == TimerState.RUNNING
        )
        notificationManager.updateNotification(NOTIFICATION_ID, notification)
    }

    /**
     * Get timer status for UI binding
     */
    fun getTimerStatus(): StateFlow<TimerStatus> = timerManager.timerStatus

    /**
     * Get audio manager for UI binding
     */
    fun getAudioManager(): AudioManager = audioManager

    /**
     * Get workout history repository for UI binding
     */
    fun getWorkoutHistoryRepository(): WorkoutHistoryRepository? = workoutHistoryRepository

    /**
     * Update timer configuration
     */
    fun updateConfig(config: TimerConfig) {
        timerManager.updateConfig(config)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Clean up resources
        statusMonitoringJob?.cancel()
        serviceScope.cancel()
        wakeLockManager.releaseWakeLock()
        performanceManager.cleanup()
        timerManager.cleanup()
        audioManager.cleanup()
    }
}
