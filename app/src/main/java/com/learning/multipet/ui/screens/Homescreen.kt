package com.learning.multipet.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
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

enum class StoolLevel(val label: String) {
    NORMAL("Normal"),
    SOFT("Soft"),
    LOOSE("Loose"),
    DIARRHEA("Diarrhea"),
    HARD("Hard"),
    BLOOD_PRESENT("Blood present"),
    MUCUS_PRESENT("Mucus present")
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


private fun energyIconResForSpecies(species: Species): Int = when (species) {
    Species.DOG -> R.drawable.ic_energy_dog
    Species.CAT -> R.drawable.ic_energy_cat
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

private fun energyEmotionIconFromNote(note: String, species: Species): Int {
    val value = note.lowercase()
    return when (species) {
        Species.CAT -> when {
            "joy" in value -> R.drawable.cat_joy
            "fear" in value || "sad" in value -> R.drawable.cat_sad
            "anger" in value -> R.drawable.cat_anger
            "anxiety" in value -> R.drawable.cat_anxiety
            "love" in value || "affection" in value -> R.drawable.cat_love
            else -> energyIconResForSpecies(species)
        }
        Species.DOG -> when {
            "joy" in value -> R.drawable.dog_joy
            "fear" in value || "sad" in value -> R.drawable.dog_sad
            "anger" in value -> R.drawable.dog_anger
            "anxiety" in value -> R.drawable.dog_anxiety
            "love" in value || "affection" in value -> R.drawable.dog_love
            else -> energyIconResForSpecies(species)
        }
    }
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

private data class AppetiteUiModel(
    val level: AppetiteLevel,
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int
)

private data class StoolUiModel(
    val level: StoolLevel,
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int
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

private fun appetiteUiModel(level: AppetiteLevel): AppetiteUiModel = when (level) {
    AppetiteLevel.VERY_POOR -> AppetiteUiModel(
        level = level,
        title = "Very poor",
        subtitle = "Barely ate or refused food",
        iconRes = R.drawable.ic_appetite_very_poor
    )
    AppetiteLevel.LOW -> AppetiteUiModel(
        level = level,
        title = "Low",
        subtitle = "Ate only a small amount",
        iconRes = R.drawable.ic_appetite_low
    )
    AppetiteLevel.NORMAL -> AppetiteUiModel(
        level = level,
        title = "Normal",
        subtitle = "Usual appetite today",
        iconRes = R.drawable.ic_appetite_normal
    )
    AppetiteLevel.GOOD -> AppetiteUiModel(
        level = level,
        title = "Good",
        subtitle = "Ate well without concern",
        iconRes = R.drawable.ic_appetite_good
    )
    AppetiteLevel.VERY_GOOD -> AppetiteUiModel(
        level = level,
        title = "Very good",
        subtitle = "Strong appetite today",
        iconRes = R.drawable.ic_appetite_very_good
    )
}

private fun stoolUiModel(level: StoolLevel): StoolUiModel = when (level) {
    StoolLevel.NORMAL -> StoolUiModel(
        level = level,
        title = "Normal",
        subtitle = "Healthy and expected",
        iconRes = R.drawable.ic_stool_normal
    )
    StoolLevel.SOFT -> StoolUiModel(
        level = level,
        title = "Soft",
        subtitle = "Softer than usual",
        iconRes = R.drawable.ic_stool_soft
    )
    StoolLevel.LOOSE -> StoolUiModel(
        level = level,
        title = "Loose",
        subtitle = "Less formed than normal",
        iconRes = R.drawable.ic_stool_loose
    )
    StoolLevel.DIARRHEA -> StoolUiModel(
        level = level,
        title = "Diarrhea",
        subtitle = "Watery or frequent stool",
        iconRes = R.drawable.ic_stool_diarrhea
    )
    StoolLevel.HARD -> StoolUiModel(
        level = level,
        title = "Hard",
        subtitle = "Dry or difficult to pass",
        iconRes = R.drawable.ic_stool_hard
    )
    StoolLevel.BLOOD_PRESENT -> StoolUiModel(
        level = level,
        title = "Blood present",
        subtitle = "Visible blood noticed",
        iconRes = R.drawable.ic_stool_blood
    )
    StoolLevel.MUCUS_PRESENT -> StoolUiModel(
        level = level,
        title = "Mucus present",
        subtitle = "Visible mucus noticed",
        iconRes = R.drawable.ic_stool_mucus
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
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colors.background,
            colors.surfaceContainerLowest,
            colors.background
        )
    )

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

    val gridRows = remember(filteredPets) { filteredPets.chunked(2) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DashboardOverviewCard(
                        petCount = state.pets.size,
                        attentionNeeded = attentionNeeded,
                        dueVaccines = vaccinesDue
                    )
                }

                item {
                    QuickActionsRow(
                        onRecords = onOpenCalendar,
                        onFindVet = onFindVet,
                        onAiCare = onOpenAi
                    )
                }

                item {
                    PremiumSectionShell {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pets",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colors.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(2.dp))

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

                            Spacer(modifier = Modifier.width(8.dp))

                            TextButton(onClick = onGoManage) {
                                Text("Manage")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        PetFilterChips(
                            selected = selectedFilter,
                            onSelected = { selectedFilter = it }
                        )
                    }
                }

                when {
                    state.pets.isEmpty() -> {
                        item {
                            HomeEmptyStateCard(
                                title = "No pets added yet",
                                subtitle = "Add your first dog or cat to begin tracking care and logs.",
                                onGoManage = onGoManage
                            )
                        }
                    }

                    filteredPets.isEmpty() -> {
                        item {
                            HomeEmptyStateCard(
                                title = "No matching pets",
                                subtitle = "Try another filter to view more pets.",
                                onGoManage = null
                            )
                        }
                    }

                    viewMode == PetViewMode.LIST -> {
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

                    else -> {
                        items(gridRows) { rowPets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowPets.forEach { pet ->
                                    ModernPetGridCard(
                                        pet = pet,
                                        onClick = {
                                            vm.selectPet(pet.id)
                                            selectedPet = pet
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (rowPets.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
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
                    val finalNote = "${stool.label}${if (note.isNotBlank()) " • $note" else ""}"

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
private fun PremiumSectionShell(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.surface.copy(alpha = 0.98f),
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
        border = BorderStroke(
            1.dp,
            colors.outlineVariant.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
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
        shape = RoundedCornerShape(28.dp),
        color = colors.surface,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
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
                    containerColor = colors.surfaceVariant.copy(alpha = 0.72f)
                )

                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Attention",
                    value = attentionNeeded.toString(),
                    valueColor = if (attentionNeeded > 0) colors.tertiary else success,
                    containerColor = if (attentionNeeded > 0) {
                        colors.tertiary.copy(alpha = 0.12f)
                    } else {
                        success.copy(alpha = 0.12f)
                    }
                )

                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Vaccines",
                    value = dueVaccines.toString(),
                    valueColor = if (dueVaccines > 0) colors.tertiary else colors.primary,
                    containerColor = if (dueVaccines > 0) {
                        colors.tertiary.copy(alpha = 0.12f)
                    } else {
                        colors.primary.copy(alpha = 0.12f)
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
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = label,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(3.dp))

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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PremiumQuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Description,
            label = "Records",
            subtitle = "View logs",
            onClick = onRecords
        )

        PremiumQuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocationOn,
            label = "Find Vet",
            subtitle = "Nearby clinics",
            onClick = onFindVet
        )

        PremiumQuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AutoAwesome,
            label = "AI Care",
            subtitle = "Ask assistant",
            onClick = onAiCare
        )
    }
}

@Composable
private fun PremiumQuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "quick_action_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (pressed) 1.dp else 5.dp,
        label = "quick_action_elevation"
    )

    ElevatedCard(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation,
            pressedElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.primary.copy(alpha = 0.10f)
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = label,
                color = colors.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
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
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PremiumToggleChip(
                selected = current == PetViewMode.LIST,
                text = "List",
                onClick = { onChange(PetViewMode.LIST) }
            )

            PremiumToggleChip(
                selected = current == PetViewMode.GRID,
                text = "Grid",
                onClick = { onChange(PetViewMode.GRID) }
            )
        }
    }
}

@Composable
private fun PremiumToggleChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "toggle_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) colors.surface else Color.Transparent,
        tonalElevation = if (selected) 2.dp else 0.dp,
        shadowElevation = if (selected) 1.dp else 0.dp,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.onSurface else colors.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun PetFilterChips(
    selected: PetFilter,
    onSelected: (PetFilter) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val filterItems = listOf(
        PetFilter.ALL to "All",
        PetFilter.DOGS to "Dogs",
        PetFilter.CATS to "Cats",
        PetFilter.VACCINATED to "Vaccinated",
        PetFilter.NEEDS_VACCINE to "Needs vaccine"
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filterItems) { (filter, label) ->
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
                    borderColor = colors.outlineVariant.copy(alpha = 0.55f),
                    selectedBorderColor = colors.primary.copy(alpha = 0.55f)
                )
            )
        }
    }
}

@Composable
private fun PremiumPetCardContainer(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = tween(140),
        label = "pet_card_scale"
    )

    ElevatedCard(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Column(content = content)
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

    PremiumPetCardContainer(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(112.dp)
                    .fillMaxSize()
                    .background(colors.surfaceVariant.copy(alpha = 0.72f))
            ) {
                if (pet.imageUri.isNullOrBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.primary.copy(alpha = 0.12f)
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
private fun ModernPetGridCard(
    pet: Pet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val success = Color(0xFF22C55E)
    val statusColor = if (pet.vaccinated) success else colors.tertiary

    PremiumPetCardContainer(
        modifier = modifier,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .background(colors.surfaceVariant.copy(alpha = 0.72f))
        ) {
            if (pet.imageUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.primary.copy(alpha = 0.12f)
                    ) {
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(26.dp)
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

        Column(modifier = Modifier.padding(12.dp)) {
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

            Spacer(modifier = Modifier.height(10.dp))

            SmallStatusBadge(
                text = if (pet.vaccinated) "Vaccinated" else "Needs vaccine",
                background = statusColor.copy(alpha = 0.14f),
                content = statusColor
            )
        }
    }
}

@Composable
private fun SmallInfoBadge(text: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant.copy(alpha = 0.70f),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.18f))
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
private fun HomeEmptyStateCard(
    title: String,
    subtitle: String,
    onGoManage: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
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
            .sortedWith(
                compareByDescending<LogEntry> { it.createdAtMillis }
                    .thenByDescending { it.date }
                    .thenByDescending { it.id }
            )
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
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
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
                                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f)),
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
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
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
                            petSpecies = pet.species,
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
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
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
                                            log = log,
                                            petSpecies = currentPet.species,
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
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
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
    petSpecies: Species,
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
                iconRes = R.drawable.ic_appetite_normal,
                onClick = onAppetite
            )
            QuickLogActionCard(
                modifier = Modifier.weight(1f),
                label = "Stool",
                subtitle = "Track stool condition",
                iconRes = R.drawable.ic_stool_normal,
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
                iconRes = energyIconResForSpecies(petSpecies),
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
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = tween(120),
        label = "quick_log_action_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(22.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.surface.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        iconRes != null -> {
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = label,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        icon != null -> {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = colors.primary,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.Start
            ) {
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
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
    )
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
            val isDismissed =
                dismissState.targetValue == SwipeToDismissBoxValue.EndToStart ||
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
    log: LogEntry,
    petSpecies: Species,
    onEditClick: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val typeText = log.type.name.replace("_", " ")
    val noteText = log.note.ifBlank { "No details provided." }

    val logIconVector: ImageVector? = when (log.type) {
        DataLogType.WEIGHT -> Icons.Default.ShowChart
        DataLogType.VACCINE -> Icons.Default.Vaccines
        DataLogType.DEWORM -> Icons.Default.Vaccines
        else -> null
    }

    val logIconRes: Int? = when (log.type) {
        DataLogType.APPETITE -> appetiteIconFromNote(log.note)
        DataLogType.STOOL -> stoolIconFromNote(log.note)
        DataLogType.ENERGY -> energyEmotionIconFromNote(
            note = log.note,
            species = petSpecies
        )
        else -> null
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
                            text = typeText,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                    text = formatLogTime(log.createdAtMillis),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = formatLogDate(log.createdAtMillis),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
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
                text = noteText,
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
                    text = "${formatLogDate(log.createdAtMillis)} • ${formatLogTime(log.createdAtMillis)}",
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
                            text = "How was ${pet.name}'s appetite today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppetiteSwipeSelector(
                            selected = selectedAppetite,
                            onSelected = { selectedAppetite = it }
                        )
                    }

                    QuickLogCategory.STOOL -> {
                        Text(
                            text = "How was ${pet.name}'s stool today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        StoolSwipeSelector(
                            selected = selectedStool,
                            onSelected = { selectedStool = it }
                        )
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
                            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
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
private fun AppetiteSwipeSelector(
    selected: AppetiteLevel,
    onSelected: (AppetiteLevel) -> Unit
) {
    val levels = AppetiteLevel.entries
    val initialPage = levels.indexOf(selected).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { levels.size }
    )

    LaunchedEffect(selected) {
        val index = levels.indexOf(selected)
        if (index >= 0 && pagerState.currentPage != index) {
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val level = levels.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (level != selected) onSelected(level)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 28.dp),
            pageSpacing = 12.dp
        ) { page ->
            val level = levels[page]
            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            val scale = 0.90f + (1f - pageOffset.coerceIn(0f, 1f)) * 0.10f
            val alpha = 0.62f + (1f - pageOffset.coerceIn(0f, 1f)) * 0.38f

            AppetiteSwipeCard(
                level = level,
                selected = page == pagerState.currentPage,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SwipePageIndicator(
            pageCount = levels.size,
            currentPage = pagerState.currentPage
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Swipe left or right to choose appetite level",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StoolSwipeSelector(
    selected: StoolLevel,
    onSelected: (StoolLevel) -> Unit
) {
    val levels = StoolLevel.entries
    val initialPage = levels.indexOf(selected).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { levels.size }
    )

    LaunchedEffect(selected) {
        val index = levels.indexOf(selected)
        if (index >= 0 && pagerState.currentPage != index) {
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val level = levels.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (level != selected) onSelected(level)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 28.dp),
            pageSpacing = 12.dp
        ) { page ->
            val level = levels[page]
            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            val scale = 0.90f + (1f - pageOffset.coerceIn(0f, 1f)) * 0.10f
            val alpha = 0.62f + (1f - pageOffset.coerceIn(0f, 1f)) * 0.38f

            StoolSwipeCard(
                level = level,
                selected = page == pagerState.currentPage,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        SwipePageIndicator(
            pageCount = levels.size,
            currentPage = pagerState.currentPage
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Swipe left or right to choose stool condition",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppetiteSwipeCard(
    level: AppetiteLevel,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val model = appetiteUiModel(level)
    val accent = when (level) {
        AppetiteLevel.VERY_POOR -> Color(0xFFE57373)
        AppetiteLevel.LOW -> Color(0xFFFFB74D)
        AppetiteLevel.NORMAL -> Color(0xFFFFD54F)
        AppetiteLevel.GOOD -> Color(0xFF81C784)
        AppetiteLevel.VERY_GOOD -> Color(0xFF4DB6AC)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = accent.copy(alpha = if (selected) 0.13f else 0.09f),
        border = BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = accent.copy(alpha = if (selected) 0.48f else 0.28f)
        ),
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.surface.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.30f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = model.iconRes),
                        contentDescription = model.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SmallInfoBadge(text = "Appetite level")

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = model.title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = model.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = accent.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "${level.score}/5",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent.copy(alpha = 0.95f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StoolSwipeCard(
    level: StoolLevel,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val model = stoolUiModel(level)
    val accent = when (level) {
        StoolLevel.NORMAL -> Color(0xFF81C784)
        StoolLevel.SOFT -> Color(0xFFFFD54F)
        StoolLevel.LOOSE -> Color(0xFFFFB74D)
        StoolLevel.DIARRHEA -> Color(0xFFE57373)
        StoolLevel.HARD -> Color(0xFF90A4AE)
        StoolLevel.BLOOD_PRESENT -> Color(0xFFEF5350)
        StoolLevel.MUCUS_PRESENT -> Color(0xFF4FC3F7)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = accent.copy(alpha = if (selected) 0.13f else 0.09f),
        border = BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = accent.copy(alpha = if (selected) 0.48f else 0.28f)
        ),
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.surface.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.30f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = model.iconRes),
                        contentDescription = model.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SmallInfoBadge(text = "Stool condition")

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = model.title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = model.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SwipePageIndicator(
    pageCount: Int,
    currentPage: Int
) {
    val colors = MaterialTheme.colorScheme

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(
                        width = if (selected) 18.dp else 8.dp,
                        height = 8.dp
                    )
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (selected) colors.primary
                        else colors.outline.copy(alpha = 0.30f)
                    )
            )
        }
    }
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