package com.learning.multipet.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.learning.multipet.data.Pet
import com.learning.multipet.data.Species
import com.learning.multipet.viewmodel.AppViewModel

private val DOG_BREEDS = listOf("Aspin", "Beagle", "Chihuahua", "Golden Retriever", "Husky", "Labrador", "Poodle", "Shih Tzu")
private val CAT_BREEDS = listOf("Domestic Shorthair", "Persian", "Siamese", "Maine Coon", "Ragdoll", "Bengal")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePetsScreen(vm: AppViewModel) {
    val state by vm.state.collectAsState()

    var dialogOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Pet?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage Pets", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = {
                        editing = null
                        dialogOpen = true
                    }) { Icon(Icons.Default.Add, contentDescription = "Add") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.pets.isEmpty()) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("No pets yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Tap + to add a DOG or CAT profile (with optional photo).")
                        Button(onClick = { editing = null; dialogOpen = true }) {
                            Text("Add Pet")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.pets) { pet ->
                        PetGridTile(
                            pet = pet,
                            isSelected = pet.id == state.selectedPetId,
                            onClick = { vm.selectPet(pet.id) },
                            onEdit = { editing = pet; dialogOpen = true },
                            onDelete = { vm.deletePet(pet.id) }
                        )
                    }
                }
            }
        }
    }

    if (dialogOpen) {
        AddEditPetDialog(
            initial = editing,
            onDismiss = { dialogOpen = false; editing = null },
            onSave = { pet ->
                vm.upsertPet(pet)
                vm.selectPet(pet.id)
                dialogOpen = false
                editing = null
            }
        )
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
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    if (pet.imageUri.isNullOrBlank()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Pets, contentDescription = null)
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
                    Text(pet.name, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(
                        "${pet.species} • ${pet.breed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("${pet.ageYears}y") })
                AssistChip(onClick = {}, label = { Text("${pet.weightKg}kg") })
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPetDialog(
    initial: Pet?,
    onDismiss: () -> Unit,
    onSave: (Pet) -> Unit
) {
    var species by remember { mutableStateOf(initial?.species ?: Species.DOG) }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var breed by remember { mutableStateOf(initial?.breed ?: "") }
    var sex by remember { mutableStateOf(initial?.sex ?: "Unknown") }
    var ageYears by remember { mutableIntStateOf(initial?.ageYears ?: 1) }
    var weightText by remember { mutableStateOf((initial?.weightKg ?: 1.0).toString()) }
    var vaccinated by remember { mutableStateOf(initial?.vaccinated ?: false) }
    var imageUri by remember { mutableStateOf(initial?.imageUri) }

    val breedOptions = remember(species) { if (species == Species.DOG) DOG_BREEDS else CAT_BREEDS }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri.toString()
    }

    val weightValue = weightText.toDoubleOrNull()
    val canSave = name.trim().isNotEmpty() && breed.trim().isNotEmpty() && weightValue != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Pet" else "Edit Pet", fontWeight = FontWeight.ExtraBold) },
        confirmButton = {
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
                enabled = canSave
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Photo picker row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(CircleShape)
                            .clickable { pickImage.launch("image/*") },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (imageUri.isNullOrBlank()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Pick photo")
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

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text("Pet photo", fontWeight = FontWeight.Bold)
                        Text("Tap to upload (optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!imageUri.isNullOrBlank()) {
                            TextButton(onClick = { imageUri = null }) { Text("Remove") }
                        }
                    }
                }

                Divider()

                // Species segmented
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = species == Species.DOG,
                        onClick = { species = Species.DOG },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("DOG") }
                    SegmentedButton(
                        selected = species == Species.CAT,
                        onClick = { species = Species.CAT },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("CAT") }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                BreedAutocomplete(
                    label = "Breed",
                    value = breed,
                    options = breedOptions,
                    onValueChange = { breed = it }
                )

                // Sex dropdown
                var sexExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = sexExpanded,
                    onExpandedChange = { sexExpanded = !sexExpanded }
                ) {
                    OutlinedTextField(
                        value = sex,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sex") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sexExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false }
                    ) {
                        listOf("Male", "Female", "Unknown").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { sex = option; sexExpanded = false }
                            )
                        }
                    }
                }

                // Age stepper
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Age", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text("$ageYears year(s)", style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            FilledTonalIconButton(onClick = { if (ageYears > 0) ageYears-- }) { Text("−") }
                            Spacer(Modifier.width(8.dp))
                            FilledTonalIconButton(onClick = { if (ageYears < 30) ageYears++ }) { Text("+") }
                        }
                    }
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = weightText.isNotBlank() && weightValue == null,
                    supportingText = {
                        if (weightText.isNotBlank() && weightValue == null) Text("Enter a valid number (e.g., 3.5)")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = vaccinated, onCheckedChange = { vaccinated = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Vaccinated (basic)")
                }
            }
        }
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
        onExpandedChange = { expanded = !expanded }
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
