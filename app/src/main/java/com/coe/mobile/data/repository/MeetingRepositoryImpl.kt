package com.coe.mobile.data.repository

import com.coe.mobile.data.api.ApiService
import com.coe.mobile.data.api.RetrofitInstance
import com.coe.mobile.data.model.MeetingDetailResponse
import com.coe.mobile.data.model.MeetingHistoryItem
import com.coe.mobile.data.model.ProcessingStatusResponse
import org.json.JSONObject
import java.io.IOException

class MeetingRepositoryImpl(
    private val apiService: ApiService = RetrofitInstance.apiService
) : MeetingRepository {

    override suspend fun getRecentMeetings(limit: Int): Result<List<MeetingHistoryItem>> {
        return try {
            val response = apiService.getRecentMeetings(limit)
            if (response.isSuccessful) {
                Result.success(response.body()?.items.orEmpty())
            } else {
                Result.failure(IllegalStateException(parseError(response.errorBody()?.string())
                    ?: "Failed to fetch recent meetings (${response.code()})."))
            }
        } catch (error: IOException) {
            Result.failure(IllegalStateException("Unable to reach laptop API.", error))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun getMeetingDetail(meetingId: String): Result<MeetingDetailResponse> {
        return try {
            val response = apiService.getMeetingDetail(meetingId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    Result.failure(IllegalStateException("Meeting detail response was empty."))
                } else {
                    Result.success(body)
                }
            } else {
                Result.failure(IllegalStateException(parseError(response.errorBody()?.string())
                    ?: "Failed to fetch meeting detail (${response.code()})."))
            }
        } catch (error: IOException) {
            Result.failure(IllegalStateException("Unable to reach laptop API.", error))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun getProcessingStatus(meetingId: String): Result<ProcessingStatusResponse> {
        return try {
            val response = apiService.getProcessingStatus(meetingId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    Result.failure(IllegalStateException("Processing status response was empty."))
                } else {
                    Result.success(body)
                }
            } else {
                Result.failure(
                    IllegalStateException(
                        parseError(response.errorBody()?.string())
                            ?: "Failed to fetch processing status (${response.code()})."
                    )
                )
            }
        } catch (error: IOException) {
            Result.failure(IllegalStateException("Unable to reach laptop API.", error))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun deleteMeeting(meetingId: String): Result<String> {
        return try {
            val response = apiService.deleteMeeting(meetingId)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Meeting deleted")
            } else {
                Result.failure(IllegalStateException(parseError(response.errorBody()?.string())
                    ?: "Failed to delete meeting (${response.code()})."))
            }
        } catch (error: IOException) {
            Result.failure(IllegalStateException("Unable to reach laptop API.", error))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun forwardMeetingPdf(meetingId: String): Result<String> {
        return try {
            val response = apiService.forwardMeetingPdf(meetingId)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Forwarded successfully")
            } else {
                Result.failure(IllegalStateException(parseError(response.errorBody()?.string())
                    ?: "Failed to forward PDF (${response.code()})."))
            }
        } catch (error: IOException) {
            Result.failure(IllegalStateException("Unable to reach laptop API.", error))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun parseError(rawError: String?): String? {
        if (rawError.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(rawError)
            json.optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
