package com.learning.multipet.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.learning.multipet.viewmodel.AppViewModel
import com.learning.multipet.viewmodel.ChatMessage
import com.learning.multipet.viewmodel.ChatRole

private enum class ChatFocusMode {
    SINGLE_PET,
    CATEGORY,
    NAME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    vm: AppViewModel,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val state by vm.state.collectAsState()
    val pet = state.pets.find { it.id == state.selectedPetId }

    val messages by vm.chatMessages.collectAsState()
    val isAiLoading by vm.isAiLoading.collectAsState()

    var input by remember { mutableStateOf("") }
    var focusMode by remember { mutableStateOf(ChatFocusMode.SINGLE_PET) }
    var selectedCategory by remember { mutableStateOf("All Pets") }
    var petNameSearch by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isAiLoading) {
        val lastIndex = messages.lastIndex
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colors.background,
            colors.surface
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
                    title = {
                        Column {
                            Text(
                                text = "AI Pet Care",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Ask general care questions with flexible pet context",
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
                        if (userText.isNotEmpty()) {
                            vm.sendAiMessage(
                                petName = when (focusMode) {
                                    ChatFocusMode.SINGLE_PET -> pet?.name
                                    ChatFocusMode.CATEGORY -> selectedCategory
                                    ChatFocusMode.NAME -> petNameSearch.ifBlank { null }
                                },
                                species = pet?.species?.name,
                                userMessage = userText
                            )
                            input = ""
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                FocusSelectorCard(
                    focusMode = focusMode,
                    onFocusModeChange = { focusMode = it },
                    petName = pet?.name,
                    species = pet?.species?.name,
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    petNameSearch = petNameSearch,
                    onPetNameSearchChange = { petNameSearch = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SafetyNotice()

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message = message)
                    }

                    if (isAiLoading) {
                        item {
                            TypingBubble()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusSelectorCard(
    focusMode: ChatFocusMode,
    onFocusModeChange: (ChatFocusMode) -> Unit,
    petName: String?,
    species: String?,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    petNameSearch: String,
    onPetNameSearchChange: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val categories = listOf("All Pets", "Cats", "Dogs")

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
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

                Column {
                    Text(
                        text = "Conversation Focus",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = when (focusMode) {
                            ChatFocusMode.SINGLE_PET ->
                                if (petName == null || species == null) {
                                    "Single pet mode is selected, but no pet is currently active."
                                } else {
                                    "Focused on $petName • ${species.replaceFirstChar { it.uppercase() }}"
                                }

                            ChatFocusMode.CATEGORY ->
                                "Focused on category: $selectedCategory"

                            ChatFocusMode.NAME ->
                                if (petNameSearch.isBlank()) {
                                    "Search or type a pet name to focus the conversation."
                                } else {
                                    "Focused on typed name: $petNameSearch"
                                }
                        },
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FocusModeTabs(
                selected = focusMode,
                onSelected = onFocusModeChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (focusMode) {
                ChatFocusMode.SINGLE_PET -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (petName == null || species == null) {
                                "No selected pet yet. Chat will still work in general mode."
                            } else {
                                "Current selected pet: $petName • ${species.replaceFirstChar { it.uppercase() }}"
                            },
                            color = colors.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                ChatFocusMode.CATEGORY -> {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { onCategoryChange(category) },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary.copy(alpha = 0.14f),
                                    selectedLabelColor = colors.primary,
                                    containerColor = colors.surfaceContainerHigh,
                                    labelColor = colors.onSurface
                                )
                            )
                        }
                    }
                }

                ChatFocusMode.NAME -> {
                    OutlinedTextField(
                        value = petNameSearch,
                        onValueChange = onPetNameSearchChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type a pet name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            cursorColor = colors.primary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedPlaceholderColor = colors.onSurfaceVariant,
                            unfocusedPlaceholderColor = colors.onSurfaceVariant,
                            focusedLeadingIconColor = colors.primary,
                            unfocusedLeadingIconColor = colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusModeTabs(
    selected: ChatFocusMode,
    onSelected: (ChatFocusMode) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FocusTab(
            text = "Single Pet",
            selected = selected == ChatFocusMode.SINGLE_PET,
            onClick = { onSelected(ChatFocusMode.SINGLE_PET) },
            modifier = Modifier.weight(1f),
            containerColor = colors.primary,
            onSelectedColor = colors.onPrimary
        )
        FocusTab(
            text = "Category",
            selected = selected == ChatFocusMode.CATEGORY,
            onClick = { onSelected(ChatFocusMode.CATEGORY) },
            modifier = Modifier.weight(1f),
            containerColor = colors.primary,
            onSelectedColor = colors.onPrimary
        )
        FocusTab(
            text = "Name",
            selected = selected == ChatFocusMode.NAME,
            onClick = { onSelected(ChatFocusMode.NAME) },
            modifier = Modifier.weight(1f),
            containerColor = colors.primary,
            onSelectedColor = colors.onPrimary
        )
    }
}

@Composable
private fun FocusTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color,
    onSelectedColor: androidx.compose.ui.graphics.Color
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            containerColor
        } else {
            colors.surfaceContainerHigh
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (selected) onSelectedColor else colors.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
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