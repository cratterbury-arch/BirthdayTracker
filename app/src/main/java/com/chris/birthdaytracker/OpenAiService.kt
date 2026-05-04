package com.chris.birthdaytracker

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class OpenAiService(
    private val apiKey: String
) : AiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun generateCard(
        name: String,
        interests: String,
        style: String
    ): AiResult = withContext(Dispatchers.IO) {

        if (apiKey.isBlank()) {
            throw Exception("OPENAI_API_KEY is missing. Add it to gradle.properties.")
        }

        val safeInterests = interests.ifBlank { "birthday cake, balloons, colourful celebration" }

        val prompt = """
            Create a birthday card concept for $name.

            Interests: $safeInterests
            Art style: $style

            Return ONLY valid JSON in this exact format:
            {
              "message": "short warm birthday message",
              "imagePrompt": "detailed image prompt"
            }

            The message must not start with "Happy Birthday".
            The image prompt must describe a birthday card illustration.
            Do not ask for text, letters, names, or readable writing inside the image.
        """.trimIndent()

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", "You create JSON only. No markdown. No extra text.")
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val json = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messages)
            put("temperature", 0.9)
            put("response_format", JSONObject().apply {
                put("type", "json_object")
            })
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string().orEmpty()

        Log.d("OpenAI", "HTTP ${response.code}")
        Log.d("OpenAI", body)

        if (!response.isSuccessful) {
            throw Exception("OpenAI error ${response.code}: $body")
        }

        val responseJson = JSONObject(body)
        val content = responseJson
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        val parsed = JSONObject(content)

        val message = parsed.optString(
            "message",
            "Wishing you a wonderful birthday filled with joy!"
        )

        val imagePrompt = parsed.optString(
            "imagePrompt",
            "beautiful birthday card illustration with cake, balloons and confetti, $style style"
        )

        val finalImagePrompt = """
            $imagePrompt,
            birthday card artwork,
            no readable text,
            no letters,
            no watermark,
            vibrant,
            polished,
            high quality
        """.trimIndent()

        val encodedPrompt = URLEncoder.encode(
            finalImagePrompt,
            StandardCharsets.UTF_8.toString()
        )

        val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt"

        AiResult(
            message = message,
            imageUrl = imageUrl
        )
    }
}