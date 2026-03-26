package com.learning.multipet.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.learning.multipet.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    vm: AppViewModel,
    onGoManage: () -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = MaterialTheme.colorScheme

    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var filterPetId by remember { mutableStateOf<String?>(null) }

    val logsForDate = remember(state.logs, selectedDate, filterPetId) {
        state.logs
            .filter { it.date == selectedDate }
            .filter { filterPetId == null || it.petId == filterPetId }
            .sortedByDescending { it.type.name }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        if (state.pets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(20.dp))
                CalendarEmptyStateCard(
                    title = "Add a pet first",
                    subtitle = "Records and logs are linked to pets. Add your dog or cat to begin tracking activity and care updates.",
                    buttonText = "Go to Pets",
                    onClick = onGoManage
                )
            }
            return@Box
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(20.dp)) }

            item {
                FilterSection(
                    pets = state.pets.map { it.id to it.name },
                    selectedPetId = filterPetId,
                    onSelectAll = { filterPetId = null },
                    onSelectPet = { filterPetId = it }
                )
            }

            item {
                MonthHeaderModern(
                    month = month,
                    onPrev = { month = month.minusMonths(1) },
                    onNext = { month = month.plusMonths(1) }
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        ModernCalendar(
                            month = month,
                            selected = selectedDate,
                            hasLog = { date ->
                                state.logs.any {
                                    it.date == date && (filterPetId == null || it.petId == filterPetId)
                                }
                            },
                            onSelect = { selectedDate = it }
                        )
                    }
                }
            }

            item {
                LogsHeader(
                    selectedDate = selectedDate,
                    logCount = logsForDate.size,
                    onJumpToday = {
                        selectedDate = LocalDate.now()
                        month = YearMonth.from(selectedDate)
                    }
                )
            }

            if (logsForDate.isEmpty()) {
                item {
                    CalendarEmptyStateCard(
                        title = "No logs for this date",
                        subtitle = "There are no records available for the selected date. Try another date or add a new log.",
                        buttonText = "Today",
                        onClick = {
                            selectedDate = LocalDate.now()
                            month = YearMonth.from(selectedDate)
                        }
                    )
                }
            } else {
                items(logsForDate) { entry ->
                    val petName = state.pets.find { it.id == entry.petId }?.name ?: "Pet"
                    LogEntryCard(
                        type = entry.type.name,
                        petName = petName,
                        note = entry.note.ifBlank { "No details provided." }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun FilterSection(
    pets: List<Pair<String, String>>,
    selectedPetId: String?,
    onSelectAll: () -> Unit,
    onSelectPet: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column {
        Text(
            text = "Filter by pet",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = selectedPetId == null,
                onClick = onSelectAll,
                label = { Text("All Pets") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(alpha = 0.14f),
                    selectedLabelColor = colors.primary,
                    containerColor = colors.surfaceVariant,
                    labelColor = colors.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedPetId == null,
                    borderColor = colors.outline,
                    selectedBorderColor = colors.primary
                )
            )

            pets.take(3).forEach { (id, name) ->
                FilterChip(
                    selected = selectedPetId == id,
                    onClick = { onSelectPet(id) },
                    label = {
                        Text(
                            text = name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary.copy(alpha = 0.14f),
                        selectedLabelColor = colors.primary,
                        containerColor = colors.surfaceVariant,
                        labelColor = colors.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedPetId == id,
                        borderColor = colors.outline,
                        selectedBorderColor = colors.primary
                    )
                )
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
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "Previous month",
                    tint = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.weight(1f))

            IconButton(onClick = onNext) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Next month",
                    tint = colors.onSurfaceVariant
                )
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
    val colors = MaterialTheme.colorScheme
    val first = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = (first.dayOfWeek.value % 7)
    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
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
    val colors = MaterialTheme.colorScheme

    val background = when {
        selected -> colors.primary
        enabled -> colors.surfaceVariant
        else -> Color.Transparent
    }

    val textColor = when {
        selected -> colors.onPrimary
        enabled -> colors.onSurface
        else -> colors.onSurfaceVariant.copy(alpha = 0.35f)
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (enabled) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayNum.toString(),
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(2.dp))

                if (hasLog) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (selected) colors.onPrimary else colors.primary)
                    )
                } else {
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun LogsHeader(
    selectedDate: LocalDate,
    logCount: Int,
    onJumpToday: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Logs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground
            )
            Text(
                text = "$selectedDate • $logCount log${if (logCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }

        TextButton(onClick = onJumpToday) {
            Text("Today")
        }
    }
}

@Composable
private fun LogEntryCard(
    type: String,
    petName: String,
    note: String
) {
    val colors = MaterialTheme.colorScheme
    val label = formatLogType(type)
    val icon = logTypeEmoji(type)

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "For: $petName",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = note,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatLogType(raw: String): String {
    return raw
        .replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}

private fun logTypeEmoji(type: String): String {
    return when (type.uppercase()) {
        "APPETITE" -> "🍽️"
        "ENERGY" -> "💛"
        "WEIGHT" -> "⚖️"
        "VACCINE" -> "💉"
        "DEWORM" -> "🩺"
        "STOOL" -> "💩"
        "NOTES" -> "📝"
        else -> "🐾"
    }
}

@Composable
private fun CalendarEmptyStateCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text(buttonText)
            }
        }
    }
}
