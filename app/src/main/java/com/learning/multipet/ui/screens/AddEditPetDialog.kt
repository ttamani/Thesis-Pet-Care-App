package com.learning.multipet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.learning.multipet.data.Pet
import com.learning.multipet.data.Species

private val DOG_BREEDS = listOf(
    "Aspin", "Beagle", "Chihuahua", "Golden Retriever", "Husky",
    "Labrador", "Poodle", "Shih Tzu"
)

private val CAT_BREEDS = listOf(
    "Domestic Shorthair", "Persian", "Siamese", "Maine Coon", "Ragdoll", "Bengal"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPetDialog(
    initial: Pet?,
    onDismiss: () -> Unit,
    onSave: (Pet) -> Unit
) {
    var species by remember { mutableStateOf(initial?.species ?: Species.DOG) }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var breed by remember { mutableStateOf(initial?.breed ?: "") }
    var sex by remember { mutableStateOf(initial?.sex ?: "Unknown") }
    var ageYears by remember { mutableIntStateOf(initial?.ageYears ?: 1) }
    var weight by remember { mutableStateOf((initial?.weightKg ?: 1.0).toString()) }
    var vaccinated by remember { mutableStateOf(initial?.vaccinated ?: false) }

    val breedOptions = remember(species) { if (species == Species.DOG) DOG_BREEDS else CAT_BREEDS }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val w = weight.toDoubleOrNull() ?: 1.0
                    onSave(
                        (initial ?: Pet()).copy(
                            species = species,
                            name = name.trim(),
                            breed = breed.trim(),
                            sex = sex,
                            ageYears = ageYears,
                            weightKg = w,
                            vaccinated = vaccinated
                        )
                    )
                },
                enabled = name.isNotBlank() && breed.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (initial == null) "Add Pet" else "Edit Pet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // DOG / CAT only
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
                var expandedSex by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedSex,
                    onExpandedChange = { expandedSex = !expandedSex }
                ) {
                    OutlinedTextField(
                        value = sex,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sex") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedSex) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSex,
                        onDismissRequest = { expandedSex = false }
                    ) {
                        listOf("Male", "Female", "Unknown").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = { sex = it; expandedSex = false }
                            )
                        }
                    }
                }

                // Age stepper
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Age (years): $ageYears")
                    Row {
                        TextButton(onClick = { if (ageYears > 0) ageYears-- }) { Text("−") }
                        TextButton(onClick = { if (ageYears < 30) ageYears++ }) { Text("+") }
                    }
                }

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
