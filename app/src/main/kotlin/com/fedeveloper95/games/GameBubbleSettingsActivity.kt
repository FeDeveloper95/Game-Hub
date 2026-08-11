package com.fedeveloper95.games

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.fedeveloper95.games.elements.UI.ExpressiveIconButton
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class GameBubbleSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameBubbleSettingsScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBubbleSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 600
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }
    val haptic = LocalHapticFeedback.current

    var isBubbleEnabled by remember { mutableStateOf(prefs.getBoolean("pref_bubble_enabled", false)) }
    var isAutoHideEnabled by remember { mutableStateOf(prefs.getBoolean("pref_bubble_autohide", true)) }

    val defaultTools = listOf("fps", "dnd", "brightness", "screenshot", "ping", "boost")
    var toolsOrder by remember {
        mutableStateOf(
            prefs.getString("pref_bubble_tools_order", defaultTools.joinToString(","))!!.split(",")
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val hasPermission = Settings.canDrawOverlays(context)
        isBubbleEnabled = hasPermission
        prefs.edit().putBoolean("pref_bubble_enabled", hasPermission).apply()
    }

    val toolDetails = mapOf(
        "fps" to Triple(R.string.tool_fps, Icons.Rounded.Speed, Color(0xFF67d4ff)),
        "dnd" to Triple(R.string.tool_dnd, Icons.Rounded.DoNotDisturbOn, Color(0xFFffb3ae)),
        "brightness" to Triple(R.string.tool_brightness, Icons.Rounded.BrightnessHigh, Color(0xFFfcbd00)),
        "screenshot" to Triple(R.string.tool_screenshot, R.drawable.screenshot_frame, Color(0xFF80da88)),
        "ping" to Triple(R.string.tool_ping, Icons.Rounded.Wifi, Color(0xFFd8b9fc)),
        "boost" to Triple(R.string.tool_boost, Icons.Rounded.RocketLaunch, Color(0xFFffb683))
    )

    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        if (!isBubbleEnabled) return@rememberReorderableLazyListState
        val fromKey = from.key as? String ?: return@rememberReorderableLazyListState
        val toKey = to.key as? String ?: return@rememberReorderableLazyListState
        val currentList = toolsOrder.toMutableList()
        val fromIndex = currentList.indexOf(fromKey)
        val toIndex = currentList.indexOf(toKey)
        if (fromIndex != -1 && toIndex != -1) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            toolsOrder = currentList
            prefs.edit().putString("pref_bubble_tools_order", currentList.joinToString(",")).apply()
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_game_bubble_title),
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .then(if (isExpandedScreen) Modifier.padding(horizontal = 64.dp) else Modifier),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = padding.calculateBottomPadding() + 48.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.header_bubble),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
            item {
                val interactionSource = remember { MutableInteractionSource() }
                val shape = RoundedCornerShape(64.dp)
                val handleToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (!isBubbleEnabled && !Settings.canDrawOverlays(context)) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        permissionLauncher.launch(intent)
                    } else {
                        isBubbleEnabled = !isBubbleEnabled
                        prefs.edit().putBoolean("pref_bubble_enabled", isBubbleEnabled).apply()
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current
                        ) {
                            handleToggle()
                        },
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
                                onCheckedChange = { handleToggle() },
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
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                GameBubbleSegmentedSwitchItem(
                    icon = Icons.Rounded.VisibilityOff,
                    title = stringResource(R.string.settings_bubble_autohide_title),
                    subtitle = stringResource(R.string.settings_bubble_autohide_desc),
                    containerColor = if (isAutoHideEnabled && isBubbleEnabled) Color(0xFFd8b9fc) else MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = if (isAutoHideEnabled && isBubbleEnabled) Color(0xFF5629a4) else MaterialTheme.colorScheme.onSurfaceVariant,
                    index = 0,
                    count = 1,
                    checked = isAutoHideEnabled,
                    enabled = isBubbleEnabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isAutoHideEnabled = it
                        prefs.edit().putBoolean("pref_bubble_autohide", it).apply()
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                Text(
                    text = stringResource(R.string.settings_bubble_tools_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 8.dp)
                        .alpha(if (isBubbleEnabled) 1f else 0.5f)
                )
            }
            itemsIndexed(toolsOrder, key = { _, item -> item }) { index, toolId ->
                val details = toolDetails[toolId]
                if (details != null) {
                    var isToolEnabled by remember {
                        mutableStateOf(prefs.getBoolean("pref_bubble_tool_$toolId", true))
                    }
                    ReorderableItem(reorderState, key = toolId) { isDragging ->
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            animationSpec = tween(200), label = ""
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    shadowElevation = elevation.toPx()
                                }
                        ) {
                            GameBubbleSegmentedSwitchItem(
                                icon = details.second,
                                title = stringResource(details.first),
                                subtitle = "",
                                containerColor = if (isToolEnabled && isBubbleEnabled) details.third else MaterialTheme.colorScheme.surfaceVariant,
                                iconColor = if (isToolEnabled && isBubbleEnabled) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                index = index,
                                count = toolsOrder.size,
                                checked = isToolEnabled,
                                enabled = isBubbleEnabled,
                                isCompact = true,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isToolEnabled = it
                                    prefs.edit().putBoolean("pref_bubble_tool_$toolId", it).apply()
                                },
                                dragHandleModifier = if (isBubbleEnabled) Modifier.draggableHandle() else Modifier
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameBubbleSegmentedSwitchItem(
    icon: Any,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconColor: Color,
    index: Int,
    count: Int,
    checked: Boolean,
    enabled: Boolean = true,
    isCompact: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
    dragHandleModifier: Modifier? = null
) {
    SegmentedListItem(
        selected = false,
        onClick = { if (enabled) onCheckedChange(!checked) },
        modifier = if (count == 1) Modifier.clip(RoundedCornerShape(20.dp)) else Modifier,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else 0.5f)
                    .padding(vertical = if (isCompact) 0.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 40.dp else 48.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    val iconModifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                    if (icon is ImageVector) {
                        Icon(icon, null, tint = iconColor, modifier = iconModifier)
                    } else if (icon is Int) {
                        Icon(painterResource(icon), null, tint = iconColor, modifier = iconModifier)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal,
                        style = if (isCompact) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium
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
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = onCheckedChange,
                    thumbContent = {
                        Icon(
                            imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                )
                if (dragHandleModifier != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = dragHandleModifier
                    )
                }
            }
        }
    )
}