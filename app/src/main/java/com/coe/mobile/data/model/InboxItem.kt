package com.coe.mobile.data.model

import com.google.gson.annotations.SerializedName

data class InboxItem(
    @SerializedName("meeting_id")
    val meetingId: String,
    @SerializedName("candidate_id")
    val candidateId: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("summary")
    val summary: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("display_date")
    val displayDate: String?,
    @SerializedName("display_time")
    val displayTime: String?,
    @SerializedName("confidence")
    val confidence: String?,
    @SerializedName("eligibility_status")
    val eligibilityStatus: String?,
    @SerializedName("blockers")
    val blockers: List<String> = emptyList()
)
