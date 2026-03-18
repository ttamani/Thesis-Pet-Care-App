package com.learning.multipet.viewmodel

import androidx.lifecycle.ViewModel
import com.learning.multipet.data.AppState
import com.learning.multipet.data.LogEntry
import com.learning.multipet.data.LogType
import com.learning.multipet.data.Pet
import com.learning.multipet.data.Repository
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class AppViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    val state: StateFlow<AppState> = repo.state

    fun addPet(pet: Pet) = repo.addPet(pet)
    fun updatePet(pet: Pet) = repo.updatePet(pet)
    fun deletePet(petId: String) = repo.deletePet(petId)
    fun selectPet(petId: String) = repo.selectPet(petId)

    fun addLog(petId: String, date: LocalDate, type: LogType, note: String) {
        repo.addLog(LogEntry(petId = petId, date = date, type = type, note = note))
    }

    // aayusin pa
    fun resolvedPetForAI(): Pet? {
        val s = state.value
        val pets = s.pets
        if (pets.isEmpty()) return null
        if (pets.size == 1) return pets.first()

        // Unang makita sa screen sa pet  homescreen lastActive -> selected -> first
        val id = s.lastActivePetId ?: s.selectedPetId ?: pets.first().id
        return pets.find { it.id == id } ?: pets.first()
    }


    fun upsertPet(pet: Pet) = repo.upsertPet(pet)
}
