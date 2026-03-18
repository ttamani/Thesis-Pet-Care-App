package com.learning.multipet.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.learning.multipet.data.Pet
import com.learning.multipet.ui.AppColors
import com.learning.multipet.viewmodel.AppViewModel

enum class LogType { Appetite, Stool, Energy, Weight, VaccineDeworm }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: AppViewModel,
    onGoManage: () -> Unit
) {

    val state by vm.state.collectAsState()

    // popup sheet
    var sheetPet by remember { mutableStateOf<Pet?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var permissionsAsked by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    val photosPermission = if (Build.VERSION.SDK_INT >= 33)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

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
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Heads Up
        Card(colors = CardDefaults.cardColors(containerColor = AppColors.Teal)) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Heads Up",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (state.pets.isEmpty()) "Add a pet to start monitoring and logging."
                    else "Log appetite, stool, energy, and weight. The AI will summarize trends safely.",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Pets", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onGoManage) { Text("Manage") }
        }

        if (state.pets.isEmpty()) {
            EmptyStateCard(
                title = "No pets yet",
                subtitle = "Add a DOG or CAT to begin.",
                onGoManage = onGoManage
            )
            return
        }

        Spacer(Modifier.height(10.dp))

        // ✅ Grid view for pet cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(state.pets) { pet ->
                PetGridCard(
                    pet = pet,
                    onClick = {
                        vm.selectPet(pet.id)
                        sheetPet = pet
                    }
                )
            }
        }


        Spacer(Modifier.height(14.dp))


    }

    // profile + quick logs
    if (sheetPet != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetPet = null },
            sheetState = sheetState
        ) {
            PetProfileSheet(
                pet = sheetPet!!,
                onClose = { sheetPet = null },
                onLogClick = { logType ->
                    // TODO: connect to Records/Logs flow later
                    // vm.addLog(petId = sheetPet!!.id, type = logType, ...)
                }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PetGridCard(
    pet: Pet,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {

            // ✅ avatar
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
                        Image(
                            painter = rememberAsyncImagePainter(pet.imageUri),
                            contentDescription = "Pet photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(pet.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                    Text(
                        "${pet.species} • ${pet.breed}",
                        color = AppColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            AssistChip(onClick = {}, label = { Text("Age: ${pet.ageYears}") })
        }
    }
}

@Composable
private fun PetProfileSheet(
    pet: Pet,
    onClose: () -> Unit,
    onLogClick: (LogType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // ✅ large avatar
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (pet.imageUri.isNullOrBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Pets, contentDescription = null)
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

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(pet.name, style = MaterialTheme.typography.titleLarge)
                Text("${pet.species} • ${pet.breed}", color = AppColors.TextMuted)
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(Modifier.height(12.dp))

        // ... keep the rest of your chips + quick logs
    }
}
@Composable
private fun EmptyStateCard(
    title: String,
    subtitle: String,
    onGoManage: (() -> Unit)? = null
) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = AppColors.TextMuted)

            if (onGoManage != null) {
                Spacer(Modifier.height(10.dp))
                Button(onClick = onGoManage) { Text("Go to Manage Pets") }
            }
        }
    }
}
