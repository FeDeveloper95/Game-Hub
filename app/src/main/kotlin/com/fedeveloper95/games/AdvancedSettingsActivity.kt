package com.fedeveloper95.games

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.fedeveloper95.games.elements.AdvancedSettingsActivity.RestartPopup
import com.fedeveloper95.games.elements.AdvancedSettingsActivity.exportSettings
import com.fedeveloper95.games.elements.AdvancedSettingsActivity.importSettings
import com.fedeveloper95.games.elements.MainActivity.CommunityBottomSheet
import com.fedeveloper95.games.elements.UI.ControllerBottomSheet
import com.fedeveloper95.games.elements.UI.ExpressiveIconButton
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AdvancedSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdvancedSettingsScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 600
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }
    val haptic = LocalHapticFeedback.current

    var autoUpdates by remember { mutableStateOf(prefs.getBoolean("pref_auto_updates", true)) }

    var showRestartDialog by remember { mutableStateOf(false) }
    var showCommunitySheet by remember { mutableStateOf(false) }
    var showResetPopup by remember { mutableStateOf(false) }

    val hasOverlay = Settings.canDrawOverlays(context)
    val hasBt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else true

    val testControllerFeatures = prefs.getBoolean("test_controller_features", hasOverlay && hasBt)
    var showControllerSheet by remember { mutableStateOf(false) }
    var selectedControllerName by remember { mutableStateOf(context.getString(R.string.controller_name_xbox)) }
    var showControllerMenu by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                exportSettings(context, it)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                if (importSettings(context, it)) {
                    showRestartDialog = true
                }
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_advanced_title),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        ExpressiveIconButton(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .then(if (isExpandedScreen) Modifier.padding(horizontal = 64.dp) else Modifier)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = padding.calculateBottomPadding() + 48.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Settings,
                    title = stringResource(R.string.settings_auto_updates_title),
                    subtitle = stringResource(R.string.settings_auto_updates_desc),
                    containerColor = Color(0xFFfcbd00),
                    iconColor = Color(0xFF6d3a01),
                    index = 0,
                    count = 2,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        autoUpdates = !autoUpdates
                        prefs.edit().putBoolean("pref_auto_updates", autoUpdates).apply()
                    },
                    trailingContent = {
                        Switch(
                            checked = autoUpdates,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                autoUpdates = it
                                prefs.edit().putBoolean("pref_auto_updates", it).apply()
                            },
                            thumbContent = {
                                if (autoUpdates) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize))
                                } else {
                                    Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize))
                                }
                            }
                        )
                    }
                )

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Flag,
                    title = stringResource(R.string.settings_setup_title),
                    subtitle = stringResource(R.string.settings_setup_desc),
                    containerColor = Color(0xFFffaee4),
                    iconColor = Color(0xFF8d0053),
                    index = 1,
                    count = 2,
                    onClick = {
                        val intent = Intent(context, WelcomeActivity::class.java).apply {
                            putExtra("FORCE_SHOW", true)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_backup_header),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.CloudUpload,
                    title = stringResource(R.string.settings_export_title),
                    subtitle = stringResource(R.string.settings_export_desc),
                    containerColor = Color(0xFF80da88),
                    iconColor = Color(0xFF00522c),
                    index = 0,
                    count = 2,
                    onClick = {
                        val timestamp = Calendar.getInstance().timeInMillis
                        exportLauncher.launch("gamehub_backup_$timestamp.json")
                    }
                )

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.CloudDownload,
                    title = stringResource(R.string.settings_import_title),
                    subtitle = stringResource(R.string.settings_import_desc),
                    containerColor = Color(0xFF67d4ff),
                    iconColor = Color(0xFF004e5d),
                    index = 1,
                    count = 2,
                    onClick = {
                        importLauncher.launch(arrayOf("application/json"))
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_testing_header),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val testCount = 2 + (if (testControllerFeatures) 1 else 0)
                var testIndex = 0

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Group,
                    title = stringResource(R.string.settings_test_telegram_title),
                    subtitle = stringResource(R.string.settings_test_telegram_desc),
                    containerColor = Color(0xFF97cbff),
                    iconColor = Color(0xFF003355),
                    index = minOf(testIndex++, testCount - 1),
                    count = testCount,
                    onClick = { showCommunitySheet = true }
                )

                if (testControllerFeatures) {
                    GameSettingsSegmentedItem(
                        icon = Icons.Rounded.Gamepad,
                        title = stringResource(R.string.settings_test_controller_simulate_title),
                        subtitle = stringResource(R.string.settings_test_controller_simulate_desc),
                        containerColor = Color(0xFFFCBD00),
                        iconColor = Color(0xFF6D3A01),
                        index = minOf(testIndex++, testCount - 1),
                        count = testCount,
                        onClick = { showControllerSheet = true },
                        trailingContent = {
                            Box {
                                IconButton(
                                    onClick = { showControllerMenu = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = showControllerMenu,
                                    onDismissRequest = { showControllerMenu = false },
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(R.string.controller_xbox), fontFamily = GoogleSansFlex) },
                                        onClick = {
                                            selectedControllerName = context.getString(R.string.controller_name_xbox)
                                            showControllerMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(R.string.controller_playstation), fontFamily = GoogleSansFlex) },
                                        onClick = {
                                            selectedControllerName = context.getString(R.string.controller_name_playstation)
                                            showControllerMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(R.string.controller_joycon), fontFamily = GoogleSansFlex) },
                                        onClick = {
                                            selectedControllerName = context.getString(R.string.controller_name_joycon)
                                            showControllerMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(R.string.controller_generic), fontFamily = GoogleSansFlex) },
                                        onClick = {
                                            selectedControllerName = context.getString(R.string.controller_name_generic)
                                            showControllerMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    )
                }

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.BugReport,
                    title = stringResource(R.string.settings_test_crash_title),
                    subtitle = stringResource(R.string.settings_test_crash_desc),
                    containerColor = Color(0xFFffb869),
                    iconColor = Color(0xFF5c3000),
                    index = minOf(testIndex++, testCount - 1),
                    count = testCount,
                    onClick = { throw RuntimeException("Test Crash Triggered") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_danger_zone_header),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.DeleteForever,
                    title = stringResource(R.string.settings_reset_title),
                    subtitle = stringResource(R.string.settings_reset_desc),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    iconColor = MaterialTheme.colorScheme.onErrorContainer,
                    index = 0,
                    count = 1,
                    onClick = { showResetPopup = true }
                )
            }
        }
    }

    if (showCommunitySheet) {
        CommunityBottomSheet(
            onDismiss = { showCommunitySheet = false }
        )
    }

    if (showControllerSheet) {
        ControllerBottomSheet(
            controllerName = selectedControllerName,
            onDismiss = { showControllerSheet = false },
            onSettingsClick = { showControllerSheet = false }
        )
    }

    if (showResetPopup) {
        ResetPopup(
            onDismiss = { showResetPopup = false },
            onConfirm = {
                showResetPopup = false
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityManager.clearApplicationUserData()
            }
        )
    }

    if (showRestartDialog) {
        RestartPopup(onDismiss = { showRestartDialog = false })
    }
}

@Composable
fun ResetPopup(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 20 else 50,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btnMorph"
    )

    val textInteractionSource = remember { MutableInteractionSource() }
    val textIsPressed by textInteractionSource.collectIsPressedAsState()
    val textCornerPercent by animateIntAsState(
        targetValue = if (textIsPressed) 20 else 50,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "txtBtnMorph"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_reset_title),
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.settings_reset_confirmation_desc),
                fontFamily = GoogleSansFlex
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(cornerPercent),
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(
                    text = stringResource(R.string.settings_reset_action),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(textCornerPercent),
                interactionSource = textInteractionSource,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 6.dp
    )
}