package com.fedeveloper95.games.elements.MainActivity

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderModifier = if (isFocused) Modifier.border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(32.dp)) else Modifier

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.72f)
            .clip(RoundedCornerShape(32.dp))
            .then(borderModifier)
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
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderModifier = if (isFocused) Modifier.border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)) else Modifier

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .then(borderModifier)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameListItem(
    game: GameApp,
    isSingle: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onLaunch: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val topRound = if (isSingle || isFirst) 28.dp else 4.dp
    val bottomRound = if (isSingle || isLast) 28.dp else 4.dp

    val topStart by animateDpAsState(targetValue = if (isPressed) 28.dp else topRound, animationSpec = tween(200), label = "")
    val topEnd by animateDpAsState(targetValue = if (isPressed) 28.dp else topRound, animationSpec = tween(200), label = "")
    val bottomStart by animateDpAsState(targetValue = if (isPressed) 28.dp else bottomRound, animationSpec = tween(200), label = "")
    val bottomEnd by animateDpAsState(targetValue = if (isPressed) 28.dp else bottomRound, animationSpec = tween(200), label = "")

    val shape = RoundedCornerShape(topStart, topEnd, bottomStart, bottomEnd)
    val borderModifier = if (isFocused) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape) else Modifier

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(borderModifier)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onLaunch,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 104.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameIconDisplay(
                game = game,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(24.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = GoogleSansFlex
                )
            }

            AnimatedPlayButton(onClick = onLaunch)
        }
    }
}