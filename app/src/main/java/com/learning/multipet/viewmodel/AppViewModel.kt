package com.learning.multipet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learning.multipet.ai.PhiRepository
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
    private val phiRepository: PhiRepository = PhiRepository()
) : ViewModel() {

    val state: StateFlow<AppState> = repo.state

    private val _chatMessages: MutableStateFlow<List<ChatMessage>> = MutableStateFlow(
        listOf(
            ChatMessage(
                role = ChatRole.AI,
                text = "Hi! I can provide general and safety-focused pet-care guidance. Choose one or more pets above, or ask in general mode."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun addPet(pet: Pet) {
        repo.addPet(pet)
    }

    fun updatePet(pet: Pet) {
        repo.updatePet(pet)
    }

    fun deletePet(petId: String) {
        repo.deletePet(petId)
    }

    fun selectPet(petId: String) {
        repo.selectPet(petId)
    }

    fun upsertPet(pet: Pet) {
        repo.upsertPet(pet)
    }

    fun addLog(
        petId: String,
        date: LocalDate,
        type: LogType,
        note: String,
        createdAtMillis: Long = System.currentTimeMillis()
    ) {
        repo.addLog(
            LogEntry(
                petId = petId,
                date = date,
                type = type,
                note = note,
                createdAtMillis = createdAtMillis
            )
        )
    }

    fun updateLog(updatedLog: LogEntry) {
        repo.updateLog(updatedLog)
    }

    fun deleteLog(logId: String) {
        repo.deleteLog(logId)
    }

    fun sendAiMessage(
        selectedPetIds: Set<String>,
        userMessage: String
    ) {
        val trimmedMessage: String = userMessage.trim()
        if (trimmedMessage.isEmpty()) {
            return
        }

        _chatMessages.value = _chatMessages.value + ChatMessage(
            role = ChatRole.USER,
            text = trimmedMessage
        )

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val currentState: AppState = state.value
                val selectedPets: List<Pet> = currentState.pets.filter { it.id in selectedPetIds }

                val relatedLogs: List<LogEntry> = if (selectedPets.isEmpty()) {
                    currentState.logs
                        .sortedByDescending { it.createdAtMillis }
                        .take(12)
                } else {
                    val selectedIds: Set<String> = selectedPets.map { it.id }.toSet()
                    currentState.logs
                        .filter { it.petId in selectedIds }
                        .sortedByDescending { it.createdAtMillis }
                        .take(12)
                }

                val reply: String = phiRepository.generatePetCareReply(
                    selectedPets = selectedPets,
                    relatedLogs = relatedLogs,
                    userMessage = trimmedMessage
                )

                _chatMessages.value = _chatMessages.value + ChatMessage(
                    role = ChatRole.AI,
                    text = reply
                )
            } catch (exception: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    role = ChatRole.AI,
                    text = "Error: ${exception.message ?: "Unknown error"}"
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
                text = "Hi! I can provide general and safety-focused pet-care guidance. Choose one or more pets above, or ask in general mode."
            )
        )
    }
}