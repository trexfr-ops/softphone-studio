package com.softphone.studio.network

import com.softphone.studio.model.MessageThread
import com.softphone.studio.model.PhoneNumberItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

/**
 * Result container for raw HTTP responses.
 */
data class HttpResult(
    val statusCode: Int,
    val body: String,
    val cookies: List<String>
)

/**
 * Client for interfacing directly with the live 2NR virtual telecom cloud endpoints.
 */
object NrApiClient {
    private const val BASE_URL = "https://api.2nr.xyz"
    private const val APP_VERSION = "52"

    private fun postJson(endpoint: String, payload: JSONObject, token: String? = null): HttpResult {
        val url = URL("$BASE_URL/$endpoint")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.doOutput = true
        conn.doInput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "okhttp/4.9.2")

        if (!token.isNullOrEmpty()) {
            conn.setRequestProperty("Cookie", "token=$token; x-app-version=$APP_VERSION")
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
            writer.write(payload.toString())
            writer.flush()
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            ?: throw Exception("HTTP $code with no body")

        val responseText = BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }

        // Extract Set-Cookie headers
        val cookies = conn.headerFields.entries
            .filter { it.key != null && it.key.equals("Set-Cookie", ignoreCase = true) }
            .flatMap { it.value }

        if (code !in 200..299) {
            val errorObj = try { JSONObject(responseText) } catch (e: Exception) { null }
            val errorMsg = errorObj?.optString("error") ?: errorObj?.optString("reason") ?: "HTTP $code"
            throw Exception(errorMsg)
        }

