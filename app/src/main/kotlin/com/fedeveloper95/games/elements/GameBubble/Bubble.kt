package com.fedeveloper95.games.elements.GameBubble

import android.animation.ValueAnimator
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameBubbleService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private lateinit var bubbleView: ComposeView
    private lateinit var dismissView: ComposeView

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var isDraggingBubble by mutableStateOf(false)
    private var isHoveringDismiss by mutableStateOf(false)
    private var expanded by mutableStateOf(false)
    private var isDeleting by mutableStateOf(false)
    private var wasLeftSide by mutableStateOf(true)
    private var currentX = 100f
    private var currentY = 100f
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var dismissParams: WindowManager.LayoutParams
    private var snapAnimator: ValueAnimator? = null
    private var currentTargetPackage: String? = null

    private val homeButtonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                stopSelf()
            }
        }
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.fedeveloper95.games.PACKAGE_CHANGED") {
                val currentForeground = intent.getStringExtra("PACKAGE_NAME") ?: return
                if (currentForeground != packageName && currentForeground != "com.android.systemui") {
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
                    val launcherInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    val launcherPackage = launcherInfo?.activityInfo?.packageName ?: ""

                    val wentHome = currentForeground == launcherPackage
                    val leftTarget = currentTargetPackage != null && currentForeground != currentTargetPackage

                    if (wentHome || leftTarget) {
                        stopSelf()
                    }
                }
            }
        }
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val filter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        val packageFilter = IntentFilter("com.fedeveloper95.games.PACKAGE_CHANGED")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(homeButtonReceiver, filter, RECEIVER_EXPORTED)
            registerReceiver(packageReceiver, packageFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(homeButtonReceiver, filter)
            registerReceiver(packageReceiver, packageFilter)
        }

        dismissView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameBubbleService)
            setViewTreeViewModelStoreOwner(this@GameBubbleService)
            setViewTreeSavedStateRegistryOwner(this@GameBubbleService)
            setContent {
                val darkTheme = isSystemInDarkTheme()
                val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                } else {
                    if (darkTheme) darkColorScheme() else lightColorScheme()
                }
                MaterialTheme(colorScheme = colorScheme) {
                    DismissAreaOverlay(
                        isDragging = isDraggingBubble,
                        isExpanded = expanded,
                        isHovering = isHoveringDismiss,
                        isDeleting = isDeleting
                    )
                }
            }
        }

        dismissParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        bubbleView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GameBubbleService)
            setViewTreeViewModelStoreOwner(this@GameBubbleService)
            setViewTreeSavedStateRegistryOwner(this@GameBubbleService)

            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    if (expanded) {
                        expanded = false
                        bubbleParams.flags = bubbleParams.flags and WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH.inv()
                        windowManager.updateViewLayout(bubbleView, bubbleParams)
                    }
                    true
                } else {
                    false
                }
            }

            setContent {
                val darkTheme = isSystemInDarkTheme()
                val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                } else {
                    if (darkTheme) darkColorScheme() else lightColorScheme()
                }
                MaterialTheme(colorScheme = colorScheme) {
                    GameBubbleContent(
                        isExpanded = expanded,
                        isDragging = isDraggingBubble,
                        wasLeftSide = wasLeftSide,
                        isDeleting = isDeleting,
                        onExpand = {
                            expanded = true
                            bubbleParams.flags = bubbleParams.flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

                            val displayMetrics = resources.displayMetrics
                            val expandedHeightPx = 340 * displayMetrics.density
                            val maxAllowedY = displayMetrics.heightPixels - expandedHeightPx - (32 * displayMetrics.density)

                            if (currentY > maxAllowedY) {
                                snapAnimator?.cancel()
                                snapAnimator = ValueAnimator.ofFloat(currentY, maxAllowedY).apply {
                                    duration = 300
                                    interpolator = android.view.animation.DecelerateInterpolator()
                                    addUpdateListener { animator ->
                                        currentY = animator.animatedValue as Float
                                        bubbleParams.y = currentY.toInt()
                                        windowManager.updateViewLayout(bubbleView, bubbleParams)
                                    }
                                    start()
                                }
                            } else {
                                windowManager.updateViewLayout(bubbleView, bubbleParams)
                            }
                        },
                        onCollapse = {
                            expanded = false
                            bubbleParams.flags = bubbleParams.flags and WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH.inv()
                            windowManager.updateViewLayout(bubbleView, bubbleParams)
                        },
                        onDragStart = {
                            if (!expanded) {
                                snapAnimator?.cancel()
                                isDraggingBubble = true
                            }
                        },
                        onDrag = { dx, dy -> updateBubblePosition(dx, dy) },
                        onDragEnd = {
                            if (!expanded) {
                                val dragging = isDraggingBubble
                                isDraggingBubble = false
                                if (dragging && isHoveringDismiss) {
                                    isDeleting = true
                                    lifecycle.coroutineScope.launch {
                                        delay(300)
                                        stopSelf()
                                    }
                                } else {
                                    snapToNearestEdge()
                                }
                            }
                        },
                        onWidthChange = { widthPx ->
                            if (!isDraggingBubble && !wasLeftSide) {
                                val sw = resources.displayMetrics.widthPixels
                                val edgeOffsetPx = 12 * resources.displayMetrics.density
                                currentX = sw - widthPx - edgeOffsetPx
                                bubbleParams.x = currentX.toInt()
                                windowManager.updateViewLayout(bubbleView, bubbleParams)
                            }
                        }
                    )
                }
            }
        }

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = currentX.toInt()
            y = currentY.toInt()
        }

        windowManager.addView(dismissView, dismissParams)
        windowManager.addView(bubbleView, bubbleParams)
        bubbleView.post { snapToNearestEdge() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentTargetPackage = intent?.getStringExtra("TARGET_PACKAGE") ?: currentTargetPackage
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        snapAnimator?.cancel()
        try {
            unregisterReceiver(homeButtonReceiver)
            unregisterReceiver(packageReceiver)
        } catch (e: Exception) {}
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        if (::windowManager.isInitialized) {
            if (::bubbleView.isInitialized) windowManager.removeView(bubbleView)
            if (::dismissView.isInitialized) windowManager.removeView(dismissView)
        }
    }

    private fun updateBubblePosition(dx: Float, dy: Float) {
        currentX += dx
        currentY += dy
        bubbleParams.x = currentX.toInt()
        bubbleParams.y = currentY.toInt()
        windowManager.updateViewLayout(bubbleView, bubbleParams)

        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        val cookieSizePx = 112 * displayMetrics.density
        val cookieBottomPaddingPx = 64 * displayMetrics.density

        val cookieTopY = screenHeight - cookieBottomPaddingPx - cookieSizePx
        val cookieLeftX = (screenWidth / 2f) - (cookieSizePx / 2f)
        val cookieRightX = (screenWidth / 2f) + (cookieSizePx / 2f)

        val bubbleCenterX = currentX + (48 * displayMetrics.density) / 2f
        val bubbleCenterY = currentY + (48 * displayMetrics.density) / 2f

        isHoveringDismiss = !expanded &&
                bubbleCenterY > cookieTopY &&
                bubbleCenterX > cookieLeftX &&
                bubbleCenterX < cookieRightX
    }

    private fun snapToNearestEdge() {
        snapAnimator?.cancel()
        val sw = resources.displayMetrics.widthPixels
        val bubbleWidth = 48 * resources.displayMetrics.density
        val edgeOffset = 12 * resources.displayMetrics.density

        val isLeft = currentX + bubbleWidth / 2 < sw / 2
        wasLeftSide = isLeft
        val targetX = if (isLeft) edgeOffset else sw - bubbleWidth - edgeOffset

        snapAnimator = ValueAnimator.ofFloat(currentX, targetX).apply {
            duration = 300
            interpolator = android.view.animation.DecelerateInterpolator()
            addUpdateListener { animation ->
                currentX = animation.animatedValue as Float
                bubbleParams.x = currentX.toInt()
                windowManager.updateViewLayout(bubbleView, bubbleParams)
            }
            start()
        }
    }
}

