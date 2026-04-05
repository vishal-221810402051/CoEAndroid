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

class MeetingDetailViewModel(
    private val meetingRepository: MeetingRepository = MeetingRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingDetailUiState())
    val uiState: StateFlow<MeetingDetailUiState> = _uiState.asStateFlow()

    fun loadMeetingDetail(meetingId: String) {
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            meetingRepository.getMeetingDetail(meetingId).fold(
                onSuccess = { detail ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            detail = detail,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load meeting detail."
                        )
                    }
                }
            )
        }
    }

    fun deleteMeeting(meetingId: String) {
        _uiState.update { state ->
            state.copy(
                isDeleting = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            meetingRepository.deleteMeeting(meetingId).fold(
                onSuccess = { message ->
                    _uiState.update { state ->
                        state.copy(
                            isDeleting = false,
                            isDeleted = true,
                            lastActionMessage = message
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isDeleting = false,
                            errorMessage = error.message ?: "Delete failed.",
                            lastActionMessage = error.message ?: "Delete failed."
                        )
                    }
                }
            )
        }
    }

    fun forwardPdf(meetingId: String) {
        _uiState.update { state ->
            state.copy(
                isForwarding = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            meetingRepository.forwardMeetingPdf(meetingId).fold(
                onSuccess = { message ->
                    _uiState.update { state ->
                        state.copy(
                            isForwarding = false,
                            lastActionMessage = message
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isForwarding = false,
                            errorMessage = error.message ?: "Forward failed.",
                            lastActionMessage = error.message ?: "Forward failed."
                        )
                    }
                }
            )
        }
    }

    fun consumeLastActionMessage() {
        _uiState.update { state ->
            state.copy(lastActionMessage = null)
        }
    }
}
