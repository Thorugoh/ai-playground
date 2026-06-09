package com.thorugoh.babyfeedtracker.model

import kotlinx.serialization.Serializable

@Serializable
enum class FeedingSide {
    LEFT, RIGHT
}

@Serializable
data class FeedingInterval(
    val side: FeedingSide,
    val startTime: Long,
    val endTime: Long? = null
) {
    fun durationMillis(currentTime: Long): Long {
        return (endTime ?: currentTime) - startTime
    }
}

@Serializable
data class FeedingSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startTime: Long,
    val endTime: Long? = null,
    val intervals: List<FeedingInterval> = emptyList()
) {
    fun totalDurationMillis(currentTime: Long): Long {
        return intervals.sumOf { it.durationMillis(currentTime) }
    }

    fun leftDurationMillis(currentTime: Long): Long {
        return intervals.filter { it.side == FeedingSide.LEFT }.sumOf { it.durationMillis(currentTime) }
    }

    fun rightDurationMillis(currentTime: Long): Long {
        return intervals.filter { it.side == FeedingSide.RIGHT }.sumOf { it.durationMillis(currentTime) }
    }
}
