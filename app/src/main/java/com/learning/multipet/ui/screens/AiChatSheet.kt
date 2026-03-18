package com.learning.multipet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learning.multipet.viewmodel.AppViewModel

@Composable
fun AiChatSheet(vm: AppViewModel) {
    val pet = vm.resolvedPetForAI()
    var input by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf("AI: Hi! I can give general, safe pet-care guidance.")) }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("AI Pet Care", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))

        Text(
            text = if (pet == null) "Context: No pets yet (general mode)"
            else "Context: ${pet.name} (${pet.species}) • tap Manage to change selection anytime",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(12.dp))

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                messages.takeLast(6).forEach { Text(it) }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ask about appetite, stool, energy, vaccines…") }
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                val userText = input.trim()
                if (userText.isNotEmpty()) {
                    messages = messages + "You: $userText" + "AI: Thanks. I’ll give general guidance only. If symptoms are severe (no eating ~24h, watery diarrhea, difficulty breathing), please visit a vet clinic."
                    input = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send")
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "Safety: No medication dosing. No human medicines. Clinic referral for red flags. References can be shown in future API version.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
