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

class OpenAiService(
    private val apiKey: String
) : AiService {

    private val client = OkHttpClient()

    override suspend fun generateCard(
        name: String,
        interests: String,
        style: String
    ): AiResult = withContext(Dispatchers.IO) {

        val prompt = """
            Create a birthday card for $name.
            The person likes: $interests.
            The art style should be: $style.

            Return ONLY a JSON object with two fields:
            1. "message": a short, warm, and creative birthday message (not starting with "Happy Birthday").
            2. "imagePrompt": a detailed prompt for an image generator (like DALL-E) to create a vibrant, beautiful birthday illustration incorporating their interests and the requested style.
        """.trimIndent()

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val json = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messages)
            put("response_format", JSONObject().apply { put("type", "json_object") })
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }

        val body = response.body?.string().orEmpty()
        Log.d("OpenAI", "Response: $body")

        if (!response.isSuccessful) {
            throw Exception("OpenAI error ${response.code}: $body")
        }

        val responseJson = JSONObject(body)
        val choices = responseJson.optJSONArray("choices") ?: throw Exception("Invalid response: No choices")
        val content = choices.getJSONObject(0).getJSONObject("message").getString("content")

        val parsed = try {
            JSONObject(content)
        } catch (e: Exception) {
            // Fallback if JSON parsing of content fails
            JSONObject().apply {
                put("message", "Wishing you a wonderful birthday!")
                put("imagePrompt", "vibrant birthday celebration with balloons and cake, $style style, $interests theme")
            }
        }

        val message = parsed.optString("message", "Have an amazing day!")
        val imagePrompt = parsed.optString("imagePrompt", "birthday illustration $style $interests")

        val encodedPrompt = URLEncoder.encode(imagePrompt, StandardCharsets.UTF_8.toString())
        val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt"

        AiResult(message, imageUrl)
    }
}