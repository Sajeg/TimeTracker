package com.sajeg.timetracker.classes

import com.sajeg.timetracker.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class FeedbackManager {
    fun sendDiscordMessage(message: String) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("content", message)
            .build()
        val request = Request.Builder().apply {
            url("https://discord.com/api/v9/channels/1308191480623403079/messages")
            post(body)
            addHeader("Content-Type", "application/json")
            addHeader("authorization", "Bot ${BuildConfig.botToken}")
        }.build()
        CoroutineScope(Dispatchers.IO).launch {
            val response = client.newCall(request).execute()
        }
    }
}