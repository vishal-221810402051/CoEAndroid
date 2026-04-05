package com.coe.mobile.data.model

import com.google.gson.annotations.SerializedName

data class UploadAudioResponse(
    @SerializedName("meeting_id")
    val meetingId: String?,
    @SerializedName("intake_status")
    val intakeStatus: String?,
    @SerializedName("source_audio_filename")
    val sourceAudioFilename: String?,
    @SerializedName("created_at")
    val createdAt: String?
)