@Composable
fun GameBubbleContent(
    isExpanded: Boolean,
    isDragging: Boolean,
    wasLeftSide: Boolean,
    isDeleting: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onWidthChange: (Float) -> Unit
) {
    val BUBBLE_SIZE = 48.dp
    val EXPANDED_WIDTH = 320.dp
    val EXPANDED_HEIGHT = 340.dp

    val isFpsActive = GameBubbleStateManager.activeTools["fps"] == true
    val fpsValue = GameBubbleStateManager.toolValues["fps"] ?: ""

    val interactionSourceMain = remember { MutableInteractionSource() }
    val isPressedMain by interactionSourceMain.collectIsPressedAsState()

    val interactionSourceFps = remember { MutableInteractionSource() }
    val isPressedFps by interactionSourceFps.collectIsPressedAsState()

    val animatedWidth by animateDpAsState(
        targetValue = if (isExpanded) EXPANDED_WIDTH else BUBBLE_SIZE,
        animationSpec = if (isExpanded) tween(150, delayMillis = 150) else tween(150, delayMillis = 0),
        label = "width"
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (isExpanded) EXPANDED_HEIGHT else BUBBLE_SIZE,
        animationSpec = if (isExpanded) tween(150, delayMillis = 0) else tween(150, delayMillis = 150),
        label = "height"
    )

    val mainBottomCorner by animateDpAsState(
        targetValue = if (isExpanded || isPressedMain) 24.dp else if (isFpsActive) 4.dp else 24.dp,
        animationSpec = tween(150),
        label = "mainBottomCorner"
    )

    val fpsTopCorner by animateDpAsState(
        targetValue = if (isPressedFps) 24.dp else 4.dp,
        animationSpec = tween(150),
        label = "fpsTopCorner"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(300),
        label = "bgColor"
    )
    val deleteScale by animateFloatAsState(
        targetValue = if (isDeleting) 0f else 1f,
        animationSpec = tween(300),
        label = "deleteScale"
    )
    val deleteAlpha by animateFloatAsState(
        targetValue = if (isDeleting) 0f else 1f,
        animationSpec = tween(300),
        label = "deleteAlpha"
    )

    val density = LocalDensity.current
    val context = LocalContext.current

    LaunchedEffect(animatedWidth) {
        val widthPx = with(density) { animatedWidth.toPx() }
        onWidthChange(widthPx)
    }

    Box(
        modifier = Modifier
            .scale(deleteScale)
            .alpha(deleteAlpha)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                )
            },
        contentAlignment = if (wasLeftSide) Alignment.TopStart else Alignment.TopEnd
    ) {

        AnimatedVisibility(
            visible = !isExpanded && isFpsActive,
            enter = fadeIn(tween(150)) + expandVertically(tween(150), expandFrom = Alignment.Top),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150), shrinkTowards = Alignment.Top),
            modifier = Modifier.align(if (wasLeftSide) Alignment.TopStart else Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier.padding(top = 52.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(
                            topStart = fpsTopCorner,
                            topEnd = fpsTopCorner,
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        ))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(
                            interactionSource = interactionSourceFps,
                            indication = null
                        ) {
                            GameBubbleStateManager.toggleTool("fps", context, onCollapse)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fpsValue,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(animatedWidth, animatedHeight)
                .clip(RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = mainBottomCorner,
                    bottomEnd = mainBottomCorner
                ))
                .background(bgColor)
                .clickable(
                    interactionSource = interactionSourceMain,
                    indication = null,
                    enabled = !isExpanded
                ) { onExpand() },
            contentAlignment = if (wasLeftSide) Alignment.TopStart else Alignment.TopEnd
        ) {
            AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn(tween(150, delayMillis = 150)),
                exit = fadeOut(tween(150, delayMillis = 0)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_monochrome),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(150, delayMillis = 150)),
                exit = fadeOut(tween(150, delayMillis = 0))
            ) {
                GameBubbleMenu(onCollapse = onCollapse)
            }
        }
    }
}