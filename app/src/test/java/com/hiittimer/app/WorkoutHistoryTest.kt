package com.hiittimer.app

import com.hiittimer.app.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for workout history functionality (FR-010, FR-011, FR-012)
 */
class WorkoutHistoryTest {

    @Test
    fun `workout session creation from timer config works correctly`() {
        val config = TimerConfig(
            workTimeSeconds = 30,
            restTimeSeconds = 15,
            totalRounds = 5,
            isUnlimited = false,
            noRest = false
        )
        
        val startTime = System.currentTimeMillis() - 300000 // 5 minutes ago
        val endTime = System.currentTimeMillis()
        
        val session = WorkoutSession.fromTimerSession(
            config = config,
            presetId = "test-preset",
            presetName = "Test Workout",
            exerciseName = "Test Exercise",
            completedRounds = 4,
            startTime = startTime,
            endTime = endTime,
            actualWorkTimeMs = 120000, // 2 minutes
            actualRestTimeMs = 60000   // 1 minute
        )
        
        assertEquals("Test Workout", session.presetName)
        assertEquals("Test Exercise", session.exerciseName)
        assertEquals(30, session.workTimeSeconds)
        assertEquals(15, session.restTimeSeconds)
        assertEquals(5, session.plannedRounds)
        assertEquals(4, session.completedRounds)
        assertEquals(80f, session.completionPercentage) // 4/5 * 100
        assertTrue(session.isCompleted) // 80% >= 70%
        assertEquals(endTime - startTime, session.totalDurationMs)
        assertEquals(120000, session.actualWorkTimeMs)
        assertEquals(60000, session.actualRestTimeMs)
    }

    @Test
    fun `completion percentage calculation works correctly`() {
        // Test normal completion
        assertEquals(80f, WorkoutSession.calculateCompletionPercentage(4, 5, false))
        assertEquals(100f, WorkoutSession.calculateCompletionPercentage(5, 5, false))
        assertEquals(0f, WorkoutSession.calculateCompletionPercentage(0, 5, false))
        
        // Test unlimited mode (always 100%)
        assertEquals(100f, WorkoutSession.calculateCompletionPercentage(10, 5, true))
        assertEquals(100f, WorkoutSession.calculateCompletionPercentage(1, 5, true))
    }

    @Test
    fun `workout completion determination works correctly`() {
        // Test 70% threshold
        assertTrue(WorkoutSession.isWorkoutCompleted(4, 5, false)) // 80% >= 70%
        assertTrue(WorkoutSession.isWorkoutCompleted(5, 5, false)) // 100% >= 70%
        assertFalse(WorkoutSession.isWorkoutCompleted(3, 5, false)) // 60% < 70%
        assertFalse(WorkoutSession.isWorkoutCompleted(0, 5, false)) // 0% < 70%
        
        // Test unlimited mode (any rounds completed = complete)
        assertTrue(WorkoutSession.isWorkoutCompleted(1, 5, true))
        assertTrue(WorkoutSession.isWorkoutCompleted(10, 5, true))
        assertFalse(WorkoutSession.isWorkoutCompleted(0, 5, true))
    }

    @Test
    fun `workout session formatting methods work correctly`() {
        val session = WorkoutSession(
            presetName = "Test Workout",
            workTimeSeconds = 30,
            restTimeSeconds = 15,
            plannedRounds = 5,
            completedRounds = 4,
            isUnlimited = false,
            noRest = false,
            startTime = System.currentTimeMillis() - 180000, // 3 minutes ago
            endTime = System.currentTimeMillis(),
            completionPercentage = 80f,
            totalDurationMs = 180000, // 3 minutes
            actualWorkTimeMs = 120000,
            actualRestTimeMs = 60000,
            isCompleted = true
        )
        
        assertEquals("3m 0s", session.getFormattedDuration())
        assertEquals("4 of 5 rounds (80%)", session.getCompletionStatusText())
        assertEquals("30s work, 15s rest", session.getWorkoutSummary())
    }

    @Test
    fun `workout session formatting with no rest works correctly`() {
        val session = WorkoutSession(
            presetName = "EMOM Workout",
            workTimeSeconds = 60,
            restTimeSeconds = 0,
            plannedRounds = 10,
            completedRounds = 10,
            isUnlimited = false,
            noRest = true,
            startTime = System.currentTimeMillis() - 600000, // 10 minutes ago
            endTime = System.currentTimeMillis(),
            completionPercentage = 100f,
            totalDurationMs = 600000, // 10 minutes
            actualWorkTimeMs = 600000,
            actualRestTimeMs = 0,
            isCompleted = true
        )
        
        assertEquals("10m 0s", session.getFormattedDuration())
        assertEquals("10 of 10 rounds (100%)", session.getCompletionStatusText())
        assertEquals("60s work, no rest", session.getWorkoutSummary())
    }

    @Test
    fun `workout session formatting with unlimited rounds works correctly`() {
        val session = WorkoutSession(
            presetName = "Unlimited Workout",
            workTimeSeconds = 45,
            restTimeSeconds = 15,
            plannedRounds = 1, // Not used for unlimited
            completedRounds = 8,
            isUnlimited = true,
            noRest = false,
            startTime = System.currentTimeMillis() - 480000, // 8 minutes ago
            endTime = System.currentTimeMillis(),
            completionPercentage = 100f,
            totalDurationMs = 480000, // 8 minutes
            actualWorkTimeMs = 360000,
            actualRestTimeMs = 120000,
            isCompleted = true
        )
        
        assertEquals("8m 0s", session.getFormattedDuration())
        assertEquals("8 rounds completed", session.getCompletionStatusText())
        assertEquals("45s work, 15s rest", session.getWorkoutSummary())
    }

