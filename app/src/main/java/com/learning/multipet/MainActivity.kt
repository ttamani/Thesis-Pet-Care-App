package com.learning.multipet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learning.multipet.ui.AppTheme
import com.learning.multipet.ui.ThemePreference
import com.learning.multipet.ui.ThemeViewModel
import com.learning.multipet.ui.screens.AiChatScreen
import com.learning.multipet.ui.screens.CalendarScreen
import com.learning.multipet.ui.screens.HomeScreen
import com.learning.multipet.ui.screens.LoginScreen
import com.learning.multipet.ui.screens.ManagePetsScreen
import com.learning.multipet.ui.screens.PetViewMode
import com.learning.multipet.ui.screens.VetMapScreen
import com.learning.multipet.viewmodel.AppViewModel
import com.learning.multipet.viewmodel.SessionViewModel
import com.learning.multipet.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay

enum class BottomTab(
    val label: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String
) {
    Home(
        label = "Home",
        icon = Icons.Filled.Home,
        title = "Home",
        subtitle = "Pet care at a glance"
    ),
    Calendar(
        label = "Calendar",
        icon = Icons.Filled.DateRange,
        title = "Calendar",
        subtitle = "Logs and care history"
    ),
    Pets(
        label = "Pets",
        icon = Icons.Filled.Pets,
        title = "Pets",
        subtitle = "Manage pet profiles"
    ),
    Vets(
        label = "Vets",
        icon = Icons.Filled.LocationOn,
        title = "Vets",
        subtitle = "Nearby clinics"
    )
}

private enum class DashboardRoute {
    Main,
    Profile,
    Ai
}

