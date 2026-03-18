package com.learning.multipet.data

import java.time.LocalDate
import java.util.UUID

enum class Species { DOG, CAT }

data class Pet(
    val id: String = UUID.randomUUID().toString(),
    val species: Species = Species.DOG,
    val name: String = "",
    val breed: String = "",
    val sex: String = "Unknown", // Male/Female/Unknown
    val ageYears: Int = 1,
    val weightKg: Double = 1.0,
    val vaccinated: Boolean = false,
    val lastDewormDate: LocalDate? = null
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
