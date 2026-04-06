package com.coe.mobile.data.api

import com.coe.mobile.data.model.DecisionRequest
import com.coe.mobile.data.model.DecisionResponse
import com.coe.mobile.data.model.MeetingActionResponse
import com.coe.mobile.data.model.MeetingDetailResponse
import com.coe.mobile.data.model.PendingInboxResponse
import com.coe.mobile.data.model.ProcessingStatusResponse
import com.coe.mobile.data.model.RecentMeetingsResponse
import com.coe.mobile.data.model.UploadAudioResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("/api/meetings/upload-audio")
    suspend fun uploadAudio(
        @Part audioFile: MultipartBody.Part,
        @Part("recorded_at") recordedAt: RequestBody? = null
    ): Response<UploadAudioResponse>

    @GET("/api/inbox/pending")
    suspend fun getPendingInbox(): Response<PendingInboxResponse>

    @POST("/api/inbox/decision")
    suspend fun submitDecision(
        @Body request: DecisionRequest
    ): Response<DecisionResponse>

    @GET("/api/meetings/recent")
    suspend fun getRecentMeetings(
        @Query("limit") limit: Int = 5
    ): Response<RecentMeetingsResponse>

    @GET("/api/meetings/{meeting_id}")
    suspend fun getMeetingDetail(
        @Path("meeting_id") meetingId: String
    ): Response<MeetingDetailResponse>

    @GET("/api/meetings/{meeting_id}/processing-status")
    suspend fun getProcessingStatus(
        @Path("meeting_id") meetingId: String
    ): Response<ProcessingStatusResponse>

    @DELETE("/api/meetings/{meeting_id}")
    suspend fun deleteMeeting(
        @Path("meeting_id") meetingId: String
    ): Response<MeetingActionResponse>

    @POST("/api/meetings/{meeting_id}/forward-pdf")
    suspend fun forwardMeetingPdf(
        @Path("meeting_id") meetingId: String
    ): Response<MeetingActionResponse>
}
