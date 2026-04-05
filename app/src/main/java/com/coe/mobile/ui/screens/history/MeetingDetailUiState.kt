package com.coe.mobile.ui.screens.history

import com.coe.mobile.data.model.MeetingDetailResponse

data class MeetingDetailUiState(
    val isLoading: Boolean = false,
    val detail: MeetingDetailResponse? = null,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val isForwarding: Boolean = false,
    val isDeleted: Boolean = false,
    val lastActionMessage: String? = null
)
