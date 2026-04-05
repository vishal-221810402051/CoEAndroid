package com.coe.mobile.data.model

data class ProcessingStatus(
    val state: String,
    val progressPercent: Int? = null,
    val message: String? = null
)
