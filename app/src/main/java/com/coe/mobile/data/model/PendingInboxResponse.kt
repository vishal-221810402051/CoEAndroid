package com.coe.mobile.data.model

import com.google.gson.annotations.SerializedName

data class PendingInboxResponse(
    @SerializedName("items")
    val items: List<InboxItem> = emptyList()
)