        return HttpResult(code, responseText, cookies)
    }

    suspend fun login(email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("id", 101)
                put("query", JSONObject().apply {
                    put("email", email.trim())
                    put("password", pass)
                    put("imei", "358249051111111")
                    put("language", "en")
                })
            }
            val httpResult = postJson("auth/login", payload)

            // 1. Extract token from Set-Cookie header (2NR primary auth transport)
            var extractedToken: String? = null
            for (cookie in httpResult.cookies) {
                val matcher = Regex("""token=([^;]+)""").find(cookie)
                if (matcher != null) {
                    extractedToken = matcher.groupValues[1]
                    break
                }
            }

            // 2. Fallback: check JSON body
            if (extractedToken.isNullOrEmpty()) {
                val json = try { JSONObject(httpResult.body) } catch (e: Exception) { null }
                extractedToken = json?.optString("token")
            }

            if (!extractedToken.isNullOrEmpty()) {
                Result.success(extractedToken)
            } else {
                Result.failure(Exception("Login succeeded but no session token received in response."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, pass: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("id", 103)
                put("query", JSONObject().apply {
                    put("email", email.trim())
                    put("password", pass)
                    put("imei", "358249051111111")
                })
            }
            val httpResult = postJson("auth/register", payload)
            val json = JSONObject(httpResult.body)
            if (json.optBoolean("success", false)) {
                Result.success(true)
            } else {
                Result.failure(Exception("Registration failed: ${httpResult.body}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserNumbers(token: String): Result<List<PhoneNumberItem>> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("id", 302)
            }
            val httpResult = postJson("numbers/getUserNumbers", payload, token)
            val jsonArray = try {
                JSONArray(httpResult.body)
            } catch (e: Exception) {
                JSONObject(httpResult.body).optJSONArray("result") ?: JSONArray()
            }
            val list = mutableListOf<PhoneNumberItem>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val nowMillis = System.currentTimeMillis()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.optString("number_id", item.optString("id", i.toString()))
                val rawNumber = item.optString("number")
                val formatted = if (rawNumber.startsWith("+")) {
                    rawNumber
                } else if (rawNumber.startsWith("48")) {
                    "+$rawNumber"
                } else {
                    "+48 $rawNumber"
                }
                val name = item.optString("name", "PhantomLine ${i + 1}")
                val reservationTo = item.optString("reservation_to", "")

                var calculatedDays = 3
                var formattedExpStr = ""
                if (reservationTo.isNotBlank()) {
                    try {
                        val cleanDateStr = reservationTo.substringBefore(".")
                        val expDate = dateFormat.parse(cleanDateStr)
                        if (expDate != null) {
                            val diffMillis = expDate.time - nowMillis
                            calculatedDays = max(0, (diffMillis / (1000L * 60L * 60L * 24L)).toInt())
                            val displayFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            formattedExpStr = displayFormat.format(expDate)
                        }
                    } catch (_: Exception) {
                        calculatedDays = 3
                    }
                }

                list.add(
                    PhoneNumberItem(
                        id = id,
                        number = formatted,
                        countryTag = "PL",
                        carrierName = "PhantomLine Warsaw [2NR]",
                        daysRemaining = calculatedDays,
                        totalDays = 3,
                        isActive = calculatedDays > 0,
                        expirationDateStr = formattedExpStr
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRandomNumber(token: String): Result<Pair<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("id", 300)
            }
            val httpResult = postJson("numbers/getRandomNumber", payload, token)
            val jsonArray = try {
                JSONArray(httpResult.body)
            } catch (e: Exception) {
                JSONObject(httpResult.body).optJSONArray("result") ?: JSONArray()
            }
            if (jsonArray.length() > 0) {
                val first = jsonArray.getJSONObject(0)
                val rawNumber = first.optString("number")
                val numberId = first.optInt("id")
                val formatted = if (rawNumber.startsWith("+")) {
                    rawNumber
                } else if (rawNumber.startsWith("48")) {
                    "+$rawNumber"
                } else {
                    "+48 $rawNumber"
                }
                Result.success(Pair(formatted, numberId))
            } else {
                Result.failure(Exception("No numbers currently available from carrier pool."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reserveNumber(token: String, numberId: Int, name: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("id", 301)
                put("query", JSONObject().apply {
                    put("number_id", numberId)
                    put("name", name)
                    put("color", "#E63147")
                    put("marketing", true)
                })
            }
            val httpResult = postJson("numbers/reserveNumber", payload, token)
            val json = JSONObject(httpResult.body)
            if (json.optBoolean("success", true)) {
                Result.success(true)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to reserve number.")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extendNumberValidity(token: String, numberId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("id", 405)
                put("query", JSONObject().apply {
                    put("number_id", numberId)
                })
            }
            val httpResult = postJson("sms/extendNumberValidityMessage", payload, token)
            val json = JSONObject(httpResult.body)
            if (json.optBoolean("success", false)) {
                Result.success(true)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to extend validity on carrier.")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSms(token: String): Result<List<MessageThread>> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("id", 400)
            }
            val httpResult = postJson("sms/get", payload, token)
            val jsonArray = try {
                JSONObject(httpResult.body).optJSONArray("result") ?: JSONArray(httpResult.body)
            } catch (e: Exception) {
                JSONArray(httpResult.body)
            }
            val threads = mutableListOf<MessageThread>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.optString("id", i.toString())
                val sender = item.optString("phone", item.optString("sender", "2NR Service"))
                val text = item.optString("body", item.optString("text", ""))
                val timeRaw = item.optString("created_at", "Just now")
                val formattedTime = if (timeRaw.contains("T")) {
                    timeRaw.substringBefore("T")
                } else {
                    timeRaw
                }
                val isUnread = item.optInt("status", 0) == 0
                val numberId = item.optInt("number_id", 0)
                val numObj = item.optJSONObject("number")
                val lineName = numObj?.optString("name") ?: ""
                val recipientLine = if (lineName.isNotBlank()) "To: $lineName" else if (numberId > 0) "To: Line #$numberId" else ""

                threads.add(
                    MessageThread(
                        id = id,
                        title = sender,
                        phoneNumber = sender,
                        lastMessage = text,
                        timestamp = formattedTime,
                        isUnread = isUnread,
                        recipientLine = recipientLine,
                        numberId = if (numberId > 0) numberId else null
                    )
                )
            }
            Result.success(threads)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
