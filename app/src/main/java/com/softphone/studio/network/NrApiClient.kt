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

object NrApiClient {
    private const val BASE_URL = "https://api.2nr.xyz"
    private const val APP_VERSION = "52"

    private fun postJson(endpoint: String, payload: JSONObject, token: String? = null): String {
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
        if (code !in 200..299) {
            val errorObj = try { JSONObject(responseText) } catch (e: Exception) { null }
            val errorMsg = errorObj?.optString("error") ?: errorObj?.optString("reason") ?: "HTTP $code"
            throw Exception(errorMsg)
        }

        return responseText
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
            val response = postJson("auth/login", payload)
            val json = JSONObject(response)
            val token = json.optString("token")
            if (token.isNotEmpty()) {
                Result.success(token)
            } else {
                Result.failure(Exception("Login succeeded but no token received."))
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
            val response = postJson("auth/register", payload)
            val json = JSONObject(response)
            if (json.optBoolean("success", false)) {
                Result.success(true)
            } else {
                Result.failure(Exception("Registration failed: $response"))
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
            val response = postJson("numbers/getUserNumbers", payload, token)
            val jsonArray = JSONArray(response)
            val list = mutableListOf<PhoneNumberItem>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.optString("id", i.toString())
                val rawNumber = item.optString("number")
                val formatted = if (rawNumber.startsWith("+")) rawNumber else "+48 $rawNumber"
                val name = item.optString("name", "2NR Line ${i + 1}")
                val daysLeft = item.optInt("days_to_expire", 7)
                list.add(PhoneNumberItem(id, formatted, "PL", name, daysLeft))
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
            val response = postJson("numbers/getRandomNumber", payload, token)
            val jsonArray = JSONArray(response)
            if (jsonArray.length() > 0) {
                val first = jsonArray.getJSONObject(0)
                val rawNumber = first.optString("number")
                val numberId = first.optInt("id")
                val formatted = if (rawNumber.startsWith("+")) rawNumber else "+48 $rawNumber"
                Result.success(Pair(formatted, numberId))
            } else {
                Result.failure(Exception("No numbers currently available from 2NR pool."))
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
                    put("color", "default")
                    put("marketing", false)
                })
            }
            val response = postJson("numbers/reserveNumber", payload, token)
            val json = JSONObject(response)
            if (json.optBoolean("success", true)) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to reserve number."))
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
            val response = postJson("sms/get", payload, token)
            val jsonArray = JSONArray(response)
            val threads = mutableListOf<MessageThread>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.optString("id", i.toString())
                val sender = item.optString("sender", "2NR Service")
                val text = item.optString("text", "")
                val time = item.optString("created_at", "Just now")
                threads.add(
                    MessageThread(
                        id = id,
                        title = sender,
                        phoneNumber = sender,
                        lastMessage = text,
                        timestamp = time,
                        isUnread = item.optBoolean("unread", false)
                    )
                )
            }
            Result.success(threads)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
