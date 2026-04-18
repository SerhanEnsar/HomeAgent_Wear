package com.serhanensar.agentjee.data.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object ApiClient {
    private var baseUrl = "http://AgentJee.local:8000"
    // Handoff belgesindeki ESP32 ile aynı API Key'i kullanıyoruz
    private const val API_KEY = "YOUR_API_KEY"

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
    }

    fun get(path: String): String? = try {
        // API Key'i sorgu parametresi olarak ekliyoruz
        val url = "$baseUrl$path".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("api_key", API_KEY)
            ?.build()

        if (url != null) {
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().body?.string()
        } else null
    } catch (e: Exception) {
        null
    }

    fun post(path: String, body: String): String? = try {
        val url = "$baseUrl$path".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("api_key", API_KEY)
            ?.build()

        if (url != null) {
            val rb = body.toRequestBody("application/json".toMediaType())
            val req = Request.Builder().url(url).post(rb).build()
            client.newCall(req).execute().body?.string()
        } else null
    } catch (e: Exception) {
        null
    }
}
