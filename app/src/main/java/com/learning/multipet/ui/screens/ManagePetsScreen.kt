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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.learning.multipet.data.Pet
import com.learning.multipet.data.Species
import com.learning.multipet.viewmodel.AppViewModel
import java.util.Locale

private val DOG_BREEDS = listOf(
    "Aspin", "Beagle", "Chihuahua", "Golden Retriever",
    "Husky", "Labrador", "Poodle", "Shih Tzu"
)

private val CAT_BREEDS = listOf(
    "Domestic Shorthair", "Persian", "Siamese",
    "Maine Coon", "Ragdoll", "Bengal"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePetsScreen(vm: AppViewModel) {
    val state by vm.state.collectAsState()
    val colors = MaterialTheme.colorScheme

    var editorOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Pet?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Scaffold(
            containerColor = colors.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (state.pets.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyPetsState(
                            onAddPet = {
                                editing = null
                                editorOpen = true
                            }
                        )
                    }
                } else {
                    ManagePetsHeader(
                        petCount = state.pets.size,
                        onAddPet = {
                            editing = null
                            editorOpen = true
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.pets, key = { it.id }) { pet ->
                            PetGridTile(
                                pet = pet,
                                isSelected = pet.id == state.selectedPetId,
                                onClick = { vm.selectPet(pet.id) },
                                onEdit = {
                                    editing = pet
                                    editorOpen = true
                                },
                                onDelete = { vm.deletePet(pet.id) }
                            )
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
                editing = null
            },
            sheetState = sheetState,
            containerColor = colors.surface,
            tonalElevation = 0.dp,
            dragHandle = null
        ) {
            AddEditPetSheet(
                initial = editing,
                onDismiss = {
                    editorOpen = false
                    editing = null
                },
                onSave = { pet ->
                    vm.upsertPet(pet)
                    vm.selectPet(pet.id)
                    editorOpen = false
                    editing = null
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {

        }

        Button(
            onClick = onAddPet,
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
@Composable
private fun EmptyPetsState(
    onAddPet: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth()
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

            Spacer(Modifier.height(14.dp))

            Text(
                text = "No pets added yet",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Create your first pet profile to start tracking care records, health logs, and reminders.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onAddPet,
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

@Composable
private fun PetGridTile(
    pet: Pet,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) colors.surfaceVariant else colors.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) colors.primary else colors.outline
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        color = colors.surfaceVariant
                    ) {
                        if (pet.imageUri.isNullOrBlank()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = colors.onSurface
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

                    Spacer(Modifier.width(10.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = pet.name,
                            color = colors.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
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
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaChip("${pet.ageYears}y")
                MetaChip("${formatWeight(pet.weightKg)}kg")
            }

            StatusChip(
                text = if (pet.vaccinated) "Vaccinated" else "Needs vaccine",
                active = pet.vaccinated
            )

            HorizontalDivider(color = colors.outline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = colors.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
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
    val success = Color(0xFF22C55E)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (active) success.copy(alpha = 0.14f) else colors.tertiary.copy(alpha = 0.14f),
        border = BorderStroke(
            1.dp,
            if (active) success else colors.tertiary
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = if (active) success else colors.tertiary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPetSheet(
    initial: Pet?,
    onDismiss: () -> Unit,
    onSave: (Pet) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    var species by rememberSaveable { mutableStateOf(initial?.species ?: Species.DOG) }
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var breed by rememberSaveable { mutableStateOf(initial?.breed ?: "") }
    var sex by rememberSaveable { mutableStateOf(initial?.sex ?: "Unknown") }
    var ageYears by rememberSaveable { mutableIntStateOf(initial?.ageYears ?: 1) }
    var weightText by rememberSaveable {
        mutableStateOf(initial?.weightKg?.let { formatWeight(it) } ?: "1")
    }
    var vaccinated by rememberSaveable { mutableStateOf(initial?.vaccinated ?: false) }
    var imageUri by rememberSaveable { mutableStateOf(initial?.imageUri) }

    val breedOptions = remember(species) {
        if (species == Species.DOG) DOG_BREEDS else CAT_BREEDS
    }

    val scrollState = rememberScrollState()

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri.toString()
    }

    val weightValue = weightText.toDoubleOrNull()
    val canSave = name.trim().isNotEmpty() &&
            breed.trim().isNotEmpty() &&
            weightValue != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = if (initial == null) "Add Pet" else "Edit Pet",
            color = colors.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Create or update your pet’s profile details.",
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(18.dp))

        FormSection(title = "Photo") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .clickable { pickImage.launch("image/*") },
                    shape = CircleShape,
                    color = colors.surfaceVariant,
                    border = BorderStroke(1.dp, colors.outline)
                ) {
                    if (imageUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
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

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Pet photo",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Upload an optional profile image for easier identification.",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (!imageUri.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        TextButton(onClick = { imageUri = null }) {
                            Text("Remove photo")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        FormSection(title = "Pet Type") {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = species == Species.DOG,
                    onClick = {
                        species = Species.DOG
                        if (breed !in DOG_BREEDS) breed = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) {
                    Text("Dog")
                }

                SegmentedButton(
                    selected = species == Species.CAT,
                    onClick = {
                        species = Species.CAT
                        if (breed !in CAT_BREEDS) breed = ""
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) {
                    Text("Cat")
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        FormSection(title = "Basic Information") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

        Spacer(Modifier.height(14.dp))

        FormSection(title = "Health Details") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AgeStepper(
                    ageYears = ageYears,
                    onDecrease = { if (ageYears > 0) ageYears-- },
                    onIncrease = { if (ageYears < 30) ageYears++ }
                )

                StyledField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = "Weight (kg)",
                    keyboardType = KeyboardType.Decimal,
                    isError = weightText.isNotBlank() && weightValue == null,
                    supportingText = if (weightText.isNotBlank() && weightValue == null)
                        "Enter a valid number like 3.5"
                    else null
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = vaccinated,
                        onCheckedChange = { vaccinated = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Vaccinated",
                            color = colors.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Mark if the pet has basic vaccination coverage.",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, colors.outline)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    val pet = (initial ?: Pet()).copy(
                        species = species,
                        name = name.trim(),
                        breed = breed.trim(),
                        sex = sex,
                        ageYears = ageYears,
                        weightKg = weightValue ?: 1.0,
                        vaccinated = vaccinated,
                        imageUri = imageUri
                    )
                    onSave(pet)
                },
                enabled = canSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text("Save Pet")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
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

            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AgeStepper(
    ageYears: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Age",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$ageYears year(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalIconButton(onClick = onDecrease) {
                    Text("−", color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
                }
                FilledTonalIconButton(onClick = onIncrease) {
                    Text("+", color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
                }
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
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
        modifier = Modifier.fillMaxWidth()
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

    val filtered = remember(value, options) {
        val q = value.trim()
        if (q.isEmpty()) emptyList()
        else options.filter { it.contains(q, ignoreCase = true) }.take(6)
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = {
            if (filtered.isNotEmpty()) expanded = !expanded
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
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatWeight(value: Double?): String {
    val safeValue = value ?: 0.0
    return if (safeValue.rem(1.0) == 0.0) {
        safeValue.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", safeValue)
    }
}