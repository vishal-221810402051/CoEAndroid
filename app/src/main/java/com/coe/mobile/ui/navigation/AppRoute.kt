package com.coe.mobile.ui.navigation

sealed class AppRoute(val route: String) {
    data object Dashboard : AppRoute("dashboard")
    data object Recorder : AppRoute("recorder")
    data object History : AppRoute("history")
    data object MeetingDetail : AppRoute("history/meeting/{meetingId}") {
        fun create(meetingId: String): String = "history/meeting/$meetingId"
    }
    data object Processing : AppRoute("processing")
    data object Inbox : AppRoute("inbox")
    data object SuggestionDetail : AppRoute("suggestionDetail")
}
