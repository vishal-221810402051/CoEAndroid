package com.coe.mobile.ui.screens.history

import com.coe.mobile.data.model.MeetingHistoryItem

data class HistoryUiState(
    val isLoading: Boolean = false,
    val items: List<MeetingHistoryItem> = emptyList(),
    val errorMessage: String? = null
)
