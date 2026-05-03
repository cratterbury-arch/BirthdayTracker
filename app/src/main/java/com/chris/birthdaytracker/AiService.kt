package com.chris.birthdaytracker

interface AiService {
    suspend fun generateCard(
        name: String,
        interests: String,
        style: String
    ): AiResult
}

data class AiResult(
    val message: String,
    val imageUrl: String
)