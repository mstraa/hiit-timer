package com.hiittimer.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hiittimer.app.MainActivity
import com.hiittimer.app.R
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.performance.PerformanceManager
import com.hiittimer.app.timer.UnifiedTimerManager
import com.hiittimer.app.timer.UnifiedTimerState

/**
 * Manages notifications for both simple and complex workouts
 */
class UnifiedTimerNotificationManager(
    private val context: Context,
    private val performanceManager: PerformanceManager? = null
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = UnifiedTimerService.NOTIFICATION_CHANNEL_ID
        private const val CHANNEL_NAME = "Timer Notifications"
        private const val CHANNEL_DESCRIPTION = "Shows timer progress and controls"
    }

    /**
     * Create notification channel for Android O and above
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create notification for current timer state
     */
    fun createTimerNotification(
        state: UnifiedTimerState,
        workoutMode: UnifiedTimerManager.WorkoutMode
    ): Notification {
        // Main intent to open app
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        // Set title based on workout mode
        val title = when (workoutMode) {
            UnifiedTimerManager.WorkoutMode.SIMPLE -> state.statusText
            UnifiedTimerManager.WorkoutMode.COMPLEX -> {
                state.currentExerciseName ?: state.currentPhaseName ?: "Complex Workout"
            }
        }
        builder.setContentTitle(title)

        // Set content text based on state
        val contentText = when {
            state.timerState == TimerState.FINISHED -> "Workout Complete!"
            state.needsRepInput -> "Reps: ${state.displayTime}"
            workoutMode == UnifiedTimerManager.WorkoutMode.COMPLEX -> {
                "${state.displayTime} • ${state.progressText}"
            }
            else -> {
                "${state.displayTime} • ${state.progressText}"
            }
        }
        builder.setContentText(contentText)

        // Add progress bar for time-based exercises
        if (!state.needsRepInput && state.timeRemainingSeconds > 0) {
            when (workoutMode) {
                UnifiedTimerManager.WorkoutMode.SIMPLE -> {
                    // For simple workouts, we don't have total duration easily available
                    builder.setProgress(0, 0, true) // Indeterminate progress
                }
                UnifiedTimerManager.WorkoutMode.COMPLEX -> {
                    // For complex workouts, show phase progress
                    if (state.totalPhases > 0) {
                        builder.setProgress(
                            state.totalPhases,
                            state.currentPhaseIndex,
                            false
                        )
                    }
                }
            }
        }

        // Add actions based on state
        when {
            state.canPause -> {
                addAction(builder, "Pause", UnifiedTimerService.ACTION_PAUSE_TIMER)
                if (state.needsRepInput) {
                    addAction(builder, "Rep Done", UnifiedTimerService.ACTION_MARK_REP_COMPLETED)
                }
            }
            state.canResume -> {
                addAction(builder, "Resume", UnifiedTimerService.ACTION_RESUME_TIMER)
                addAction(builder, "Reset", UnifiedTimerService.ACTION_RESET_TIMER)
            }
            state.timerState == TimerState.FINISHED -> {
                addAction(builder, "Done", UnifiedTimerService.ACTION_STOP_TIMER)
            }
        }

        // Add skip action for complex workouts
        if (workoutMode == UnifiedTimerManager.WorkoutMode.COMPLEX && 
            state.isRunning && 
            state.currentExerciseName != null) {
            addAction(builder, "Skip", UnifiedTimerService.ACTION_SKIP_TO_NEXT)
        }

        // Performance optimization: Use low-detail notification if needed
        // Note: shouldReduceNotificationDetail is not available in PerformanceManager
        // This optimization can be added later if needed

        return builder.build()
    }

    /**
     * Add action to notification
     */
    private fun addAction(
        builder: NotificationCompat.Builder,
        title: String,
        action: String
    ) {
        val intent = Intent(context, UnifiedTimerService::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Use appropriate icon based on action
        val icon = when (action) {
            UnifiedTimerService.ACTION_PAUSE_TIMER -> android.R.drawable.ic_media_pause
            UnifiedTimerService.ACTION_RESUME_TIMER -> android.R.drawable.ic_media_play
            UnifiedTimerService.ACTION_RESET_TIMER -> android.R.drawable.ic_delete
            UnifiedTimerService.ACTION_MARK_REP_COMPLETED -> android.R.drawable.checkbox_on_background
            UnifiedTimerService.ACTION_SKIP_TO_NEXT -> android.R.drawable.ic_media_next
            else -> android.R.drawable.ic_media_play
        }
        
        builder.addAction(icon, title, pendingIntent)
    }

    /**
     * Update existing notification
     */
    fun updateNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }
}