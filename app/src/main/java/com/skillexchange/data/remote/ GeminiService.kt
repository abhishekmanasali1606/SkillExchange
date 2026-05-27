package com.skillexchange.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiService {

    private const val API_KEY = "AIzaSyAG2c9XJMnCDxn6yStXlrN9K3iQnutHYwE"
    private const val API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY"

    suspend fun getSkillSuggestion(
        skillOffered: String,
        skillWanted: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a skill advisor for a rural community skill-exchange app in India.
                A user offers the skill: "$skillOffered" and wants to learn: "$skillWanted".
                Suggest ONE specific complementary skill they should learn next to increase 
                their value in the community. Keep the response under 2 sentences, practical, 
                and relevant to rural Indian communities.
            """.trimIndent()

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
            } else {
                "Consider learning a skill that complements $skillOffered — it will increase your swap opportunities."
            }
        } catch (e: Exception) {
            "Consider learning a skill that complements $skillOffered — it will increase your swap opportunities."
        }
    }
}