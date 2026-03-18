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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learning.multipet.data.Pet
import com.learning.multipet.ui.AppColors
import com.learning.multipet.viewmodel.AppViewModel

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onGoManage: () -> Unit
) {
    val state by vm.state.collectAsState()
    val selected = state.selectedPetId?.let { id -> state.pets.find { it.id == id } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        // Heads Up / AI summary card
        Card(colors = CardDefaults.cardColors(containerColor = AppColors.Teal)) {
            Column(Modifier.padding(16.dp)) {
                Text("Heads Up", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (state.pets.isEmpty()) "Add a pet to start monitoring and logging."
                    else "Log appetite, stool, energy, and weight. The AI will summarize trends safely.",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Your Pets", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onGoManage) { Text("Manage") }
        }

        if (state.pets.isEmpty()) {
            EmptyStateCard("No pets yet", "Add a DOG or CAT to begin.", onGoManage)
            return
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.pets) { pet ->
                PetCard(
                    pet = pet,
                    isSelected = pet.id == selected?.id,
                    onClick = { vm.selectPet(pet.id) }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        if (selected == null) {
            EmptyStateCard("No pet selected", "Tap a pet card to show its summary.", onGoManage = {})
        } else {
            PetSummaryCard(selected)
        }

        Spacer(Modifier.height(14.dp))

        Text("Quick Logs", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        // Quick log actions (monitoring-focused)
        QuickLogRow(enabled = selected != null)
    }
}

@Composable
private fun PetCard(pet: Pet, isSelected: Boolean, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.width(180.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) AppColors.ScreenBg else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(Icons.Default.Pets, contentDescription = null)
            Spacer(Modifier.height(8.dp))
            Text(pet.name, style = MaterialTheme.typography.titleSmall)
            Text("${pet.species} • ${pet.breed}", color = AppColors.TextMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            AssistChip(onClick = {}, label = { Text("Age: ${pet.ageYears}") })
        }
    }
}

@Composable
private fun PetSummaryCard(pet: Pet) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text(pet.name, style = MaterialTheme.typography.titleLarge)
            Text("${pet.species} • ${pet.breed}", color = AppColors.TextMuted)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(onClick = {}, label = { Text("Sex: ${pet.sex}") })
                AssistChip(onClick = {}, label = { Text("Wt: ${pet.weightKg} kg") })
                AssistChip(onClick = {}, label = { Text(if (pet.vaccinated) "Vaccinated" else "No vaccine") })
            }
        }
    }
}

@Composable
private fun QuickLogRow(enabled: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = { }, enabled = enabled, modifier = Modifier.weight(1f)) { Text("Appetite") }
            FilledTonalButton(onClick = { }, enabled = enabled, modifier = Modifier.weight(1f)) { Text("Stool") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = { }, enabled = enabled, modifier = Modifier.weight(1f)) { Text("Energy") }
            FilledTonalButton(onClick = { }, enabled = enabled, modifier = Modifier.weight(1f)) { Text("Weight") }
        }
        FilledTonalButton(onClick = { }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text("Vaccine / Deworm Log")
        }
    }
}

@Composable
private fun EmptyStateCard(title: String, subtitle: String, onGoManage: () -> Unit) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = AppColors.TextMuted)
            if (onGoManage != {}) {
                Spacer(Modifier.height(10.dp))
                Button(onClick = onGoManage) { Text("Go to Manage Pets") }
            }
        }
    }
}
