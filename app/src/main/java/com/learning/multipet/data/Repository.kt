package com.learning.multipet.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class AppState(
    val pets: List<Pet> = emptyList(),
    val selectedPetId: String? = null,
    val logs: List<LogEntry> = emptyList(),
    val lastActivePetId: String? = null
)

class Repository {
    private val _state: MutableStateFlow<AppState> = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state

    fun addPet(pet: Pet) {
        _state.update { currentState ->
            val updatedPets: List<Pet> = currentState.pets + pet
            currentState.copy(
                pets = updatedPets,
                selectedPetId = pet.id,
                lastActivePetId = pet.id
            )
        }
    }

    fun updatePet(pet: Pet) {
        _state.update { currentState ->
            val updatedPets: List<Pet> = currentState.pets.map { existingPet ->
                if (existingPet.id == pet.id) {
                    pet
                } else {
                    existingPet
                }
            }
            currentState.copy(
                pets = updatedPets,
                selectedPetId = pet.id,
                lastActivePetId = pet.id
            )
        }
    }

    fun deletePet(petId: String) {
        _state.update { currentState ->
            val remainingPets: List<Pet> = currentState.pets.filterNot { it.id == petId }
            val remainingLogs: List<LogEntry> = currentState.logs.filterNot { it.petId == petId }
            val newSelectedPetId: String? = if (currentState.selectedPetId == petId) {
                remainingPets.firstOrNull()?.id
            } else {
                currentState.selectedPetId
            }

            currentState.copy(
                pets = remainingPets,
                logs = remainingLogs,
                selectedPetId = newSelectedPetId,
                lastActivePetId = newSelectedPetId
            )
        }
    }

    fun selectPet(petId: String) {
        _state.update { currentState ->
            currentState.copy(
                selectedPetId = petId,
                lastActivePetId = petId
            )
        }
    }

    fun addLog(entry: LogEntry) {
        val stampedEntry: LogEntry = entry.copy(
            createdAtMillis = if (entry.createdAtMillis <= 0L) {
                System.currentTimeMillis()
            } else {
                entry.createdAtMillis
            }
        )

        _state.update { currentState ->
            currentState.copy(
                logs = currentState.logs + stampedEntry,
                lastActivePetId = stampedEntry.petId
            )
        }
    }

    fun updateLog(updatedLog: LogEntry) {
        _state.update { currentState ->
            currentState.copy(
                logs = currentState.logs.map { existingLog ->
                    if (existingLog.id == updatedLog.id) {
                        updatedLog
                    } else {
                        existingLog
                    }
                },
                lastActivePetId = updatedLog.petId
            )
        }
    }

    fun deleteLog(logId: String) {
        _state.update { currentState ->
            val logToDelete: LogEntry = currentState.logs.find { it.id == logId } ?: return@update currentState
            val remainingLogs: List<LogEntry> = currentState.logs.filterNot { it.id == logId }

            val updatedPets: List<Pet> = currentState.pets.map { pet ->
                if (pet.id != logToDelete.petId) {
                    pet
                } else {
                    when (logToDelete.type) {
                        LogType.VACCINE -> {
                            val stillHasVaccineLog: Boolean = remainingLogs.any { entry ->
                                entry.petId == pet.id && entry.type == LogType.VACCINE
                            }
                            pet.copy(vaccinated = stillHasVaccineLog)
                        }
                        else -> pet
                    }
                }
            }

            currentState.copy(
                pets = updatedPets,
                logs = remainingLogs,
                lastActivePetId = logToDelete.petId
            )
        }
    }

    fun logsFor(date: LocalDate, petId: String?): List<LogEntry> {
        val currentState: AppState = _state.value
        return currentState.logs.filter { entry ->
            entry.date == date && (petId == null || entry.petId == petId)
        }
    }

    fun upsertPet(pet: Pet) {
        _state.update { currentState ->
            val exists: Boolean = currentState.pets.any { it.id == pet.id }
            val updatedPets: List<Pet> = if (exists) {
                currentState.pets.map { existingPet ->
                    if (existingPet.id == pet.id) {
                        pet
                    } else {
                        existingPet
                    }
                }
            } else {
                currentState.pets + pet
            }

            currentState.copy(
                pets = updatedPets,
                selectedPetId = pet.id,
                lastActivePetId = pet.id
            )
        }
    }
}