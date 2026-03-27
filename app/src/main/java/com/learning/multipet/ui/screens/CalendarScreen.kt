package com.learning.multipet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.learning.multipet.R
import com.learning.multipet.data.LogEntry
import com.learning.multipet.data.LogType
import com.learning.multipet.data.Species
import com.learning.multipet.viewmodel.AppViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class CalendarPetFilterType {
    ALL,
    DOGS,
    CATS,
    SINGLE
}

private data class CalendarPetFilterItem(
    val id: String,
    val name: String,
    val species: Species?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    vm: AppViewModel,
    onGoManage: () -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = MaterialTheme.colorScheme

    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    var filterType by remember { mutableStateOf(CalendarPetFilterType.ALL) }
    var selectedPetId by remember { mutableStateOf<String?>(null) }
    var showPetPickerSheet by remember { mutableStateOf(false) }

    val petItems = remember(state.pets) {
        state.pets.map { pet ->
            CalendarPetFilterItem(
                id = pet.id,
                name = pet.name,
                species = pet.species
            )
        }
    }

    val selectedPetName = remember(petItems, selectedPetId) {
        petItems.find { it.id == selectedPetId }?.name
    }

    val filteredPetIds = remember(petItems, filterType, selectedPetId) {
        when (filterType) {
            CalendarPetFilterType.ALL -> petItems.map { it.id }.toSet()
            CalendarPetFilterType.DOGS -> petItems
                .filter { it.species == Species.DOG }
                .map { it.id }
                .toSet()
            CalendarPetFilterType.CATS -> petItems
                .filter { it.species == Species.CAT }
                .map { it.id }
                .toSet()
            CalendarPetFilterType.SINGLE -> selectedPetId?.let { setOf(it) } ?: emptySet()
        }
    }

    val logsForDate = remember(state.logs, selectedDate, filteredPetIds) {
        state.logs
            .filter { it.date == selectedDate }
            .filter { it.petId in filteredPetIds }
            .sortedWith(
                compareByDescending<LogEntry> { it.createdAtMillis }
                    .thenByDescending { it.date }
                    .thenByDescending { it.id }
            )
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
                SmartFilterSection(
                    pets = petItems,
                    filterType = filterType,
                    selectedPetId = selectedPetId,
                    onSelectAll = {
                        filterType = CalendarPetFilterType.ALL
                        selectedPetId = null
                    },
                    onSelectDogs = {
                        filterType = CalendarPetFilterType.DOGS
                        selectedPetId = null
                    },
                    onSelectCats = {
                        filterType = CalendarPetFilterType.CATS
                        selectedPetId = null
                    },
                    onOpenPetPicker = {
                        showPetPickerSheet = true
                    },
                    onClearSinglePet = {
                        filterType = CalendarPetFilterType.ALL
                        selectedPetId = null
                    }
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
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        ModernCalendar(
                            month = month,
                            selected = selectedDate,
                            hasLog = { date ->
                                state.logs.any { log ->
                                    log.date == date && log.petId in filteredPetIds
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

            if (
                filterType == CalendarPetFilterType.SINGLE &&
                selectedPetId != null &&
                selectedPetName != null
            ) {
                item {
                    SelectedPetBanner(
                        petName = selectedPetName,
                        onChange = { showPetPickerSheet = true },
                        onClear = {
                            filterType = CalendarPetFilterType.ALL
                            selectedPetId = null
                        }
                    )
                }
            }

            if (logsForDate.isEmpty()) {
                item {
                    CalendarEmptyStateCard(
                        title = "No logs for this date",
                        subtitle = when (filterType) {
                            CalendarPetFilterType.ALL -> {
                                "There are no records available for the selected date. Try another date or add a new log."
                            }
                            CalendarPetFilterType.DOGS -> {
                                "No dog records were found for the selected date."
                            }
                            CalendarPetFilterType.CATS -> {
                                "No cat records were found for the selected date."
                            }
                            CalendarPetFilterType.SINGLE -> {
                                "No records were found for the selected pet on this date."
                            }
                        },
                        buttonText = "Today",
                        onClick = {
                            selectedDate = LocalDate.now()
                            month = YearMonth.from(selectedDate)
                        }
                    )
                }
            } else {
                items(
                    items = logsForDate,
                    key = { it.id }
                ) { entry ->
                    val pet = state.pets.find { it.id == entry.petId }
                    val petName = pet?.name ?: "Pet"
                    val petSpecies = pet?.species

                    LogEntryCard(
                        entry = entry,
                        petName = petName,
                        petSpecies = petSpecies
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        if (showPetPickerSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { showPetPickerSheet = false },
                sheetState = sheetState,
                containerColor = colors.surface,
                tonalElevation = 0.dp
            ) {
                PetPickerSheet(
                    pets = petItems,
                    selectedPetId = selectedPetId,
                    onSelectPet = { petId ->
                        selectedPetId = petId
                        filterType = CalendarPetFilterType.SINGLE
                        showPetPickerSheet = false
                    },
                    onDismiss = { showPetPickerSheet = false }
                )
            }
        }
    }
}

@Composable
private fun SmartFilterSection(
    pets: List<CalendarPetFilterItem>,
    filterType: CalendarPetFilterType,
    selectedPetId: String?,
    onSelectAll: () -> Unit,
    onSelectDogs: () -> Unit,
    onSelectCats: () -> Unit,
    onOpenPetPicker: () -> Unit,
    onClearSinglePet: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val selectedPetName = pets.find { it.id == selectedPetId }?.name

    Column {
        Text(
            text = "Filter by pet",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                FilterChip(
                    selected = filterType == CalendarPetFilterType.ALL,
                    onClick = onSelectAll,
                    label = { Text("All Pets") },
                    colors = filterChipColors(),
                    border = filterChipBorder(filterType == CalendarPetFilterType.ALL)
                )
            }

            item {
                FilterChip(
                    selected = filterType == CalendarPetFilterType.DOGS,
                    onClick = onSelectDogs,
                    label = { Text("Dogs") },
                    colors = filterChipColors(),
                    border = filterChipBorder(filterType == CalendarPetFilterType.DOGS)
                )
            }

            item {
                FilterChip(
                    selected = filterType == CalendarPetFilterType.CATS,
                    onClick = onSelectCats,
                    label = { Text("Cats") },
                    colors = filterChipColors(),
                    border = filterChipBorder(filterType == CalendarPetFilterType.CATS)
                )
            }

            item {
                FilterChip(
                    selected = filterType == CalendarPetFilterType.SINGLE,
                    onClick = onOpenPetPicker,
                    label = {
                        Text(
                            text = if (
                                filterType == CalendarPetFilterType.SINGLE &&
                                !selectedPetName.isNullOrBlank()
                            ) {
                                selectedPetName
                            } else {
                                "Choose Pet"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = filterChipColors(),
                    border = filterChipBorder(filterType == CalendarPetFilterType.SINGLE)
                )
            }
        }

        if (
            filterType == CalendarPetFilterType.SINGLE &&
            !selectedPetName.isNullOrBlank()
        ) {
            Spacer(Modifier.height(10.dp))

            InputChip(
                selected = true,
                onClick = onOpenPetPicker,
                label = {
                    Text(
                        text = "Selected: $selectedPetName",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear selected pet",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onClearSinglePet)
                    )
                }
            )
        }
    }
}

@Composable
private fun PetPickerSheet(
    pets: List<CalendarPetFilterItem>,
    selectedPetId: String?,
    onSelectPet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }

    val filteredPets = remember(pets, query) {
        pets.filter { pet ->
            val searchableSpecies = pet.species?.name.orEmpty()
            pet.name.contains(query, ignoreCase = true) ||
                    searchableSpecies.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Choose a pet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = "${pets.size} pet${if (pets.size == 1) "" else "s"} available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            label = { Text("Search pet") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            colors = OutlinedTextFieldDefaults.colors()
        )

        Spacer(Modifier.height(14.dp))

        if (filteredPets.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = colors.primary
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "No pets match your search",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Try a different pet name or species.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredPets,
                    key = { it.id }
                ) { pet ->
                    val isSelected = pet.id == selectedPetId

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPet(pet.id) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) {
                            colors.primary.copy(alpha = 0.10f)
                        } else {
                            colors.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.22f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = colors.surface
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pet.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(Modifier.height(2.dp))

                                Text(
                                    text = pet.species
                                        ?.name
                                        ?.lowercase()
                                        ?.replaceFirstChar { it.uppercase() }
                                        ?: "Pet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (isSelected) colors.primary else colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedPetBanner(
    petName: String,
    onChange: () -> Unit,
    onClear: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.primary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Showing logs for",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = petName,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TextButton(
                onClick = onChange,
                colors = textButtonColors(contentColor = colors.primary)
            ) {
                Text("Change")
            }

            TextButton(
                onClick = onClear,
                colors = textButtonColors(contentColor = colors.onSurfaceVariant)
            ) {
                Text("Clear")
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
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.30f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
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
                    imageVector = Icons.Filled.ChevronRight,
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
    val startOffset = first.dayOfWeek.value % 7
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

        TextButton(
            onClick = onJumpToday,
            colors = textButtonColors(contentColor = colors.primary)
        ) {
            Text("Today")
        }
    }
}

@Composable
private fun LogEntryCard(
    entry: LogEntry,
    petName: String,
    petSpecies: Species?
) {
    val colors = MaterialTheme.colorScheme
    val typeText = entry.type.name.replace("_", " ")
    val noteText = entry.note.ifBlank { "No details provided." }

    val logIconVector = when (entry.type) {
        LogType.WEIGHT -> Icons.Default.ShowChart
        LogType.VACCINE -> Icons.Default.Vaccines
        LogType.DEWORM -> Icons.Default.Vaccines
        else -> null
    }

    val logIconRes = when (entry.type) {
        LogType.APPETITE -> appetiteIconFromNote(entry.note)
        LogType.STOOL -> stoolIconFromNote(entry.note)
        LogType.ENERGY -> energyEmotionIconFromNote(entry.note, petSpecies)
        else -> null
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surface.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                logIconRes != null -> {
                                    Image(
                                        painter = painterResource(id = logIconRes),
                                        contentDescription = typeText,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                logIconVector != null -> {
                                    Icon(
                                        imageVector = logIconVector,
                                        contentDescription = typeText,
                                        tint = colors.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = typeText,
                                        tint = colors.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatLogType(typeText),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "For: $petName",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = colors.primary.copy(alpha = 0.10f)
                            ) {
                                Text(
                                    text = formatLogTime(entry.createdAtMillis),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = formatLogDate(entry.createdAtMillis),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = noteText,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
    containerColor = MaterialTheme.colorScheme.surfaceVariant
)

@Composable
private fun filterChipBorder(selected: Boolean): BorderStroke {
    val colors = MaterialTheme.colorScheme
    return BorderStroke(
        width = 1.dp,
        color = if (selected) colors.primary else colors.outline
    )
}

private fun formatLogType(raw: String): String {
    return raw
        .replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}

private fun formatLogDate(createdAtMillis: Long): String {
    val dateTime = Instant.ofEpochMilli(createdAtMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
}

private fun formatLogTime(createdAtMillis: Long): String {
    val dateTime = Instant.ofEpochMilli(createdAtMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
}

private fun appetiteIconFromNote(note: String): Int {
    val value = note.lowercase()
    return when {
        "very poor" in value -> R.drawable.ic_appetite_very_poor
        "very good" in value -> R.drawable.ic_appetite_very_good
        "low" in value -> R.drawable.ic_appetite_low
        "good" in value -> R.drawable.ic_appetite_good
        "normal" in value -> R.drawable.ic_appetite_normal
        else -> R.drawable.ic_appetite_normal
    }
}

private fun stoolIconFromNote(note: String): Int {
    val value = note.lowercase()
    return when {
        "blood" in value -> R.drawable.ic_stool_blood
        "mucus" in value -> R.drawable.ic_stool_mucus
        "diarrhea" in value -> R.drawable.ic_stool_diarrhea
        "loose" in value -> R.drawable.ic_stool_loose
        "soft" in value -> R.drawable.ic_stool_soft
        "hard" in value -> R.drawable.ic_stool_hard
        "normal" in value -> R.drawable.ic_stool_normal
        else -> R.drawable.ic_stool_normal
    }
}

private fun energyEmotionIconFromNote(
    note: String,
    species: Species?
): Int {
    val value = note.lowercase()
    return when (species) {
        Species.CAT -> when {
            "joy" in value -> R.drawable.cat_joy
            "fear" in value -> R.drawable.cat_sad
            "anger" in value -> R.drawable.cat_anger
            "anxiety" in value -> R.drawable.cat_anxiety
            "love" in value || "affection" in value -> R.drawable.cat_love
            else -> R.drawable.ic_energy_cat
        }
        Species.DOG -> when {
            "joy" in value -> R.drawable.dog_joy
            "fear" in value -> R.drawable.dog_sad
            "anger" in value -> R.drawable.dog_anger
            "anxiety" in value -> R.drawable.dog_anxiety
            "love" in value || "affection" in value -> R.drawable.dog_love
            else -> R.drawable.ic_energy_dog
        }
        else -> R.drawable.ic_energy_dog
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