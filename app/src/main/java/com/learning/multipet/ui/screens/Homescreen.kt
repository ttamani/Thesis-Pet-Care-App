package com.learning.multipet.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.learning.multipet.data.Pet
import com.learning.multipet.data.Species
import com.learning.multipet.viewmodel.AppViewModel

enum class LogType { Appetite, Stool, Energy, Weight, VaccineDeworm }

enum class PetViewMode {
    GRID,
    LIST
}

private enum class PetFilter {
    ALL, DOGS, CATS, VACCINATED, NEEDS_VACCINE
}

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onGoManage: () -> Unit,
    onOpenAi: () -> Unit,
    viewMode: PetViewMode,
    onViewModeChange: (PetViewMode) -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = MaterialTheme.colorScheme
    val success = Color(0xFF22C55E)

    var selectedFilter by remember { mutableStateOf(PetFilter.ALL) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var permissionsAsked by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    val photosPermission =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

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

    val vaccinesDue = state.pets.count { !it.vaccinated }
    val attentionNeeded = vaccinesDue

    val filteredPets = remember(state.pets, selectedFilter) {
        when (selectedFilter) {
            PetFilter.ALL -> state.pets
            PetFilter.DOGS -> state.pets.filter { it.species == Species.DOG }
            PetFilter.CATS -> state.pets.filter { it.species == Species.CAT }
            PetFilter.VACCINATED -> state.pets.filter { it.vaccinated }
            PetFilter.NEEDS_VACCINE -> state.pets.filter { !it.vaccinated }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        DashboardOverviewCard(
            petCount = state.pets.size,
            attentionNeeded = attentionNeeded,
            dueVaccines = vaccinesDue
        )

        Spacer(modifier = Modifier.height(16.dp))

        QuickActionsRow(
            onRecords = { },
            onFindVet = { },
            onAiCare = onOpenAi
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Pets",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${filteredPets.size} visible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ViewToggleButton(
                current = viewMode,
                onChange = onViewModeChange
            )

            Spacer(Modifier.width(8.dp))

            TextButton(onClick = onGoManage) {
                Text("Manage")
            }
        }


        Spacer(modifier = Modifier.height(10.dp))

        PetFilterChips(
            selected = selectedFilter,
            onSelected = { selectedFilter = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.pets.isEmpty()) {
            HomeEmptyStateCard(
                title = "No pets added yet",
                subtitle = "Add your first dog or cat to begin tracking care and logs.",
                onGoManage = onGoManage
            )
        } else if (filteredPets.isEmpty()) {
            HomeEmptyStateCard(
                title = "No matching pets",
                subtitle = "Try another filter to view more pets.",
                onGoManage = null
            )
        } else {
            if (viewMode == PetViewMode.LIST) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredPets, key = { it.id }) { pet ->
                        ModernPetListCard(
                            pet = pet,
                            onClick = {
                                vm.selectPet(pet.id)
                                selectedPet = pet
                            }
                        )
                    }
                }

            } else {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredPets, key = { it.id }) { pet ->
                        ModernPetGridCard(
                            pet = pet,
                            onClick = {
                                vm.selectPet(pet.id)
                                selectedPet = pet
                            }
                        )
                    }
                }

            }
        }
    }

    selectedPet?.let { pet ->
        FullScreenPetProfile(
            pet = pet,
            vm = vm,
            onClose = { selectedPet = null },
            onLogClick = { }
        )
    }
}

@Composable
private fun ModernPetGridCard(
    pet: Pet,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface
        )
    ) {

        Column {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(colors.surfaceVariant)
            ) {

                if (pet.imageUri.isNullOrBlank()) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
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

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                Text(
                    text = pet.breed,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
@Composable
private fun ViewToggleButton(
    current: PetViewMode,
    onChange: (PetViewMode) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row {

            ToggleItem(
                selected = current == PetViewMode.LIST,
                text = "List",
                onClick = { onChange(PetViewMode.LIST) }
            )

            ToggleItem(
                selected = current == PetViewMode.GRID,
                text = "Grid",
                onClick = { onChange(PetViewMode.GRID) }
            )
        }
    }
}

@Composable
private fun ToggleItem(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        color = if (selected) colors.primary.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.primary else colors.onSurfaceVariant
        )
    }
}
@Composable
private fun DashboardOverviewCard(
    petCount: Int,
    attentionNeeded: Int,
    dueVaccines: Int
) {
    val colors = MaterialTheme.colorScheme
    val success = Color(0xFF22C55E)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Pet Care Overview",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Pets",
                    value = petCount.toString(),
                    valueColor = colors.onSurface,
                    containerColor = colors.surfaceVariant
                )

                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Attention",
                    value = attentionNeeded.toString(),
                    valueColor = if (attentionNeeded > 0) colors.tertiary else success,
                    containerColor = if (attentionNeeded > 0) colors.tertiary.copy(alpha = 0.14f) else success.copy(alpha = 0.14f)
                )

                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Vaccines",
                    value = dueVaccines.toString(),
                    valueColor = if (dueVaccines > 0) colors.tertiary else colors.primary,
                    containerColor = if (dueVaccines > 0) colors.tertiary.copy(alpha = 0.14f) else colors.primary.copy(alpha = 0.14f)
                )
            }
        }
    }
}

