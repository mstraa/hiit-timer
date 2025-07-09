package com.hiittimer.app.performance

import android.content.Context
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Performance manager for optimizing app performance and battery usage (TS-003, TS-004)
 * Handles CPU optimization, memory management, and battery-aware operations
 */
class PerformanceManager(private val context: Context) {

    companion object {
        private const val TAG = "PerformanceManager"
        private const val MEMORY_CLEANUP_INTERVAL_MS = 30000L // 30 seconds
        private const val PERFORMANCE_MONITORING_INTERVAL_MS = 5000L // 5 seconds
    }

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val performanceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private var memoryCleanupJob: Job? = null
    private var performanceMonitoringJob: Job? = null
    
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    private val _batteryOptimizationEnabled = MutableStateFlow(false)
    val batteryOptimizationEnabled: StateFlow<Boolean> = _batteryOptimizationEnabled.asStateFlow()

    /**
     * Data class for performance metrics
     */
    data class PerformanceMetrics(
        val memoryUsageMB: Long = 0,
        val cpuUsagePercent: Float = 0f,
        val batteryLevel: Int = 100,
        val isLowPowerMode: Boolean = false,
        val timerAccuracyMs: Long = 0,
        val lastUpdateTime: Long = System.currentTimeMillis()
    )

    /**
     * Initialize performance monitoring
     */
    fun initialize() {
        checkBatteryOptimization()
        startPerformanceMonitoring()
        startMemoryCleanup()
    }

    /**
     * Check if battery optimization is enabled
     */
    private fun checkBatteryOptimization() {
        val isLowPowerMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager.isPowerSaveMode
        } else {
            false
        }
        _batteryOptimizationEnabled.value = isLowPowerMode
    }

    /**
     * Start performance monitoring
     */
    private fun startPerformanceMonitoring() {
        performanceMonitoringJob?.cancel()
        performanceMonitoringJob = performanceScope.launch {
            while (isActive) {
                updatePerformanceMetrics()
                delay(PERFORMANCE_MONITORING_INTERVAL_MS)
            }
        }
    }

    /**
     * Update performance metrics
     */
    private fun updatePerformanceMetrics() {
        try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val memoryUsageMB = usedMemory / (1024 * 1024)
            
            val isLowPowerMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                powerManager.isPowerSaveMode
            } else {
                false
            }
            
            _performanceMetrics.value = PerformanceMetrics(
                memoryUsageMB = memoryUsageMB,
                cpuUsagePercent = 0f, // CPU usage is complex to measure accurately
                batteryLevel = 100, // Would need BatteryManager for real battery level
                isLowPowerMode = isLowPowerMode,
                timerAccuracyMs = 0, // Would be updated by timer manager
                lastUpdateTime = System.currentTimeMillis()
            )
            
            _batteryOptimizationEnabled.value = isLowPowerMode
        } catch (e: Exception) {
            // Handle performance monitoring errors gracefully
        }
    }

    /**
     * Start periodic memory cleanup
     */
    private fun startMemoryCleanup() {
        memoryCleanupJob?.cancel()
        memoryCleanupJob = performanceScope.launch {
            while (isActive) {
                delay(MEMORY_CLEANUP_INTERVAL_MS)
                performMemoryCleanup()
            }
        }
    }

    /**
     * Perform memory cleanup
     */
    private fun performMemoryCleanup() {
        try {
            // Suggest garbage collection (not guaranteed)
            System.gc()
            
            // Update metrics after cleanup
            updatePerformanceMetrics()
        } catch (e: Exception) {
            // Handle cleanup errors gracefully
        }
    }

    /**
     * Optimize timer precision based on battery state
     */
    fun getOptimalTimerInterval(): Long {
        return if (_batteryOptimizationEnabled.value) {
            // Reduce precision in low power mode to save battery
            200L // 200ms intervals instead of 100ms
        } else {
            // Normal precision for accurate timing
            100L // 100ms intervals
        }
    }

    /**
     * Check if background processing should be limited
     */
    fun shouldLimitBackgroundProcessing(): Boolean {
        val metrics = _performanceMetrics.value
        return metrics.isLowPowerMode || metrics.memoryUsageMB > 100 // Limit if using >100MB
    }

    /**
     * Get recommended notification update frequency
     */
    fun getNotificationUpdateInterval(): Long {
        return if (_batteryOptimizationEnabled.value) {
            // Update notifications less frequently in low power mode
            2000L // 2 seconds
        } else {
            // Normal update frequency
            1000L // 1 second
        }
    }

    /**
     * Optimize coroutine dispatcher based on performance
     */
    fun getOptimalDispatcher(): CoroutineDispatcher {
        return if (shouldLimitBackgroundProcessing()) {
            // Use single thread dispatcher to reduce CPU usage
            Dispatchers.Default.limitedParallelism(1)
        } else {
            // Use default dispatcher for normal performance
            Dispatchers.Default
        }
    }

    /**
     * Check if wake lock should be used
     */
    fun shouldUseWakeLock(): Boolean {
        val metrics = _performanceMetrics.value
        // Don't use wake lock in low power mode or when battery is very low
        return !metrics.isLowPowerMode && metrics.batteryLevel > 20
    }

    /**
     * Get memory usage information
     */
    fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        return MemoryInfo(
            maxMemoryMB = maxMemory / (1024 * 1024),
            totalMemoryMB = totalMemory / (1024 * 1024),
            usedMemoryMB = usedMemory / (1024 * 1024),
            freeMemoryMB = freeMemory / (1024 * 1024),
            usagePercentage = (usedMemory.toFloat() / maxMemory.toFloat()) * 100f
        )
    }

    /**
     * Data class for memory information
     */
    data class MemoryInfo(
        val maxMemoryMB: Long,
        val totalMemoryMB: Long,
        val usedMemoryMB: Long,
        val freeMemoryMB: Long,
        val usagePercentage: Float
    )

    /**
     * Force memory cleanup
     */
    fun forceMemoryCleanup() {
        performanceScope.launch {
            performMemoryCleanup()
        }
    }

    /**
     * Update timer accuracy metric
     */
    fun updateTimerAccuracy(accuracyMs: Long) {
        val currentMetrics = _performanceMetrics.value
        _performanceMetrics.value = currentMetrics.copy(
            timerAccuracyMs = accuracyMs,
            lastUpdateTime = System.currentTimeMillis()
        )
    }

    /**
     * Check if performance is within acceptable limits
     */
    fun isPerformanceAcceptable(): Boolean {
        val metrics = _performanceMetrics.value
        return metrics.memoryUsageMB < 150 && // Less than 150MB memory usage
               metrics.timerAccuracyMs < 100    // Timer accuracy within 100ms
    }

    /**
     * Get performance recommendations
     */
    fun getPerformanceRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val metrics = _performanceMetrics.value
        val memoryInfo = getMemoryInfo()
        
        if (metrics.isLowPowerMode) {
            recommendations.add("Low power mode detected - timer precision reduced to save battery")
        }
        
        if (memoryInfo.usagePercentage > 80) {
            recommendations.add("High memory usage detected - consider closing other apps")
        }
        
        if (metrics.timerAccuracyMs > 50) {
            recommendations.add("Timer accuracy reduced - device may be under heavy load")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Performance is optimal")
        }
        
        return recommendations
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        memoryCleanupJob?.cancel()
        performanceMonitoringJob?.cancel()
        performanceScope.cancel()
    }
}
