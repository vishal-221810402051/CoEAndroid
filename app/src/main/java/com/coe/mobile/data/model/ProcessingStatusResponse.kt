package com.coe.mobile.data.model

import com.google.gson.annotations.SerializedName

data class ProcessingStatusResponse(
    @SerializedName("meeting_id")
    val meetingId: String?,
    @SerializedName(value = "overall_status", alternate = ["status"])
    val overallStatus: String?,
    @SerializedName(value = "current_stage", alternate = ["active_stage"])
    val currentStage: String?,
    @SerializedName(value = "stages", alternate = ["stage_statuses"])
    val stages: Map<String, Any?>? = null,
    @SerializedName("updated_at")
    val updatedAt: String?
)
