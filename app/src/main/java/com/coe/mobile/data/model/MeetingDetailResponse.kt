package com.coe.mobile.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class MeetingDetailResponse(
    @SerializedName("meeting_id")
    val meetingId: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("summary")
    val summary: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("stage_statuses")
    val stageStatuses: JsonObject? = null,
    @SerializedName("report_available")
    val reportAvailable: Boolean? = null,
    @SerializedName("pdf_available")
    val pdfAvailable: Boolean? = null,
    @SerializedName("calendar_stats")
    val calendarStats: JsonObject? = null
)
