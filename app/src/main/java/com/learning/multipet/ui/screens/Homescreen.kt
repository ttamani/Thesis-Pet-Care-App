package com.learning.multipet.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.learning.multipet.R
import com.learning.multipet.data.LogEntry
import com.learning.multipet.data.Pet
import com.learning.multipet.data.Species
import com.learning.multipet.viewmodel.AppViewModel
import java.time.LocalDate
import com.learning.multipet.data.LogType as DataLogType

enum class ProfileQuickLogType { Appetite, Stool, Energy, Weight, VaccineDeworm }

enum class PetViewMode {
    GRID,
    LIST
}

private enum class PetFilter {
    ALL, DOGS, CATS, VACCINATED, NEEDS_VACCINE
}

enum class QuickLogCategory {
    EMOTION,
    APPETITE,
    STOOL,
    WEIGHT,
    VACCINE
}

enum class StoolLevel(
    val label: String,
    val emoji: String
) {
    NORMAL("Normal", "✅"),
    SOFT("Soft", "🟡"),
    LOOSE("Loose", "🟠"),
    DIARRHEA("Diarrhea", "🚨"),
    HARD("Hard", "🪨"),
    BLOOD_PRESENT("Blood Present", "🩸"),
    MUCUS_PRESENT("Mucus Present", "⚠️")
}

enum class PetEmotion(
    val label: String,
    val catEmoji: String,
    val dogEmoji: String
) {
    JOY("Joy", "😺", "😄"),
    FEAR("Fear", "😿", "😨"),
    ANGER("Anger", "😾", "😠"),
    ANXIETY("Anxiety", "🙀", "😬"),
    LOVE("Affection", "😻", "🥰")
}

enum class AppetiteLevel(
    val label: String,
    val score: Int
) {
    VERY_POOR("Very poor", 1),
    LOW("Low", 2),
    NORMAL("Normal", 3),
    GOOD("Good", 4),
    VERY_GOOD("Very good", 5)
}

enum class VaccineLogAction(val label: String) {
    VACCINATED("Vaccinated"),
    DEWORMED("Dewormed")
}

private data class EmotionUiModel(
    val emotion: PetEmotion,
    val title: String,
    val description: String,
    val accent: Color
)

