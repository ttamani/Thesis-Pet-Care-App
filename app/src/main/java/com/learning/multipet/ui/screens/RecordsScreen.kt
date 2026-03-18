package com.learning.multipet.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.learning.multipet.ui.AppColors
import com.learning.multipet.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun RecordsScreen(
    vm: AppViewModel,
    onGoManage: () -> Unit
) {
    val state by vm.state.collectAsState()

    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // filter: null = all pets
    var filterPetId by remember { mutableStateOf<String?>(null) }

    val logsForDate = remember(state.logs, selectedDate, filterPetId) {
        state.logs
            .filter { it.date == selectedDate }
            .filter { filterPetId == null || it.petId == filterPetId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Records", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.weight(1f))
            AssistChip(
                onClick = { /* later: Add Log */ },
                label = { Text("Add Log") },
                leadingIcon = { Icon(Icons.Filled.EventNote, contentDescription = null) }
            )
        }

        Spacer(Modifier.height(10.dp))

        if (state.pets.isEmpty()) {
            NiceEmptyCard(
                title = "Add a pet first",
                subtitle = "Records and logs are linked to pets. Add your DOG/CAT to begin.",
                buttonText = "Go to Manage Pets",
                onClick = onGoManage
            )
            return
        }

        // Pet Filter Chips
        Text("Filter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = filterPetId == null,
                onClick = { filterPetId = null },
                label = { Text("All Pets") }
            )
            // show a few pets as chips; you can make this scroll later if many
            state.pets.take(3).forEach { p ->
                FilterChip(
                    selected = filterPetId == p.id,
                    onClick = { filterPetId = p.id },
                    label = {
                        Text(
                            p.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Month header
        MonthHeaderModern(
            month = month,
            onPrev = { month = month.minusMonths(1) },
            onNext = { month = month.plusMonths(1) }
        )

        Spacer(Modifier.height(10.dp))

        // Calendar
        ModernCalendar(
            month = month,
            selected = selectedDate,
            hasLog = { date ->
                state.logs.any { it.date == date && (filterPetId == null || it.petId == filterPetId) }
            },
            onSelect = { selectedDate = it }
        )

        Spacer(Modifier.height(14.dp))

        // Logs header
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Logs • $selectedDate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.weight(1f))
            AssistChip(
                onClick = { /* later: jump to today */ selectedDate = LocalDate.now() },
                label = { Text("Today") }
            )
        }

        Spacer(Modifier.height(10.dp))

        if (logsForDate.isEmpty()) {
            NiceEmptyCard(
                title = "No logs for this date",
                subtitle = "Use the Quick Logs from Home, or add a log here (optional).",
                buttonText = "Go to Home",
                onClick = { /* optional: navigate */ }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(logsForDate) { entry ->
                    val petName = state.pets.find { it.id == entry.petId }?.name ?: "Pet"

                    ElevatedCard(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // icon bubble
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    entry.type.name.take(1),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    entry.type.name.replace("_", " "),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "For: $petName",
                                    color = AppColors.TextMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    entry.note.ifBlank { "No details" },
                                    color = AppColors.TextMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeaderModern(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Prev")
            }
            Spacer(Modifier.weight(1f))
            Text(
                "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onNext) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next")
            }
        }
    }
}


@Composable
private fun ModernCalendar(
    month: YearMonth,
    selected: LocalDate,
    hasLog: (LocalDate) -> Boolean,
    onSelect: (LocalDate) -> Unit
) {
    val first = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = (first.dayOfWeek.value % 7) // Sun=0
    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("S","M","T","W","T","F","S").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
            }
        }

        Spacer(Modifier.height(8.dp))

        for (row in 0 until totalCells step 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0..6) {
                    val cell = row + col
                    val dayNum = cell - startOffset + 1
                    val date = if (dayNum in 1..daysInMonth) month.atDay(dayNum) else null

                    CalendarDayTile(
                        dayNum = dayNum,
                        enabled = date != null,
                        selected = date == selected,
                        hasLog = date != null && hasLog(date),
                        onClick = { if (date != null) onSelect(date) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CalendarDayTile(
    dayNum: Int,
    enabled: Boolean,
    selected: Boolean,
    hasLog: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val fg = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (enabled) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(dayNum.toString(), color = fg, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                if (hasLog) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (selected) fg else MaterialTheme.colorScheme.primary)
                    )
                } else {
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun NiceEmptyCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = AppColors.TextMuted)
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(onClick = onClick) { Text(buttonText) }
        }
    }
}



