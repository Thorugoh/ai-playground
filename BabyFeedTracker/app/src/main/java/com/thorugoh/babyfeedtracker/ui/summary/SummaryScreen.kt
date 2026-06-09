package com.thorugoh.babyfeedtracker.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thorugoh.babyfeedtracker.model.FeedingInterval
import com.thorugoh.babyfeedtracker.model.FeedingSession
import com.thorugoh.babyfeedtracker.model.FeedingSide
import com.thorugoh.babyfeedtracker.ui.theme.BabyFeedTrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    session: FeedingSession,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Session Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SessionHeader(session)

            Spacer(modifier = Modifier.height(24.dp))

            DurationBreakdown(session)

            Spacer(modifier = Modifier.height(24.dp))

            IntervalList(session.intervals)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
fun SessionHeader(session: FeedingSession) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val startTime = timeFormat.format(Date(session.startTime))
    val endTime = session.endTime?.let { timeFormat.format(Date(it)) } ?: "Ongoing"
    val date = dateFormat.format(Date(session.startTime))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = date, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "$startTime - $endTime",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val totalDuration = session.totalDurationMillis(session.endTime ?: System.currentTimeMillis())
            Text(
                text = formatDuration(totalDuration),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DurationBreakdown(session: FeedingSession) {
    val endTime = session.endTime ?: System.currentTimeMillis()
    val leftDuration = session.leftDurationMillis(endTime)
    val rightDuration = session.rightDurationMillis(endTime)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        BreakdownCard(
            modifier = Modifier.weight(1f),
            label = "Left Side",
            duration = leftDuration,
            color = MaterialTheme.colorScheme.primary
        )
        BreakdownCard(
            modifier = Modifier.weight(1f),
            label = "Right Side",
            duration = rightDuration,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun BreakdownCard(modifier: Modifier, label: String, duration: Long, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun IntervalList(intervals: List<FeedingInterval>) {
    Text(
        text = "Session Intervals",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        fontWeight = FontWeight.Bold
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            intervals.forEachIndexed { index, interval ->
                IntervalItem(interval)
                if (index < intervals.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun IntervalItem(interval: FeedingInterval) {
    val duration = interval.durationMillis(interval.endTime ?: System.currentTimeMillis())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                tint = if (interval.side == FeedingSide.LEFT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = interval.side.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60))
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun SummaryScreenPreview() {
    val session = FeedingSession(
        startTime = System.currentTimeMillis() - 3600000,
        endTime = System.currentTimeMillis(),
        intervals = listOf(
            FeedingInterval(FeedingSide.LEFT, System.currentTimeMillis() - 3600000, System.currentTimeMillis() - 2400000),
            FeedingInterval(FeedingSide.RIGHT, System.currentTimeMillis() - 2400000, System.currentTimeMillis())
        )
    )
    BabyFeedTrackerTheme {
        SummaryScreen(session = session, onBack = {})
    }
}
