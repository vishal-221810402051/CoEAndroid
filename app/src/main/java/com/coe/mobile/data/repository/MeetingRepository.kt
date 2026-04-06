package com.coe.mobile.data.repository

import com.coe.mobile.data.model.MeetingDetailResponse
import com.coe.mobile.data.model.MeetingHistoryItem
import com.coe.mobile.data.model.ProcessingStatusResponse

interface MeetingRepository {
    suspend fun getRecentMeetings(limit: Int = 5): Result<List<MeetingHistoryItem>>
    suspend fun getMeetingDetail(meetingId: String): Result<MeetingDetailResponse>
    suspend fun getProcessingStatus(meetingId: String): Result<ProcessingStatusResponse>
    suspend fun deleteMeeting(meetingId: String): Result<String>
    suspend fun forwardMeetingPdf(meetingId: String): Result<String>
}
