package com.coe.mobile.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coe.mobile.data.repository.MeetingRepository
import com.coe.mobile.data.repository.MeetingRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val meetingRepository: MeetingRepository = MeetingRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadRecentMeetings(limit: Int = 5) {
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = meetingRepository.getRecentMeetings(limit)
            result.fold(
                onSuccess = { meetings ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            items = meetings,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load recent meetings."
                        )
                    }
                }
            )
        }
    }
}
