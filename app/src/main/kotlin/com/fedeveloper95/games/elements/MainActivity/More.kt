package com.fedeveloper95.games.elements.MainActivity

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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fedeveloper95.games.R
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
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                val count = 4
                SegmentedListItem(
                    selected = false,
                    onClick = {},
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = count),
                    trailingContent = {
                        Text(
                            "${game.launchCount} plays   ${formatPlayTime(game.totalPlayTime)}",
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
                SegmentedListItem(
                    selected = false,
                    onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onEditClick() } },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = count),
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
                    shapes = ListItemDefaults.segmentedShapes(index = 2, count = count),
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
                    shapes = ListItemDefaults.segmentedShapes(index = 3, count = count),
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