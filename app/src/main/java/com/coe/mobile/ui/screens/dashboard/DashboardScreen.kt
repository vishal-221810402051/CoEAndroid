package com.coe.mobile.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coe.mobile.ui.components.AppCard
import com.coe.mobile.ui.components.AtmosphereBackground
import com.coe.mobile.ui.components.ScreenEnterReveal
import com.coe.mobile.ui.components.SectionHeader
import com.coe.mobile.ui.components.pressFeedbackModifier
import kotlin.math.max

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onStartRecording: () -> Unit = {},
    onOpenInbox: () -> Unit = {},
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        dashboardViewModel.refreshDashboard()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                dashboardViewModel.refreshDashboard()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AtmosphereBackground(modifier = modifier) {
        ScreenEnterReveal {
            val recordInteraction = remember { MutableInteractionSource() }
            val inboxInteraction = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            SectionHeader(
                title = "System Status",
                subtitle = "Operational truth from live API checks"
            )
            AppCard {
                val statusText = when (uiState.apiReachable) {
                    true -> "Backend Online"
                    false -> "Backend unavailable"
                    null -> if (uiState.isRefreshing) "Checking backend..." else "Status unavailable"
                }
                val statusColor = when (uiState.apiReachable) {
                    true -> MaterialTheme.colorScheme.secondary
                    false -> MaterialTheme.colorScheme.error
                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor
                )
                Text(
                    text = formatUpdateText(
                        lastRefreshEpochMillis = uiState.lastRefreshEpochMillis,
                        isRefreshing = uiState.isRefreshing
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (!uiState.pendingApprovalsError.isNullOrBlank() || !uiState.recentMeetingsError.isNullOrBlank()) {
                    Text(
                        text = listOfNotNull(uiState.pendingApprovalsError, uiState.recentMeetingsError)
                            .joinToString(separator = " | "),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            SectionHeader(title = "Overview")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardKpiCard(
                    title = "Pending Approvals",
                    valueText = formatMetricValue(
                        value = uiState.pendingApprovalsCount,
                        error = uiState.pendingApprovalsError,
                        isRefreshing = uiState.isRefreshing
                    ),
                    accentColor = when {
                        uiState.pendingApprovalsError != null -> MaterialTheme.colorScheme.onSurfaceVariant
                        (uiState.pendingApprovalsCount ?: 0) > 0 -> Color(0xFFFFC857)
                        uiState.pendingApprovalsCount != null -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f)
                )
                DashboardKpiCard(
                    title = "Recent Meetings",
                    valueText = formatMetricValue(
                        value = uiState.recentMeetingsCount,
                        error = uiState.recentMeetingsError,
                        isRefreshing = uiState.isRefreshing
                    ),
                    accentColor = when {
                        uiState.recentMeetingsError != null -> MaterialTheme.colorScheme.onSurfaceVariant
                        uiState.recentMeetingsCount != null -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            DashboardKpiCard(
                title = "Reports Ready (Recent)",
                valueText = formatMetricValue(
                    value = uiState.reportsReadyRecentCount,
                    error = uiState.recentMeetingsError,
                    isRefreshing = uiState.isRefreshing
                ),
                accentColor = when {
                    uiState.recentMeetingsError != null -> MaterialTheme.colorScheme.onSurfaceVariant
                    uiState.reportsReadyRecentCount != null -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            SectionHeader(title = "Quick Actions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    interactionSource = recordInteraction,
                    onClick = onStartRecording,
                    modifier = Modifier
                        .then(pressFeedbackModifier(recordInteraction))
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Start Recording")
                }
                OutlinedButton(
                    interactionSource = inboxInteraction,
                    onClick = onOpenInbox,
                    modifier = Modifier
                        .then(pressFeedbackModifier(inboxInteraction))
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Open Inbox")
                }
            }
        }
        }
    }
}

@Composable
private fun DashboardKpiCard(
    title: String,
    valueText: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(2.dp)
                    .background(accentColor.copy(alpha = 0.72f))
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleLarge,
                color = accentColor
            )
        }
    }
}

private fun formatMetricValue(
    value: Int?,
    error: String?,
    isRefreshing: Boolean
): String {
    return when {
        error != null -> "Unavailable"
        value != null -> value.toString()
        isRefreshing -> "Loading..."
        else -> "Unavailable"
    }
}

private fun formatUpdateText(
    lastRefreshEpochMillis: Long?,
    isRefreshing: Boolean
): String {
    if (isRefreshing && lastRefreshEpochMillis == null) return "Updating..."
    if (lastRefreshEpochMillis == null) return "Updated: Unavailable"

    val elapsedSeconds = max(0L, (System.currentTimeMillis() - lastRefreshEpochMillis) / 1000L)
    return when {
        elapsedSeconds < 45 -> "Updated just now"
        elapsedSeconds < 3600 -> "Updated ${elapsedSeconds / 60} min ago"
        elapsedSeconds < 86_400 -> "Updated ${elapsedSeconds / 3600} hr ago"
        else -> "Updated ${elapsedSeconds / 86_400} day ago"
    }
}
