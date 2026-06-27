package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object GoogleScriptSync {
    private const val SCRIPT_URL = "https://script.google.com/macros/s/AKfycby1Do6PmC0H4FEo7u7Ar5nEah38Q3gCMoaTuloxp7Xpft-snohIiz5y5KZOnC8QTf0B/exec"
    private val okHttpClient = OkHttpClient()

    suspend fun sendData(action: String, payload: Map<String, Any?>) {
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject(payload)
                json.put("action", action)
                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(SCRIPT_URL)
                    .post(body)
                    .build()
                okHttpClient.newCall(request).execute().use { response ->
                    response.body?.string()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
