package com.learning.multipet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.multipet.ai.GeminiRepository
import com.learning.multipet.data.AppState
import com.learning.multipet.data.LogEntry
import com.learning.multipet.data.LogType
import com.learning.multipet.data.Pet
import com.learning.multipet.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ChatMessage(
    val role: ChatRole,
    val text: String
)

enum class ChatRole {
    USER,
    AI
}

class AppViewModel(
    private val repo: Repository = Repository(),
    private val geminiRepository: GeminiRepository = GeminiRepository()
) : ViewModel() {

    val state: StateFlow<AppState> = repo.state

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                role = ChatRole.AI,
                text = "Hi! I can provide general and safety-focused pet-care guidance."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun addPet(pet: Pet) = repo.addPet(pet)
    fun updatePet(pet: Pet) = repo.updatePet(pet)
    fun deletePet(petId: String) = repo.deletePet(petId)
    fun selectPet(petId: String) = repo.selectPet(petId)

    fun addLog(petId: String, date: LocalDate, type: LogType, note: String) {
        repo.addLog(LogEntry(petId = petId, date = date, type = type, note = note))
    }

    fun updateLog(updatedLog: LogEntry) {
        repo.updateLog(updatedLog)
    }

    fun deleteLog(logId: String) {
        repo.deleteLog(logId)
    }

    fun resolvedPetForAI(): Pet? {
        val s = state.value
        val pets = s.pets
        if (pets.isEmpty()) return null
        if (pets.size == 1) return pets.first()

        val id = s.lastActivePetId ?: s.selectedPetId ?: pets.first().id
        return pets.find { it.id == id } ?: pets.first()
    }

    fun upsertPet(pet: Pet) = repo.upsertPet(pet)

    fun sendAiMessage(
        petName: String?,
        species: String?,
        userMessage: String
    ) {
        val trimmed = userMessage.trim()
        if (trimmed.isEmpty()) return

        _chatMessages.value = _chatMessages.value + ChatMessage(
            role = ChatRole.USER,
            text = trimmed
        )

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val reply = geminiRepository.generatePetCareReply(
                    petName = petName,
                    species = species,
                    userMessage = trimmed
                )

                _chatMessages.value = _chatMessages.value + ChatMessage(
                    role = ChatRole.AI,
                    text = reply
                )
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    role = ChatRole.AI,
                    text = "Error: ${e.message ?: "Unknown error"}"
                )
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                role = ChatRole.AI,
                text = "Hi! I can provide general and safety-focused pet-care guidance."
            )
        )
    }
}