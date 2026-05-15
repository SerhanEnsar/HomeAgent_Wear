package com.serhanensar.agentjee.data.network

import android.content.Context
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object ApiClient {
    private var baseUrl = "http://AgentJee.local:8000"
    // API key is read at runtime from SharedPreferences (set via the app's settings).
    // Never hardcode credentials in source code.
    private var apiKey: String = ""

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    /** Call once on startup (e.g. in MainActivity) to load saved credentials. */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences("agentjee_prefs", Context.MODE_PRIVATE)
        baseUrl = prefs.getString("base_url", "http://AgentJee.local:8000") ?: "http://AgentJee.local:8000"
        apiKey  = prefs.getString("api_key", "") ?: ""
    }

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
    }

    fun get(path: String): String? = try {
        val url = "$baseUrl$path".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("api_key", apiKey)
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
            ?.addQueryParameter("api_key", apiKey)
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
