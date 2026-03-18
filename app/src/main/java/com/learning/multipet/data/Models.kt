package com.learning.multipet.data

import java.time.LocalDate
import java.util.UUID

enum class Species { DOG, CAT }


data class Pet(
    val id: String = java.util.UUID.randomUUID().toString(),
    val species: Species = Species.DOG,
    val name: String = "",
    val breed: String = "",
    val sex: String = "Unknown",
    val ageYears: Int = 1,
    val weightKg: Double? = null,
    val vaccinated: Boolean = false,
    val imageUri: String? = null
)


enum class LogType {
    APPETITE, STOOL, ENERGY, WEIGHT, VACCINE, DEWORM, NOTES
}

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val petId: String,
    val date: LocalDate,
    val type: LogType,
    val note: String = ""
)
