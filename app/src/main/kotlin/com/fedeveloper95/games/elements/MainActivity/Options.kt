package com.fedeveloper95.games.elements.MainActivity

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.GameIconDisplay
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import com.fedeveloper95.games.services.mainactivity.GameApp
import com.fedeveloper95.games.services.mainactivity.formatPlayTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MoreBottomSheet(
    game: GameApp,
    gamesCount: Int,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onStoreClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }
    val showLaunchCount = prefs.getBoolean("pref_show_launch_count", true)
    val showPlayTime = prefs.getBoolean("pref_show_play_time", true)
    val showStats = showLaunchCount || showPlayTime
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val deleteColor = if (isDark) Color(0xFFF2B8B5) else Color(0xFFB3261E)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.statusBarsPadding(),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 90.dp)
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
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
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val count = if (showStats) 4 else 3
                var currentIndex = 0

                if (showStats) {
                    SegmentedListItem(
                        selected = false,
                        onClick = {},
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shapes = ListItemDefaults.segmentedShapes(index = currentIndex++, count = count),
                        trailingContent = {
                            Text(
                                text = buildString {
                                    if (showLaunchCount) append("${game.launchCount} plays")
                                    if (showLaunchCount && showPlayTime) append("   ")
                                    if (showPlayTime) append(formatPlayTime(game.totalPlayTime))
                                },
                                fontFamily = GoogleSansFlex,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        content = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(stringResource(R.string.stats), fontFamily = GoogleSansFlex, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    )
                }

                SegmentedListItem(
                    selected = false,
                    onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onEditClick() } },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shapes = ListItemDefaults.segmentedShapes(index = currentIndex++, count = count),
                    content = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(stringResource(R.string.edit_mode_title), fontFamily = GoogleSansFlex, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                )

                SegmentedListItem(
                    selected = false,
                    onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onStoreClick() } },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shapes = ListItemDefaults.segmentedShapes(index = currentIndex++, count = count),
                    content = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(stringResource(R.string.open_in_store), fontFamily = GoogleSansFlex, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                )

                SegmentedListItem(
                    selected = false,
                    onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDeleteClick() } },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shapes = ListItemDefaults.segmentedShapes(index = currentIndex, count = count),
                    content = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, tint = deleteColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(stringResource(R.string.remove), fontFamily = GoogleSansFlex, color = deleteColor, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                )
            }

            if (gamesCount > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween, Alignment.CenterHorizontally),
                ) {
                    Button(
                        onClick = onMoveUp,
                        modifier = Modifier.weight(1f),
                        shape = ButtonGroupDefaults.connectedLeadingButtonShapes().shape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.move_up),
                            fontFamily = GoogleSansFlex,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = onMoveDown,
                        modifier = Modifier.weight(1f),
                        shape = ButtonGroupDefaults.connectedTrailingButtonShapes().shape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.move_down),
                            fontFamily = GoogleSansFlex,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}