package com.fedeveloper95.games.elements.MainActivity

import android.view.KeyEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.fedeveloper95.games.elements.ui.AnimatedPlayButton
import com.fedeveloper95.games.elements.ui.GameIconDisplay
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import com.fedeveloper95.games.services.mainactivity.GameApp
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalGamePager(
    games: List<GameApp>,
    onLaunch: (GameApp) -> Unit,
    onLongClick: (GameApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { games.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 24.dp),
        pageSpacing = 16.dp,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) { page ->
        val game = games[page]
        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val scale = lerp(1f, 0.9f, pageOffset.absoluteValue.coerceIn(0f, 1f))
        val alpha = lerp(1f, 0.6f, pageOffset.absoluteValue.coerceIn(0f, 1f))

        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            HorizontalGameCard(
                game = game,
                onLaunch = { onLaunch(game) },
                onLongClick = { onLongClick(game) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalGameCard(
    game: GameApp,
    onLaunch: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderModifier = if (isFocused) Modifier.border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(32.dp)) else Modifier

    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.72f)
            .clip(RoundedCornerShape(32.dp))
            .then(borderModifier)
            .onPreviewKeyEvent { event ->
                if (event.key.nativeKeyCode == KeyEvent.KEYCODE_BUTTON_START || event.key.nativeKeyCode == KeyEvent.KEYCODE_MENU) {
                    if (event.type == KeyEventType.KeyUp) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                    true
                } else {
                    false
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onLaunch,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GameIconDisplay(
                game = game,
                modifier = Modifier
                    .size(140.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = game.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFlex
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedPlayButton(onClick = onLaunch)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridGameCard(
    game: GameApp,
    columns: Int,
    onLaunch: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderModifier = if (isFocused) Modifier.border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)) else Modifier

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .then(borderModifier)
            .onPreviewKeyEvent { event ->
                if (event.key.nativeKeyCode == KeyEvent.KEYCODE_BUTTON_START || event.key.nativeKeyCode == KeyEvent.KEYCODE_MENU) {
                    if (event.type == KeyEventType.KeyUp) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                    true
                } else {
                    false
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onLaunch,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (columns <= 2) 16.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val iconFraction = if (columns <= 2) 0.55f else 0.8f
                GameIconDisplay(
                    game = game,
                    modifier = Modifier
                        .fillMaxWidth(iconFraction)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                )

                if (columns <= 2) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameListItem(
    game: GameApp,
    isSingle: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onLaunch: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val count = if (isSingle) 1 else if (isFirst || isLast) 2 else 3
    val index = if (isSingle) 0 else if (isFirst) 0 else if (isLast) count - 1 else 1

    var isLongPress by remember { mutableStateOf(false) }
    var baseModifier = modifier
        .then(if (count == 1) Modifier.clip(RoundedCornerShape(20.dp)) else Modifier)

    baseModifier = baseModifier
        .onPreviewKeyEvent { event ->
            if (event.key.nativeKeyCode == KeyEvent.KEYCODE_BUTTON_START || event.key.nativeKeyCode == KeyEvent.KEYCODE_MENU) {
                if (event.type == KeyEventType.KeyUp) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
                true
            } else {
                false
            }
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    isLongPress = false
                    val upOrCancel = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        var event = awaitPointerEvent(PointerEventPass.Initial)
                        while (event.changes.any { it.pressed }) {
                            event = awaitPointerEvent(PointerEventPass.Initial)
                        }
                        event
                    }
                    if (upOrCancel == null) {
                        isLongPress = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                }
            }
        }

    SegmentedListItem(
        selected = false,
        onClick = {
            if (!isLongPress) {
                onLaunch()
            }
        },
        modifier = baseModifier,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 72.dp)
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameIconDisplay(
                    game = game,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = GoogleSansFlex
                    )
                }
                AnimatedPlayButton(onClick = {
                    if (!isLongPress) {
                        onLaunch()
                    }
                })
            }
        }
    )
}