package com.hiittimer.app.timer

import com.hiittimer.app.audio.AudioManager
import com.hiittimer.app.data.*
import com.hiittimer.app.error.ErrorHandler
import com.hiittimer.app.utils.Logger
import com.hiittimer.app.performance.PerformanceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Extended TimerManager that supports complex presets with multiple round groups
 * Maintains backward compatibility with simple timer configurations
 */
class ComplexTimerManager(
    audioManager: AudioManager? = null,
    workoutHistoryRepository: WorkoutHistoryRepository? = null,
    performanceManager: PerformanceManager? = null
) {
    // Delegate to base TimerManager for core functionality
    private val baseManager = TimerManager(audioManager, workoutHistoryRepository, performanceManager)
    
    // Store references to dependencies
    private val audioManager = audioManager
    
    // Access to mutable state for updates
    private val _timerStatus = MutableStateFlow(baseManager.timerStatus.value)
    
    // Expose timer status - uses our managed state when complex timer is active
    val timerStatus: StateFlow<TimerStatus> get() = _timerStatus.asStateFlow()
    
    // Complex timer state
    private var complexConfig: ComplexTimerConfig? = null
    private var timerSegments: List<TimerSegment> = emptyList()
    private var currentSegmentIndex: Int = 0
    private var complexTimerState: ComplexTimerState? = null
    
    private val scope = CoroutineScope(
        Dispatchers.Main +
        SupervisorJob() +
        CoroutineName("ComplexTimerManager")
    )
    
    private var timerJob: Job? = null
    
    init {
        // Keep our local state in sync with base manager
        scope.launch {
            baseManager.timerStatus.collect { status ->
                if (!isComplexTimerActive()) {
                    _timerStatus.value = status
                }
            }
        }
    }
    
    private fun isComplexTimerActive(): Boolean {
        return timerSegments.isNotEmpty() && complexConfig != null && timerJob?.isActive == true
    }
    
    private fun updateTimerStatus(status: TimerStatus) {
        _timerStatus.value = status
    }
    
    // Update configuration with simple parameters
    fun updateConfig(
        workTimeSeconds: Int,
        restTimeSeconds: Int,
        totalRounds: Int,
        isUnlimited: Boolean,
        noRest: Boolean,
        countdownDurationSeconds: Int
    ) {
        val config = TimerConfig(
            workTimeSeconds = workTimeSeconds,
            restTimeSeconds = restTimeSeconds,
            totalRounds = totalRounds,
            isUnlimited = isUnlimited,
            noRest = noRest,
            countdownDurationSeconds = countdownDurationSeconds
        )
        
        baseManager.updateConfig(config)
        
        // Clear complex config when updating with simple config
        complexConfig = null
        timerSegments = emptyList()
        complexTimerState = null
    }
    
    // Alternative method that takes TimerConfig directly
    fun updateConfig(config: TimerConfig) {
        baseManager.updateConfig(config)
        
        // Clear complex config when updating with simple config
        complexConfig = null
        timerSegments = emptyList()
        complexTimerState = null
    }
    
    /**
     * Update configuration with a complex preset
     */
    fun updateComplexConfig(preset: ComplexPreset) {
        // Create complex config
        complexConfig = ComplexTimerConfig(preset)
        
        // Update base config for compatibility
        val simpleConfig = preset.toSimplePreset().toTimerConfig()
        baseManager.updateConfig(simpleConfig)
        
        // Generate timer segments
        timerSegments = complexConfig!!.toTimerSegments()
        currentSegmentIndex = 0
        
        // Initialize complex timer state
        complexTimerState = ComplexTimerState(
            baseState = timerStatus.value,
            complexPreset = preset,
            currentGroupIndex = 0,
            currentRoundInGroup = 1,
            currentPhaseIndex = 0
        )
        
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, 
            "Updated complex config: ${preset.name} with ${timerSegments.size} segments")
    }
    
    /**
     * Update configuration with a simple preset (maintains compatibility)
     */
    fun updateSimpleConfig(preset: Preset) {
        if (preset.isComplex && preset.complexPresetId != null) {
            Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION,
                "Simple preset references complex preset but complex data not provided")
        }
        
        updateConfig(
            workTimeSeconds = preset.workTimeSeconds,
            restTimeSeconds = preset.restTimeSeconds,
            totalRounds = preset.totalRounds,
            isUnlimited = preset.isUnlimited,
            noRest = preset.noRest,
            countdownDurationSeconds = 5 // Default countdown
        )
    }
    
    fun start(config: TimerConfig, presetId: String? = null, presetName: String = "Custom Workout", exerciseName: String? = null) {
        // If we have timer segments, use complex logic
        if (timerSegments.isNotEmpty() && complexConfig != null) {
            startComplexTimer()
        } else {
            baseManager.start(config, presetId, presetName, exerciseName)
        }
    }
    
    fun pause() = baseManager.pause()
    fun resume() = baseManager.resume()
    
    /**
     * Start complex timer with segments
     */
    private fun startComplexTimer() {
        val currentState = timerStatus.value.state
        
        if (!timerStatus.value.canStart) {
            Logger.w(ErrorHandler.ErrorCategory.TIMER_OPERATION,
                "Cannot start timer from state: $currentState")
            return
        }
        
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Starting complex timer")
        
        // Reset segment tracking
        currentSegmentIndex = 0
        
        // Initialize complex state
        complexConfig?.complexPreset?.let { preset ->
            complexTimerState = ComplexTimerState(
                baseState = timerStatus.value,
                complexPreset = preset,
                currentGroupIndex = 0,
                currentRoundInGroup = 1,
                currentPhaseIndex = 0
            )
        }
        
        // Start with BEGIN state (countdown)
        // For complex timer, we handle countdown ourselves
        startComplexBeginCountdown()
    }
    
    /**
     * Start countdown for complex timer
     */
    private fun startComplexBeginCountdown() {
        val countdownDuration = timerStatus.value.config.countdownDurationSeconds
        
        timerJob = scope.launch {
            for (i in countdownDuration downTo 1) {
                if (!isActive) break
                
                // Update status for countdown
                val newStatus = timerStatus.value.copy(
                    state = TimerState.BEGIN,
                    timeRemainingSeconds = i,
                    timeRemainingMilliseconds = 0,
                    countdownText = "Start in $i",
                    shouldFlashBlue = false
                )
                updateTimerStatus(newStatus)
                
                delay(1000)
            }
            
            if (isActive) {
                // Show "GO!" briefly
                updateTimerStatus(timerStatus.value.copy(
                    timeRemainingSeconds = 0,
                    countdownText = "GO!",
                    shouldFlashBlue = true
                ))
                
                delay(500)
                
                // Start main timer
                startSegmentTimer()
            }
        }
    }
    
    /**
     * Run timer for current segment
     */
    private fun startSegmentTimer() {
        if (currentSegmentIndex >= timerSegments.size) {
            // All segments completed
            transitionToFinished()
            return
        }
        
        val segment = timerSegments[currentSegmentIndex]
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION,
            "Starting segment $currentSegmentIndex: ${segment.name} (${segment.duration}s)")
        
        // Update complex state based on segment
        updateComplexStateForSegment(segment)
        
        // Create timer status for this segment
        val intervalType = when (segment.type) {
            SegmentType.WORK -> IntervalType.WORK
            else -> IntervalType.REST
        }
        
        // Update timer status with segment info
        _timerStatus.value = TimerStatus(
            state = TimerState.RUNNING,
            currentInterval = intervalType,
            timeRemainingSeconds = segment.duration,
            timeRemainingMilliseconds = 0,
            currentRound = complexTimerState?.getCompletedIntervals() ?: 1,
            config = timerStatus.value.config
        )
        
        // Start countdown for this segment
        timerJob = scope.launch {
            var remainingMs = segment.duration * 1000L
            val startTime = System.currentTimeMillis()
            
            while (remainingMs > 0 && isActive) {
                delay(50) // Update every 50ms
                
                val elapsed = System.currentTimeMillis() - startTime
                remainingMs = (segment.duration * 1000L) - elapsed
                
                if (remainingMs > 0) {
                    val seconds = (remainingMs / 1000).toInt()
                    val millis = (remainingMs % 1000).toInt()
                    
                    updateTimerStatus(timerStatus.value.copy(
                        timeRemainingSeconds = seconds,
                        timeRemainingMilliseconds = millis
                    ))
                }
            }
            
            if (isActive) {
                // Segment completed - move to next
                onSegmentCompleted()
            }
        }
    }
    
    /**
     * Update complex state based on current segment
     */
    private fun updateComplexStateForSegment(segment: TimerSegment) {
        if (complexConfig?.complexPreset == null) return
        
        val groupIndex = segment.groupIndex ?: 0
        val round = segment.round ?: 1
        val phaseIndex = segment.phaseIndex ?: 0
        
        complexTimerState = ComplexTimerState(
            baseState = timerStatus.value,
            complexPreset = complexConfig!!.complexPreset,
            currentGroupIndex = groupIndex,
            currentRoundInGroup = round,
            currentPhaseIndex = phaseIndex,
            isInSpecialRest = segment.type == SegmentType.SPECIAL_REST,
            isInPhaseRest = segment.type == SegmentType.PHASE_REST,
            isInRoundRest = segment.type == SegmentType.ROUND_REST
        )
    }
    
    /**
     * Handle segment completion
     */
    private fun onSegmentCompleted() {
        val segment = timerSegments[currentSegmentIndex]
        
        // Play appropriate sound
        when (segment.type) {
            SegmentType.WORK -> audioManager?.playWorkIntervalSound()
            else -> audioManager?.playRestIntervalSound()
        }
        
        // Move to next segment
        currentSegmentIndex++
        
        if (currentSegmentIndex < timerSegments.size) {
            // Continue with next segment
            startSegmentTimer()
        } else {
            // All segments completed
            transitionToFinished()
        }
    }
    
    private fun transitionToFinished() {
        timerJob?.cancel()
        
        updateTimerStatus(timerStatus.value.copy(
            state = TimerState.FINISHED,
            timeRemainingSeconds = 0,
            timeRemainingMilliseconds = 0
        ))
        
        // Play finish sound
        audioManager?.playCompletionSound()
        
        Logger.d(ErrorHandler.ErrorCategory.TIMER_OPERATION, "Complex workout completed")
    }
    
    /**
     * Get enhanced timer status with complex state information
     */
    fun getEnhancedStatus(): Pair<TimerStatus, ComplexTimerState?> {
        return Pair(timerStatus.value, complexTimerState)
    }
    
    /**
     * Get current activity name (work phase name or generic)
     */
    fun getCurrentActivityName(): String {
        return complexTimerState?.getCurrentActivityName() 
            ?: when (timerStatus.value.currentInterval) {
                IntervalType.WORK -> "Work"
                IntervalType.REST -> "Rest"
            }
    }
    
    /**
     * Get enhanced round/group progress text
     */
    fun getEnhancedProgressText(): String {
        return complexTimerState?.getGroupRoundText() 
            ?: timerStatus.value.getRoundProgressText()
    }
    
    /**
     * Get enhanced next preview
     */
    fun getEnhancedNextPreview(): String? {
        return complexTimerState?.getNextActivityPreview()
            ?: timerStatus.value.getNextIntervalPreview()
    }
    
    fun reset() {
        // Cancel any active timer
        timerJob?.cancel()
        
        // Reset base manager
        baseManager.reset()
        
        // Reset complex state
        currentSegmentIndex = 0
        complexTimerState = null
        
        // Regenerate segments if we have complex config
        complexConfig?.let {
            timerSegments = it.toTimerSegments()
        }
    }
    
    fun cleanup() {
        timerJob?.cancel()
        scope.cancel()
    }
}