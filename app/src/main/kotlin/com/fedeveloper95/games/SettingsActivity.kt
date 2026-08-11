package com.fedeveloper95.games

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.fedeveloper95.games.elements.SettingsActivity.CardStylePopup
import com.fedeveloper95.games.elements.SettingsActivity.GameOrderPopup
import com.fedeveloper95.games.elements.SettingsActivity.NamePopup
import com.fedeveloper95.games.elements.UI.ExpressiveIconButton
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import kotlin.math.roundToInt

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val pm = packageManager
        val prefs = getSharedPreferences("game_hub_settings", MODE_PRIVATE)

        if (prefs.getString(PREF_APP_ICON, "Expressive") != "Expressive") {
            prefs.edit().putString(PREF_APP_ICON, "Expressive").apply()
            val expressiveComponent = ComponentName(this, "com.fedeveloper95.games.ExpressiveIcon")
            val flatComponent = ComponentName(this, "com.fedeveloper95.games.FlatIcon")

            pm.setComponentEnabledSetting(expressiveComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(flatComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }

        setContent {
            val context = LocalContext.current
            val composePrefs = remember { context.getSharedPreferences("game_hub_settings",
                MODE_PRIVATE
            ) }
            val savedTheme = composePrefs.getInt(PREF_THEME, THEME_SYSTEM)

            var currentThemeOverride by remember { mutableIntStateOf(savedTheme) }

            Crossfade(
                targetState = currentThemeOverride,
                animationSpec = tween(durationMillis = 350),
                label = "theme_fade"
            ) { animatedTheme ->
                GameHubTheme(themeOverride = animatedTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SettingsScreen(
                            onBack = { finish() },
                            currentTheme = animatedTheme,
                            onThemeChanged = { newTheme -> currentThemeOverride = newTheme }
                        )
                    }
                }
            }
        }
    }
}

const val PREF_THEME = "pref_theme"
const val THEME_SYSTEM = 0
const val THEME_LIGHT = 1
const val THEME_DARK = 2

const val PREF_APP_ICON = "pref_app_icon"
const val PREF_CARD_STYLE = "pref_card_style"
const val CARD_STYLE_DEFAULT = "Default"
const val CARD_STYLE_HORIZONTAL = "Horizontal"
const val CARD_STYLE_GRID = "Grid"

