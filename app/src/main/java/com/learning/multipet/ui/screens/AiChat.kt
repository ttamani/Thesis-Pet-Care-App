package com.learning.multipet.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.learning.multipet.data.Pet
import com.learning.multipet.data.Species
import com.learning.multipet.viewmodel.AppViewModel
import com.learning.multipet.viewmodel.ChatMessage
import com.learning.multipet.viewmodel.ChatRole

private enum class QuickScope {
    CUSTOM,
    ALL_PETS,
    CATS,
    DOGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    vm: AppViewModel,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val state by vm.state.collectAsState()
    val messages by vm.chatMessages.collectAsState()
    val isAiLoading by vm.isAiLoading.collectAsState()

    val pets = state.pets
    val listState = rememberLazyListState()
    val pickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var input by rememberSaveable { mutableStateOf("") }
    var selectedPetIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var quickScope by rememberSaveable { mutableStateOf(QuickScope.CUSTOM) }
    var showPetPicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(messages.size, isAiLoading) {
        val lastIndex = messages.lastIndex
        if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
    }

    LaunchedEffect(pets, state.selectedPetId, state.lastActivePetId) {
        if (selectedPetIds.isEmpty() && pets.isNotEmpty()) {
            val preselected = state.selectedPetId ?: state.lastActivePetId
            if (preselected != null && pets.any { it.id == preselected }) {
                selectedPetIds = setOf(preselected)
                quickScope = QuickScope.CUSTOM
            }
        }

        val validIds = pets.map { it.id }.toSet()
        selectedPetIds = selectedPetIds.filterTo(linkedSetOf()) { it in validIds }
    }

    val selectedPets = pets.filter { it.id in selectedPetIds }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(colors.background, colors.surface)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = colors.onBackground,
                        navigationIconContentColor = colors.onBackground
                    ),
                    title = {
                        Column {
                            Text(
                                text = "AI Pet Care",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Choose one pet, many pets, or chat in general mode",
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    navigationIcon = {
                        FilledTonalIconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    modifier = Modifier.statusBarsPadding()
                )
            },
            bottomBar = {
                ChatComposer(
                    value = input,
                    onValueChange = { input = it },
                    onSend = {
                        val userText = input.trim()
                        if (userText.isEmpty()) return@ChatComposer

                        vm.sendAiMessage(
                            selectedPetIds = selectedPetIds,
                            userMessage = userText
                        )
                        input = ""
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CompactFocusCard(
                        pets = pets,
                        selectedPetIds = selectedPetIds,
                        quickScope = quickScope,
                        onScopeSelected = { scope ->
                            quickScope = scope
                            selectedPetIds = when (scope) {
                                QuickScope.ALL_PETS -> pets.map { it.id }.toSet()
                                QuickScope.CATS -> pets.filter { it.species == Species.CAT }.map { it.id }.toSet()
                                QuickScope.DOGS -> pets.filter { it.species == Species.DOG }.map { it.id }.toSet()
                                QuickScope.CUSTOM -> selectedPetIds
                            }
                        },
                        onClear = {
                            quickScope = QuickScope.CUSTOM
                            selectedPetIds = emptySet()
                        },
                        onOpenPicker = { showPetPicker = true }
                    )
                }

                item {
                    SafetyNotice()
                }

                items(messages) { message ->
                    ChatBubble(message = message)
                }

                if (isAiLoading) {
                    item {
                        TypingBubble()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showPetPicker) {
            PetPickerBottomSheet(
                pets = pets,
                selectedPetIds = selectedPetIds,
                quickScope = quickScope,
                sheetState = pickerSheetState,
                onDismiss = { showPetPicker = false },
                onScopeSelected = { scope ->
                    quickScope = scope
                    selectedPetIds = when (scope) {
                        QuickScope.ALL_PETS -> pets.map { it.id }.toSet()
                        QuickScope.CATS -> pets.filter { it.species == Species.CAT }.map { it.id }.toSet()
                        QuickScope.DOGS -> pets.filter { it.species == Species.DOG }.map { it.id }.toSet()
                        QuickScope.CUSTOM -> selectedPetIds
                    }
                },
                onTogglePet = { petId ->
                    quickScope = QuickScope.CUSTOM
                    selectedPetIds = if (petId in selectedPetIds) {
                        selectedPetIds - petId
                    } else {
                        selectedPetIds + petId
                    }
                },
                onClear = {
                    quickScope = QuickScope.CUSTOM
                    selectedPetIds = emptySet()
                },
                onApply = { showPetPicker = false }
            )
        }
    }
}

@Composable
private fun CompactFocusCard(
    pets: List<Pet>,
    selectedPetIds: Set<String>,
    quickScope: QuickScope,
    onScopeSelected: (QuickScope) -> Unit,
    onClear: () -> Unit,
    onOpenPicker: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val selectedPets = pets.filter { it.id in selectedPetIds }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.primary.copy(alpha = 0.12f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Conversation Focus",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Keep chat focused without cluttering the screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            QuickScopeRow(
                selectedScope = quickScope,
                onScopeSelected = onScopeSelected,
                onClear = onClear
            )

            Spacer(modifier = Modifier.height(12.dp))

            FocusSummaryCompact(
                selectedPets = selectedPets
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onOpenPicker,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Pets")
                }
            }
        }
    }
}

