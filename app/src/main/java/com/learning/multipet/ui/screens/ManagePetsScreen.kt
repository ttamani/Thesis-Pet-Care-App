package com.learning.multipet.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.learning.multipet.data.Pet
import com.learning.multipet.data.Species
import com.learning.multipet.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val DOG_BREEDS: List<String> = listOf(
    "Aspin",
    "Beagle",
    "Chihuahua",
    "Golden Retriever",
    "Husky",
    "Labrador",
    "Poodle",
    "Shih Tzu"
)

private val CAT_BREEDS: List<String> = listOf(
    "Domestic Shorthair",
    "Persian",
    "Siamese",
    "Maine Coon",
    "Ragdoll",
    "Bengal"
)

private enum class ManagePetsViewMode {
    LIST,
    GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePetsScreen(vm: AppViewModel) {
    val state by vm.state.collectAsState()
    val colors = MaterialTheme.colorScheme

    var editorOpen by remember { mutableStateOf(false) }
    var editingPet by remember { mutableStateOf<Pet?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var viewMode by rememberSaveable { mutableStateOf(ManagePetsViewMode.LIST) }

    val filteredPets: List<Pet> = remember(state.pets, searchQuery) {
        val normalizedQuery: String = searchQuery.trim()
        if (normalizedQuery.isBlank()) {
            state.pets
        } else {
            state.pets.filter { pet ->
                pet.name.contains(normalizedQuery, ignoreCase = true) ||
                        pet.breed.contains(normalizedQuery, ignoreCase = true) ||
                        pet.species.name.contains(normalizedQuery, ignoreCase = true) ||
                        pet.sex.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    val gridRows: List<List<Pet>> = remember(filteredPets) {
        filteredPets.chunked(2)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            ManagePetsHeader(
                petCount = state.pets.size,
                onAddPet = {
                    editingPet = null
                    editorOpen = true
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            SearchAndViewBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            when {
                state.pets.isEmpty() -> {
                    EmptyPetsState()
                }

                filteredPets.isEmpty() -> {
                    EmptySearchState()
                }

                viewMode == ManagePetsViewMode.LIST -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredPets,
                            key = { it.id }
                        ) { pet ->
                            PetListCard(
                                pet = pet,
                                isSelected = pet.id == state.selectedPetId,
                                onClick = { vm.selectPet(pet.id) },
                                onEdit = {
                                    editingPet = pet
                                    editorOpen = true
                                },
                                onDelete = { vm.deletePet(pet.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(gridRows) { rowPets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowPets.forEach { pet ->
                                    PetGridCard(
                                        pet = pet,
                                        isSelected = pet.id == state.selectedPetId,
                                        onClick = { vm.selectPet(pet.id) },
                                        onEdit = {
                                            editingPet = pet
                                            editorOpen = true
                                        },
                                        onDelete = { vm.deletePet(pet.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (rowPets.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    if (editorOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                editorOpen = false
                editingPet = null
            },
            sheetState = sheetState,
            containerColor = colors.surface,
            tonalElevation = 0.dp,
            dragHandle = null
        ) {
            AddEditPetSheet(
                initialPet = editingPet,
                onDismiss = {
                    editorOpen = false
                    editingPet = null
                },
                onSave = { pet ->
                    vm.upsertPet(pet)
                    vm.selectPet(pet.id)
                    editorOpen = false
                    editingPet = null
                }
            )
        }
    }
}

@Composable
private fun ManagePetsHeader(
    petCount: Int,
    onAddPet: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
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
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Manage Pets",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (petCount == 0) {
                            "Create pet profiles and manage them in one organized place."
                        } else {
                            "$petCount pet profile${if (petCount > 1) "s" else ""} available."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = onAddPet,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Pet")
            }
        }
    }
}

@Composable
private fun SearchAndViewBar(
    query: String,
    onQueryChange: (String) -> Unit,
    viewMode: ManagePetsViewMode,
    onViewModeChange: (ManagePetsViewMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            label = {
                Text("Search pets")
            },
            placeholder = {
                Text("Name, breed, species, sex")
            }
        )

        ViewModeToggle(
            viewMode = viewMode,
            onViewModeChange = onViewModeChange
        )
    }
}

@Composable
private fun ViewModeToggle(
    viewMode: ManagePetsViewMode,
    onViewModeChange: (ManagePetsViewMode) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilledTonalIconButton(
                onClick = { onViewModeChange(ManagePetsViewMode.LIST) },
                modifier = Modifier.size(42.dp),
                colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (viewMode == ManagePetsViewMode.LIST) {
                        colors.primary.copy(alpha = 0.14f)
                    } else {
                        Color.Transparent
                    },
                    contentColor = if (viewMode == ManagePetsViewMode.LIST) {
                        colors.primary
                    } else {
                        colors.onSurfaceVariant
                    }
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ViewAgenda,
                    contentDescription = "List view"
                )
            }

            FilledTonalIconButton(
                onClick = { onViewModeChange(ManagePetsViewMode.GRID) },
                modifier = Modifier.size(42.dp),
                colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (viewMode == ManagePetsViewMode.GRID) {
                        colors.primary.copy(alpha = 0.14f)
                    } else {
                        Color.Transparent
                    },
                    contentColor = if (viewMode == ManagePetsViewMode.GRID) {
                        colors.primary
                    } else {
                        colors.onSurfaceVariant
                    }
                )
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "Grid view"
                )
            }
        }
    }
}

@Composable
private fun EmptyPetsState() {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
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
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No pets added yet",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Use the Add Pet button above to create your first pet profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptySearchState() {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No matching pets",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Try searching with a different name, breed, or species.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PetListCard(
    pet: Pet,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val ageLabel: String = formatPetAge(pet.birthDateMillis)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) colors.surfaceVariant else colors.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(58.dp),
                    shape = CircleShape,
                    color = colors.surfaceVariant,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) {
                            colors.primary.copy(alpha = 0.45f)
                        } else {
                            colors.outline.copy(alpha = 0.45f)
                        }
                    )
                ) {
                    if (pet.imageUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant
                            )
                        }
                    } else {
                        AsyncImage(
                            model = pet.imageUri,
                            contentDescription = "Pet photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${pet.species} • ${pet.breed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetaChip(text = ageLabel)
                MetaChip(text = "${formatWeight(pet.weightKg)} kg")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetaChip(text = formatShortDate(pet.birthDateMillis))
                StatusChip(
                    text = if (pet.vaccinated) "Vaccinated" else "Needs vaccine",
                    active = pet.vaccinated
                )
            }

            HorizontalDivider(color = colors.outline.copy(alpha = 0.45f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = colors.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun PetGridCard(
    pet: Pet,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val ageLabel: String = formatPetAge(pet.birthDateMillis)

    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) colors.surfaceVariant else colors.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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
                        Surface(
                            shape = CircleShape,
                            color = colors.primary.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    AsyncImage(
                        model = pet.imageUri,
                        contentDescription = "Pet photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = pet.breed,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetaChip(text = ageLabel)
                    MetaChip(text = "${formatWeight(pet.weightKg)} kg")
                    StatusChip(
                        text = if (pet.vaccinated) "Vaccinated" else "Needs vaccine",
                        active = pet.vaccinated
                    )
                }

                HorizontalDivider(color = colors.outline.copy(alpha = 0.45f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = colors.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = colors.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun StatusChip(
    text: String,
    active: Boolean
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (active) {
            colors.primary.copy(alpha = 0.12f)
        } else {
            colors.tertiary.copy(alpha = 0.12f)
        },
        border = BorderStroke(
            1.dp,
            if (active) {
                colors.primary.copy(alpha = 0.45f)
            } else {
                colors.tertiary.copy(alpha = 0.6f)
            }
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = if (active) colors.primary else colors.tertiary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPetSheet(
    initialPet: Pet?,
    onDismiss: () -> Unit,
    onSave: (Pet) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    var species by rememberSaveable { mutableStateOf(initialPet?.species ?: Species.DOG) }
    var name by rememberSaveable { mutableStateOf(initialPet?.name ?: "") }
    var breed by rememberSaveable { mutableStateOf(initialPet?.breed ?: "") }
    var sex by rememberSaveable { mutableStateOf(initialPet?.sex ?: "Unknown") }
    var weightText by rememberSaveable {
        mutableStateOf(initialPet?.weightKg?.let { formatWeight(it) } ?: "")
    }
    var vaccinated by rememberSaveable { mutableStateOf(initialPet?.vaccinated ?: false) }
    var imageUri by rememberSaveable { mutableStateOf(initialPet?.imageUri) }
    var birthDateMillis by rememberSaveable {
        mutableLongStateOf(initialPet?.birthDateMillis ?: defaultBirthDateMillis())
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val breedOptions: List<String> = remember(species) {
        if (species == Species.DOG) DOG_BREEDS else CAT_BREEDS
    }

    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    val weightValue: Double? = weightText.toDoubleOrNull()
    val ageLabel: String = formatPetAge(birthDateMillis)
    val canSave: Boolean = name.trim().isNotEmpty() &&
            breed.trim().isNotEmpty() &&
            weightValue != null &&
            birthDateMillis > 0L

    if (showDatePicker) {
        val context = LocalContext.current

        LaunchedEffect(showDatePicker) {
            if (showDatePicker) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = if (birthDateMillis > 0L) {
                        birthDateMillis
                    } else {
                        System.currentTimeMillis()
                    }
                }

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val dialog = android.app.DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                        val selectedCalendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, selectedYear)
                            set(Calendar.MONTH, selectedMonth)
                            set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val selectedDateMillis = selectedCalendar.timeInMillis
                        val todayMillis = System.currentTimeMillis()

                        if (selectedDateMillis <= todayMillis) {
                            birthDateMillis = selectedDateMillis
                        }

                        showDatePicker = false
                    },
                    year,
                    month,
                    day
                )

                dialog.datePicker.maxDate = System.currentTimeMillis()

                dialog.setOnDismissListener {
                    showDatePicker = false
                }

                dialog.show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SheetTopHeader(
            title = if (initialPet == null) "Add Pet" else "Edit Pet",
            subtitle = "Set up your pet profile with cleaner and more complete information."
        )

        Spacer(modifier = Modifier.height(18.dp))

        FormSection(title = "Photo") {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    shape = CircleShape,
                    color = colors.surfaceVariant,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.55f))
                ) {
                    if (imageUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Pick photo",
                                tint = colors.onSurfaceVariant
                            )
                        }
                    } else {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Pet photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pet photo",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add a clear photo for faster recognition and a better-looking profile.",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (!imageUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { imageUri = null }
                        ) {
                            Text("Remove photo")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        FormSection(title = "Pet Type") {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = species == Species.DOG,
                    onClick = {
                        species = Species.DOG
                        if (breed !in DOG_BREEDS) {
                            breed = ""
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Dog")
                }

                SegmentedButton(
                    selected = species == Species.CAT,
                    onClick = {
                        species = Species.CAT
                        if (breed !in CAT_BREEDS) {
                            breed = ""
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Cat")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        FormSection(title = "Basic Information") {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StyledField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name"
                )

                BreedAutocomplete(
                    label = "Breed",
                    value = breed,
                    options = breedOptions,
                    onValueChange = { breed = it }
                )

                SexDropdown(
                    value = sex,
                    onValueChange = { sex = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        FormSection(title = "Health Details") {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BirthDatePickerCard(
                    birthDateMillis = birthDateMillis,
                    ageLabel = ageLabel,
                    onClick = { showDatePicker = true }
                )

                StyledField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = "Weight (kg)",
                    keyboardType = KeyboardType.Decimal,
                    isError = weightText.isNotBlank() && weightValue == null,
                    supportingText = if (weightText.isNotBlank() && weightValue == null) {
                        "Enter a valid number like 3.5"
                    } else {
                        null
                    }
                )

                VaccinationRow(
                    checked = vaccinated,
                    onCheckedChange = { vaccinated = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    val petToSave: Pet = if (initialPet != null) {
                        initialPet.copy(
                            species = species,
                            name = name.trim(),
                            breed = breed.trim(),
                            sex = sex,
                            birthDateMillis = birthDateMillis,
                            weightKg = weightValue,
                            vaccinated = vaccinated,
                            imageUri = imageUri
                        )
                    } else {
                        Pet(
                            species = species,
                            name = name.trim(),
                            breed = breed.trim(),
                            sex = sex,
                            birthDateMillis = birthDateMillis,
                            weightKg = weightValue,
                            vaccinated = vaccinated,
                            imageUri = imageUri
                        )
                    }
                    onSave(petToSave)
                },
                enabled = canSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text("Save Pet")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SheetTopHeader(
    title: String,
    subtitle: String
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            color = colors.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun BirthDatePickerCard(
    birthDateMillis: Long,
    ageLabel: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Birth date",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatFullDate(birthDateMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Auto age: $ageLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            Text(
                text = "Change",
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
private fun VaccinationRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "Vaccinated",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Mark this if the pet already has basic vaccination coverage.",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SexDropdown(
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sex") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Wc,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("Male", "Female", "Unknown").forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StyledField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        isError = isError,
        supportingText = {
            if (supportingText != null) {
                Text(supportingText)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreedAutocomplete(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions: List<String> = remember(value, options) {
        val query: String = value.trim()
        if (query.isEmpty()) {
            options.take(6)
        } else {
            options.filter { option ->
                option.contains(query, ignoreCase = true)
            }.take(6)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredOptions.isNotEmpty(),
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded && filteredOptions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatWeight(value: Double?): String {
    val safeValue: Double = value ?: 0.0
    return if (safeValue.rem(1.0) == 0.0) {
        safeValue.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", safeValue)
    }
}

private fun formatFullDate(value: Long): String {
    val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(value))
}

private fun formatShortDate(value: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(value))
}

private fun formatPetAge(birthDateMillis: Long): String {
    val birthCalendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = birthDateMillis
    }
    val todayCalendar: Calendar = Calendar.getInstance()

    var years: Int = todayCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
    var months: Int = todayCalendar.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)
    val days: Int = todayCalendar.get(Calendar.DAY_OF_MONTH) - birthCalendar.get(Calendar.DAY_OF_MONTH)

    if (days < 0) {
        months -= 1
    }

    if (months < 0) {
        years -= 1
        months += 12
    }

    if (years < 0) {
        years = 0
    }

    if (years == 0) {
        return when {
            months <= 0 -> "0 mo"
            months == 1 -> "1 mo"
            else -> "$months mos"
        }
    }

    return if (months <= 0) {
        if (years == 1) "1 yr" else "$years yrs"
    } else {
        val yearsText: String = if (years == 1) "1 yr" else "$years yrs"
        val monthsText: String = if (months == 1) "1 mo" else "$months mos"
        "$yearsText $monthsText"
    }
}

private fun defaultBirthDateMillis(): Long {
    val calendar: Calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, -1)
    return calendar.timeInMillis
}