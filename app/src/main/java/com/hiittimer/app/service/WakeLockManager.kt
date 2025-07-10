package com.hiittimer.app.service

import android.content.Context
import android.os.PowerManager
import android.util.Log

/**
 * Manager for wake lock functionality to keep screen on during workouts (TS-005: Platform Integration)
 * Prevents screen from turning off during active timer sessions
 */
class WakeLockManager(private val context: Context) {

    companion object {
        private const val TAG = "WakeLockManager"
        private const val WAKE_LOCK_TAG = "HIITTimer:WorkoutWakeLock"
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    /**
     * Acquire wake lock to keep screen on during workout
     */
    fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld != true) {
                @Suppress("DEPRECATION")
                wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                    WAKE_LOCK_TAG
                ).apply {
                    acquire(10 * 60 * 1000L) // 10 minutes timeout for safety
                }
                Log.d(TAG, "Wake lock acquired")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock", e)
        }
    }

    /**
     * Release wake lock to allow screen to turn off
     */
    fun releaseWakeLock() {
        try {
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                    Log.d(TAG, "Wake lock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wake lock", e)
        }
    }

    /**
     * Check if wake lock is currently held
     */
    fun isWakeLockHeld(): Boolean {
        return wakeLock?.isHeld == true
    }

    /**
     * Clean up wake lock on destruction
     */
    fun cleanup() {
        releaseWakeLock()
    }
}
