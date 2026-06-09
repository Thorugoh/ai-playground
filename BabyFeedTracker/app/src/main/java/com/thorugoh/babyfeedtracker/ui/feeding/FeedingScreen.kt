package com.thorugoh.babyfeedtracker.ui.feeding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thorugoh.babyfeedtracker.model.FeedingSession
import com.thorugoh.babyfeedtracker.model.FeedingSide
import com.thorugoh.babyfeedtracker.ui.theme.BabyFeedTrackerTheme
import java.util.Locale

@Composable
fun FeedingScreen(
    viewModel: FeedingViewModel = viewModel(),
    onSessionFinished: (FeedingSession) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "BabyFeed Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(48.dp))

            TimerDisplay(uiState.elapsedTotal)

            Spacer(modifier = Modifier.height(32.dp))

            SideBreakdown(
                leftMillis = uiState.elapsedLeft,
                rightMillis = uiState.elapsedRight,
                totalMillis = uiState.elapsedTotal
            )

            Spacer(modifier = Modifier.height(48.dp))

            Controls(
                isActive = uiState.isActive,
                currentSide = uiState.currentSide,
                onStart = viewModel::startFeeding,
                onSwitch = viewModel::switchSide,
                onStop = {
                    viewModel.stopFeeding()
                    viewModel.uiState.value.currentSession?.let { onSessionFinished(it) }
                }
            )
        }
    }
}

@Composable
fun TimerDisplay(elapsedMillis: Long) {
    val seconds = (elapsedMillis / 1000) % 60
    val minutes = (elapsedMillis / (1000 * 60))
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Text(
        text = timeString,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 80.sp,
            fontWeight = FontWeight.Black
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun SideBreakdown(
    leftMillis: Long,
    rightMillis: Long,
    totalMillis: Long
) {
    val progress = if (totalMillis > 0) leftMillis.toFloat() / totalMillis else 0.5f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SideInfo(label = "Left", millis = leftMillis, color = MaterialTheme.colorScheme.primary)
            SideInfo(label = "Right", millis = rightMillis, color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Round
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SideInfo(label: String, millis: Long, color: Color) {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60))
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = color)
        Text(
            text = timeString,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Controls(
    isActive: Boolean,
    currentSide: FeedingSide?,
    onStart: (FeedingSide) -> Unit,
    onSwitch: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = !isActive) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StartButton(
                    side = FeedingSide.LEFT,
                    label = "Start Left",
                    onClick = { onStart(FeedingSide.LEFT) }
                )
                StartButton(
                    side = FeedingSide.RIGHT,
                    label = "Start Right",
                    onClick = { onStart(FeedingSide.RIGHT) }
                )
            }
        }

        AnimatedVisibility(visible = isActive) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Currently Feeding: ${currentSide?.name}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onSwitch,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(Icons.Rounded.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Switch Side")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop")
                    }
                }
            }
        }
    }
}

@Composable
fun StartButton(side: FeedingSide, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(140.dp, 56.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun FeedingScreenPreview() {
    BabyFeedTrackerTheme {
        FeedingScreen()
    }
}
