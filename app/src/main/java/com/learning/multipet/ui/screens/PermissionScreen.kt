package com.learning.multipet.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun PermissionScreen(
    requireLocationPermission: Boolean = true,
    requirePhotoPermission: Boolean = false,
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    val photoPermission: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var locationGranted by remember {
        mutableStateOf(
            !requireLocationPermission || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var photoGranted by remember {
        mutableStateOf(
            !requirePhotoPermission || ContextCompat.checkSelfPermission(
                context,
                photoPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        locationGranted = granted
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        photoGranted = granted
    }

    LaunchedEffect(locationGranted, photoGranted) {
        val allGranted = (!requireLocationPermission || locationGranted) &&
                (!requirePhotoPermission || photoGranted)

        if (allGranted) {
            onPermissionsGranted()
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = if (requirePhotoPermission) Icons.Default.PhotoLibrary else Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Permissions Required",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    requireLocationPermission && requirePhotoPermission ->
                        "Allow location and photo access so the app can work properly."
                    requireLocationPermission ->
                        "Allow location access so the app can show nearby veterinary clinics."
                    requirePhotoPermission ->
                        "Allow photo access so the app can select and display images."
                    else ->
                        "No permission required."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (requireLocationPermission) {
                Button(
                    onClick = {
                        locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !locationGranted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (locationGranted) "Location Allowed" else "Allow Location Access"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (requirePhotoPermission) {
                Button(
                    onClick = {
                        photoLauncher.launch(photoPermission)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !photoGranted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (photoGranted) "Photos Allowed" else "Allow Photo Access"
                    )
                }
            }
        }
    }
}