package com.thorugoh.babyfeedtracker.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.thorugoh.babyfeedtracker.model.FeedingSession
import kotlinx.serialization.Serializable

@Serializable
sealed interface FeedingRoute : NavKey {
    @Serializable
    data object History : FeedingRoute

    @Serializable
    data object Feeding : FeedingRoute

    @Serializable
    data class Summary(val session: FeedingSession) : FeedingRoute
}
