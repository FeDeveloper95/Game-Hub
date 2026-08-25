package com.fedeveloper95.games.elements.GameBubble

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Choreographer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

data class BubbleTool(val id: String, val title: String, val icon: Any)

object GameBubbleStateManager {
    val activeTools = mutableStateMapOf<String, Boolean>()
    val toolValues = mutableStateMapOf<String, String>()

    var originalBrightness = -1
    var originalBrightnessMode = -1
    private var pingJob: Job? = null
    private var fpsCallback: Choreographer.FrameCallback? = null
    private var isFpsActive = false
    private var lifecycle: Lifecycle? = null
    private var appContext: Context? = null

    private val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            cleanup()
        }
    }

    fun attachLifecycle(newLifecycle: Lifecycle, context: Context) {
        if (lifecycle != newLifecycle) {
            lifecycle?.removeObserver(observer)
            lifecycle = newLifecycle
            appContext = context.applicationContext
            lifecycle?.addObserver(observer)
        }
    }

    fun toggleTool(id: String, context: Context, onCollapse: () -> Unit) {
        val isNowActive = !(activeTools[id] ?: false)
        activeTools[id] = isNowActive

        when (id) {
            "boost" -> {
                if (isNowActive) {
                    try {
                        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        am.runningAppProcesses?.forEach {
                            if (it.processName != context.packageName) {
                                am.killBackgroundProcesses(it.processName)
                            }
                        }
                    } catch (e: Exception) {}
                }
            }
            "brightness" -> {
                if (!Settings.System.canWrite(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    activeTools[id] = false
                } else {
                    try {
                        val cr = context.contentResolver
                        if (isNowActive) {
                            originalBrightnessMode = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                            if (originalBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                                Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                            }
                            originalBrightness = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS, 100)
                            Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, 255)
                        } else {
                            if (originalBrightness != -1) {
                                Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, originalBrightness)
                            }
                            if (originalBrightnessMode != -1) {
                                Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, originalBrightnessMode)
                            }
                        }
                    } catch (e: Exception) {
                        activeTools[id] = false
                    }
                }
            }
            "dnd" -> {
                try {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (nm.isNotificationPolicyAccessGranted) {
                        nm.setInterruptionFilter(if (isNowActive) NotificationManager.INTERRUPTION_FILTER_PRIORITY else NotificationManager.INTERRUPTION_FILTER_ALL)
                    } else {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        activeTools[id] = false
                    }
                } catch (e: Exception) {
                    activeTools[id] = false
                }
            }
            "screenshot" -> {
                onCollapse()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(250)
                    context.sendBroadcast(Intent("com.fedeveloper95.games.TAKE_SCREENSHOT"))
                    activeTools["screenshot"] = false
                }
            }
            "ping" -> {
                if (isNowActive) {
                    pingJob?.cancel()
                    pingJob = CoroutineScope(Dispatchers.IO).launch {
                        while (isActive) {
                            try {
                                val process = Runtime.getRuntime().exec("ping -c 1 8.8.8.8")
                                val reader = BufferedReader(InputStreamReader(process.inputStream))
                                val output = reader.readText()
                                val time = output.substringAfter("time=").substringBefore(" ms").trim()
                                if (time.isNotEmpty() && time.toFloatOrNull() != null) {
                                    toolValues["ping"] = "${time.substringBefore(".")}ms"
                                } else {
                                    toolValues["ping"] = "Err"
                                }
                            } catch (e: Exception) {
                                toolValues["ping"] = "Err"
                            }
                            delay(1000)
                        }
                    }
                } else {
                    pingJob?.cancel()
                    pingJob = null
                }
            }
            "fps" -> {
                if (isNowActive) {
                    if (!isFpsActive) {
                        isFpsActive = true
                        var lastTime = System.nanoTime()
                        var frames = 0
                        fpsCallback = object : Choreographer.FrameCallback {
                            override fun doFrame(frameTimeNanos: Long) {
                                frames++
                                val currentTime = System.nanoTime()
                                if (currentTime - lastTime >= 1_000_000_000) {
                                    toolValues["fps"] = "$frames"
                                    frames = 0
                                    lastTime = currentTime
                                }
                                if (isFpsActive) {
                                    Choreographer.getInstance().postFrameCallback(this)
                                }
                            }
                        }
                        Choreographer.getInstance().postFrameCallback(fpsCallback!!)
                    }
                } else {
                    isFpsActive = false
                }
            }
        }
    }

    private fun cleanup() {
        pingJob?.cancel()
        pingJob = null
        isFpsActive = false
        appContext?.let { ctx ->
            if (activeTools["brightness"] == true) {
                try {
                    if (originalBrightness != -1) {
                        Settings.System.putInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS, originalBrightness)
                    }
                    if (originalBrightnessMode != -1) {
                        Settings.System.putInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, originalBrightnessMode)
                    }
                } catch (e: Exception) {}
            }
            if (activeTools["dnd"] == true) {
                try {
                    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (nm.isNotificationPolicyAccessGranted) {
                        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                } catch (e: Exception) {}
            }
        }
        activeTools.clear()
        toolValues.clear()
    }
}

