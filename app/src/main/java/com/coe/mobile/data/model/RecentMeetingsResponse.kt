package com.coe.mobile.data.model

import com.google.gson.annotations.SerializedName

data class RecentMeetingsResponse(
    @SerializedName("items")
    val items: List<MeetingHistoryItem> = emptyList()
)
