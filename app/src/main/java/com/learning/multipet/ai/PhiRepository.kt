package com.learning.multipet.ai

import com.learning.multipet.BuildConfig
import com.learning.multipet.data.LogEntry
import com.learning.multipet.data.Pet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId

class PhiRepository {

    private val client: OkHttpClient = OkHttpClient()

    suspend fun generatePetCareReply(
        selectedPets: List<Pet>,
        relatedLogs: List<LogEntry>,
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        val prompt: String = buildPrompt(
            selectedPets = selectedPets,
            relatedLogs = relatedLogs,
            userMessage = userMessage
        )

        val payload = JSONObject().apply {
            put("model", BuildConfig.PHI_MODEL)
            put("messages", JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("role", "system")
                        put(
                            "content",
                            """
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
                            """.trimIndent()
                        )
                    }
                )
                put(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    }
                )
            })
            put("temperature", 0.4)
            put("max_tokens", 500)
        }

        val requestBody = payload.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${BuildConfig.PHI_BASE_URL}chat/completions?api-version=2024-05-01-preview")
            .addHeader("Content-Type", "application/json")
            .addHeader("api-key", BuildConfig.PHI_API_KEY)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val rawBody: String = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    return@withContext "Sorry, the AI request failed: HTTP ${response.code} - $rawBody"
                }

                val json = JSONObject(rawBody)
                val choices = json.optJSONArray("choices")

                val reply: String = if (choices != null && choices.length() > 0) {
                    choices.getJSONObject(0)
                        .optJSONObject("message")
                        ?.optString("content")
                        .orEmpty()
                } else {
                    ""
                }

                reply.ifBlank {
                    "Sorry, I couldn't generate a response right now."
                }.trim()
            }
        } catch (e: Exception) {
            "Sorry, I couldn't generate a response right now. ${e.message.orEmpty()}".trim()
        }
    }

    private fun buildPrompt(
        selectedPets: List<Pet>,
        relatedLogs: List<LogEntry>,
        userMessage: String
    ): String {
        val petContext: String = if (selectedPets.isEmpty()) {
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
                    appendLine("Age: ${formatPetAge(pet.birthDateMillis)}")
                    appendLine("Weight (kg): ${pet.weightKg ?: "Unknown"}")
                    appendLine("Vaccinated: ${if (pet.vaccinated) "Yes" else "No"}")
                    appendLine()
                }
            }.trimIndent()
        }

        val logsContext: String = if (relatedLogs.isEmpty()) {
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
            $petContext

            $logsContext

            User question:
            $userMessage
        """.trimIndent()
    }

    private fun formatPetAge(birthDateMillis: Long?): String {
        if (birthDateMillis == null || birthDateMillis <= 0L) {
            return "Unknown"
        }

        return try {
            val birthDate: LocalDate = Instant.ofEpochMilli(birthDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val today: LocalDate = LocalDate.now()
            val period: Period = Period.between(birthDate, today)

            when {
                period.years > 0 && period.months > 0 -> "${period.years} year(s), ${period.months} month(s)"
                period.years > 0 -> "${period.years} year(s)"
                period.months > 0 -> "${period.months} month(s)"
                period.days >= 0 -> "${period.days} day(s)"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}