package com.example.data.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class ValueRange(
    val range: String? = null,
    val majorDimension: String? = null,
    val values: List<List<String>>? = null
)

interface GoogleSheetsApiService {
    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getSheetValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("key") apiKey: String
    ): ValueRange
}
