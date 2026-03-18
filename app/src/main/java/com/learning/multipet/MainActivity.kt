package com.learning.multipet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learning.multipet.ui.AppColors
import com.learning.multipet.ui.AppTheme
import com.learning.multipet.ui.screens.AiChatSheet
import com.learning.multipet.ui.screens.HomeScreen
import com.learning.multipet.ui.screens.LoginScreen
import com.learning.multipet.ui.screens.ManagePetsScreen
import com.learning.multipet.ui.screens.RecordsScreen
import com.learning.multipet.ui.screens.VetMapScreen
import com.learning.multipet.viewmodel.AppViewModel


enum class BottomTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Home("Home", Icons.Filled.Home),
    Records("Records", Icons.Filled.DateRange),
    Manage("Manage", Icons.Filled.Pets),

    Map("Map", Icons.Filled.LocationOn)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme { AppRoot() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    var isLoggedIn by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { isLoggedIn = true }
        )
        return
    }
    var tab by remember { mutableStateOf(BottomTab.Home) }
    var showChat by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = { AppTopBar() },
        bottomBar = {
            AppBottomBar(
                selected = tab,
                onSelect = { tab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChat = true },
                shape = CircleShape,
                containerColor = AppColors.Teal
            ) {
                Icon(Icons.Filled.SmartToy, contentDescription = "AI Care", tint = Color.White)
            }
        },
        containerColor = AppColors.ScreenBg
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                BottomTab.Home -> HomeScreen(vm = vm, onGoManage = { tab = BottomTab.Manage })
                BottomTab.Records -> RecordsScreen(vm = vm, onGoManage = { tab = BottomTab.Manage })
                BottomTab.Manage -> ManagePetsScreen(vm = vm)
                BottomTab.Map -> VetMapScreen()
            }
        }
    }

    if (showChat) {
        ModalBottomSheet(
            onDismissRequest = { showChat = false },
            sheetState = sheetState
        ) {
            AiChatSheet(vm = vm)
            Spacer(Modifier.height(12.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar() {
    TopAppBar(
        title = { Text("MultiPetCare") },
        actions = {
            IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, null) }
            IconButton(onClick = { }) { Icon(Icons.Default.Person, null) }
        }
    )
}

@Composable
private fun AppBottomBar(selected: BottomTab, onSelect: (BottomTab) -> Unit) {
    NavigationBar {
        BottomTab.entries.forEach { t ->
            NavigationBarItem(
                selected = t == selected,
                onClick = { onSelect(t) },
                icon = { Icon(t.icon, null) },
                label = { Text(t.label) }
            )
        }
    }
}
