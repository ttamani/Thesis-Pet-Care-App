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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.learning.multipet.data.Pet
import com.learning.multipet.viewmodel.AppViewModel

enum class LogType { Appetite, Stool, Energy, Weight, VaccineDeworm }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: AppViewModel,
    onGoManage: () -> Unit,
    onOpenAi: () -> Unit
) {
    val state by vm.state.collectAsState()
    val colors = MaterialTheme.colorScheme

    var sheetPet by remember { mutableStateOf<Pet?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            SummaryCard(
                petCount = state.pets.size,
                attentionNeeded = attentionNeeded,
                dueVaccines = vaccinesDue
            )

            Spacer(modifier = Modifier.height(20.dp))

            QuickActionsRow(
                onRecords = { },
                onFindVet = { },
                onAiCare = onOpenAi
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Your Pets",
                subtitle = "View pet profiles and current care status."
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.pets.isEmpty()) {
                HomeEmptyStateCard(
                    title = "No pets added yet",
                    subtitle = "Add a pet profile to start monitoring care records and reminders.",
                    onGoManage = onGoManage
                )
            } else {
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
            }
        }
    }

    if (sheetPet != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetPet = null },
            sheetState = sheetState,
            containerColor = colors.surface
        ) {
            PetProfileSheet(
                pet = sheetPet!!,
                onClose = { sheetPet = null },
                onLogClick = { }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    petCount: Int,
    attentionNeeded: Int,
    dueVaccines: Int
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Pet Care Overview",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Monitor pet status, reminders, and recent updates.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Pets",
                    value = petCount.toString(),
                    valueColor = colors.onSurface,
                    containerColor = colors.surfaceVariant
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Attention",
                    value = attentionNeeded.toString(),
                    valueColor = if (attentionNeeded > 0) colors.tertiary else Color(0xFF22C55E),
                    containerColor = if (attentionNeeded > 0) colors.tertiary.copy(alpha = 0.14f) else Color(0xFF22C55E).copy(alpha = 0.14f)
                )

                StatCard(
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
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = valueColor,
                style = MaterialTheme.typography.headlineSmall,
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
        QuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Description,
            label = "Records",
            onClick = onRecords
        )

        QuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocationOn,
            label = "Find Vet",
            onClick = onFindVet
        )

        QuickActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AutoAwesome,
            label = "AI Care",
            onClick = onAiCare
        )
    }
}

@Composable
private fun QuickActionCard(
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
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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

            Spacer(modifier = Modifier.width(10.dp))

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
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    val colors = MaterialTheme.colorScheme

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun PetGridCard(
    pet: Pet,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val needsAttention = !pet.vaccinated

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape),
                    shape = CircleShape,
                    color = colors.surfaceVariant
                ) {
                    if (pet.imageUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = colors.onSurface
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

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${pet.species} • ${pet.breed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetaBadge(text = "Age ${pet.ageYears}")

                StatusBadge(
                    text = if (needsAttention) "Needs vaccine" else "Up to date",
                    background = if (needsAttention) colors.tertiary.copy(alpha = 0.14f) else Color(0xFF22C55E).copy(alpha = 0.14f),
                    content = if (needsAttention) colors.tertiary else Color(0xFF22C55E)
                )
            }
        }
    }
}

@Composable
private fun MetaBadge(text: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Text(
            text = text,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatusBadge(
    text: String,
    background: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = background,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Text(
            text = text,
            color = content,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PetProfileSheet(
    pet: Pet,
    onClose: () -> Unit,
    onLogClick: (LogType) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                color = colors.surfaceVariant
            ) {
                if (pet.imageUri.isNullOrBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = colors.onSurface
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${pet.species} • ${pet.breed}",
                    color = colors.onSurfaceVariant
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colors.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        InfoRow("Age", "${pet.ageYears}")
        InfoRow("Weight", "${pet.weightKg} kg")
        InfoRow("Vaccinated", if (pet.vaccinated) "Yes" else "No")

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onLogClick(LogType.Appetite) },
                border = BorderStroke(1.dp, colors.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Log Appetite")
            }

            OutlinedButton(
                onClick = { onLogClick(LogType.Energy) },
                border = BorderStroke(1.dp, colors.outline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Log Energy")
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.onSurfaceVariant
        )
        Text(
            text = value,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium
        )
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