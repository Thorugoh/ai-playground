package com.thorugoh.babyfeedtracker.ui.feeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thorugoh.babyfeedtracker.model.FeedingInterval
import com.thorugoh.babyfeedtracker.model.FeedingSession
import com.thorugoh.babyfeedtracker.model.FeedingSide
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedingUiState(
    val currentSession: FeedingSession? = null,
    val isActive: Boolean = false,
    val currentSide: FeedingSide? = null,
    val elapsedTotal: Long = 0L,
    val elapsedLeft: Long = 0L,
    val elapsedRight: Long = 0L,
    val pastSessions: List<FeedingSession> = emptyList()
)

class FeedingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedingUiState())
    val uiState: StateFlow<FeedingUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startFeeding(side: FeedingSide) {
        val now = System.currentTimeMillis()
        val newInterval = FeedingInterval(side = side, startTime = now)
        val newSession = FeedingSession(
            startTime = now,
            intervals = listOf(newInterval)
        )

        _uiState.update {
            it.copy(
                currentSession = newSession,
                isActive = true,
                currentSide = side
            )
        }
        startTimer()
    }

    fun switchSide() {
        val currentState = _uiState.value
        val session = currentState.currentSession ?: return
        val currentSide = currentState.currentSide ?: return
        val now = System.currentTimeMillis()

        // Close current interval
        val lastInterval = session.intervals.last().copy(endTime = now)
        val updatedIntervals = session.intervals.dropLast(1) + lastInterval

        // Start new interval on the other side
        val nextSide = if (currentSide == FeedingSide.LEFT) FeedingSide.RIGHT else FeedingSide.LEFT
        val newInterval = FeedingInterval(side = nextSide, startTime = now)

        _uiState.update {
            it.copy(
                currentSession = session.copy(intervals = updatedIntervals + newInterval),
                currentSide = nextSide
            )
        }
    }

    fun stopFeeding() {
        val currentState = _uiState.value
        val session = currentState.currentSession ?: return
        val now = System.currentTimeMillis()

        // Close current interval
        val lastInterval = session.intervals.last().copy(endTime = now)
        val updatedIntervals = session.intervals.dropLast(1) + lastInterval

        val finishedSession = session.copy(
            endTime = now,
            intervals = updatedIntervals
        )

        _uiState.update {
            it.copy(
                currentSession = finishedSession,
                isActive = false,
                currentSide = null,
                pastSessions = listOf(finishedSession) + it.pastSessions
            )
        }
        stopTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                _uiState.update { state ->
                    state.currentSession?.let { session ->
                        state.copy(
                            elapsedTotal = session.totalDurationMillis(now),
                            elapsedLeft = session.leftDurationMillis(now),
                            elapsedRight = session.rightDurationMillis(now)
                        )
                    } ?: state
                }
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        
        // Final update to set exact durations
        val now = System.currentTimeMillis()
        _uiState.update { state ->
            state.currentSession?.let { session ->
                state.copy(
                    elapsedTotal = session.totalDurationMillis(now),
                    elapsedLeft = session.leftDurationMillis(now),
                    elapsedRight = session.rightDurationMillis(now)
                )
            } ?: state
        }
    }
}
