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
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state

    fun addPet(pet: Pet) {
        _state.update { s ->
            val newPets = s.pets + pet
            s.copy(
                pets = newPets,
                selectedPetId = pet.id,
                lastActivePetId = pet.id
            )
        }
    }

    fun updatePet(pet: Pet) {
        _state.update { s ->
            val updated = s.pets.map { if (it.id == pet.id) pet else it }
            s.copy(pets = updated, lastActivePetId = pet.id)
        }
    }

    fun deletePet(petId: String) {
        _state.update { s ->
            val remaining = s.pets.filterNot { it.id == petId }
            val newSelected = if (s.selectedPetId == petId) remaining.firstOrNull()?.id else s.selectedPetId
            s.copy(
                pets = remaining,
                selectedPetId = newSelected,
                lastActivePetId = newSelected
            )
        }
    }

    fun selectPet(petId: String) {
        _state.update { s -> s.copy(selectedPetId = petId, lastActivePetId = petId) }
    }

    fun addLog(entry: LogEntry) {
        _state.update { s ->
            s.copy(
                logs = s.logs + entry,
                lastActivePetId = entry.petId
            )
        }
    }

    fun updateLog(updatedLog: LogEntry) {
        _state.update { s ->
            s.copy(
                logs = s.logs.map { existing ->
                    if (existing.id == updatedLog.id) updatedLog else existing
                },
                lastActivePetId = updatedLog.petId
            )
        }
    }

    fun deleteLog(logId: String) {
        _state.update { s ->
            val logToDelete = s.logs.find { it.id == logId } ?: return@update s

            val remainingLogs = s.logs.filterNot { it.id == logId }

            val updatedPets = s.pets.map { pet ->
                if (pet.id != logToDelete.petId) {
                    pet
                } else {
                    when (logToDelete.type) {
                        LogType.VACCINE -> {
                            val stillHasVaccineLog = remainingLogs.any { entry ->
                                entry.petId == pet.id && entry.type == LogType.VACCINE
                            }
                            pet.copy(vaccinated = stillHasVaccineLog)
                        }
                        else -> pet
                    }
                }
            }

            s.copy(
                pets = updatedPets,
                logs = remainingLogs,
                lastActivePetId = logToDelete.petId
            )
        }
    }

    fun logsFor(date: LocalDate, petId: String?): List<LogEntry> {
        val s = _state.value
        return s.logs.filter { it.date == date && (petId == null || it.petId == petId) }
    }
    fun upsertPet(pet: Pet) {
        _state.update { s ->
            val exists = s.pets.any { it.id == pet.id }
            val newPets = if (exists) {
                s.pets.map { if (it.id == pet.id) pet else it }
            } else {
                s.pets + pet
            }

            s.copy(
                pets = newPets,
                selectedPetId = pet.id,
                lastActivePetId = pet.id
            )
        }
    }

}