@Composable
fun GameBubbleMenu(onCollapse: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }

    LaunchedEffect(lifecycle) {
        GameBubbleStateManager.attachLifecycle(lifecycle, context)
    }

    val displayTools = remember {
        val defaultOrder = "fps,dnd,brightness,screenshot,ping,boost"
        val toolsOrderString = prefs.getString("pref_bubble_tools_order", defaultOrder) ?: defaultOrder
        val toolsOrder = toolsOrderString.split(",")

        val toolMap = mapOf(
            "fps" to Pair(context.getString(R.string.tool_fps), Icons.Rounded.Speed),
            "dnd" to Pair(context.getString(R.string.tool_dnd), Icons.Rounded.DoNotDisturbOn),
            "brightness" to Pair(context.getString(R.string.tool_brightness), Icons.Rounded.BrightnessHigh),
            "screenshot" to Pair(context.getString(R.string.tool_screenshot), R.drawable.screenshot_frame),
            "ping" to Pair(context.getString(R.string.tool_ping), Icons.Rounded.Wifi),
            "boost" to Pair(context.getString(R.string.tool_boost), Icons.Rounded.RocketLaunch)
        )

        toolsOrder.filter { toolId ->
            prefs.getBoolean("pref_bubble_tool_$toolId", true)
        }.mapNotNull { toolId ->
            toolMap[toolId]?.let { BubbleTool(toolId, it.first, it.second) }
        }
    }

    Column(
        modifier = Modifier
            .requiredSize(320.dp, 340.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Game Hub",
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onCollapse) {
                Icon(Icons.Rounded.CloseFullscreen, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(264.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(displayTools, span = { index, _ ->
                if (index < 4) GridItemSpan(2) else GridItemSpan(1)
            }) { index, tool ->
                val isActive = GameBubbleStateManager.activeTools[tool.id] ?: false
                val dynamicValue = GameBubbleStateManager.toolValues[tool.id] ?: ""
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                var holdState by remember { mutableStateOf(false) }

                LaunchedEffect(isPressed) {
                    if (isPressed) {
                        holdState = true
                    } else {
                        delay(150)
                        holdState = false
                    }
                }

                val cornerPercent by animateIntAsState(
                    targetValue = if (holdState) 15 else if (isActive) 25 else 50,
                    animationSpec = tween(durationMillis = 150),
                    label = "cornerAnim"
                )
                val isExpandedTile = index < 4
                val tileBgColor by animateColorAsState(
                    targetValue = if (!isExpandedTile && isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    animationSpec = tween(150),
                    label = "bgAnim"
                )
                val circleBgColor by animateColorAsState(
                    targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                    animationSpec = tween(150),
                    label = "circleBgAnim"
                )
                val iconTint by animateColorAsState(
                    targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(150),
                    label = "iconTintAnim"
                )

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(percent = cornerPercent))
                        .background(tileBgColor)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            GameBubbleStateManager.toggleTool(tool.id, context, onCollapse)
                        },
                    contentAlignment = if (isExpandedTile) Alignment.CenterStart else Alignment.Center
                ) {
                    if (isExpandedTile) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(circleBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (tool.icon is ImageVector) {
                                    Icon(tool.icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(painterResource(tool.icon as Int), null, tint = iconTint, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isActive && (tool.id == "ping" || tool.id == "fps")) dynamicValue else tool.title,
                                fontFamily = GoogleSansFlex,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        if (isActive && (tool.id == "ping" || tool.id == "fps")) {
                            Text(
                                text = dynamicValue,
                                fontFamily = GoogleSansFlex,
                                style = MaterialTheme.typography.labelMedium,
                                color = iconTint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            if (tool.icon is ImageVector) {
                                Icon(tool.icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(painterResource(tool.icon as Int), null, tint = iconTint, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}