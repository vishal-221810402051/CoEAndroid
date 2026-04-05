package com.coe.mobile.data.repository

import com.coe.mobile.data.model.CalendarCandidate
import com.coe.mobile.data.model.SuggestionSummary

interface SuggestionRepository {
    suspend fun getSuggestions(): List<SuggestionSummary>
    suspend fun getCalendarCandidate(suggestionId: String): CalendarCandidate?
}
