package com.coe.mobile.data.model

import com.google.gson.annotations.SerializedName

data class DecisionRequest(
    @SerializedName("meeting_id")
    val meetingId: String,
    @SerializedName("candidate_id")
    val candidateId: String,
    @SerializedName("decision")
    val decision: String,
    @SerializedName("actor")
    val actor: String
)