private enum class LaunchState {
    LOGIN,
    LOADING,
    DASHBOARD
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot(
    vm: AppViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)),
    sessionVm: SessionViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application)),
    themeVm: ThemeViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isLoggedIn = sessionVm.isLoggedIn.collectAsState().value
    val themePreference = themeVm.themePreference.collectAsState().value
    val authError = sessionVm.authError.collectAsState().value
    val isLoading = sessionVm.isLoading.collectAsState().value

    // Show Toast for authentication errors
    androidx.compose.runtime.LaunchedEffect(authError) {
        authError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            sessionVm.clearError()
        }
    }

    var launchState by rememberSaveable {
        mutableStateOf(
            if (isLoggedIn) LaunchState.DASHBOARD else LaunchState.LOGIN
        )
    }

    AppTheme(themePreference = themePreference) {
        when (launchState) {
            LaunchState.LOGIN -> {
                LoginScreen(
                    onLogin = { email, password, _ ->
                        if (!isLoading) {
                            sessionVm.signIn(email, password) {
                                launchState = LaunchState.LOADING
                            }
                        }
                    },
                    onRegister = { email, password ->
                        if (!isLoading) {
                            sessionVm.signUp(email, password) {
                                Toast.makeText(context, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onVerifyOtp = { _, _ ->
                        // OTP verification is not implemented - simple email/password only
                        launchState = LaunchState.LOADING
                    },
                    onGoogleSignIn = { },
                    onAuthSuccess = {
                        // Not used - authentication success handled via signIn/signUp callbacks
                    }
                )
            }

            LaunchState.LOADING -> {
                LoadingTransitionScreen(
                    onFinished = {
                        launchState = LaunchState.DASHBOARD
                    }
                )
            }

            LaunchState.DASHBOARD -> {
                DashboardScaffold(
                    vm = vm,
                    sessionVm = sessionVm,
                    themeVm = themeVm,
                    onRequireLogin = {
                        sessionVm.logout()
                        launchState = LaunchState.LOGIN
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingTransitionScreen(
    onFinished: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colors.background,
            colors.surfaceContainerLowest,
            colors.background
        )
    )

    LaunchedEffect(Unit) {
        delay(1100)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = colors.primary.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Preparing your dashboard",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Loading pets, reminders, and activity",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScaffold(
    vm: AppViewModel,
    sessionVm: SessionViewModel,
    themeVm: ThemeViewModel,
    onRequireLogin: () -> Unit
) {
    val state by vm.state.collectAsState()
    val currentTheme = themeVm.themePreference.collectAsState().value
    val colors = MaterialTheme.colorScheme

    var homePetViewMode by rememberSaveable { mutableStateOf(PetViewMode.LIST) }
    var tab by rememberSaveable { mutableStateOf(BottomTab.Home) }
    var route by rememberSaveable { mutableStateOf(DashboardRoute.Main) }
    var showNotifications by remember { mutableStateOf(false) }

    var profileName by rememberSaveable { mutableStateOf("Pet Owner") }
    var accountEmail by rememberSaveable { mutableStateOf("signedin@example.com") }

    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var remindersEnabled by rememberSaveable { mutableStateOf(true) }
    var aiSuggestionsEnabled by rememberSaveable { mutableStateOf(true) }

    val vaccinesDue = state.pets.count { !it.vaccinated }
    val attentionCount = vaccinesDue

    when (route) {
        DashboardRoute.Profile -> {
            ProfileScreen(
                currentName = profileName,
                currentEmail = accountEmail,
                currentTheme = currentTheme,
                notificationsEnabled = notificationsEnabled,
                remindersEnabled = remindersEnabled,
                aiSuggestionsEnabled = aiSuggestionsEnabled,
                onBack = { route = DashboardRoute.Main },
                onSaveName = { profileName = it },
                onThemeChange = { themeVm.setThemePreference(it) },
                onNotificationsChange = { notificationsEnabled = it },
                onRemindersChange = { remindersEnabled = it },
                onAiSuggestionsChange = { aiSuggestionsEnabled = it },
                onSignOut = {
                    route = DashboardRoute.Main
                    onRequireLogin()
                }
            )
        }

        DashboardRoute.Ai -> {
            AiChatScreen(
                vm = vm,
                onBack = { route = DashboardRoute.Main }
            )
        }

        DashboardRoute.Main -> {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background),
                containerColor = colors.background,
                topBar = {
                    AppTopBar(
                        title = tab.title,
                        subtitle = tab.subtitle,
                        notificationCount = attentionCount,
                        showNotifications = showNotifications,
                        attentionCount = attentionCount,
                        vaccinesDue = vaccinesDue,
                        onNotificationsClick = { showNotifications = !showNotifications },
                        onDismissNotifications = { showNotifications = false },
                        onProfileClick = {
                            showNotifications = false
                            route = DashboardRoute.Profile
                        }
                    )
                },
                bottomBar = {
                    AppBottomBar(
                        selected = tab,
                        onSelect = { tab = it }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    when (tab) {
                        BottomTab.Home -> HomeScreen(
                            vm = vm,
                            onGoManage = { tab = BottomTab.Pets },
                            onOpenAi = {
                                showNotifications = false
                                route = DashboardRoute.Ai
                            },
                            onFindVet = { tab = BottomTab.Vets },
                            onOpenCalendar = { tab = BottomTab.Calendar },
                            viewMode = homePetViewMode,
                            onViewModeChange = { homePetViewMode = it }
                        )

                        BottomTab.Calendar -> CalendarScreen(
                            vm = vm,
                            onGoManage = { tab = BottomTab.Pets }
                        )

                        BottomTab.Pets -> ManagePetsScreen(vm = vm)

                        BottomTab.Vets -> VetMapScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTopBar(
    title: String,
    subtitle: String,
    notificationCount: Int,
    showNotifications: Boolean,
    attentionCount: Int,
    vaccinesDue: Int,
    onNotificationsClick: () -> Unit,
    onDismissNotifications: () -> Unit,
    onProfileClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.background.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = colors.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box {
                        TopBarIconButton(
                            icon = Icons.Filled.NotificationsNone,
                            contentDescription = "Notifications",
                            badgeCount = notificationCount,
                            onClick = onNotificationsClick
                        )

                        NotificationPopup(
                            expanded = showNotifications,
                            attentionCount = attentionCount,
                            vaccinesDue = vaccinesDue,
                            onDismiss = onDismissNotifications
                        )
                    }

                    TopBarIconButton(
                        icon = Icons.Filled.Person,
                        contentDescription = "Profile",
                        onClick = onProfileClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "top_bar_icon_scale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = colors.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            if (badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = colors.tertiary,
                            contentColor = colors.onTertiary
                        ) {
                            Text(
                                text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = colors.onSurface
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun NotificationPopup(
    expanded: Boolean,
    attentionCount: Int,
    vaccinesDue: Int,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(18.dp),
        containerColor = colors.surface,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .padding(14.dp)
        ) {
            Text(
                text = "Notifications",
                color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Reminders and updates for your pets.",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (attentionCount == 0) {
                Text(
                    text = "No new notifications",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You’re all caught up for now.",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                NotificationPopupItem(
                    title = "Care attention needed",
                    message = "$attentionCount pet(s) currently need attention.",
                    statusText = "Pending",
                    statusColor = colors.tertiary,
                    statusBackground = colors.tertiary.copy(alpha = 0.14f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                NotificationPopupItem(
                    title = "Vaccination reminder",
                    message = "$vaccinesDue pet(s) may need a vaccine update.",
                    statusText = "Pending",
                    statusColor = colors.tertiary,
                    statusBackground = colors.tertiary.copy(alpha = 0.14f)
                )
            }
        }
    }
}

@Composable
private fun NotificationPopupItem(
    title: String,
    message: String,
    statusText: String,
    statusColor: Color,
    statusBackground: Color
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.40f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = statusBackground
                ) {
                    Box(modifier = Modifier.size(10.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    color = colors.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AppBottomBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        color = colors.surface.copy(alpha = 0.98f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.40f))
    ) {
        NavigationBar(
            modifier = Modifier.navigationBarsPadding(),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            BottomTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = tab == selected,
                    onClick = { onSelect(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.primary,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.onSurfaceVariant,
                        unselectedTextColor = colors.onSurfaceVariant,
                        indicatorColor = colors.primary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    currentName: String,
    currentEmail: String,
    currentTheme: ThemePreference,
    notificationsEnabled: Boolean,
    remindersEnabled: Boolean,
    aiSuggestionsEnabled: Boolean,
    onBack: () -> Unit,
    onSaveName: (String) -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onRemindersChange: (Boolean) -> Unit,
    onAiSuggestionsChange: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    var editedName by remember(currentName) { mutableStateOf(currentName) }
    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = colors.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Profile Settings",
                        color = colors.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Manage your account and preferences",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier.size(76.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = colors.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = editedName.ifBlank { "Pet Owner" },
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentEmail,
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Account",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Display Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = currentEmail,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Signed-in Account") },
                        enabled = false,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Appearance",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Choose how the app looks",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = currentTheme == ThemePreference.LIGHT,
                            onClick = { onThemeChange(ThemePreference.LIGHT) },
                            shape = SegmentedButtonDefaults.itemShape(0, 3)
                        ) {
                            Text("Light")
                        }

                        SegmentedButton(
                            selected = currentTheme == ThemePreference.DARK,
                            onClick = { onThemeChange(ThemePreference.DARK) },
                            shape = SegmentedButtonDefaults.itemShape(1, 3)
                        ) {
                            Text("Dark")
                        }

                        SegmentedButton(
                            selected = currentTheme == ThemePreference.SYSTEM,
                            onClick = { onThemeChange(ThemePreference.SYSTEM) },
                            shape = SegmentedButtonDefaults.itemShape(2, 3)
                        ) {
                            Text("System")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = when (currentTheme) {
                            ThemePreference.LIGHT -> "Light mode is currently active."
                            ThemePreference.DARK -> "Dark mode is currently active."
                            ThemePreference.SYSTEM -> "The app follows your device appearance."
                        },
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Preferences",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Control how the app behaves",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PreferenceToggleRow(
                        title = "Notifications",
                        subtitle = "Receive reminders and updates",
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsChange
                    )

                    PreferenceDivider()

                    PreferenceToggleRow(
                        title = "Reminder Alerts",
                        subtitle = "Show pet-care and vaccine reminders",
                        checked = remindersEnabled,
                        onCheckedChange = onRemindersChange
                    )

                    PreferenceDivider()

                    PreferenceToggleRow(
                        title = "AI Quick Suggestions",
                        subtitle = "Show suggested prompts in AI chat",
                        checked = aiSuggestionsEnabled,
                        onCheckedChange = onAiSuggestionsChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Actions",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onSaveName(editedName.trim().ifBlank { currentName }) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save Changes")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = colors.outlineVariant)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f))
                    ) {
                        Text("Sign Out")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = colors.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.onSurfaceVariant,
                uncheckedTrackColor = colors.surfaceVariant,
                uncheckedBorderColor = colors.outlineVariant
            )
        )
    }
}

@Composable
private fun PreferenceDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant
    )
}