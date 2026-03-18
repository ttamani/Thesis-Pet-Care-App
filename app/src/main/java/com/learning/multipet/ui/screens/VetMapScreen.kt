package com.learning.multipet.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private data class VetClinicUi(
    val id: String,
    val name: String,
    val address: String,
    val distanceLabel: String,
    val position: LatLng
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetMapScreen() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val fallbackLocation = remember { LatLng(14.5995, 120.9842) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var selectedClinic by remember { mutableStateOf<VetClinicUi?>(null) }

    val nearbyClinics = remember {
        listOf(
            VetClinicUi(
                id = "1",
                name = "Paws & Care Veterinary Clinic",
                address = "Quiapo, Manila",
                distanceLabel = "0.9 km away",
                position = LatLng(14.6020, 120.9878)
            ),
            VetClinicUi(
                id = "2",
                name = "Metro Pet Animal Care",
                address = "Sampaloc, Manila",
                distanceLabel = "1.7 km away",
                position = LatLng(14.6107, 120.9891)
            ),
            VetClinicUi(
                id = "3",
                name = "City Pet Wellness Center",
                address = "Ermita, Manila",
                distanceLabel = "2.4 km away",
                position = LatLng(14.5888, 120.9812)
            )
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallbackLocation, 13f)
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun moveCamera(target: LatLng, zoom: Float) {
        scope.launch {
            runCatching {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(target, zoom),
                    durationMs = 900
                )
            }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            isLoadingLocation = false
            userLocation = null
            return@LaunchedEffect
        }

        isLoadingLocation = true

        runCatching {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    userLocation = location?.let { LatLng(it.latitude, it.longitude) }
                    isLoadingLocation = false
                }
                .addOnFailureListener {
                    isLoadingLocation = false
                }
        }.onFailure {
            isLoadingLocation = false
        }
    }

    LaunchedEffect(userLocation, hasLocationPermission) {
        if (hasLocationPermission) {
            moveCamera(userLocation ?: fallbackLocation, 13.5f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        when {
            !hasLocationPermission -> {
                PermissionScreen(
                    requireLocationPermission = true,
                    requirePhotoPermission = false,
                    onPermissionsGranted = {
                        hasLocationPermission = true
                    }
                )
            }

            isLoadingLocation -> {
                VetMapLoadingState()
            }

            else -> {
                VetMapContent(
                    nearbyClinics = nearbyClinics,
                    userLocation = userLocation ?: fallbackLocation,
                    selectedClinic = selectedClinic,
                    cameraPositionState = cameraPositionState,
                    hasLocationPermission = hasLocationPermission,
                    onClinicSelected = { clinic ->
                        selectedClinic = clinic
                        moveCamera(clinic.position, 15f)
                    },
                    onRecenter = {
                        selectedClinic = null
                        moveCamera(userLocation ?: fallbackLocation, 13.5f)
                    }
                )
            }
        }

        if (selectedClinic != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedClinic = null },
                sheetState = bottomSheetState,
                containerColor = colors.surface
            ) {
                VetClinicDetailSheet(
                    clinic = selectedClinic!!,
                    onClose = { selectedClinic = null }
                )
            }
        }
    }
}

@Composable
private fun VetMapContent(
    nearbyClinics: List<VetClinicUi>,
    userLocation: LatLng,
    selectedClinic: VetClinicUi?,
    cameraPositionState: CameraPositionState,
    hasLocationPermission: Boolean,
    onClinicSelected: (VetClinicUi) -> Unit,
    onRecenter: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        SafeVetMap(
            userLocation = userLocation,
            nearbyClinics = nearbyClinics,
            cameraPositionState = cameraPositionState,
            hasLocationPermission = hasLocationPermission,
            onClinicSelected = onClinicSelected
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            VetMapTopCard(clinicCount = nearbyClinics.size)
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 96.dp),
            shape = CircleShape,
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outline),
            shadowElevation = 8.dp
        ) {
            FilledTonalIconButton(
                onClick = onRecenter,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter map"
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outline),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nearby Vet Clinics",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap a clinic to inspect it on the map",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    TextButton(onClick = onRecenter) {
                        Text("Near me")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = nearbyClinics,
                        key = { clinic -> clinic.id }
                    ) { clinic ->
                        VetClinicCard(
                            clinic = clinic,
                            selected = selectedClinic?.id == clinic.id,
                            onClick = {
                                onClinicSelected(clinic)
                            }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun SafeVetMap(
    userLocation: LatLng,
    nearbyClinics: List<VetClinicUi>,
    cameraPositionState: CameraPositionState,
    hasLocationPermission: Boolean,
    onClinicSelected: (VetClinicUi) -> Unit
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission
        )
    ) {
        Marker(
            state = MarkerState(position = userLocation),
            title = "You are here"
        )

        nearbyClinics.forEach { clinic ->
            Marker(
                state = MarkerState(position = clinic.position),
                title = clinic.name,
                snippet = clinic.address,
                onClick = {
                    onClinicSelected(clinic)
                    true
                }
            )
        }
    }
}

@Composable
private fun VetMapTopCard(clinicCount: Int) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationSearching,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Find Vet Clinics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$clinicCount nearby clinics around your area",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VetMapLoadingState() {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = colors.primary,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Finding your location...",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VetClinicCard(
    clinic: VetClinicUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) colors.primary.copy(alpha = 0.10f) else colors.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary else colors.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.14f)
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
                    text = clinic.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = clinic.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            Text(
                text = clinic.distanceLabel,
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VetClinicDetailSheet(
    clinic: VetClinicUi,
    onClose: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = clinic.name,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = clinic.address,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.primary.copy(alpha = 0.12f)
        ) {
            Text(
                text = clinic.distanceLabel,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = colors.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        HorizontalDivider(color = colors.outline)

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Close")
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}