private fun emotionUiModel(emotion: PetEmotion): EmotionUiModel = when (emotion) {
    PetEmotion.JOY -> EmotionUiModel(
        emotion = emotion,
        title = "Joy",
        description = "Playful, relaxed, and feeling good.",
        accent = Color(0xFFFFC857)
    )
    PetEmotion.FEAR -> EmotionUiModel(
        emotion = emotion,
        title = "Fear",
        description = "Sad, uneasy, or needing comfort.",
        accent = Color(0xFF7AA2F7)
    )
    PetEmotion.ANGER -> EmotionUiModel(
        emotion = emotion,
        title = "Anger",
        description = "Irritated, defensive, or tense.",
        accent = Color(0xFFE57373)
    )
    PetEmotion.ANXIETY -> EmotionUiModel(
        emotion = emotion,
        title = "Anxiety",
        description = "Restless, worried, or stressed.",
        accent = Color(0xFF9FA8DA)
    )
    PetEmotion.LOVE -> EmotionUiModel(
        emotion = emotion,
        title = "Affection",
        description = "Clingy, sweet, and seeking comfort.",
        accent = Color(0xFFF48FB1)
    )
}

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onGoManage: () -> Unit,
    onOpenAi: () -> Unit,
    onFindVet: () -> Unit,
    onOpenCalendar: () -> Unit,
    viewMode: PetViewMode,
    onViewModeChange: (PetViewMode) -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = MaterialTheme.colorScheme

    var selectedFilter by remember { mutableStateOf(PetFilter.ALL) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var quickLogPet by remember { mutableStateOf<Pet?>(null) }
    var quickLogCategory by remember { mutableStateOf<QuickLogCategory?>(null) }
    var editingLog by remember { mutableStateOf<LogEntry?>(null) }
    var permissionsAsked by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    val photosPermission =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

    val photosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (!permissionsAsked) {
            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            photosLauncher.launch(photosPermission)
            permissionsAsked = true
        }
    }

    val vaccinesDue = state.pets.count { !it.vaccinated }
    val attentionNeeded = vaccinesDue

    val filteredPets = remember(state.pets, selectedFilter) {
        when (selectedFilter) {
            PetFilter.ALL -> state.pets
            PetFilter.DOGS -> state.pets.filter { it.species == Species.DOG }
            PetFilter.CATS -> state.pets.filter { it.species == Species.CAT }
            PetFilter.VACCINATED -> state.pets.filter { it.vaccinated }
            PetFilter.NEEDS_VACCINE -> state.pets.filter { !it.vaccinated }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        DashboardOverviewCard(
            petCount = state.pets.size,
            attentionNeeded = attentionNeeded,
            dueVaccines = vaccinesDue
        )

        Spacer(modifier = Modifier.height(16.dp))

        QuickActionsRow(
            onRecords = onOpenCalendar,
            onFindVet = onFindVet,
            onAiCare = onOpenAi
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pets",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${filteredPets.size} visible",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            ViewToggleButton(
                current = viewMode,
                onChange = onViewModeChange
            )

            Spacer(Modifier.width(8.dp))

            TextButton(onClick = onGoManage) {
                Text("Manage")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        PetFilterChips(
            selected = selectedFilter,
            onSelected = { selectedFilter = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.pets.isEmpty()) {
            HomeEmptyStateCard(
                title = "No pets added yet",
                subtitle = "Add your first dog or cat to begin tracking care and logs.",
                onGoManage = onGoManage
            )
        } else if (filteredPets.isEmpty()) {
            HomeEmptyStateCard(
                title = "No matching pets",
                subtitle = "Try another filter to view more pets.",
                onGoManage = null
            )
        } else {
            if (viewMode == PetViewMode.LIST) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredPets, key = { it.id }) { pet ->
                        ModernPetListCard(
                            pet = pet,
                            onClick = {
                                vm.selectPet(pet.id)
                                selectedPet = pet
                            }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredPets, key = { it.id }) { pet ->
                        ModernPetGridCard(
                            pet = pet,
                            onClick = {
                                vm.selectPet(pet.id)
                                selectedPet = pet
                            }
                        )
                    }
                }
            }
        }
    }

    selectedPet?.let { pet ->
        val latestPet = state.pets.find { it.id == pet.id } ?: pet
        FullScreenPetProfile(
            pet = latestPet,
            vm = vm,
            onClose = { selectedPet = null },
            onEditLog = { editingLog = it },
            onDeleteLog = { vm.deleteLog(it.id) },
            onLogClick = { logType ->
                when (logType) {
                    ProfileQuickLogType.Appetite -> {
                        quickLogPet = pet
                        quickLogCategory = QuickLogCategory.APPETITE
                    }
                    ProfileQuickLogType.Energy -> {
                        quickLogPet = pet
                        quickLogCategory = QuickLogCategory.EMOTION
                    }
                    ProfileQuickLogType.Weight -> {
                        quickLogPet = pet
                        quickLogCategory = QuickLogCategory.WEIGHT
                    }
                    ProfileQuickLogType.VaccineDeworm -> {
                        quickLogPet = pet
                        quickLogCategory = QuickLogCategory.VACCINE
                    }
                    ProfileQuickLogType.Stool -> {
                        quickLogPet = pet
                        quickLogCategory = QuickLogCategory.STOOL
                    }
                }
            }
        )
    }

    quickLogPet?.let { pet ->
        val latestPet = state.pets.find { it.id == pet.id } ?: pet
        quickLogCategory?.let { category ->
            QuickLogDialog(
                pet = latestPet,
                category = category,
                onDismiss = {
                    quickLogPet = null
                    quickLogCategory = null
                },
                onSaveEmotion = { emotion, note ->
                    val speciesLabel = if (pet.species == Species.CAT) "cat" else "dog"
                    val emoji = if (pet.species == Species.CAT) emotion.catEmoji else emotion.dogEmoji
                    val finalNote = "$emoji ${emotion.label} ($speciesLabel)${if (note.isNotBlank()) " • $note" else ""}"

                    vm.addLog(
                        petId = pet.id,
                        date = LocalDate.now(),
                        type = DataLogType.ENERGY,
                        note = finalNote
                    )

                    quickLogPet = null
                    quickLogCategory = null
                },
                onSaveAppetite = { appetite, note ->
                    val finalNote = "${appetite.label} (${appetite.score}/5)${if (note.isNotBlank()) " • $note" else ""}"

                    vm.addLog(
                        petId = pet.id,
                        date = LocalDate.now(),
                        type = DataLogType.APPETITE,
                        note = finalNote
                    )

                    quickLogPet = null
                    quickLogCategory = null
                },
                onSaveWeight = { weightText, note ->
                    val weightValue = weightText.toDoubleOrNull()
                    if (weightValue != null) {
                        val finalNote = "${weightText} kg${if (note.isNotBlank()) " • $note" else ""}"

                        vm.addLog(
                            petId = latestPet.id,
                            date = LocalDate.now(),
                            type = DataLogType.WEIGHT,
                            note = finalNote
                        )

                        vm.updatePet(latestPet.copy(weightKg = weightValue))
                    }

                    quickLogPet = null
                    quickLogCategory = null
                },
                onSaveStool = { stool, note ->
                    val finalNote = "${stool.emoji} ${stool.label}${if (note.isNotBlank()) " • $note" else ""}"

                    vm.addLog(
                        petId = latestPet.id,
                        date = LocalDate.now(),
                        type = DataLogType.STOOL,
                        note = finalNote
                    )

                    quickLogPet = null
                    quickLogCategory = null
                },
                onSaveVaccine = { action, note ->
                    val finalNote = "${action.label} today${if (note.isNotBlank()) " • $note" else ""}"
                    val dataType = when (action) {
                        VaccineLogAction.VACCINATED -> DataLogType.VACCINE
                        VaccineLogAction.DEWORMED -> DataLogType.DEWORM
                    }

                    vm.addLog(
                        petId = latestPet.id,
                        date = LocalDate.now(),
                        type = dataType,
                        note = finalNote
                    )

                    if (action == VaccineLogAction.VACCINATED && !latestPet.vaccinated) {
                        vm.updatePet(latestPet.copy(vaccinated = true))
                    }

                    quickLogPet = null
                    quickLogCategory = null
                }
            )
        }
    }

    editingLog?.let { log ->
        EditLogDialog(
            log = log,
            onDismiss = { editingLog = null },
            onSave = { updatedNote ->
                vm.updateLog(log.copy(note = updatedNote))
                editingLog = null
            }
        )
    }

}

@Composable
private fun ModernPetGridCard(
    pet: Pet,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(colors.surfaceVariant)
            ) {
                if (pet.imageUri.isNullOrBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(pet.imageUri),
                        contentDescription = "Pet photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                Text(
                    text = pet.breed,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ViewToggleButton(
    current: PetViewMode,
    onChange: (PetViewMode) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row {
            ToggleItem(
                selected = current == PetViewMode.LIST,
                text = "List",
                onClick = { onChange(PetViewMode.LIST) }
            )

            ToggleItem(
                selected = current == PetViewMode.GRID,
                text = "Grid",
                onClick = { onChange(PetViewMode.GRID) }
            )
        }
    }
}

@Composable
private fun ToggleItem(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        color = if (selected) colors.primary.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.primary else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun DashboardOverviewCard(
    petCount: Int,
    attentionNeeded: Int,
    dueVaccines: Int
) {
    val colors = MaterialTheme.colorScheme
    val success = Color(0xFF22C55E)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Pet Care Overview",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Pets",
                    value = petCount.toString(),
                    valueColor = colors.onSurface,
                    containerColor = colors.surfaceVariant
                )

                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Attention",
                    value = attentionNeeded.toString(),
                    valueColor = if (attentionNeeded > 0) colors.tertiary else success,
                    containerColor = if (attentionNeeded > 0) {
                        colors.tertiary.copy(alpha = 0.14f)
                    } else {
                        success.copy(alpha = 0.14f)
                    }
                )

                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Vaccines",
                    value = dueVaccines.toString(),
                    valueColor = if (dueVaccines > 0) colors.tertiary else colors.primary,
                    containerColor = if (dueVaccines > 0) {
                        colors.tertiary.copy(alpha = 0.14f)
                    } else {
                        colors.primary.copy(alpha = 0.14f)
                    }
                )
            }
        }
    }
}

