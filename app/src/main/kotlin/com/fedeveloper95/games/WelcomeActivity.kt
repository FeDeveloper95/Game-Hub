package com.fedeveloper95.games

import android.Manifest
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SwipeRight
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import kotlinx.coroutines.launch

data class OnboardingPageInfo(
    val content: @Composable (onUpdateScrollState: (Boolean) -> Unit) -> Unit
)

fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
    val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    val expectedComponentName = ComponentName(context, service).flattenToString()
    return enabledServices?.contains(expectedComponentName) == true
}

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("game_hub_settings", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("is_first_run", true)
        val forceShow = intent.getBooleanExtra("FORCE_SHOW", false)
        if (!isFirstRun && !forceShow) {
            finishOnboarding()
            return
        }
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WelcomePagerScreen(onFinished = {
                        prefs.edit().putBoolean("is_first_run", false).apply()
                        if (forceShow) {
                            finish()
                        } else {
                            finishOnboarding()
                        }
                    })
                }
            }
        }
    }

    private fun finishOnboarding() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomePagerScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }
    val commonAnimSpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

    val customWelcomeFontFamily = FontFamily(
        Font(
            resId = R.font.sans_flex,
            variationSettings = FontVariation.Settings(
                FontVariation.slant(-9f),
                FontVariation.width(111f),
                FontVariation.weight(333),
                FontVariation.Setting("GRAD", 100f),
                FontVariation.Setting("ROND", 100f)
            )
        )
    )

    val thinHeaderStyle = TextStyle(
        fontFamily = customWelcomeFontFamily,
        fontSize = 48.sp
    )

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var canInstallPackages by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else true
        )
    }
    var hasUsageStatsPermission by remember {
        mutableStateOf(checkUsageStatsPermission(context))
    }
    var hasBluetoothPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }
    var hasDndPermission by remember {
        mutableStateOf((context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).isNotificationPolicyAccessGranted)
    }
    var hasWriteSettingsPermission by remember {
        mutableStateOf(Settings.System.canWrite(context))
    }
    var hasAccessibilityPermission by remember {
        mutableStateOf(isAccessibilityServiceEnabled(context, com.fedeveloper95.games.services.GameBubbleAccessibilityService::class.java))
    }

    var isLastPageScrolledToEnd by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    canInstallPackages = context.packageManager.canRequestPackageInstalls()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    hasBluetoothPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                }
                hasUsageStatsPermission = checkUsageStatsPermission(context)

                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasDndPermission = (context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).isNotificationPolicyAccessGranted
                hasWriteSettingsPermission = Settings.System.canWrite(context)
                hasAccessibilityPermission = isAccessibilityServiceEnabled(context, com.fedeveloper95.games.services.GameBubbleAccessibilityService::class.java)

                prefs.edit().apply {
                    putBoolean("pref_bubble_tool_dnd", hasDndPermission)
                    putBoolean("pref_bubble_tool_brightness", hasWriteSettingsPermission)
                    putBoolean("pref_bubble_tool_screenshot", hasAccessibilityPermission)
                }.apply()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    val bluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasBluetoothPermission = isGranted }
    )

    val installParamsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canInstallPackages = context.packageManager.canRequestPackageInstalls()
        }
    }

    val pages = listOf(
        OnboardingPageInfo(
            content = { _ ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = stringResource(R.string.welcome_to),
                        style = thinHeaderStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 48.sp,
                        modifier = Modifier.offset(y = (-12).dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RotatingShapeContainer(
                            modifier = Modifier.size(280.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(R.string.welcome_preparing_subtitle),
                            fontFamily = GoogleSansFlex,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(1.2f))
                }
            }
        ),
        OnboardingPageInfo(
            content = { _ ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = stringResource(R.string.perm_required),
                        style = thinHeaderStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.perm_permissions),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 48.sp,
                        modifier = Modifier.offset(y = (-12).dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.perm_intro_text),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = GoogleSansFlex
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        val permCount = 4
                        var permIndex = 0

                        WelcomeSegmentedItem(
                            icon = Icons.Rounded.Notifications,
                            iconColor = Color(0xFFffaee4),
                            iconTint = Color(0xFF8d0053),
                            title = stringResource(R.string.perm_notif_title),
                            description = stringResource(R.string.perm_notif_desc),
                            index = permIndex++,
                            count = permCount,
                            control = {
                                Switch(
                                    checked = hasNotificationPermission,
                                    onCheckedChange = {
                                        if (hasNotificationPermission) {
                                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            }
                                            context.startActivity(intent)
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                                }
                                                context.startActivity(intent)
                                            }
                                        }
                                    },
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (hasNotificationPermission) Icons.Rounded.Check else Icons.Rounded.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            },
                            onClick = {
                                if (hasNotificationPermission) {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        )

                        WelcomeSegmentedItem(
                            icon = Icons.Rounded.Timer,
                            iconColor = Color(0xFFd8b9fc),
                            iconTint = Color(0xFF5629a4),
                            title = stringResource(R.string.feat_history_title),
                            description = stringResource(R.string.perm_usage_desc),
                            index = permIndex++,
                            count = permCount,
                            control = {
                                Switch(
                                    checked = hasUsageStatsPermission,
                                    onCheckedChange = {
                                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                    },
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (hasUsageStatsPermission) Icons.Rounded.Check else Icons.Rounded.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            },
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }
                        )

                        WelcomeSegmentedItem(
                            icon = R.drawable.ic_phone_update,
                            iconColor = Color(0xFFffb683),
                            iconTint = Color(0xFF753403),
                            title = stringResource(R.string.perm_install_title),
                            description = stringResource(R.string.perm_install_desc),
                            index = permIndex++,
                            count = permCount,
                            control = {
                                Switch(
                                    checked = canInstallPackages,
                                    onCheckedChange = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            installParamsLauncher.launch(intent)
                                        }
                                    },
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (canInstallPackages) Icons.Rounded.Check else Icons.Rounded.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            },
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    installParamsLauncher.launch(intent)
                                }
                            }
                        )

                        WelcomeSegmentedItem(
                            icon = Icons.Rounded.Gamepad,
                            iconColor = Color(0xFFcba6ff),
                            iconTint = Color(0xFF320073),
                            title = stringResource(R.string.perm_bluetooth_title),
                            description = stringResource(R.string.perm_bluetooth_desc),
                            index = permIndex++,
                            count = permCount,
                            control = {
                                Switch(
                                    checked = hasBluetoothPermission,
                                    onCheckedChange = {
                                        if (hasBluetoothPermission) {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(intent)
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                bluetoothLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                            }
                                        }
                                    },
                                    thumbContent = {
                                        Icon(
                                            imageVector = if (hasBluetoothPermission) Icons.Rounded.Check else Icons.Rounded.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            },
                            onClick = {
                                if (hasBluetoothPermission) {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        bluetoothLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        ),
        OnboardingPageInfo(
            content = { _ ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = stringResource(R.string.bubble_perm_title),
                        style = thinHeaderStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.bubble_perm_subtitle),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 48.sp,
                        modifier = Modifier.offset(y = (-12).dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.bubble_perm_intro),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = GoogleSansFlex
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        var isBubbleEnabled by remember { mutableStateOf(prefs.getBoolean("pref_bubble_enabled", true)) }

                        val haptic = LocalHapticFeedback.current
                        val interactionSource = remember { MutableInteractionSource() }
                        val shape = RoundedCornerShape(64.dp)

                        val handleBubbleToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isBubbleEnabled = !isBubbleEnabled
                            prefs.edit().putBoolean("pref_bubble_enabled", isBubbleEnabled).apply()
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current
                                ) { handleBubbleToggle() },
                            shape = shape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            ListItem(
                                modifier = Modifier.padding(vertical = 4.dp),
                                headlineContent = {
                                    Text(
                                        text = stringResource(R.string.settings_bubble_enable_title),
                                        fontFamily = GoogleSansFlex,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                trailingContent = {
                                    Switch(
                                        checked = isBubbleEnabled,
                                        onCheckedChange = { handleBubbleToggle() },
                                        thumbContent = {
                                            Icon(
                                                imageVector = if (isBubbleEnabled) Icons.Rounded.Check else Icons.Rounded.Close,
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize)
                                            )
                                        }
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val bubbleCount = 4
                        var bubbleIndex = 0

                        WelcomeSegmentedItem(
                            icon = Icons.Rounded.Layers,
                            iconColor = Color(0xFF67d4ff),
                            iconTint = Color(0xFF004e5d),
                            title = stringResource(R.string.perm_overlay_title),
                            description = stringResource(R.string.perm_overlay_desc),
                            index = bubbleIndex++,
                            count = bubbleCount,
                            enabled = isBubbleEnabled,
                            control = {
                                Switch(
                                    checked = hasOverlayPermission,
                                    enabled = isBubbleEnabled,
                                    onCheckedChange = {
                                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                                    },
                                    thumbContent = { Icon(if (hasOverlayPermission) Icons.Rounded.Check else Icons.Rounded.Close, null, modifier = Modifier.size(16.dp)) }
                                )
                            },
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                            }
                        )

                        WelcomeSegmentedItem(
                            icon = Icons.Rounded.DoNotDisturbOn,
                            iconColor = Color(0xFFffb3ae),
                            iconTint = Color(0xFF8a1a16),
                            title = stringResource(R.string.perm_dnd_title),
                            description = stringResource(R.string.perm_dnd_desc),
                            index = bubbleIndex++,
                            count = bubbleCount,
                            enabled = isBubbleEnabled,
                            control = {
                                Switch(
                                    checked = hasDndPermission,
                                    enabled = isBubbleEnabled,
                                    onCheckedChange = {
                                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                                    },
                                    thumbContent = { Icon(if (hasDndPermission) Icons.Rounded.Check else Icons.Rounded.Close, null, modifier = Modifier.size(16.dp)) }
                                )
                            },
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                            }
                        )

                        WelcomeSegmentedItem(
                            icon = Icons.Rounded.BrightnessHigh,
                            iconColor = Color(0xFFfcbd00),
                            iconTint = Color(0xFF6d3a01),
                            title = stringResource(R.string.perm_write_settings_title),
                            description = stringResource(R.string.perm_write_settings_desc),
                            index = bubbleIndex++,
                            count = bubbleCount,
                            enabled = isBubbleEnabled,
                            control = {
                                Switch(
                                    checked = hasWriteSettingsPermission,
                                    enabled = isBubbleEnabled,
                                    onCheckedChange = {
                                        context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}")))
                                    },
                                    thumbContent = { Icon(if (hasWriteSettingsPermission) Icons.Rounded.Check else Icons.Rounded.Close, null, modifier = Modifier.size(16.dp)) }
                                )
                            },
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}")))
                            }
                        )

                        WelcomeSegmentedItem(
                            icon = R.drawable.screenshot_frame,
                            iconColor = Color(0xFF80da88),
                            iconTint = Color(0xFF00522c),
                            title = stringResource(R.string.perm_accessibility_title),
                            description = stringResource(R.string.perm_accessibility_desc),
                            index = bubbleIndex++,
                            count = bubbleCount,
                            enabled = isBubbleEnabled,
                            control = {
                                Switch(
                                    checked = hasAccessibilityPermission,
                                    enabled = isBubbleEnabled,
                                    onCheckedChange = {
                                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                    },
                                    thumbContent = { Icon(if (hasAccessibilityPermission) Icons.Rounded.Check else Icons.Rounded.Close, null, modifier = Modifier.size(16.dp)) }
                                )
                            },
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            }
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        ),
        OnboardingPageInfo(
            content = { onUpdateScroll ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = stringResource(R.string.feat_discover),
                            style = thinHeaderStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.feat_features),
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 48.sp,
                            modifier = Modifier.offset(y = (-12).dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.feat_intro),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = GoogleSansFlex
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        val scrollState = rememberScrollState()
                        val isAtBottom by remember {
                            derivedStateOf {
                                val layoutInfo = scrollState.maxValue
                                layoutInfo == 0 || scrollState.value >= (layoutInfo - 20)
                            }
                        }

                        LaunchedEffect(isAtBottom) {
                            onUpdateScroll(isAtBottom)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                        ) {
                            val featCount = 7
                            var featIndex = 0

                            WelcomeSegmentedItem(
                                icon = Icons.Rounded.DragHandle,
                                iconColor = Color(0xFFfcbd00),
                                iconTint = Color(0xFF6d3a01),
                                title = stringResource(R.string.feat_order_title),
                                description = stringResource(R.string.feat_order_desc),
                                index = featIndex++,
                                count = featCount
                            )

                            WelcomeSegmentedItem(
                                icon = Icons.Rounded.GridView,
                                iconColor = Color(0xFF80da88),
                                iconTint = Color(0xFF00522c),
                                title = stringResource(R.string.feat_layout_title),
                                description = stringResource(R.string.feat_layout_desc),
                                index = featIndex++,
                                count = featCount
                            )

                            WelcomeSegmentedItem(
                                icon = Icons.Rounded.Person,
                                iconColor = Color(0xFFffb683),
                                iconTint = Color(0xFF753403),
                                title = stringResource(R.string.feat_name_title),
                                description = stringResource(R.string.feat_name_desc),
                                index = featIndex++,
                                count = featCount
                            )

                            WelcomeSegmentedItem(
                                icon = Icons.Rounded.SwipeRight,
                                iconColor = Color(0xFFffb3ae),
                                iconTint = Color(0xFF8a1a16),
                                title = stringResource(R.string.feat_manage_title),
                                description = stringResource(R.string.feat_manage_desc),
                                index = featIndex++,
                                count = featCount
                            )

                            WelcomeSegmentedItem(
                                icon = Icons.Rounded.ShoppingBag,
                                iconColor = Color(0xFFffaee4),
                                iconTint = Color(0xFF8d0053),
                                title = stringResource(R.string.feat_store_title),
                                description = stringResource(R.string.feat_store_desc),
                                index = featIndex++,
                                count = featCount
                            )

                            WelcomeSegmentedItem(
                                icon = Icons.Rounded.History,
                                iconColor = Color(0xFFd8b9fc),
                                iconTint = Color(0xFF5629a4),
                                title = stringResource(R.string.feat_history_title),
                                description = stringResource(R.string.feat_history_desc),
                                index = featIndex++,
                                count = featCount
                            )

                            WelcomeSegmentedItem(
                                icon = R.drawable.ic_phone_update,
                                iconColor = Color(0xFF67d4ff),
                                iconTint = Color(0xFF004e5d),
                                title = stringResource(R.string.feat_update_title),
                                description = stringResource(R.string.feat_update_desc),
                                index = featIndex++,
                                count = featCount
                            )

                            Spacer(modifier = Modifier.height(100.dp))
                        }

                        this@Column.AnimatedVisibility(
                            visible = scrollState.canScrollForward,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            LargeFloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isFirstPage = pagerState.currentPage == 0
    val isLastPage = pagerState.currentPage == pages.size - 1

    BackHandler(enabled = !isFirstPage) {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1, animationSpec = commonAnimSpec)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { index ->
            OnboardingPageItem(
                page = pages[index],
                onUpdateScroll = { scrolledToEnd ->
                    if (index == pages.size - 1) {
                        isLastPageScrolledToEnd = scrolledToEnd
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val backButtonWeight by animateFloatAsState(
            targetValue = if (isFirstPage) 0.0001f else 1f,
            animationSpec = commonAnimSpec,
            label = "backWeight"
        )
        val spacerWeight by animateFloatAsState(
            targetValue = if (isFirstPage) 0.0001f else 0.05f,
            animationSpec = commonAnimSpec,
            label = "spacerWeight"
        )

        val alphaBack by animateFloatAsState(
            targetValue = if (isFirstPage) 0f else 1f,
            animationSpec = commonAnimSpec,
            label = "backAlpha"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(backButtonWeight)
                    .fillMaxHeight()
                    .alpha(alphaBack)
            ) {
                ExpressiveOutlinedButton(
                    text = stringResource(R.string.back),
                    onClick = {
                        if (!isFirstPage) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1, animationSpec = commonAnimSpec)
                            }
                        }
                    },
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.weight(spacerWeight))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val isNextEnabled = !isLastPage || isLastPageScrolledToEnd
                val alphaNext by animateFloatAsState(
                    targetValue = if (isNextEnabled) 1f else 0.5f,
                    label = "nextAlpha"
                )
                ExpressiveButton(
                    text = if (isLastPage) stringResource(R.string.get_started) else stringResource(R.string.next),
                    onClick = {
                        if (isLastPage) {
                            if (isLastPageScrolledToEnd) onFinished()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1, animationSpec = commonAnimSpec)
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = alphaNext),
                    contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = alphaNext),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun checkUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    } else {
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
fun RotatingShapeContainer(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shapeRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_twelve_sided_cookie),
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_monochrome),
            contentDescription = null,
            modifier = Modifier.size(280.dp),
            tint = backgroundColor
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeSegmentedItem(
    icon: Any,
    iconColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    index: Int,
    count: Int,
    enabled: Boolean = true,
    control: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    SegmentedListItem(
        selected = false,
        onClick = { if (enabled) onClick() },
        modifier = if (count <= 1) Modifier.clip(RoundedCornerShape(28.dp)) else Modifier,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else 0.5f)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon is ImageVector) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (icon is Int) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (description.isNotEmpty()) {
                        Text(
                            text = description,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (control != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    control()
                }
            }
        }
    )
}

@Composable
fun OnboardingPageItem(
    page: OnboardingPageInfo,
    onUpdateScroll: (Boolean) -> Unit
) {
    page.content(onUpdateScroll)
}

@Composable
fun ExpressiveButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
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

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerPercent),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ExpressiveOutlinedButton(
    text: String,
    onClick: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
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

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerPercent),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
        }
    }
}