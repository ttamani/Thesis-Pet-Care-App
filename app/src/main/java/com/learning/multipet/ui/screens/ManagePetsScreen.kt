package com.learning.multipet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learning.multipet.data.Pet
import com.learning.multipet.viewmodel.AppViewModel

@Composable
fun ManagePetsScreen(vm: AppViewModel) {
    val state by vm.state.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var editPet by remember { mutableStateOf<Pet?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Manage Pets", style = MaterialTheme.typography.titleLarge)
            FilledTonalButton(onClick = { editPet = null; showForm = true }) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("Add")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (state.pets.isEmpty()) {
            ElevatedCard {
                Column(Modifier.padding(16.dp)) {
                    Text("No pets yet")
                    Spacer(Modifier.height(6.dp))
                    Text("Add a DOG or CAT to begin.")
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.pets) { pet ->
                    ElevatedCard(
                        onClick = { vm.selectPet(pet.id) }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Pets, contentDescription = null)
                            Spacer(Modifier.height(8.dp))
                            Text(pet.name, style = MaterialTheme.typography.titleSmall)
                            Text("${pet.species} • ${pet.breed}", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { editPet = pet; showForm = true }) {
                                    Icon(Icons.Default.Edit, null)
                                }
                                IconButton(onClick = { vm.deletePet(pet.id) }) {
                                    Icon(Icons.Default.Delete, null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        AddEditPetDialog(
            initial = editPet,
            onDismiss = { showForm = false },
            onSave = { pet ->
                if (editPet == null) vm.addPet(pet) else vm.updatePet(pet)
                showForm = false
            }
        )
    }
}
