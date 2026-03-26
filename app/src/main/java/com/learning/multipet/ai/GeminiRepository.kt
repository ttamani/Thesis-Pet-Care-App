package com.learning.multipet.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.learning.multipet.BuildConfig
import com.learning.multipet.data.LogEntry
import com.learning.multipet.data.Pet

class GeminiRepository {

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generatePetCareReply(
        selectedPets: List<Pet>,
        relatedLogs: List<LogEntry>,
        userMessage: String
    ): String {
        val prompt = buildPrompt(
            selectedPets = selectedPets,
            relatedLogs = relatedLogs,
            userMessage = userMessage
        )

        val response = model.generateContent(prompt)
        return response.text?.trim().orEmpty()
            .ifBlank { "Sorry, I couldn't generate a response right now." }
    }

    private fun buildPrompt(
        selectedPets: List<Pet>,
        relatedLogs: List<LogEntry>,
        userMessage: String
    ): String {
        val petContext = if (selectedPets.isEmpty()) {
            """
            Chat context:
            No specific pets were selected.
            Treat this as a general pet-care question for cats and dogs unless the user message says otherwise.
            """.trimIndent()
        } else {
            buildString {
                appendLine("Selected pets:")
                selectedPets.forEachIndexed { index, pet ->
                    appendLine("${index + 1}.")
                    appendLine("Name: ${pet.name.ifBlank { "Unknown" }}")
                    appendLine("Species: ${pet.species.name}")
                    appendLine("Breed: ${pet.breed.ifBlank { "Unknown" }}")
                    appendLine("Sex: ${pet.sex.ifBlank { "Unknown" }}")
                    appendLine("Age (years): ${pet.ageYears}")
                    appendLine("Weight (kg): ${pet.weightKg ?: "Unknown"}")
                    appendLine("Vaccinated: ${if (pet.vaccinated) "Yes" else "No"}")
                    appendLine()
                }
            }.trimIndent()
        }

        val logsContext = if (relatedLogs.isEmpty()) {
            "Recent logs: No recent logs available for the selected context."
        } else {
            buildString {
                appendLine("Recent logs related to the selected context:")
                relatedLogs
                    .sortedByDescending { it.date }
                    .take(12)
                    .forEach { log ->
                        appendLine(
                            "- Date: ${log.date} | Pet ID: ${log.petId} | Type: ${log.type.name} | Note: ${log.note}"
                        )
                    }
            }.trimIndent()
        }

        return """
            You are a pet-care assistant for cats and dogs.

            Your role:
            - Give only general, safety-focused pet-care guidance.
            - Use the provided pet profiles and recent logs as context.
            - If multiple pets are selected, consider the group context carefully and mention which advice applies broadly versus individually when needed.
            - Keep the answer clear, practical, and friendly.

            Safety rules:
            - Do not diagnose diseases.
            - Do not confirm that a pet has a specific illness.
            - Do not provide medication dosages.
            - Do not prescribe treatment.
            - Do not recommend human medicine.
            - If symptoms seem severe, urgent, unusual, or persistent, advise consulting a veterinarian.

            $petContext

            $logsContext

            User question:
            $userMessage
        """.trimIndent()
    }
}