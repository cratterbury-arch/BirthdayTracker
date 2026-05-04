package com.chris.birthdaytracker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

data class PixabayImage(
    val id: Int,
    val imageUrl: String,
    val previewUrl: String,
    val user: String
)

class PixabayService(
    private val apiKey: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun searchImages(
        query: String,
        page: Int = 1
    ): List<PixabayImage> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw Exception("PIXABAY_API_KEY is missing. Add it to gradle.properties.")
        }

        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())

        val url =
            "https://pixabay.com/api/?key=$apiKey&q=$encodedQuery&image_type=illustration&orientation=vertical&per_page=30&page=$page&safesearch=true"

        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw Exception("Pixabay error ${response.code}: $body")
        }

        val json = JSONObject(body)
        val hits = json.getJSONArray("hits")

        buildList {
            for (i in 0 until hits.length()) {
                val item = hits.getJSONObject(i)

                add(
                    PixabayImage(
                        id = item.getInt("id"),
                        imageUrl = item.optString("largeImageURL"),
                        previewUrl = item.optString("previewURL"),
                        user = item.optString("user")
                    )
                )
            }
        }
    }
}