@Composable
private fun CompactStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color,
    containerColor: Color
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                color = valueColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onRecords: () -> Unit,
    onFindVet: () -> Unit,
    onAiCare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CompactQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Description,
            label = "Records",
            onClick = onRecords
        )

        CompactQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocationOn,
            label = "Find Vet",
            onClick = onFindVet
        )

        CompactQuickAction(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AutoAwesome,
            label = "AI Care",
            onClick = onAiCare
        )
    }
}

@Composable
private fun CompactQuickAction(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = colors.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PetFilterChips(
    selected: PetFilter,
    onSelected: (PetFilter) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val items = listOf(
        PetFilter.ALL to "All",
        PetFilter.DOGS to "Dogs",
        PetFilter.CATS to "Cats",
        PetFilter.VACCINATED to "Vaccinated",
        PetFilter.NEEDS_VACCINE to "Needs vaccine"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { (filter, label) ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(alpha = 0.14f),
                    selectedLabelColor = colors.primary,
                    containerColor = colors.surfaceVariant,
                    labelColor = colors.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == filter,
                    borderColor = colors.outline,
                    selectedBorderColor = colors.primary
                )
            )
        }
    }
}

@Composable
private fun ModernPetListCard(
    pet: Pet,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val success = Color(0xFF22C55E)
    val statusColor = if (pet.vaccinated) success else colors.tertiary
    val speciesLabel = if (pet.species == Species.DOG) "Dog" else "Cat"

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(108.dp)
                    .fillMaxSize()
                    .background(colors.surfaceVariant)
            ) {
                if (pet.imageUri.isNullOrBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.primary.copy(alpha = 0.14f)
                        ) {
                            Box(
                                modifier = Modifier.size(52.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pet.breed,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallInfoBadge(text = speciesLabel)
                    SmallStatusBadge(
                        text = if (pet.vaccinated) "Vaccinated" else "Needs vaccine",
                        background = statusColor.copy(alpha = 0.14f),
                        content = statusColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallInfoBadge(text: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun SmallStatusBadge(
    text: String,
    background: Color,
    content: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = background
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FullScreenPetProfile(
    pet: Pet,
    vm: AppViewModel,
    onClose: () -> Unit,
    onLogClick: (LogType) -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = MaterialTheme.colorScheme
    val success = Color(0xFF22C55E)
    val petLogs = remember(state.logs, pet.id) {
        state.logs.filter { it.petId == pet.id }.sortedByDescending { it.date }
    }
    val speciesLabel = if (pet.species == Species.DOG) "Dog" else "Cat"
    val statusColor = if (pet.vaccinated) success else colors.tertiary

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colors.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = pet.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Profile and logs",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(colors.surfaceVariant)
                        ) {
                            if (pet.imageUri.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = colors.primary.copy(alpha = 0.14f)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(96.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Pets,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
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

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SmallInfoBadge(text = speciesLabel)
                                SmallStatusBadge(
                                    text = if (pet.vaccinated) "Vaccinated" else "Needs vaccine",
                                    background = statusColor.copy(alpha = 0.14f),
                                    content = statusColor
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = pet.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = pet.breed,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProfileMetaChip("Age ${pet.ageYears}")
                                ProfileMetaChip("Weight ${pet.weightKg}kg")
                                ProfileMetaChip("Sex ${pet.sex}")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Quick Logs",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                QuickLogChip(
                                    label = "Appetite",
                                    icon = Icons.Default.Restaurant,
                                    onClick = { onLogClick(LogType.Appetite) }
                                )
                            }
                            item {
                                QuickLogChip(
                                    label = "Energy",
                                    icon = Icons.Default.ShowChart,
                                    onClick = { onLogClick(LogType.Energy) }
                                )
                            }
                            item {
                                QuickLogChip(
                                    label = "Weight",
                                    icon = Icons.Default.Description,
                                    onClick = { onLogClick(LogType.Weight) }
                                )
                            }
                            item {
                                QuickLogChip(
                                    label = "Vaccine",
                                    icon = Icons.Default.Vaccines,
                                    onClick = { onLogClick(LogType.VaccineDeworm) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Recent Logs",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (petLogs.isEmpty()) {
                            Text(
                                text = "No logs yet for this pet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        } else {
                            petLogs.forEachIndexed { index, log ->
                                PetLogCard(
                                    type = log.type.name.replace("_", " "),
                                    note = log.note.ifBlank { "No details provided." },
                                    dateText = log.date.toString()
                                )
                                if (index != petLogs.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun ProfileMetaChip(text: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickLogChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = colors.surfaceVariant,
            labelColor = colors.onSurfaceVariant,
            leadingIconContentColor = colors.primary
        ),
        border = BorderStroke(1.dp, colors.outline)
    )
}

@Composable
private fun PetLogCard(
    type: String,
    note: String,
    dateText: String
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = type,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomeEmptyStateCard(
    title: String,
    subtitle: String,
    onGoManage: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline)
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

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            if (onGoManage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGoManage,
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
}