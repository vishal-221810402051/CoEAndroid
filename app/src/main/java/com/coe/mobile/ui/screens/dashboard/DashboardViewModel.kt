package com.coe.mobile.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coe.mobile.data.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun refreshDashboard() {
        if (_uiState.value.isRefreshing) return

        _uiState.update { state ->
            state.copy(
                isRefreshing = true,
                pendingApprovalsError = null,
                recentMeetingsError = null
            )
        }

        viewModelScope.launch {
            val pendingDeferred = async(Dispatchers.IO) { fetchPendingApprovalsCount() }
            val recentDeferred = async(Dispatchers.IO) { fetchRecentMeetings() }

            val pendingResult = pendingDeferred.await()
            val recentResult = recentDeferred.await()

            var pendingCount: Int? = null
            var pendingError: String? = null
            var pendingSuccess = false

            when (pendingResult) {
                is DashboardFetchResult.Success -> {
                    pendingSuccess = true
                    pendingCount = pendingResult.value
                }

                is DashboardFetchResult.Failure -> {
                    pendingError = pendingResult.message
                }
            }

            var recentCount: Int? = null
            var reportsReadyCount: Int? = null
            var recentError: String? = null
            var recentSuccess = false

            when (recentResult) {
                is DashboardFetchResult.Success -> {
                    recentSuccess = true
                    recentCount = recentResult.value.totalCount
                    reportsReadyCount = recentResult.value.reportsReadyCount
                }

                is DashboardFetchResult.Failure -> {
                    recentError = recentResult.message
                }
            }

            val anyFetchSucceeded = pendingSuccess || recentSuccess
            val now = System.currentTimeMillis()

            _uiState.update { state ->
                state.copy(
                    pendingApprovalsCount = pendingCount,
                    recentMeetingsCount = recentCount,
                    reportsReadyRecentCount = reportsReadyCount,
                    apiReachable = anyFetchSucceeded,
                    lastRefreshEpochMillis = if (anyFetchSucceeded) now else state.lastRefreshEpochMillis,
                    pendingApprovalsError = pendingError,
                    recentMeetingsError = recentError,
                    isRefreshing = false
                )
            }
        }
    }

    private suspend fun fetchPendingApprovalsCount(): DashboardFetchResult<Int> {
        return try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.apiService.getPendingInbox()
            }
            if (response.isSuccessful) {
                DashboardFetchResult.Success(response.body()?.items.orEmpty().size)
            } else {
                DashboardFetchResult.Failure(
                    parseServerErrorMessage(response.errorBody()?.string())
                        ?: "Inbox unavailable (${response.code()})"
                )
            }
        } catch (_: IOException) {
            DashboardFetchResult.Failure("Inbox unavailable")
        } catch (error: Exception) {
            DashboardFetchResult.Failure(error.message ?: "Inbox unavailable")
        }
    }

    private suspend fun fetchRecentMeetings(): DashboardFetchResult<RecentMetrics> {
        return try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.apiService.getRecentMeetings(limit = 5)
            }
            if (response.isSuccessful) {
                val meetings = response.body()?.items.orEmpty()
                val reportsReady = meetings.count { it.reportAvailable == true || it.pdfAvailable == true }
                DashboardFetchResult.Success(
                    RecentMetrics(
                        totalCount = meetings.size,
                        reportsReadyCount = reportsReady
                    )
                )
            } else {
                DashboardFetchResult.Failure(
                    parseServerErrorMessage(response.errorBody()?.string())
                        ?: "Recent meetings unavailable (${response.code()})"
                )
            }
        } catch (_: IOException) {
            DashboardFetchResult.Failure("Recent meetings unavailable")
        } catch (error: Exception) {
            DashboardFetchResult.Failure(error.message ?: "Recent meetings unavailable")
        }
    }

    private fun parseServerErrorMessage(rawError: String?): String? {
        if (rawError.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(rawError)
            json.optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}

private data class RecentMetrics(
    val totalCount: Int,
    val reportsReadyCount: Int
)

private sealed interface DashboardFetchResult<out T> {
    data class Success<T>(val value: T) : DashboardFetchResult<T>
    data class Failure(val message: String) : DashboardFetchResult<Nothing>
}