@Composable
private fun FocusSummaryCompact(
    selectedPets: List<Pet>
) {
    val colors = MaterialTheme.colorScheme

    val summary = when {
        selectedPets.isEmpty() -> "General mode: no pets selected"
        selectedPets.size == 1 -> "Focused on ${selectedPets.first().name}"
        selectedPets.size <= 3 -> "Focused on ${selectedPets.joinToString { it.name }}"
        else -> {
            val firstThree = selectedPets.take(3).joinToString { it.name }
            "Focused on $firstThree, +${selectedPets.size - 3} more"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = summary,
                color = colors.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (selectedPets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedPets.take(6).forEach { pet ->
                        SelectedPetChip(name = pet.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedPetChip(name: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = colors.surface.copy(alpha = 0.65f)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = colors.onSurface,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetPickerBottomSheet(
    pets: List<Pet>,
    selectedPetIds: Set<String>,
    quickScope: QuickScope,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onScopeSelected: (QuickScope) -> Unit,
    onTogglePet: (String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Choose Pets",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Select one pet, many pets, or use quick groups.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuickScopeRow(
                selectedScope = quickScope,
                onScopeSelected = onScopeSelected,
                onClear = onClear
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (pets.isEmpty()) {
                EmptyPetState()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pets.forEach { pet ->
                            PetSelectionCard(
                                pet = pet,
                                selected = pet.id in selectedPetIds,
                                onClick = { onTogglePet(pet.id) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Apply Selection")
            }
        }
    }
}

@Composable
private fun QuickScopeRow(
    selectedScope: QuickScope,
    onScopeSelected: (QuickScope) -> Unit,
    onClear: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedScope == QuickScope.ALL_PETS,
            onClick = { onScopeSelected(QuickScope.ALL_PETS) },
            label = { Text("All Pets") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.primary.copy(alpha = 0.15f),
                selectedLabelColor = colors.primary
            )
        )

        FilterChip(
            selected = selectedScope == QuickScope.CATS,
            onClick = { onScopeSelected(QuickScope.CATS) },
            label = { Text("Cats") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.primary.copy(alpha = 0.15f),
                selectedLabelColor = colors.primary
            )
        )

        FilterChip(
            selected = selectedScope == QuickScope.DOGS,
            onClick = { onScopeSelected(QuickScope.DOGS) },
            label = { Text("Dogs") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.primary.copy(alpha = 0.15f),
                selectedLabelColor = colors.primary
            )
        )

        Surface(
            shape = RoundedCornerShape(100.dp),
            color = colors.surfaceContainerHigh,
            modifier = Modifier.clickable(onClick = onClear)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Clear",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PetSelectionCard(
    pet: Pet,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val containerColor = if (selected) colors.primaryContainer else colors.surface
    val borderColor = if (selected) colors.primary else colors.outlineVariant
    val titleColor = if (selected) colors.onPrimaryContainer else colors.onSurface
    val subtitleColor = if (selected) colors.onPrimaryContainer.copy(alpha = 0.8f) else colors.onSurfaceVariant

    Surface(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        tonalElevation = if (selected) 4.dp else 0.dp,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) colors.primary.copy(alpha = 0.16f) else colors.surfaceContainerHigh
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = if (selected) colors.primary else colors.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                if (selected) {
                    Surface(
                        shape = CircleShape,
                        color = colors.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = colors.onPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = pet.name.ifBlank { "Unnamed Pet" },
                style = MaterialTheme.typography.titleSmall,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = pet.species.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            val details = buildString {
                if (pet.breed.isNotBlank()) append(pet.breed)
                if (pet.ageYears > 0) {
                    if (isNotBlank()) append(" • ")
                    append("${pet.ageYears}y")
                }
            }

            Text(
                text = if (details.isBlank()) "Tap to select" else details,
                style = MaterialTheme.typography.labelSmall,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyPetState() {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainerHigh
    ) {
        Text(
            text = "No pets yet. You can still chat in general mode, or add pets first for more contextual guidance.",
            modifier = Modifier.padding(14.dp),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SafetyNotice() {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.tertiaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = colors.onTertiaryContainer
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Safety: No diagnosis, no medication dosing, and no human medicine advice.",
                color = colors.onTertiaryContainer,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage
) {
    val colors = MaterialTheme.colorScheme
    val isUser = message.role == ChatRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 18.dp
            ),
            color = if (isUser) colors.primary else colors.surfaceContainerHigh
        ) {
            Text(
                text = message.text,
                color = if (isUser) colors.onPrimary else colors.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun TypingBubble() {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 6.dp,
                bottomEnd = 18.dp
            ),
            color = colors.surfaceContainerHigh
        ) {
            Text(
                text = "AI is typing...",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        color = colors.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            HorizontalDivider(color = colors.outlineVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Ask about appetite, stool, energy, vaccines…")
                    },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        disabledTextColor = colors.onSurfaceVariant,
                        cursorColor = colors.primary,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedPlaceholderColor = colors.onSurfaceVariant,
                        unfocusedPlaceholderColor = colors.onSurfaceVariant,
                        focusedLabelColor = colors.primary,
                        unfocusedLabelColor = colors.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onSend,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
}