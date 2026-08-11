package com.fedeveloper95.games.elements.MainActivity.Edit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.GoogleSansFlex

@Composable
fun ExpressiveTextButton(
    onClick: () -> Unit,
    text: String,
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

    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerPercent),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun IconSelector(
    currentIcon: String,
    currentColor: Int,
    onIconSelected: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIcon by remember { mutableStateOf(currentIcon) }
    var selectedColor by remember { mutableIntStateOf(currentColor) }

    val icons = listOf(
        "default" to Icons.Rounded.Gamepad,
        "controller" to Icons.Rounded.SportsEsports,
        "bolt" to Icons.Rounded.Bolt,
        "heart" to Icons.Rounded.Favorite,
        "star" to Icons.Rounded.Star,
        "videogame" to Icons.Rounded.VideogameAsset,
        "toy" to Icons.Rounded.SmartToy,
        "puzzle" to Icons.Rounded.Extension,
        "dice" to Icons.Rounded.Casino,
        "rocket" to Icons.Rounded.RocketLaunch,
        "trophy" to Icons.Rounded.EmojiEvents,
        "explore" to Icons.Rounded.Explore
    )

    val colorOptions = listOf(
        0,
        android.graphics.Color.parseColor("#ffb3b6"),
        android.graphics.Color.parseColor("#ffb869"),
        android.graphics.Color.parseColor("#e8c349"),
        android.graphics.Color.parseColor("#a0d57b"),
        android.graphics.Color.parseColor("#97cbff"),
        android.graphics.Color.parseColor("#b6c6ed"),
        android.graphics.Color.parseColor("#cabeff"),
        android.graphics.Color.parseColor("#f7adfd")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        ),
        modifier = Modifier.widthIn(max = 400.dp),
        title = {
            Text(
                text = stringResource(R.string.edit_app_icon_title),
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    val rows = icons.chunked(4)
                    rows.forEachIndexed { index, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for ((name, icon) in rowItems) {
                                val isSelected = selectedIcon == name
                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()

                                val cornerPercent by animateIntAsState(
                                    targetValue = if (isPressed) 20 else 50,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "iconCorner"
                                )

                                val baseColor = MaterialTheme.colorScheme.surfaceVariant
                                val selectedColorBg = MaterialTheme.colorScheme.primaryContainer

                                val containerColor by animateColorAsState(
                                    targetValue = if (isSelected) selectedColorBg else baseColor,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "iconContainerColor"
                                )

                                val iconTint by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "iconTintColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(percent = cornerPercent))
                                        .background(containerColor)
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) { selectedIcon = name },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = name,
                                        tint = iconTint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            val remaining = 4 - rowItems.size
                            if (remaining > 0) {
                                repeat(remaining) {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                        if (index < rows.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorOptions.forEach { colorInt ->
                        val isSelected = selectedColor == colorInt
                        val isDynamic = colorInt == 0

                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        val targetCorner = when {
                            isPressed -> 15
                            isSelected -> 35
                            else -> 50
                        }

                        val cornerPercent by animateIntAsState(
                            targetValue = targetCorner,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "colorCorner"
                        )

                        val backgroundColor = if (isDynamic) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            Color(colorInt)
                        }

                        val borderWidth = if (isSelected) 3.dp else 0.dp
                        val borderColor = if (isDynamic) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color(
                                red = backgroundColor.red * 0.7f,
                                green = backgroundColor.green * 0.7f,
                                blue = backgroundColor.blue * 0.7f,
                                alpha = backgroundColor.alpha
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(percent = cornerPercent))
                                .background(backgroundColor)
                                .then(
                                    if (isDynamic && isSelected) Modifier.background(
                                        MaterialTheme.colorScheme.primaryContainer
                                    ) else Modifier
                                )
                                .border(
                                    borderWidth,
                                    borderColor,
                                    RoundedCornerShape(percent = cornerPercent)
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { selectedColor = colorInt },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDynamic) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            ExpressiveTextButton(
                onClick = { onIconSelected(selectedIcon, selectedColor) },
                text = stringResource(R.string.save)
            )
        },
        dismissButton = {
            ExpressiveTextButton(
                onClick = onDismiss,
                text = stringResource(R.string.cancel)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(32.dp)
    )
}