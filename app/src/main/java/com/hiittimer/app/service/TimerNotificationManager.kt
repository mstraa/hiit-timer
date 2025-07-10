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
import com.hiittimer.app.data.IntervalType
import com.hiittimer.app.data.TimerState
import com.hiittimer.app.data.TimerStatus
import com.hiittimer.app.performance.PerformanceManager

/**
 * Manager for timer notifications during background operation (TS-005: Platform Integration)
 * Provides persistent notification with timer status and controls
 * Enhanced with performance optimization (TS-003, TS-004)
 */
class TimerNotificationManager(
    private val context: Context,
    private val performanceManager: PerformanceManager? = null
) {

    companion object {
        private const val CHANNEL_ID = "hiit_timer_channel"
        private const val CHANNEL_NAME = "HIIT Timer"
        private const val CHANNEL_DESCRIPTION = "Notifications for HIIT timer background operation"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Create notification channel for Android 8.0+ (API 26+)
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low importance to avoid interrupting user
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
                setSound(null, null) // No sound for timer notifications
                enableVibration(false) // No vibration for timer notifications
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create timer notification with current status and controls
     */
    fun createTimerNotification(timerStatus: TimerStatus, @Suppress("UNUSED_PARAMETER") isRunning: Boolean): Notification {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification content
        val title = getNotificationTitle(timerStatus)
        val content = getNotificationContent(timerStatus)
        val smallIcon = if (timerStatus.currentInterval == IntervalType.WORK) {
            android.R.drawable.ic_media_play // Work interval
        } else {
            android.R.drawable.ic_media_pause // Rest interval
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(smallIcon)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true) // Cannot be dismissed by user
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Add action buttons based on timer state
        addNotificationActions(builder, timerStatus)

        return builder.build()
    }

    /**
     * Get notification title based on timer status
     */
    private fun getNotificationTitle(timerStatus: TimerStatus): String {
        return when (timerStatus.state) {
            TimerState.BEGIN -> "Starting Workout..."
            TimerState.RUNNING -> {
                val intervalText = if (timerStatus.currentInterval == IntervalType.WORK) "Work" else "Rest"
                "$intervalText - Round ${timerStatus.currentRound}"
            }
            TimerState.PAUSED -> "Timer Paused"
            TimerState.FINISHED -> "Workout Complete!"
            TimerState.STOPPED -> "HIIT Timer"
        }
    }

    /**
     * Get notification content based on timer status
     */
    private fun getNotificationContent(timerStatus: TimerStatus): String {
        return when (timerStatus.state) {
            TimerState.BEGIN -> {
                timerStatus.countdownText ?: "Get ready to start!"
            }
            TimerState.RUNNING, TimerState.PAUSED -> {
                val timeText = formatTime(timerStatus.timeRemainingSeconds)
                val roundText = if (timerStatus.config.isUnlimited) {
                    "Round ${timerStatus.currentRound}"
                } else {
                    "Round ${timerStatus.currentRound} of ${timerStatus.config.totalRounds}"
                }
                "$timeText remaining • $roundText"
            }
            TimerState.FINISHED -> "Great job! Workout completed successfully."
            TimerState.STOPPED -> "Ready to start your workout"
        }
    }

    /**
     * Add action buttons to notification
     */
    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        timerStatus: TimerStatus
    ) {
        when (timerStatus.state) {
            TimerState.BEGIN -> {
                // No actions during countdown - user should wait
            }
            TimerState.RUNNING -> {
                // Add pause action
                val pauseIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_PAUSE_TIMER
                }
                val pausePendingIntent = PendingIntent.getService(
                    context, 1, pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    pausePendingIntent
                )

                // Add stop action
                val stopIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP_TIMER
                }
                val stopPendingIntent = PendingIntent.getService(
                    context, 2, stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop",
                    stopPendingIntent
                )
            }
            TimerState.PAUSED -> {
                // Add resume action
                val resumeIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_RESUME_TIMER
                }
                val resumePendingIntent = PendingIntent.getService(
                    context, 3, resumeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_media_play,
                    "Resume",
                    resumePendingIntent
                )

                // Add stop action
                val stopIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP_TIMER
                }
                val stopPendingIntent = PendingIntent.getService(
                    context, 4, stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop",
                    stopPendingIntent
                )
            }
            TimerState.FINISHED -> {
                // Add reset action for new workout
                val resetIntent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_RESET_TIMER
                }
                val resetPendingIntent = PendingIntent.getService(
                    context, 5, resetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_menu_revert,
                    "New Workout",
                    resetPendingIntent
                )
            }
            TimerState.STOPPED -> {
                // No actions for idle state
            }
        }
    }

    /**
     * Format time for notification display
     */
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "${minutes}:${remainingSeconds.toString().padStart(2, '0')}"
        } else {
            "${remainingSeconds}s"
        }
    }

    /**
     * Update existing notification
     */
    fun updateNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Cancel notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