    @Test
    fun `in memory workout history repository works correctly`() = runBlocking {
        val repository = InMemoryWorkoutHistoryRepository()
        
        // Create test session
        val session = WorkoutSession(
            presetName = "Test Workout",
            workTimeSeconds = 30,
            restTimeSeconds = 15,
            plannedRounds = 5,
            completedRounds = 4,
            isUnlimited = false,
            noRest = false,
            startTime = System.currentTimeMillis() - 300000,
            endTime = System.currentTimeMillis(),
            completionPercentage = 80f,
            totalDurationMs = 300000,
            actualWorkTimeMs = 200000,
            actualRestTimeMs = 100000,
            isCompleted = true
        )
        
        // Test saving and retrieving
        repository.saveWorkoutSession(session)
        val allSessions = repository.getAllSessions()
        assertEquals(1, allSessions.size)
        assertEquals(session.id, allSessions[0].id)
        
        // Test getting by ID
        val retrievedSession = repository.getSessionById(session.id)
        assertNotNull(retrievedSession)
        assertEquals(session.presetName, retrievedSession?.presetName)
        
        // Test session count
        assertEquals(1, repository.getSessionCount())
        
        // Test deletion
        repository.deleteSession(session.id)
        assertEquals(0, repository.getSessionCount())
    }

    @Test
    fun `workout history filtering works correctly`() = runBlocking {
        val repository = InMemoryWorkoutHistoryRepository()
        val now = System.currentTimeMillis()
        
        // Create test sessions with different completion rates and times
        val completedSession = WorkoutSession(
            presetName = "Completed Workout",
            workTimeSeconds = 30,
            restTimeSeconds = 15,
            plannedRounds = 5,
            completedRounds = 5,
            isUnlimited = false,
            noRest = false,
            startTime = now - (2 * 24 * 60 * 60 * 1000), // 2 days ago
            endTime = now - (2 * 24 * 60 * 60 * 1000) + 300000,
            completionPercentage = 100f,
            totalDurationMs = 300000,
            actualWorkTimeMs = 150000,
            actualRestTimeMs = 75000,
            isCompleted = true
        )
        
        val incompleteSession = WorkoutSession(
            presetName = "Incomplete Workout",
            workTimeSeconds = 30,
            restTimeSeconds = 15,
            plannedRounds = 5,
            completedRounds = 2,
            isUnlimited = false,
            noRest = false,
            startTime = now - (10 * 24 * 60 * 60 * 1000), // 10 days ago
            endTime = now - (10 * 24 * 60 * 60 * 1000) + 150000,
            completionPercentage = 40f,
            totalDurationMs = 150000,
            actualWorkTimeMs = 60000,
            actualRestTimeMs = 30000,
            isCompleted = false
        )
        
        repository.saveWorkoutSession(completedSession)
        repository.saveWorkoutSession(incompleteSession)
        
        // Test completion filter
        val completedOnly = repository.getFilteredSessions(
            WorkoutHistoryFilter(completionFilter = CompletionFilter.COMPLETED_ONLY)
        )
        assertEquals(1, completedOnly.size)
        assertEquals("Completed Workout", completedOnly[0].presetName)
        
        val incompleteOnly = repository.getFilteredSessions(
            WorkoutHistoryFilter(completionFilter = CompletionFilter.INCOMPLETE_ONLY)
        )
        assertEquals(1, incompleteOnly.size)
        assertEquals("Incomplete Workout", incompleteOnly[0].presetName)
        
        // Test date filter
        val last7Days = repository.getFilteredSessions(
            WorkoutHistoryFilter(dateFilter = DateFilter.LAST_7_DAYS)
        )
        assertEquals(1, last7Days.size) // Only the 2-day-old session
        assertEquals("Completed Workout", last7Days[0].presetName)
    }

    @Test
    fun `workout analytics calculations work correctly`() = runBlocking {
        val repository = InMemoryWorkoutHistoryRepository()
        val now = System.currentTimeMillis()
        
        // Add test sessions
        repeat(5) { i ->
            val session = WorkoutSession(
                presetName = "Workout $i",
                workTimeSeconds = 30,
                restTimeSeconds = 15,
                plannedRounds = 5,
                completedRounds = if (i < 3) 5 else 2, // First 3 completed, last 2 incomplete
                isUnlimited = false,
                noRest = false,
                startTime = now - (i * 24 * 60 * 60 * 1000), // i days ago
                endTime = now - (i * 24 * 60 * 60 * 1000) + 300000,
                completionPercentage = if (i < 3) 100f else 40f,
                totalDurationMs = 300000,
                actualWorkTimeMs = 150000,
                actualRestTimeMs = 75000,
                isCompleted = i < 3
            )
            repository.saveWorkoutSession(session)
        }
        
        // Test analytics
        assertEquals(3, repository.getTotalWorkoutsCompleted())
        assertEquals(5, repository.getTotalWorkoutsAttempted())
        assertEquals(300000L, repository.getAverageWorkoutDuration())
        
        val longestSession = repository.getLongestSession()
        assertNotNull(longestSession)
        assertEquals(300000L, longestSession?.totalDurationMs)
        
        val mostRoundsSession = repository.getMostRoundsSession()
        assertNotNull(mostRoundsSession)
        assertEquals(5, mostRoundsSession?.completedRounds)
    }
}
