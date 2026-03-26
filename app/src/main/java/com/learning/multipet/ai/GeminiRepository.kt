package com.learning.multipet.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.learning.multipet.BuildConfig

class GeminiRepository {

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generatePetCareReply(
        petName: String?,
        species: String?,
        userMessage: String
    ): String {
        val prompt = """
            You are a pet-care assistant for cats and dogs.
            Give only general and safety-focused pet-care guidance.
            Do not diagnose diseases.
            Do not provide medication dosages.
            Do not prescribe treatment.
            Do not recommend human medicine.
            If symptoms seem severe, urgent, or persistent, advise consulting a veterinarian.

            Pet name: ${petName ?: "Unknown"}
            Species: ${species ?: "Unknown"}

            User question:
            $userMessage
        """.trimIndent()

        val response = model.generateContent(prompt)
        return response.text ?: "Gemini returned null"
    }
}