package com.chris.birthdaytracker

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class GeminiAiService(
    private val apiKey: String,
    private val modelName: String = "gemini-1.5-flash"
) : AiService {

    private val generativeModel = GenerativeModel(
        modelName = modelName,
        apiKey = apiKey,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH)
        )
    )

    override suspend fun generateCard(
        name: String,
        interests: String,
        style: String
    ): AiResult = withContext(Dispatchers.IO) {
        
        val prompt = """
            Create a creative birthday card concept for $name.
            The person likes: $interests.
            The art style should be: $style.

            Return a JSON object with two fields:
            1. "message": a short, warm, and creative birthday message (not starting with "Happy Birthday").
            2. "imagePrompt": a detailed prompt for an image generator. 
               CRITICAL: Do NOT include any text, names, or words in the image prompt description. 
               Focus on vibrant, beautiful, and symbolic illustrations that capture their interests and the $style style.
            
            Example output format:
            {
              "message": "Hope your day is as legendary as a rare Pokemon find!",
              "imagePrompt": "A vibrant watercolor illustration of a festive outdoor party with cute monsters wearing party hats, surrounded by colorful balloons, floating confetti, and a giant sparkling cake"
            }
        """.trimIndent()

        try {
            Log.d("GeminiAI", "Generating with model: $modelName")
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: throw Exception("Empty response from Gemini")
            
            Log.d("GeminiAI", "Response: $text")
            
            val parsed = JSONObject(text)
            val message = parsed.optString("message", "Wishing you a wonderful birthday!")
            val imagePrompt = parsed.optString("imagePrompt", "birthday illustration $style $interests")

            val encodedPrompt = URLEncoder.encode(imagePrompt, StandardCharsets.UTF_8.toString())
            // Use Pollinations AI for image generation
            val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt%2C+no+text%2C+no+letters%2C+artistic+background"

            AiResult(message, imageUrl)
            
        } catch (e: Exception) {
            Log.e("GeminiAI", "Error generating card", e)
            throw e
        }
    }
}