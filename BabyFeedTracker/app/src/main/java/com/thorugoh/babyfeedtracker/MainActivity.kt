package com.thorugoh.babyfeedtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.thorugoh.babyfeedtracker.ui.feeding.FeedingScreen
import com.thorugoh.babyfeedtracker.ui.feeding.FeedingViewModel
import com.thorugoh.babyfeedtracker.ui.history.HistoryScreen
import com.thorugoh.babyfeedtracker.ui.navigation.FeedingRoute
import com.thorugoh.babyfeedtracker.ui.summary.SummaryScreen
import com.thorugoh.babyfeedtracker.ui.theme.BabyFeedTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BabyFeedTrackerTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainContent() {
    val backStack = rememberNavBackStack(FeedingRoute.History)
    val viewModel: FeedingViewModel = viewModel()
    
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategy = listDetailStrategy
    ) { key ->
        when (key) {
            is FeedingRoute.History -> NavEntry(key) {
                HistoryScreen(
                    viewModel = viewModel,
                    onSessionSelected = { session ->
                        backStack.add(FeedingRoute.Summary(session))
                    },
                    onStartNew = {
                        backStack.add(FeedingRoute.Feeding)
                    }
                )
            }
            is FeedingRoute.Feeding -> NavEntry(key) {
                FeedingScreen(
                    viewModel = viewModel,
                    onSessionFinished = { session ->
                        backStack.removeLastOrNull() // Remove Feeding
                        backStack.add(FeedingRoute.Summary(session))
                    }
                )
            }
            is FeedingRoute.Summary -> NavEntry(key) {
                SummaryScreen(
                    session = key.session,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            else -> NavEntry(key) { }
        }
    }
}
