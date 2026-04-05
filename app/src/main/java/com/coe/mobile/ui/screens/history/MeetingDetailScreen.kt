package com.coe.mobile.ui.screens.history

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coe.mobile.ui.components.AtmosphereBackground
import com.coe.mobile.ui.components.ScreenEnterReveal
import com.coe.mobile.ui.components.SciFiPanel
import com.coe.mobile.ui.components.SectionHeader
import com.coe.mobile.ui.components.StatusChip
import com.coe.mobile.ui.components.StatusChipVariant
import com.coe.mobile.ui.components.pressFeedbackModifier

@Composable
fun MeetingDetailScreen(
    meetingId: String,
    modifier: Modifier = Modifier,
    onDeleted: () -> Unit,
    meetingDetailViewModel: MeetingDetailViewModel = viewModel()
) {
    val uiState by meetingDetailViewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(meetingId) {
        meetingDetailViewModel.loadMeetingDetail(meetingId)
    }

    LaunchedEffect(uiState.lastActionMessage) {
        uiState.lastActionMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            meetingDetailViewModel.consumeLastActionMessage()
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleted()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Meeting") },
            text = { Text("Are you sure you want to delete this meeting?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        meetingDetailViewModel.deleteMeeting(meetingId)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AtmosphereBackground(modifier = modifier) {
        ScreenEnterReveal {
            val deleteInteraction = remember { MutableInteractionSource() }
            val forwardInteraction = remember { MutableInteractionSource() }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading meeting detail...",
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            uiState.detail?.let { detail ->
                SectionHeader(
                    title = detail.title.orEmpty().ifBlank { detail.meetingId },
                    subtitle = detail.createdAt ?: detail.meetingId
                )

                SciFiPanel {
                    SectionHeader(title = "Summary")
                    Text(
                        text = detail.summary.orEmpty().ifBlank { "No summary available." },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                SciFiPanel {
                    SectionHeader(title = "Pipeline Stages")
                    val entries = detail.stageStatuses?.entrySet().orEmpty()
                    if (entries.isEmpty()) {
                        Text(
                            text = "No stage data available.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            entries.forEach { entry ->
                                val stageStatus = entry.value.asString
                                val stageVariant = when {
                                    stageStatus.contains("complete", ignoreCase = true) -> StatusChipVariant.Success
                                    stageStatus.contains("error", ignoreCase = true) -> StatusChipVariant.Error
                                    stageStatus.contains("process", ignoreCase = true) -> StatusChipVariant.Warning
                                    else -> StatusChipVariant.Neutral
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.key.replace('_', ' '),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    StatusChip(
                                        label = stageStatus,
                                        variant = stageVariant
                                    )
                                }
                            }
                        }
                    }
                }

                SciFiPanel {
                    SectionHeader(title = "Report and Calendar")
                    val reportReady = detail.reportAvailable == true || detail.pdfAvailable == true
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(
                            label = if (reportReady) "Report ready" else "Report pending",
                            variant = if (reportReady) StatusChipVariant.Success else StatusChipVariant.Warning
                        )
                        StatusChip(
                            label = detail.status.orEmpty().ifBlank { "Unknown" },
                            variant = StatusChipVariant.Neutral
                        )
                    }
                    val calendarEntries = detail.calendarStats?.entrySet().orEmpty()
                    if (calendarEntries.isNotEmpty()) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            calendarEntries.forEach { entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.key.replace('_', ' '),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = entry.value.toString(),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        interactionSource = deleteInteraction,
                        onClick = { showDeleteDialog = true },
                        enabled = !uiState.isDeleting && !uiState.isForwarding,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.64f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .then(pressFeedbackModifier(deleteInteraction))
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(text = if (uiState.isDeleting) "Deleting..." else "Delete")
                    }
                    Button(
                        interactionSource = forwardInteraction,
                        onClick = { meetingDetailViewModel.forwardPdf(meetingId) },
                        enabled = !uiState.isDeleting && !uiState.isForwarding,
                        modifier = Modifier
                            .then(pressFeedbackModifier(forwardInteraction))
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(if (uiState.isForwarding) "Forwarding..." else "Forward")
                    }
                }
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        }
    }
}