const val PREF_GRID_COLUMNS = "pref_grid_columns"
const val PREF_SHOW_GET_MORE_GAMES = "pref_show_get_more_games"
const val PREF_SHOW_LAUNCH_COUNT = "pref_show_launch_count"
const val PREF_AUTO_UPDATES = "pref_auto_updates"
const val PREF_SHOW_USER_NAME = "pref_show_user_name"
const val PREF_USER_NAME = "pref_user_name"
const val PREF_SORT_TYPE = "pref_sort_type"
const val PREF_SHOW_PLAY_TIME = "pref_show_play_time"
const val PREF_STATS_INTERVAL = "pref_stats_interval"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    currentTheme: Int,
    onThemeChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 600
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }
    val haptic = LocalHapticFeedback.current

    var currentCardStyle by remember { mutableStateOf(prefs.getString(PREF_CARD_STYLE, CARD_STYLE_DEFAULT) ?: CARD_STYLE_DEFAULT) }
    var gridColumns by remember { mutableIntStateOf(prefs.getInt(PREF_GRID_COLUMNS, 2)) }
    var currentSortType by remember { mutableStateOf(prefs.getString(PREF_SORT_TYPE, "Alphabetical") ?: "Alphabetical") }
    var showGetMoreGames by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_GET_MORE_GAMES, true)) }
    var showLaunchCount by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_LAUNCH_COUNT, true)) }
    var showPlayTime by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_PLAY_TIME, true)) }
    var statsInterval by remember { mutableFloatStateOf(prefs.getFloat(PREF_STATS_INTERVAL, 3f)) }
    var showUserName by remember { mutableStateOf(prefs.getBoolean(PREF_SHOW_USER_NAME, true)) }
    var userName by remember { mutableStateOf(prefs.getString(PREF_USER_NAME, "User") ?: "User") }

    val hasOverlay = Settings.canDrawOverlays(context)
    val hasBt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else true

    var testControllerFeatures by remember {
        mutableStateOf(prefs.getBoolean("test_controller_features", hasOverlay && hasBt))
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    var showStyleDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val isPixel = remember {
        val brand = Build.BRAND
        val manufacturer = Build.MANUFACTURER
        brand.equals("google", ignoreCase = true) || manufacturer.equals("google", ignoreCase = true)
    }

    val appInfo = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = pInfo.versionName ?: "1.0"
            val build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else pInfo.versionCode.toLong()
            "v$version ($build)"
        } catch (e: Exception) {
            context.getString(R.string.unknown)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        ExpressiveIconButton(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.discard),
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

            Text(
                text = stringResource(R.string.settings_header_appearance),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val appCount = 3 + (if (currentCardStyle == CARD_STYLE_GRID) 1 else 0) + (if (showUserName) 1 else 0)
                var appIndex = 0

                SegmentedListItem(
                    selected = false,
                    onClick = {},
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shapes = ListItemDefaults.segmentedShapes(index = minOf(appIndex++, appCount - 1), count = appCount),
                    content = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFfcbd00)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = null,
                                    tint = Color(0xFF6d3a01),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                            ) {
                                ToggleButton(
                                    checked = currentTheme == THEME_SYSTEM,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onThemeChanged(THEME_SYSTEM)
                                        prefs.edit().putInt(PREF_THEME, THEME_SYSTEM).apply()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shapes = ToggleButtonDefaults.shapes(
                                        shape = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomStartPercent = 15, bottomEndPercent = 15),
                                        checkedShape = RoundedCornerShape(50)
                                    ),
                                    colors = ToggleButtonDefaults.toggleButtonColors(
                                        containerColor = Color.Transparent,
                                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        checkedContentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    border = if (currentTheme == THEME_SYSTEM) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Smartphone, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.settings_theme_system), fontFamily = GoogleSansFlex)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    ToggleButton(
                                        checked = currentTheme == THEME_DARK,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onThemeChanged(THEME_DARK)
                                            prefs.edit().putInt(PREF_THEME, THEME_DARK).apply()
                                        },
                                        shapes = ToggleButtonDefaults.shapes(
                                            shape = RoundedCornerShape(topStartPercent = 15, bottomStartPercent = 50, topEndPercent = 15, bottomEndPercent = 15),
                                            checkedShape = RoundedCornerShape(50)
                                        ),
                                        colors = ToggleButtonDefaults.toggleButtonColors(
                                            containerColor = Color.Transparent,
                                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            checkedContentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        border = if (currentTheme == THEME_DARK) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.DarkMode, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.settings_theme_dark), fontFamily = GoogleSansFlex)
                                        }
                                    }
                                    ToggleButton(
                                        checked = currentTheme == THEME_LIGHT,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onThemeChanged(THEME_LIGHT)
                                            prefs.edit().putInt(PREF_THEME, THEME_LIGHT).apply()
                                        },
                                        shapes = ToggleButtonDefaults.shapes(
                                            shape = RoundedCornerShape(topStartPercent = 15, bottomStartPercent = 15, topEndPercent = 15, bottomEndPercent = 50),
                                            checkedShape = RoundedCornerShape(50)
                                        ),
                                        colors = ToggleButtonDefaults.toggleButtonColors(
                                            containerColor = Color.Transparent,
                                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            checkedContentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        border = if (currentTheme == THEME_LIGHT) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.LightMode, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.settings_theme_light), fontFamily = GoogleSansFlex)
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.ViewAgenda,
                    title = stringResource(R.string.settings_card_style_title),
                    subtitle = currentCardStyle,
                    containerColor = Color(0xFF80da88),
                    iconColor = Color(0xFF00522c),
                    index = minOf(appIndex++, appCount - 1),
                    count = appCount,
                    onClick = { showStyleDialog = true }
                )

                AnimatedVisibility(
                    visible = currentCardStyle == CARD_STYLE_GRID,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SegmentedListItem(
                        selected = false,
                        onClick = {},
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shapes = ListItemDefaults.segmentedShapes(index = minOf(appIndex++, appCount - 1), count = appCount),
                        content = {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(
                                    text = stringResource(R.string.settings_grid_columns_title),
                                    fontFamily = GoogleSansFlex,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                SingleChoiceSegmentedButtonRow(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(2, 3, 4).forEachIndexed { index, columns ->
                                        SegmentedButton(
                                            selected = gridColumns == columns,
                                            onClick = {
                                                gridColumns = columns
                                                prefs.edit().putInt(PREF_GRID_COLUMNS, columns).apply()
                                            },
                                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                                        ) {
                                            Text(
                                                text = columns.toString(),
                                                fontFamily = GoogleSansFlex
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Person,
                    title = stringResource(R.string.settings_show_name_title),
                    subtitle = stringResource(R.string.settings_show_name_desc),
                    containerColor = Color(0xFFffb683),
                    iconColor = Color(0xFF753403),
                    index = minOf(appIndex++, appCount - 1),
                    count = appCount,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showUserName = !showUserName
                        prefs.edit().putBoolean(PREF_SHOW_USER_NAME, showUserName).apply()
                    },
                    trailingContent = {
                        Switch(
                            checked = showUserName,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showUserName = it
                                prefs.edit().putBoolean(PREF_SHOW_USER_NAME, it).apply()
                            },
                            thumbContent = {
                                if (showUserName) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            }
                        )
                    }
                )

                AnimatedVisibility(
                    visible = showUserName,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    GameSettingsSegmentedItem(
                        icon = Icons.Rounded.Edit,
                        title = stringResource(R.string.settings_edit_name_title),
                        subtitle = userName,
                        containerColor = Color(0xFFe7e0ec),
                        iconColor = Color(0xFF49454f),
                        index = minOf(appIndex++, appCount - 1),
                        count = appCount,
                        onClick = { showNameDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_header_preferences),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val prefCount = 4 + (if (showPlayTime) 1 else 0)
                var prefIndex = 0

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Sort,
                    title = stringResource(R.string.settings_sort_title),
                    subtitle = when (currentSortType) {
                        "Alphabetical" -> stringResource(R.string.sort_alphabetical)
                        "Time" -> stringResource(R.string.sort_playtime)
                        else -> stringResource(R.string.sort_custom)
                    },
                    containerColor = Color(0xFF67d4ff),
                    iconColor = Color(0xFF004e5d),
                    index = minOf(prefIndex++, prefCount - 1),
                    count = prefCount,
                    onClick = { showSortDialog = true }
                )

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.History,
                    title = stringResource(R.string.settings_show_launch_count_title),
                    subtitle = stringResource(R.string.settings_show_launch_count_desc),
                    containerColor = Color(0xFFd8b9fc),
                    iconColor = Color(0xFF5629a4),
                    index = minOf(prefIndex++, prefCount - 1),
                    count = prefCount,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showLaunchCount = !showLaunchCount
                        prefs.edit().putBoolean(PREF_SHOW_LAUNCH_COUNT, showLaunchCount).apply()
                    },
                    trailingContent = {
                        Switch(
                            checked = showLaunchCount,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showLaunchCount = it
                                prefs.edit().putBoolean(PREF_SHOW_LAUNCH_COUNT, it).apply()
                            },
                            thumbContent = {
                                if (showLaunchCount) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            }
                        )
                    }
                )

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Timer,
                    title = stringResource(R.string.settings_show_playtime_title),
                    subtitle = stringResource(R.string.settings_show_playtime_desc),
                    containerColor = Color(0xFFffaee4),
                    iconColor = Color(0xFF8d0053),
                    index = minOf(prefIndex++, prefCount - 1),
                    count = prefCount,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showPlayTime = !showPlayTime
                        prefs.edit().putBoolean(PREF_SHOW_PLAY_TIME, showPlayTime).apply()
                    },
                    trailingContent = {
                        Switch(
                            checked = showPlayTime,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showPlayTime = it
                                prefs.edit().putBoolean(PREF_SHOW_PLAY_TIME, it).apply()
                            },
                            thumbContent = {
                                if (showPlayTime) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            }
                        )
                    }
                )

                AnimatedVisibility(
                    visible = showPlayTime,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SegmentedListItem(
                        selected = false,
                        onClick = {},
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shapes = ListItemDefaults.segmentedShapes(index = minOf(prefIndex++, prefCount - 1), count = prefCount),
                        content = {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFffb3ae)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DateRange,
                                            contentDescription = null,
                                            tint = Color(0xFF8a1a16),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.settings_stats_interval_title),
                                            fontFamily = GoogleSansFlex,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = when (statsInterval.roundToInt()) {
                                                0 -> stringResource(R.string.interval_daily)
                                                1 -> stringResource(R.string.interval_weekly)
                                                2 -> stringResource(R.string.interval_monthly)
                                                else -> stringResource(R.string.interval_yearly)
                                            },
                                            fontFamily = GoogleSansFlex,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Slider(
                                    value = statsInterval,
                                    onValueChange = {
                                        if (statsInterval != it) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        statsInterval = it
                                        prefs.edit().putFloat(PREF_STATS_INTERVAL, it).apply()
                                    },
                                    valueRange = 0f..3f,
                                    steps = 2
                                )
                            }
                        }
                    )
                }

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.ShoppingBag,
                    title = stringResource(R.string.settings_show_get_more_title),
                    subtitle = stringResource(R.string.settings_show_get_more_desc),
                    containerColor = Color(0xFF67d4ff),
                    iconColor = Color(0xFF004e5d),
                    index = minOf(prefIndex++, prefCount - 1),
                    count = prefCount,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showGetMoreGames = !showGetMoreGames
                        prefs.edit().putBoolean(PREF_SHOW_GET_MORE_GAMES, showGetMoreGames).apply()
                    },
                    trailingContent = {
                        Switch(
                            checked = showGetMoreGames,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showGetMoreGames = it
                                prefs.edit().putBoolean(PREF_SHOW_GET_MORE_GAMES, it).apply()
                            },
                            thumbContent = {
                                if (showGetMoreGames) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_header_more),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val moreCount = 2 + (if (isPixel) 1 else 0)
                var moreIndex = 0

                if (isPixel) {
                    GameSettingsSegmentedItem(
                        icon = Icons.Rounded.SportsEsports,
                        title = stringResource(R.string.settings_game_bubble_title),
                        subtitle = stringResource(R.string.settings_game_bubble_desc),
                        containerColor = Color(0xFF80da88),
                        iconColor = Color(0xFF00522c),
                        index = minOf(moreIndex++, moreCount - 1),
                        count = moreCount,
                        onClick = {
                            val intent = Intent(context, GameBubbleSettingsActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Gamepad,
                    title = stringResource(R.string.settings_test_controller_title),
                    subtitle = stringResource(R.string.settings_test_controller_desc),
                    containerColor = Color(0xFFcba6ff),
                    iconColor = Color(0xFF320073),
                    index = minOf(moreIndex++, moreCount - 1),
                    count = moreCount,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val newValue = !testControllerFeatures
                        testControllerFeatures = newValue
                        prefs.edit().putBoolean("test_controller_features", newValue).apply()

                        if (newValue) {
                            if (!Settings.canDrawOverlays(context)) {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                context.startActivity(intent)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                    bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                }
                            }
                        }
                    },
                    trailingContent = {
                        Switch(
                            checked = testControllerFeatures,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                testControllerFeatures = it
                                prefs.edit().putBoolean("test_controller_features", it).apply()

                                if (it) {
                                    if (!Settings.canDrawOverlays(context)) {
                                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                        context.startActivity(intent)
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                        }
                                    }
                                }
                            },
                            thumbContent = {
                                if (testControllerFeatures) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize))
                                } else {
                                    Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize))
                                }
                            }
                        )
                    }
                )

                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Tune,
                    title = stringResource(R.string.settings_advanced_title),
                    subtitle = stringResource(R.string.settings_advanced_desc),
                    containerColor = Color(0xFFC7C7C7),
                    iconColor = Color(0xFF2C2C2C),
                    index = minOf(moreIndex++, moreCount - 1),
                    count = moreCount,
                    onClick = {
                        val intent = Intent(context, AdvancedSettingsActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_header_info),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.settings_version_title),
                    subtitle = appInfo,
                    containerColor = Color(0xFFa1c9ff),
                    iconColor = Color(0xFF0641a0),
                    index = 0,
                    count = 4,
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )
                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.Code,
                    title = stringResource(R.string.settings_developer_title),
                    subtitle = stringResource(R.string.settings_developer_name),
                    containerColor = Color(0xFFc7c7c7),
                    iconColor = Color(0xFF474747),
                    index = 1,
                    count = 4,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/FeDeveloper95"))
                        context.startActivity(intent)
                    }
                )
                GameSettingsSegmentedItem(
                    icon = Icons.Rounded.BugReport,
                    title = stringResource(R.string.settings_report_title),
                    subtitle = stringResource(R.string.settings_report_desc),
                    containerColor = Color(0xFFffb3ae),
                    iconColor = Color(0xFF8a1a16),
                    index = 2,
                    count = 4,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/fedeveloper95"))
                        context.startActivity(intent)
                    }
                )
                GameSettingsSegmentedItem(
                    icon = R.drawable.ic_phone_update,
                    title = stringResource(R.string.settings_check_updates_title),
                    subtitle = stringResource(R.string.settings_check_updates_desc),
                    containerColor = Color(0xFF67d4ff),
                    iconColor = Color(0xFF004e5d),
                    index = 3,
                    count = 4,
                    onClick = {
                        val intent = Intent(context, UpdaterActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    if (showNameDialog) {
        NamePopup(
            currentName = userName,
            onNameSaved = { newName ->
                userName = newName
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }

    if (showStyleDialog) {
        CardStylePopup(
            currentStyle = currentCardStyle,
            onStyleSelected = { newStyle ->
                currentCardStyle = newStyle
                showStyleDialog = false
            },
            onDismiss = { showStyleDialog = false }
        )
    }

    if (showSortDialog) {
        GameOrderPopup(
            currentSort = currentSortType,
            onSortSelected = { newSort ->
                currentSortType = newSort
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameSettingsSegmentedItem(
    icon: Any,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconColor: Color,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    SegmentedListItem(
        selected = false,
        onClick = onClick,
        modifier = if (count <= 1) Modifier.clip(RoundedCornerShape(28.dp)) else Modifier,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon is ImageVector) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (icon is Int) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = iconColor,
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
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (trailingContent != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    trailingContent()
                }
            }
        }
    )
}