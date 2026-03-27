package com.learning.multipet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learning.multipet.data.LogEntry
import com.learning.multipet.data.LogType
import com.learning.multipet.data.Pet
import java.time.LocalDate

@Immutable
data class RecommendationHeroState(
    val title: String,
    val subtitle: String,
    val helperText: String,
    val chips: List<String>,
    val accentColor: Color
)

private enum class RecommendationPriority(
    val score: Int
) {
    STOOL_CONCERN(100),
    APPETITE_CONCERN(90),
    ENERGY_CONCERN(80),
    WEIGHT_CONCERN(70),
    VACCINE_DUE(60),
    NO_LOGS(20),
    HEALTHY(10)
}

private data class PrioritizedRecommendation(
    val priority: RecommendationPriority,
    val petName: String?,
    val title: String,
    val subtitle: String,
    val helperText: String,
    val chips: List<String>,
    val accentColor: Color
)

fun buildRecommendationHeroState(
    pets: List<Pet>,
    logs: List<LogEntry>
): RecommendationHeroState {
    if (pets.isEmpty()) {
        return RecommendationHeroState(
            title = "Add your first pet",
            subtitle = "Start building your pet profile.",
            helperText = "Recommendations will appear here once a pet is added.",
            chips = listOf("No pets yet"),
            accentColor = Color(0xFF7C8CF8)
        )
    }

    val candidates: MutableList<PrioritizedRecommendation> = mutableListOf()

    val recentLogs: List<LogEntry> = logs.filter { entry ->
        entry.date >= LocalDate.now().minusDays(7)
    }

    val petById: Map<String, Pet> = pets.associateBy { it.id }

    for (pet in pets) {
        val petLogs: List<LogEntry> = recentLogs.filter { entry ->
            entry.petId == pet.id
        }

        val appetiteLogs: List<LogEntry> = petLogs.filter { entry ->
            entry.type == LogType.APPETITE
        }

        val stoolLogs: List<LogEntry> = petLogs.filter { entry ->
            entry.type == LogType.STOOL
        }

        val energyLogs: List<LogEntry> = petLogs.filter { entry ->
            entry.type == LogType.ENERGY
        }

        val lowAppetiteCount: Int = appetiteLogs.count { entry ->
            containsAny(
                text = entry.note,
                keywords = listOf("very poor", "poor", "low", "didn't eat", "didnt eat", "not eating", "hasn't eaten", "hasnt eaten")
            )
        }

        val stoolConcernCount: Int = stoolLogs.count { entry ->
            containsAny(
                text = entry.note,
                keywords = listOf("diarrhea", "loose", "bloody", "blood", "mucus", "watery")
            )
        }

        val energyConcernCount: Int = energyLogs.count { entry ->
            containsAny(
                text = entry.note,
                keywords = listOf("lethargic", "weak", "anxiety", "fear", "anger", "low energy", "not active")
            )
        }

        if (stoolConcernCount > 0) {
            candidates += PrioritizedRecommendation(
                priority = RecommendationPriority.STOOL_CONCERN,
                petName = pet.name,
                title = "${pet.name}'s stool needs attention",
                subtitle = "Recent logs show changes that may need closer monitoring.",
                helperText = "View a recommendation to help guide the next steps for ${pet.name}.",
                chips = listOf("Stool concern", "Recent logs"),
                accentColor = Color(0xFFEF6C57)
            )
        }

        if (lowAppetiteCount > 0) {
            candidates += PrioritizedRecommendation(
                priority = RecommendationPriority.APPETITE_CONCERN,
                petName = pet.name,
                title = "${pet.name} hasn't eaten much",
                subtitle = "Low appetite was recorded recently.",
                helperText = "View a recommendation to help support ${pet.name}'s feeding routine.",
                chips = listOf("Low appetite", "Recent logs"),
                accentColor = Color(0xFFF59E0B)
            )
        }

        if (energyConcernCount > 0) {
            candidates += PrioritizedRecommendation(
                priority = RecommendationPriority.ENERGY_CONCERN,
                petName = pet.name,
                title = "${pet.name} seems a bit off today",
                subtitle = "Recent logs show a change in energy or behavior.",
                helperText = "Check a recommendation for guidance based on ${pet.name}'s recent activity.",
                chips = listOf("Energy change", "Behavior"),
                accentColor = Color(0xFF8B5CF6)
            )
        }

        if (!pet.vaccinated) {
            candidates += PrioritizedRecommendation(
                priority = RecommendationPriority.VACCINE_DUE,
                petName = pet.name,
                title = "${pet.name} may need a vaccine follow-up",
                subtitle = "A vaccination update may be needed.",
                helperText = "Check guidance to help keep ${pet.name} protected.",
                chips = listOf("Vaccine follow-up"),
                accentColor = Color(0xFF3B82F6)
            )
        }

        if (petLogs.isEmpty()) {
            candidates += PrioritizedRecommendation(
                priority = RecommendationPriority.NO_LOGS,
                petName = pet.name,
                title = "No updates for ${pet.name} yet",
                subtitle = "Start logging daily activity.",
                helperText = "Add logs to unlock personalized recommendations for ${pet.name}.",
                chips = listOf("No recent logs"),
                accentColor = Color(0xFF64748B)
            )
        }
    }

    val multiPetVaccineDue: List<Pet> = pets.filter { pet -> !pet.vaccinated }
    if (multiPetVaccineDue.size >= 2) {
        candidates += PrioritizedRecommendation(
            priority = RecommendationPriority.VACCINE_DUE,
            petName = null,
            title = "${multiPetVaccineDue.size} pets may need vaccine follow-up",
            subtitle = "Some vaccination records may need updating.",
            helperText = "Review recommendations to help keep their preventive care on track.",
            chips = listOf("${multiPetVaccineDue.size} pets affected", "Preventive care"),
            accentColor = Color(0xFF3B82F6)
        )
    }

    val best: PrioritizedRecommendation? = candidates.maxByOrNull { recommendation ->
        recommendation.priority.score
    }

    if (best != null) {
        return RecommendationHeroState(
            title = best.title,
            subtitle = best.subtitle,
            helperText = best.helperText,
            chips = best.chips,
            accentColor = best.accentColor
        )
    }

    val petCountText: String = if (pets.size == 1) "1 pet" else "${pets.size} pets"

    return RecommendationHeroState(
        title = if (pets.size == 1) "${pets.first().name} is doing well today" else "Your pets are doing well today",
        subtitle = "Everything looks good based on recent records.",
        helperText = "View a recommendation to help maintain their daily care routine.",
        chips = listOf(petCountText, "Up to date"),
        accentColor = Color(0xFF10B981)
    )
}

