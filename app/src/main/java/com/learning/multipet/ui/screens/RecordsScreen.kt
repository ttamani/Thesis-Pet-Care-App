package com.learning.multipet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learning.multipet.ui.AppColors
import com.learning.multipet.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun RecordsScreen(vm: AppViewModel, onGoManage: () -> Unit) {
    val state by vm.state.collectAsState()
    val selectedPet = state.selectedPetId?.let { id -> state.pets.find { it.id == id } }

    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val logs = remember(state.logs, selectedDate, selectedPet?.id) {
        state.logs.filter { it.date == selectedDate && (selectedPet == null || it.petId == selectedPet.id) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Records", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        if (state.pets.isEmpty()) {
            ElevatedCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Add a pet first")
                    Text("Records and logs are linked to pets.")
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onGoManage) { Text("Go to Manage Pets") }
                }
            }
            return
        }

        MonthHeader(month = month, onPrev = { month = month.minusMonths(1) }, onNext = { month = month.plusMonths(1) })
        Spacer(Modifier.height(8.dp))

        SimpleCalendar(
            month = month,
            selected = selectedDate,
            hasLog = { date -> state.logs.any { it.date == date } },
            onSelect = { selectedDate = it }
        )

        Spacer(Modifier.height(12.dp))

        Text("Logs for ${selectedDate}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (logs.isEmpty()) {
            ElevatedCard {
                Column(Modifier.padding(16.dp)) {
                    Text("No logs for this date", style = MaterialTheme.typography.titleMedium)
                    Text("Add logs from Home quick actions (hook up later) or extend this screen with + Add Log.")
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(logs) { entry ->
                    ElevatedCard {
                        Column(Modifier.padding(14.dp)) {
                            Text(entry.type.name, style = MaterialTheme.typography.titleSmall)
                            Text(entry.note.ifBlank { "No details" }, color = AppColors.TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onPrev) { Text("‹") }
        Text("${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}")
        TextButton(onClick = onNext) { Text("›") }
    }
}

@Composable
private fun SimpleCalendar(
    month: YearMonth,
    selected: LocalDate,
    hasLog: (LocalDate) -> Boolean,
    onSelect: (LocalDate) -> Unit
) {
    val first = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = (first.dayOfWeek.value % 7) // Sun=0 style
    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat").forEach { Text(it, style = MaterialTheme.typography.labelSmall) }
        }
        Spacer(Modifier.height(6.dp))

        for (row in 0 until totalCells step 7) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (col in 0..6) {
                    val cell = row + col
                    val dayNum = cell - startOffset + 1
                    val date = if (dayNum in 1..daysInMonth) month.atDay(dayNum) else null
                    val isSelected = date == selected
                    val dot = date != null && hasLog(date)

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(enabled = date != null) { onSelect(date!!) },
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        if (date != null) {
                            AssistChip(
                                onClick = { onSelect(date) },
                                label = { Text(dayNum.toString()) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                )
                            )
                            if (dot) {
                                Text("•", modifier = Modifier.padding(top = 28.dp), color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
