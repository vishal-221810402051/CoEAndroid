package com.coe.mobile.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coe.mobile.data.model.MeetingHistoryItem
import com.coe.mobile.ui.components.AtmosphereBackground
import com.coe.mobile.ui.components.EmptyState
import com.coe.mobile.ui.components.ScreenEnterReveal
import com.coe.mobile.ui.components.SciFiPanel
import com.coe.mobile.ui.components.SectionHeader
import com.coe.mobile.ui.components.StatusChip
import com.coe.mobile.ui.components.StatusChipVariant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onOpenMeeting: (String) -> Unit,
    historyViewModel: HistoryViewModel = viewModel()
) {
    val uiState by historyViewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        historyViewModel.loadRecentMeetings(limit = 5)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                historyViewModel.loadRecentMeetings(limit = 5)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AtmosphereBackground(modifier = modifier) {
        ScreenEnterReveal {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading recent meetings...",
                            modifier = Modifier.padding(top = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                uiState.items.isEmpty() -> {
                    if (!uiState.errorMessage.isNullOrBlank()) {
                        EmptyState(
                            icon = Icons.Default.History,
                            title = "Backend unavailable",
                            subtitle = uiState.errorMessage
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Default.History,
                            title = "No recent meetings",
                            subtitle = "Recent intelligence sessions will appear here."
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            SectionHeader(
                                title = "Recent Meetings",
                                subtitle = "Last 5 intelligence sessions"
                            )
                        }

                        items(
                            items = uiState.items,
                            key = { it.meetingId }
                        ) { item ->
                            HistoryMeetingCard(
                                item = item,
                                onClick = { onOpenMeeting(item.meetingId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryMeetingCard(
    item: MeetingHistoryItem,
    onClick: () -> Unit
) {
    val statusLabel = item.status.orEmpty().ifBlank { "unknown" }
    val statusVariant = when {
        statusLabel.contains("complete", ignoreCase = true) -> StatusChipVariant.Success
        statusLabel.contains("error", ignoreCase = true) -> StatusChipVariant.Error
        statusLabel.contains("process", ignoreCase = true) -> StatusChipVariant.Warning
        else -> StatusChipVariant.Neutral
    }

    SciFiPanel(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            text = item.title.orEmpty().ifBlank { item.meetingId },
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2
        )
        Text(
            text = formatDateTime(item.createdAt),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp)
        )
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(
                label = statusLabel.replaceFirstChar { it.uppercase() },
                variant = statusVariant
            )
            val hasPdf = item.pdfAvailable == true || item.reportAvailable == true
            StatusChip(
                label = if (hasPdf) "PDF ready" else "PDF pending",
                variant = if (hasPdf) StatusChipVariant.Success else StatusChipVariant.Neutral
            )
        }
    }
}

private fun formatDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "Unknown time"
    return runCatching {
        OffsetDateTime.parse(raw).format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
    }.getOrElse { raw }
}
