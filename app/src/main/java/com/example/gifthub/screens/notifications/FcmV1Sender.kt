package com.example.gifthub.notifications

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.auth.oauth2.GoogleCredentials
import org.json.JSONObject
import java.io.InputStream
import java.util.Collections

object FcmV1Sender {

    private const val PROJECT_ID = "gifthub-123"
    private const val FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private fun getAccessToken(context: Context): String {
        val input: InputStream = context.assets.open("fcm-service-account.json")
        val credentials = GoogleCredentials.fromStream(input)
            .createScoped(Collections.singletonList(FCM_SCOPE))
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    fun sendDataPush(
        context: Context,
        toToken: String,
        title: String,
        body: String,
        targetRoute: String = "order_history",
        notificationId: String = "",
        orderId: String = "",
        type: String = "order_update",
        onResult: (Boolean, String) -> Unit
    ) {
        if (toToken.isBlank()) {
            onResult(false, "Empty token")
            return
        }

        Thread {
            try {
                val accessToken = getAccessToken(context)

                val data = JSONObject().apply {
                    put("title", title)
                    put("body", body)
                    put("targetRoute", targetRoute)
                    put("notificationId", notificationId)
                    put("orderId", orderId)
                    put("type", type)
                }

                val message = JSONObject().apply {
                    put("token", toToken)
                    put("data", data)
                    put("android", JSONObject().apply {
                        put("priority", "high")
                    })
                }

                val root = JSONObject().apply {
                    put("message", message)
                }

                val req = Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .post(root.toString().toRequestBody(jsonType))
                    .build()

                client.newCall(req).execute().use { resp ->
                    val bodyStr = resp.body?.string().orEmpty()
                    onResult(resp.isSuccessful, "HTTP ${resp.code}: $bodyStr")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Unknown error")
            }
        }.start()
    }
}