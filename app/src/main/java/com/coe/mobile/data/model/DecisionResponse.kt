package com.coe.mobile.data.model

import com.google.gson.annotations.SerializedName

data class DecisionResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?
)
