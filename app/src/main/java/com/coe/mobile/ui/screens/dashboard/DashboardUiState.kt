package com.coe.mobile.ui.screens.dashboard

data class DashboardUiState(
    val pendingApprovalsCount: Int? = null,
    val recentMeetingsCount: Int? = null,
    val reportsReadyRecentCount: Int? = null,
    val apiReachable: Boolean? = null,
    val lastRefreshEpochMillis: Long? = null,
    val pendingApprovalsError: String? = null,
    val recentMeetingsError: String? = null,
    val isRefreshing: Boolean = false
)