private fun containsAny(
    text: String,
    keywords: List<String>
): Boolean {
    val normalizedText: String = text.lowercase()
    return keywords.any { keyword ->
        normalizedText.contains(keyword.lowercase())
    }
}

@Composable
fun AccurateAiRecommendationHeroCard(
    state: RecommendationHeroState,
    onOpenRecommendation: () -> Unit,
    onOpenRecords: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val brush = Brush.linearGradient(
        colors = listOf(
            state.accentColor.copy(alpha = 0.20f),
            colors.surface,
            colors.surface
        )
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = colors.surface,
        tonalElevation = 5.dp,
        shadowElevation = 5.dp,
        border = BorderStroke(
            1.dp,
            state.accentColor.copy(alpha = 0.24f)
        )
    ) {
        Column(
            modifier = Modifier
                .background(brush)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = state.accentColor.copy(alpha = 0.14f)
                ) {
                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Recommendation",
                            tint = state.accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FurSight AI",
                        style = MaterialTheme.typography.labelLarge,
                        color = state.accentColor,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = state.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = state.helperText,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.chips.forEach { chipText ->
                    RecommendationChip(
                        text = chipText,
                        accentColor = state.accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenRecommendation,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = state.accentColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Recommendation",
                        fontWeight = FontWeight.SemiBold
                    )
                }


            }
        }
    }
}

@Composable
fun SlimPetCareOverviewCard(
    petCount: Int,
    attentionNeeded: Int,
    dueVaccines: Int,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val successColor = Color(0xFF22C55E)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            colors.outlineVariant.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "Pet Care Overview",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SlimOverviewStat(
                    modifier = Modifier.weight(1f),
                    title = "Pets",
                    value = petCount.toString(),
                    accentColor = colors.primary
                )

                SlimOverviewStat(
                    modifier = Modifier.weight(1f),
                    title = "Attention",
                    value = attentionNeeded.toString(),
                    accentColor = if (attentionNeeded > 0) colors.tertiary else successColor
                )

                SlimOverviewStat(
                    modifier = Modifier.weight(1f),
                    title = "Vaccines",
                    value = dueVaccines.toString(),
                    accentColor = if (dueVaccines > 0) colors.tertiary else colors.primary
                )
            }
        }
    }
}

@Composable
private fun SlimOverviewStat(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accentColor.copy(alpha = 0.10f),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.16f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecommendationChip(
    text: String,
    accentColor: Color
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = accentColor.copy(alpha = 0.10f),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.18f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}