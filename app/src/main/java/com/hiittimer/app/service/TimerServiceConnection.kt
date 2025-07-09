package com.hiittimer.app.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Service connection manager for TimerService integration (TS-005: Platform Integration)
 * Handles binding/unbinding and provides UI access to service functionality
 */
class TimerServiceConnection(
    private val context: Context
) : ServiceConnection {

    private var timerService: TimerService? = null
    private var isBound = false
    private val connectionScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _timerStatus = MutableStateFlow(TimerStatus())
    val timerStatus: StateFlow<TimerStatus> = _timerStatus.asStateFlow()

    /**
     * Bind to timer service
     */
    fun bindService() {
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    /**
     * Unbind from timer service
     */
    fun unbindService() {
        if (isBound) {
            context.unbindService(this)
            isBound = false
            _isServiceConnected.value = false
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as TimerService.TimerServiceBinder
        timerService = binder.getService()
        isBound = true
        _isServiceConnected.value = true

        // Start observing timer status from service
        timerService?.let { service ->
            connectionScope.launch {
                service.getTimerStatus().collect { status ->
                    _timerStatus.value = status
                }
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        timerService = null
        isBound = false
        _isServiceConnected.value = false
    }

    /**
     * Start timer with configuration
     */
    fun startTimer(config: TimerConfig, presetId: String? = null, presetName: String = "Custom Workout", exerciseName: String? = null) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_TIMER_CONFIG, config)
            putExtra(TimerService.EXTRA_PRESET_ID, presetId)
            putExtra(TimerService.EXTRA_PRESET_NAME, presetName)
            putExtra(TimerService.EXTRA_EXERCISE_NAME, exerciseName)
        }
        // Use startForegroundService for API 26+ and startService for older versions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Pause timer
     */
    fun pauseTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE_TIMER
        }
        context.startService(intent)
    }

    /**
     * Resume timer
     */
    fun resumeTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_RESUME_TIMER
        }
        context.startService(intent)
    }

    /**
     * Stop timer
     */
    fun stopTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP_TIMER
        }
        context.startService(intent)
    }

    /**
     * Reset timer
     */
    fun resetTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_RESET_TIMER
        }
        context.startService(intent)
    }

    /**
     * Get audio manager from service
     */
    fun getAudioManager(): AudioManager? {
        return timerService?.getAudioManager()
    }

    /**
     * Get workout history repository from service
     */
    fun getWorkoutHistoryRepository(): WorkoutHistoryRepository? {
        return timerService?.getWorkoutHistoryRepository()
    }

    /**
     * Check if service is currently bound
     */
    fun isBound(): Boolean = isBound

    /**
     * Clean up connection
     */
    fun cleanup() {
        connectionScope.cancel()
        unbindService()
    }
}
