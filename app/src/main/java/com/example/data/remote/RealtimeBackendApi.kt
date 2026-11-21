package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
data class HealthResponse(
    val status: String = "",
    val aiConfigured: Boolean = false,
    val model: String = "",
    val maxSessionSeconds: Int = 600,
    val dailySessionsPerDevice: Int = 5
)

@Serializable
data class AuthSessionResponse(
    val sessionToken: String,
    val expiresInSeconds: Int = 3600
)

@Serializable
data class RealtimeTokenPayload(
    val clientSecret: String,
    val expiresAt: Long = 0L,
    val model: String = "gpt-realtime",
    val rtcUrl: String = "https://api.openai.com/v1/realtime/calls",
    val maxSessionSeconds: Int = 600,
    val remainingSessionsToday: Int = 5
)

@Serializable
data class BackendErrorPayload(
    val error: String = "",
    val message: String = ""
)

sealed interface BackendCallResult<out T> {
    data class Success<T>(val data: T) : BackendCallResult<T>
    data class Failure(val code: String, val messageBn: String) : BackendCallResult<Nothing>
}

class RealtimeBackendApi(
    private val context: Context,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val sdpMediaType = "application/sdp; charset=utf-8".toMediaType()

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun checkHealth(baseUrl: String): BackendCallResult<HealthResponse> =
        withContext(Dispatchers.IO) {
            if (!isNetworkAvailable()) {
                return@withContext BackendCallResult.Failure(
                    "NO_INTERNET",
                    "ইন্টারনেট সংযোগ নেই। অফলাইন লেসন ও শব্দভান্ডার ব্যবহার করুন।"
                )
            }
            val normalized = normalizeUrl(baseUrl)
                ?: return@withContext BackendCallResult.Failure(
                    "INVALID_URL",
                    "সার্ভারের ঠিকানা (URL) সঠিক নয়। সেটিংস থেকে ঠিক করুন।"
                )
            try {
                val request = Request.Builder()
                    .url("$normalized/health")
                    .get()
                    .build()
                httpClient.newCall(request).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        return@withContext BackendCallResult.Failure(
                            "HTTP_${resp.code}",
                            "সার্ভার এই মুহূর্তে সাড়া দিচ্ছে না (${resp.code})।"
                        )
                    }
                    val parsed = json.decodeFromString<HealthResponse>(body)
                    BackendCallResult.Success(parsed)
                }
            } catch (e: IOException) {
                BackendCallResult.Failure(
                    "API_UNAVAILABLE",
                    "সার্ভারের সাথে সংযোগ করা যায়নি। আপনার ব্যাকএন্ড সার্ভার চালু আছে কিনা দেখুন।"
                )
            } catch (e: Exception) {
                BackendCallResult.Failure(
                    "PARSE_ERROR",
                    "সার্ভারের উত্তর বুঝতে সমস্যা হয়েছে।"
                )
            }
        }

    suspend fun fetchRealtimeClientSecret(
        baseUrl: String,
        deviceId: String,
        topic: String,
        learnerName: String,
        completedLessons: Int,
        languageMode: String
    ): BackendCallResult<Pair<String, RealtimeTokenPayload>> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext BackendCallResult.Failure(
                "NO_INTERNET",
                "ইন্টারনেট সংযোগ নেই। অফলাইন লেসন ও শব্দভান্ডার ব্যবহার করুন।"
            )
        }
        val normalized = normalizeUrl(baseUrl)
            ?: return@withContext BackendCallResult.Failure(
                "INVALID_URL",
                "ব্যাকএন্ড সার্ভার URL সেটিংস থেকে সঠিকভাবে যোগ করুন।"
            )

        try {
            // Step 1: Obtain short-lived signed app session token
            val authJson = """{"deviceId":"${escapeJson(deviceId)}"}"""
            val authRequest = Request.Builder()
                .url("$normalized/api/auth/session")
                .post(authJson.toRequestBody(jsonMediaType))
                .build()

            val sessionToken = httpClient.newCall(authRequest).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    return@withContext mapHttpFailure(resp.code, body)
                }
                json.decodeFromString<AuthSessionResponse>(body).sessionToken
            }

            // Step 2: Mint short-lived OpenAI Realtime client secret via authenticated backend endpoint
            val tokenBody = """
                {
                  "topic": "${escapeJson(topic)}",
                  "learnerName": "${escapeJson(learnerName)}",
                  "completedLessons": $completedLessons,
                  "languageMode": "${escapeJson(languageMode)}"
                }
            """.trimIndent()

            val tokenRequest = Request.Builder()
                .url("$normalized/api/realtime/token")
                .header("Authorization", "Bearer $sessionToken")
                .post(tokenBody.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(tokenRequest).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    return@withContext mapHttpFailure(resp.code, body)
                }
                val payload = json.decodeFromString<RealtimeTokenPayload>(body)
                if (payload.clientSecret.isBlank()) {
                    return@withContext BackendCallResult.Failure(
                        "INVALID_TOKEN",
                        "সেশন টোকেন পাওয়া যায়নি। কিছুক্ষণ পর আবার চেষ্টা করুন।"
                    )
                }
                BackendCallResult.Success(sessionToken to payload)
            }
        } catch (e: IOException) {
            BackendCallResult.Failure(
                "API_UNAVAILABLE",
                "AI ভয়েস সার্ভারের সাথে সংযোগ ব্যর্থ হয়েছে। ব্যাকএন্ড সার্ভার ও ইন্টারনেট যাচাই করুন।"
            )
        } catch (e: Exception) {
            BackendCallResult.Failure(
                "UNEXPECTED_ERROR",
                "ভয়েস সেশন শুরু করতে সমস্যা হয়েছে।"
            )
        }
    }

    suspend fun exchangeSdpOffer(
        rtcUrl: String,
        clientSecret: String,
        offerSdp: String
    ): BackendCallResult<String> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext BackendCallResult.Failure(
                "NO_INTERNET",
                "ইন্টারনেট সংযোগ বিচ্ছিন্ন হয়েছে।"
            )
        }
        try {
            val request = Request.Builder()
                .url(rtcUrl)
                .header("Authorization", "Bearer $clientSecret")
                .post(offerSdp.toRequestBody(sdpMediaType))
                .build()

            httpClient.newCall(request).execute().use { resp ->
                val answerSdp = resp.body?.string().orEmpty()
                if (!resp.isSuccessful || answerSdp.isBlank()) {
                    val msg = if (resp.code == 401 || resp.code == 403) {
                        "টোকেনের মেয়াদ শেষ বা বাতিল হয়েছে। আবার কল শুরু করুন।"
                    } else {
                        "ভয়েস কল সংযোগ স্থাপন করা যায়নি (${resp.code})।"
                    }
                    return@withContext BackendCallResult.Failure("SDP_${resp.code}", msg)
                }
                BackendCallResult.Success(answerSdp)
            }
        } catch (e: IOException) {
            BackendCallResult.Failure(
                "WEBRTC_NETWORK_ERROR",
                "WebRTC সংযোগ স্থাপন ব্যর্থ হয়েছে। ইন্টারনেট পরীক্ষা করুন।"
            )
        }
    }

    suspend fun reportUsage(baseUrl: String, sessionToken: String, durationSeconds: Int) {
        val normalized = normalizeUrl(baseUrl) ?: return
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = """{"durationSeconds":${durationSeconds.coerceAtLeast(0)}}"""
                val req = Request.Builder()
                    .url("$normalized/api/usage/report")
                    .header("Authorization", "Bearer $sessionToken")
                    .post(payload.toRequestBody(jsonMediaType))
                    .build()
                httpClient.newCall(req).execute().close()
            }
        }
    }

    private fun mapHttpFailure(code: Int, body: String): BackendCallResult.Failure {
        val parsed = runCatching { json.decodeFromString<BackendErrorPayload>(body) }.getOrNull()
        val msgBn = when (parsed?.error) {
            "AI_NOT_CONFIGURED" ->
                "ব্যাকএন্ড সার্ভারে OpenAI API কী সেট করা হয়নি। README নির্দেশনা অনুযায়ী কনফিগার করুন।"
            "DEVICE_DAILY_LIMIT", "GLOBAL_DAILY_CAP" ->
                "আজকের AI ভয়েস সেশনের সীমা পূর্ণ হয়েছে। অফলাইন লেসন ও উচ্চারণ অনুশীলন ব্যবহার করুন।"
            "INVALID_TOKEN", "UNAUTHORIZED" ->
                "সেশন টোকেন বাতিল বা মেয়াদোত্তীর্ণ হয়েছে। আবার সংযোগ করুন।"
            "RATE_LIMITED" ->
                "খুব দ্রুত অনুরোধ পাঠানো হয়েছে। অনুগ্রহ করে ১ মিনিট অপেক্ষা করুন।"
            else -> when (code) {
                401, 403 -> "সেশন অনুমোদন ব্যর্থ হয়েছে।"
                429 -> "সার্ভার সীমা অতিক্রম করেছে। কিছুক্ষণ পর আবার চেষ্টা করুন।"
                503 -> "AI ভয়েস সেবা এখন প্রস্তুত নয়।"
                else -> "সার্ভারের সাথে সংযোগ করা যায়নি ($code)।"
            }
        }
        return BackendCallResult.Failure(parsed?.error?.ifBlank { "HTTP_$code" } ?: "HTTP_$code", msgBn)
    }

    private fun normalizeUrl(raw: String): String? {
        val trimmed = raw.trim().trimEnd('/')
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) return null
        return trimmed
    }

    private fun escapeJson(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
}
