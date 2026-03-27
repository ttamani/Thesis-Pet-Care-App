package com.learning.multipet.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
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
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex + 2)
        }
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
        colors = listOf(
            colors.background,
            colors.surfaceContainerLowest,
            colors.background
        )
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
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = onBack,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = colors.surface.copy(alpha = 0.92f),
                                contentColor = colors.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "AI Pet Care",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (selectedPets.isEmpty()) {
                                    "General guidance mode"
                                } else {
                                    "${selectedPets.size} selected for contextual care guidance"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
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
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 14.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    PremiumFocusCard(
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

                if (messages.isEmpty()) {
                    item {
                        WelcomeCard(selectedPets = selectedPets)
                    }
                }

                itemsIndexed(messages) { _, message ->
                    ChatBubble(message = message)
                }

                if (isAiLoading) {
                    item {
                        TypingBubble()
                    }
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
private fun PremiumFocusCard(
    pets: List<Pet>,
    selectedPetIds: Set<String>,
    quickScope: QuickScope,
    onScopeSelected: (QuickScope) -> Unit,
    onClear: () -> Unit,
    onOpenPicker: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val selectedPets = pets.filter { it.id in selectedPetIds }

    val focusLabel = when {
        selectedPets.isEmpty() -> "General"
        selectedPets.size == 1 -> selectedPets.first().name
        selectedPets.size <= 3 -> selectedPets.joinToString { it.name }
        else -> "${selectedPets.take(2).joinToString { it.name }} +${selectedPets.size - 2}"
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(220))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(26.dp),
            color = colors.surface.copy(alpha = 0.96f),
            tonalElevation = 3.dp,
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.22f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Conversation Focus",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        AnimatedContent(
                            targetState = focusLabel,
                            label = "focus_label"
                        ) { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = onOpenPicker
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Choose pets"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                QuickScopeRow(
                    selectedScope = quickScope,
                    onScopeSelected = onScopeSelected,
                    onClear = onClear
                )

                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconAlpha by animateFloatAsState(
                        targetValue = 0.82f,
                        animationSpec = tween(240),
                        label = "focus_safety_alpha"
                    )

                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier
                            .size(14.dp)
                            .alpha(iconAlpha)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "General pet-care guidance only",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    selectedPets: List<Pet>
) {
    val colors = MaterialTheme.colorScheme

    val welcomeText = when {
        selectedPets.isEmpty() -> "Ask about appetite, stool, energy, vaccines, or daily care."
        selectedPets.size == 1 -> "You’re now focused on ${selectedPets.first().name} for more contextual guidance."
        else -> "You’re now focused on multiple pets for broader care guidance."
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(220))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(24.dp),
            color = colors.primaryContainer.copy(alpha = 0.42f),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.10f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.primary.copy(alpha = 0.10f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "AI Care Assistant",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = welcomeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
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
        FocusChip(
            text = "All",
            selected = selectedScope == QuickScope.ALL_PETS,
            onClick = { onScopeSelected(QuickScope.ALL_PETS) }
        )

        FocusChip(
            text = "Cats",
            selected = selectedScope == QuickScope.CATS,
            onClick = { onScopeSelected(QuickScope.CATS) }
        )

        FocusChip(
            text = "Dogs",
            selected = selectedScope == QuickScope.DOGS,
            onClick = { onScopeSelected(QuickScope.DOGS) }
        )

        Surface(
            shape = RoundedCornerShape(100.dp),
            color = colors.surfaceContainerHigh,
            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f)),
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
private fun FocusChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "focus_chip_scale"
    )

    val tonalElevation by animateDpAsState(
        targetValue = if (selected) 2.dp else 0.dp,
        animationSpec = tween(180),
        label = "focus_chip_elevation"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (selected) 1.5.dp else 1.dp,
        animationSpec = tween(180),
        label = "focus_chip_border_width"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(100.dp),
        color = if (selected) {
            colors.primary.copy(alpha = 0.12f)
        } else {
            colors.surfaceContainerLow
        },
        border = BorderStroke(
            borderWidth,
            if (selected) colors.primary.copy(alpha = 0.22f) else colors.outlineVariant.copy(alpha = 0.55f)
        ),
        tonalElevation = tonalElevation,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun PetSelectionCard(
    pet: Pet,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(120),
        label = "pet_picker_scale"
    )

    val containerColor = if (selected) colors.primaryContainer else colors.surface
    val borderColor = if (selected) colors.primary else colors.outlineVariant
    val titleColor = if (selected) colors.onPrimaryContainer else colors.onSurface
    val subtitleColor = if (selected) colors.onPrimaryContainer.copy(alpha = 0.8f) else colors.onSurfaceVariant

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .width(150.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
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
private fun ChatBubble(
    message: ChatMessage
) {
    val colors = MaterialTheme.colorScheme
    val isUser = message.role == ChatRole.USER

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(120))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isUser) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.primary.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier
                                .size(34.dp)
                                .padding(7.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(
                        topStart = 22.dp,
                        topEnd = 22.dp,
                        bottomStart = if (isUser) 22.dp else 8.dp,
                        bottomEnd = if (isUser) 8.dp else 22.dp
                    ),
                    color = if (isUser) colors.primary else colors.surface.copy(alpha = 0.96f),
                    border = if (isUser) null else BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f)),
                    tonalElevation = if (isUser) 0.dp else 1.dp,
                    shadowElevation = 0.dp
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
    }
}

@Composable
private fun TypingBubble() {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.primary.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier
                        .size(34.dp)
                        .padding(7.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 22.dp,
                    topEnd = 22.dp,
                    bottomStart = 8.dp,
                    bottomEnd = 22.dp
                ),
                color = colors.surface.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "AI is typing…",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
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
    val canSend = value.trim().isNotEmpty()

    Surface(
        color = colors.surface.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.55f))

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
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        cursorColor = colors.primary,
                        focusedBorderColor = colors.primary.copy(alpha = 0.85f),
                        unfocusedBorderColor = colors.outlineVariant.copy(alpha = 0.75f),
                        focusedContainerColor = colors.surfaceContainerLowest,
                        unfocusedContainerColor = colors.surfaceContainerLowest,
                        focusedPlaceholderColor = colors.onSurfaceVariant,
                        unfocusedPlaceholderColor = colors.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                FilledIconButton(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier.size(54.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                        disabledContainerColor = colors.surfaceContainerHigh,
                        disabledContentColor = colors.onSurfaceVariant
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