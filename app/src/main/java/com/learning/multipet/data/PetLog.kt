package com.learning.multipet.data

import java.time.LocalDate

data class PetLog(
    val petId: String,
    val date: LocalDate,
    val type: LogType,
    val score: Int? = null,
    val note: String = ""
)
