package com.serhanensar.agentjee.data.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object ApiClient {
    private var baseUrl = "http://AgentJee.local:8000"

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
    }

    fun get(path: String): String? = try {
        val req = Request.Builder().url("$baseUrl$path").build()
        client.newCall(req).execute().body?.string()
    } catch (e: Exception) {
        null
    }

    fun post(path: String, body: String): String? = try {
        val rb = body.toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url("$baseUrl$path").post(rb).build()
        client.newCall(req).execute().body?.string()
    } catch (e: Exception) {
        null
    }
}
