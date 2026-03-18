package com.learning.multipet.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionScreen(
    onPermissionsGranted: () -> Unit
) {

    var locationGranted by remember { mutableStateOf(false) }
    var photosGranted by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationGranted = granted
    }

    val photosPermission = if (Build.VERSION.SDK_INT >= 33)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    val photosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        photosGranted = granted
    }

    LaunchedEffect(locationGranted, photosGranted) {
        if (locationGranted && photosGranted) {
            onPermissionsGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Permissions Required", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, null)
            Spacer(Modifier.width(8.dp))
            Text("Allow Location Access")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { photosLauncher.launch(photosPermission) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhotoLibrary, null)
            Spacer(Modifier.width(8.dp))
            Text("Allow Photo Access")
        }
    }
}