@Composable
private fun CompactStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color,
    containerColor: Color
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = label,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                color = valueColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onRecords: () -> Unit,
    onFindVet: () -> Unit,
    onAiCare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CompactQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Description,
            label = "Records",
            onClick = onRecords
        )

        CompactQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocationOn,
            label = "Find Vet",
            onClick = onFindVet
        )

        CompactQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AutoAwesome,
            label = "AI Care",
            onClick = onAiCare
        )
    }
}

@Composable
private fun CompactQuickAction(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = colors.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PetFilterChips(
    selected: PetFilter,
    onSelected: (PetFilter) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val items = listOf(
        PetFilter.ALL to "All",
        PetFilter.DOGS to "Dogs",
        PetFilter.CATS to "Cats",
        PetFilter.VACCINATED to "Vaccinated",
        PetFilter.NEEDS_VACCINE to "Needs vaccine"
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { (filter, label) ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(alpha = 0.14f),
                    selectedLabelColor = colors.primary,
                    containerColor = colors.surfaceVariant,
                    labelColor = colors.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == filter,
                    borderColor = colors.outline,
                    selectedBorderColor = colors.primary
                )
            )
        }
    }
}

@Composable
private fun ModernPetListCard(
    pet: Pet,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val success = Color(0xFF22C55E)
    val statusColor = if (pet.vaccinated) success else colors.tertiary
    val speciesLabel = if (pet.species == Species.DOG) "Dog" else "Cat"

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(108.dp)
                    .fillMaxSize()
                    .background(colors.surfaceVariant)
            ) {
                if (pet.imageUri.isNullOrBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.primary.copy(alpha = 0.14f)
                        ) {
                            Box(
                                modifier = Modifier.size(52.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(pet.imageUri),
                        contentDescription = "Pet photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pet.breed,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallInfoBadge(text = speciesLabel)
                    SmallStatusBadge(
                        text = if (pet.vaccinated) "Vaccinated" else "Needs vaccine",
                        background = statusColor.copy(alpha = 0.14f),
                        content = statusColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallInfoBadge(text: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun SmallStatusBadge(
    text: String,
    background: Color,
    content: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = background
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FullScreenPetProfile(
    pet: Pet,
    vm: AppViewModel,
    onClose: () -> Unit,
    onEditLog: (LogEntry) -> Unit,
    onLogClick: (ProfileQuickLogType) -> Unit,
    onDeleteLog: (LogEntry) -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = MaterialTheme.colorScheme
    val success = Color(0xFF22C55E)
    val currentPet = remember(state.pets, pet.id) {
        state.pets.find { it.id == pet.id } ?: pet
    }
    val petLogs = remember(state.logs, currentPet.id) {
        state.logs
            .filter { it.petId == currentPet.id }
            .sortedWith(compareByDescending<com.learning.multipet.data.LogEntry> { it.date }.thenByDescending { it.id })
    }
    val recentLogs = petLogs.take(8)
    val speciesLabel = if (currentPet.species == Species.DOG) "Dog" else "Cat"
    val statusColor = if (currentPet.vaccinated) success else colors.tertiary
    val isDewormed = remember(petLogs) { petLogs.any { it.type == DataLogType.DEWORM } }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colors.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = currentPet.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Profile and logs",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(colors.surfaceVariant)
                        ) {
                            if (currentPet.imageUri.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = colors.primary.copy(alpha = 0.14f)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(96.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Pets,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Image(
                                    painter = rememberAsyncImagePainter(currentPet.imageUri),
                                    contentDescription = "Pet photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SmallInfoBadge(text = speciesLabel)
                                SmallStatusBadge(
                                    text = if (currentPet.vaccinated) "Vaccinated" else "Needs vaccine",
                                    background = statusColor.copy(alpha = 0.14f),
                                    content = statusColor
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentPet.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = currentPet.breed,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProfileMetaChip("Age ${currentPet.ageYears}")
                                ProfileMetaChip("Weight ${currentPet.weightKg?.let { String.format("%.1f", it) } ?: "—"} kg")
                                ProfileMetaChip("Sex ${currentPet.sex}")
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = colors.surfaceVariant,
                                border = BorderStroke(1.dp, colors.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    StatusCheckboxRow(
                                        label = "Vaccinated",
                                        checked = currentPet.vaccinated
                                    )
                                    StatusCheckboxRow(
                                        label = "Dewormed",
                                        checked = isDewormed
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = "Quick Logs",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Tap a log type to quickly save today’s update.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        QuickLogGrid(
                            onAppetite = { onLogClick(ProfileQuickLogType.Appetite) },
                            onStool = { onLogClick(ProfileQuickLogType.Stool) },
                            onEnergy = { onLogClick(ProfileQuickLogType.Energy) },
                            onWeight = { onLogClick(ProfileQuickLogType.Weight) },
                            onVaccine = { onLogClick(ProfileQuickLogType.VaccineDeworm) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recent Logs",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Latest activity for ${currentPet.name}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (recentLogs.isEmpty()) {
                            Text(
                                text = "No logs yet for this pet. Quick logs you add here will also appear in Calendar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        } else {
                            recentLogs.forEachIndexed { index, log ->
                                key(log.id) {
                                    SwipeToDeleteLogCard(
                                        log = log,
                                        onDelete = { onDeleteLog(log) }
                                    ) {
                                        PetLogCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            type = log.type.name.replace("_", " "),
                                            note = log.note.ifBlank { "No details provided." },
                                            dateText = log.date.toString(),
                                            onEditClick = { onEditLog(log) }
                                        )
                                    }
                                }
                                if (index != recentLogs.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun StatusCheckboxRow(
    label: String,
    checked: Boolean
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = false,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.primary,
                uncheckedColor = colors.outline,
                checkmarkColor = colors.onPrimary,
                disabledCheckedColor = colors.primary,
                disabledUncheckedColor = colors.outline
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface
        )
    }
}

@Composable
private fun ProfileMetaChip(text: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickLogGrid(
    onAppetite: () -> Unit,
    onStool: () -> Unit,
    onEnergy: () -> Unit,
    onWeight: () -> Unit,
    onVaccine: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickLogActionCard(
                modifier = Modifier.weight(1f),
                label = "Appetite",
                subtitle = "Rate meal intake",
                icon = Icons.Default.Restaurant,
                onClick = onAppetite
            )
            QuickLogActionCard(
                modifier = Modifier.weight(1f),
                label = "Stool",
                subtitle = "Track stool condition",
                icon = Icons.Default.Description,
                onClick = onStool
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickLogActionCard(
                modifier = Modifier.weight(1f),
                label = "Energy",
                subtitle = "Mood and behavior",
                icon = Icons.Default.Favorite,
                onClick = onEnergy
            )
            QuickLogActionCard(
                modifier = Modifier.weight(1f),
                label = "Weight",
                subtitle = "Save current kg",
                icon = Icons.Default.ShowChart,
                onClick = onWeight
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickLogActionCard(
                modifier = Modifier.weight(1f),
                label = "Vaccine",
                subtitle = "Vaccinate or deworm",
                icon = Icons.Default.Vaccines,
                onClick = onVaccine
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickLogActionCard(
    modifier: Modifier = Modifier,
    label: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = colors.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickLogChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = colors.surfaceVariant,
            labelColor = colors.onSurfaceVariant,
            leadingIconContentColor = colors.primary
        ),
        border = BorderStroke(1.dp, colors.outline)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteLogCard(
    log: LogEntry,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.35f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isDismissed = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart ||
                    dismissState.currentValue == SwipeToDismissBoxValue.EndToStart
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (isDismissed) Color(0xFFB3261E) else Color(0xFFD32F2F)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete log",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        content = { content() }
    )
}

@Composable
private fun PetLogCard(
    modifier: Modifier = Modifier,
    type: String,
    note: String,
    dateText: String,
    onEditClick: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val logEmoji = when {
        type.contains("Appetite", ignoreCase = true) -> "🍽️"
        type.contains("Energy", ignoreCase = true) -> "💛"
        type.contains("Weight", ignoreCase = true) -> "⚖️"
        type.contains("Vaccine", ignoreCase = true) -> "💉"
        type.contains("Deworm", ignoreCase = true) -> "🪱"
        type.contains("Stool", ignoreCase = true) -> "💩"
        type.contains("Notes", ignoreCase = true) -> "📝"
        else -> "🐾"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
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
                    Text(
                        text = logEmoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                onEditClick?.let { onEdit ->
                    TextButton(onClick = onEdit) {
                        Text("Edit")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note.ifBlank { "No details provided." },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EditLogDialog(
    log: LogEntry,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var note by remember(log.id) { mutableStateOf(log.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Edit ${log.type.name.replace("_", " ")} Log",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.date.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Log details") },
                placeholder = { Text("Correct the saved log details") },
                maxLines = 4,
                shape = RoundedCornerShape(16.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(note.trim()) },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
@Composable
private fun HomeEmptyStateCard(
    title: String,
    subtitle: String,
    onGoManage: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            if (onGoManage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGoManage,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text("Add Pet")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickLogDialog(
    pet: Pet,
    category: QuickLogCategory,
    onDismiss: () -> Unit,
    onSaveEmotion: (PetEmotion, String) -> Unit,
    onSaveAppetite: (AppetiteLevel, String) -> Unit,
    onSaveWeight: (String, String) -> Unit,
    onSaveStool: (StoolLevel, String) -> Unit,
    onSaveVaccine: (VaccineLogAction, String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var selectedEmotion by remember { mutableStateOf(PetEmotion.JOY) }
    var selectedAppetite by remember { mutableStateOf(AppetiteLevel.NORMAL) }
    var selectedStool by remember { mutableStateOf(StoolLevel.NORMAL) }
    var selectedVaccineAction by remember { mutableStateOf(VaccineLogAction.VACCINATED) }
    var weightText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val canSave = when (category) {
        QuickLogCategory.WEIGHT -> weightText.trim().toDoubleOrNull() != null
        else -> true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = when (category) {
                        QuickLogCategory.EMOTION -> "Quick Emotion Log"
                        QuickLogCategory.APPETITE -> "Quick Appetite Log"
                        QuickLogCategory.STOOL -> "Quick Stool Log"
                        QuickLogCategory.WEIGHT -> "Quick Weight Log"
                        QuickLogCategory.VACCINE -> "Quick Vaccine Log"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "For ${pet.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                when (category) {
                    QuickLogCategory.EMOTION -> {
                        Text(
                            text = "How does ${pet.name} feel today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        EmotionSwipeSelector(
                            pet = pet,
                            selectedEmotion = selectedEmotion,
                            onSelectedEmotion = { selectedEmotion = it }
                        )
                    }

                    QuickLogCategory.APPETITE -> {
                        Text(
                            text = "Rate ${pet.name}'s appetite",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppetiteLevel.entries.forEach { level ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = if (selectedAppetite == level) {
                                    colors.primary.copy(alpha = 0.12f)
                                } else {
                                    colors.surfaceVariant
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedAppetite == level) colors.primary else colors.outline
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAppetite = level }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedAppetite == level,
                                        onClick = { selectedAppetite = level },
                                        colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = level.label,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = colors.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${level.score}/5",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    QuickLogCategory.STOOL -> {
                        Text(
                            text = "How was ${pet.name}'s stool today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StoolLevel.entries.forEach { stool ->
                                FilterChip(
                                    selected = selectedStool == stool,
                                    onClick = { selectedStool = stool },
                                    label = { Text("${stool.emoji} ${stool.label}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary.copy(alpha = 0.14f),
                                        selectedLabelColor = colors.primary,
                                        containerColor = colors.surfaceVariant,
                                        labelColor = colors.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedStool == stool,
                                        borderColor = colors.outline,
                                        selectedBorderColor = colors.primary
                                    )
                                )
                            }
                        }
                    }

                    QuickLogCategory.WEIGHT -> {
                        Text(
                            text = "Enter ${pet.name}'s current weight in kilograms.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Weight (kg)") },
                            placeholder = { Text("Example: 4.6") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    QuickLogCategory.VACCINE -> {
                        Text(
                            text = "Choose what was done for ${pet.name} today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = colors.surfaceVariant,
                            border = BorderStroke(1.dp, colors.outline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                VaccineLogAction.entries.forEachIndexed { index, action ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedVaccineAction = action }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedVaccineAction == action,
                                            onCheckedChange = { selectedVaccineAction = action },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = colors.primary,
                                                uncheckedColor = colors.outline
                                            )
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = action.label,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = colors.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    if (index != VaccineLogAction.entries.lastIndex) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Optional note") },
                    placeholder = {
                        Text(
                            when (category) {
                                QuickLogCategory.EMOTION -> "Example: nervous during thunder"
                                QuickLogCategory.APPETITE -> "Example: ate half of the meal"
                                QuickLogCategory.STOOL -> "Example: happened after changing food"
                                QuickLogCategory.WEIGHT -> "Example: weighed after breakfast"
                                QuickLogCategory.VACCINE -> "Example: anti-rabies at city vet"
                            }
                        )
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (category) {
                        QuickLogCategory.EMOTION -> onSaveEmotion(selectedEmotion, note.trim())
                        QuickLogCategory.APPETITE -> onSaveAppetite(selectedAppetite, note.trim())
                        QuickLogCategory.STOOL -> onSaveStool(selectedStool, note.trim())
                        QuickLogCategory.WEIGHT -> onSaveWeight(weightText.trim(), note.trim())
                        QuickLogCategory.VACCINE -> onSaveVaccine(selectedVaccineAction, note.trim())
                    }
                },
                enabled = canSave,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Log")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmotionSwipeSelector(
    pet: Pet,
    selectedEmotion: PetEmotion,
    onSelectedEmotion: (PetEmotion) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val emotions = PetEmotion.entries
    val initialPage = emotions.indexOf(selectedEmotion).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { emotions.size }
    )

    LaunchedEffect(selectedEmotion) {
        val index = emotions.indexOf(selectedEmotion)
        if (index >= 0 && pagerState.currentPage != index) {
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val emotion = emotions.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (emotion != selectedEmotion) onSelectedEmotion(emotion)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val emotion = emotions[page]
            EmotionSwipeCard(
                pet = pet,
                emotion = emotion,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            emotions.forEachIndexed { index, emotion ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) emotionUiModel(emotion).accent
                            else colors.outline.copy(alpha = 0.35f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Swipe left or right to choose emotion",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun emotionImageRes(
    pet: Pet,
    emotion: PetEmotion
): Int {
    return when (pet.species) {
        Species.CAT -> when (emotion) {
            PetEmotion.JOY -> R.drawable.cat_joy
            PetEmotion.FEAR -> R.drawable.cat_sad
            PetEmotion.ANGER -> R.drawable.cat_anger
            PetEmotion.ANXIETY -> R.drawable.cat_anxiety
            PetEmotion.LOVE -> R.drawable.cat_love
        }
        Species.DOG -> when (emotion) {
            PetEmotion.JOY -> R.drawable.dog_joy
            PetEmotion.FEAR -> R.drawable.dog_sad
            PetEmotion.ANGER -> R.drawable.dog_anger
            PetEmotion.ANXIETY -> R.drawable.dog_anxiety
            PetEmotion.LOVE -> R.drawable.dog_love
        }
    }
}
@Composable
private fun EmotionSwipeCard(
    pet: Pet,
    emotion: PetEmotion,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val model = emotionUiModel(emotion)
    val speciesLabel = if (pet.species == Species.CAT) "Cat" else "Dog"
    val imageRes = emotionImageRes(pet, emotion)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = model.accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, model.accent.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, model.accent.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "${emotion.label} $speciesLabel emotion",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SmallInfoBadge(text = "$speciesLabel emotion")

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = model.title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = model.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}
