package com.coe.mobile.data.model

import com.google.gson.annotations.SerializedName

data class MeetingHistoryItem(
    @SerializedName("meeting_id")
    val meetingId: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("report_available")
    val reportAvailable: Boolean? = null,
    @SerializedName("pdf_available")
    val pdfAvailable: Boolean? = null
)
