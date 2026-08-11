package com.fedeveloper95.games.elements.MainActivity.Edit

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.AppIcon
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import com.fedeveloper95.games.services.mainactivity.GameApp
import com.fedeveloper95.games.services.mainactivity.GameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditAppBottomSheet(
    game: GameApp,
    onDismiss: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var customName by remember { mutableStateOf(game.customName ?: game.name) }
    var customIconUri by remember { mutableStateOf<String?>(game.customIconUri) }
    var isFavorite by remember { mutableStateOf(game.isFavorite) }

    var showAppIconSelector by remember { mutableStateOf(false) }
    var showBuiltInIconSelector by remember { mutableStateOf(false) }

    var selectedOption by remember {
        mutableIntStateOf(
            when {
                customIconUri?.startsWith("app://") == true -> 0
                customIconUri?.startsWith("builtin://") == true -> 1
                customIconUri != null -> 2
                else -> -1
            }
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            customIconUri = uri.toString()
            selectedOption = 2
        } else {
            if (customIconUri == null) selectedOption = -1
        }
    }

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

    val favInteractionSource = remember { MutableInteractionSource() }
    val isFavPressed by favInteractionSource.collectIsPressedAsState()
    val favCornerPercent by animateIntAsState(
        targetValue = if (isFavPressed) 20 else 50,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "favBtnMorph"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = (game.customName ?: game.name),
                fontFamily = GoogleSansFlex,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val isBuiltIn = customIconUri?.startsWith("builtin://") == true

            if (isBuiltIn) {
                val uriStr = customIconUri!!
                val name = uriStr.substringAfter("builtin://").substringBefore("#")
                val colorHex = uriStr.substringAfter("#").toIntOrNull() ?: 0
                val color = if (colorHex != 0) Color(colorHex) else MaterialTheme.colorScheme.primary

                val iconVector = when(name) {
                    "star" -> Icons.Rounded.Star
                    "controller" -> Icons.Rounded.SportsEsports
                    "bolt" -> Icons.Rounded.Bolt
                    "heart" -> Icons.Rounded.Favorite
                    "gamepad" -> Icons.Rounded.Gamepad
                    "videogame" -> Icons.Rounded.VideogameAsset
                    "toy" -> Icons.Rounded.SmartToy
                    "puzzle" -> Icons.Rounded.Extension
                    "dice" -> Icons.Rounded.Casino
                    "rocket" -> Icons.Rounded.RocketLaunch
                    "trophy" -> Icons.Rounded.EmojiEvents
                    "explore" -> Icons.Rounded.Explore
                    else -> Icons.Rounded.Gamepad
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = color.darken(0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                }
            } else {
                AppIcon(
                    packageName = game.packageName,
                    customIconUri = customIconUri,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val options = listOf(
                stringResource(R.string.edit_app_icons),
                stringResource(R.string.edit_builtin_icons),
                stringResource(R.string.edit_gallery)
            )

            val icons = listOf(Icons.Outlined.Apps, Icons.Outlined.Category, Icons.Outlined.Image)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween, Alignment.CenterHorizontally),
            ) {
                options.forEachIndexed { index, label ->
                    val shape = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes().shape
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes().shape
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes().shape
                    }

                    ToggleButton(
                        checked = selectedOption == index,
                        onCheckedChange = {
                            selectedOption = index
                            when (index) {
                                0 -> showAppIconSelector = true
                                1 -> showBuiltInIconSelector = true
                                2 -> imagePickerLauncher.launch("image/*")
                            }
                        },
                        shapes = ToggleButtonDefaults.shapes(shape = shape),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { role = Role.RadioButton }
                    ) {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = label,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text(
                            text = label,
                            fontFamily = GoogleSansFlex,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val isIconChanged = customIconUri != null
            val isTitleChanged = customName != game.name

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween, Alignment.CenterHorizontally),
            ) {
                Button(
                    onClick = {
                        customIconUri = null
                        selectedOption = -1
                    },
                    enabled = isIconChanged,
                    modifier = Modifier.weight(1f),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShapes().shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.edit_restore_icon),
                        fontFamily = GoogleSansFlex,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        customName = game.name
                    },
                    enabled = isTitleChanged,
                    modifier = Modifier.weight(1f),
                    shape = ButtonGroupDefaults.connectedTrailingButtonShapes().shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.edit_restore_title),
                        fontFamily = GoogleSansFlex,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = customName,
                onValueChange = { customName = it },
                label = {
                    Text(
                        text = stringResource(R.string.edit_app_name),
                        fontFamily = GoogleSansFlex
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    val newFavState = !isFavorite
                    isFavorite = newFavState
                    viewModel.updateCustomGameData(context, game.packageName, customName, customIconUri, newFavState)
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(favCornerPercent),
                interactionSource = favInteractionSource,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFavorite) stringResource(R.string.remove_favorite) else stringResource(R.string.add_favorite),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.updateCustomGameData(context, game.packageName, customName, customIconUri, isFavorite)
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(cornerPercent),
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.save),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    if (showAppIconSelector) {
        AppIconSelector(
            onDismiss = {
                showAppIconSelector = false
                if (customIconUri == null) selectedOption = -1
            },
            onAppSelected = { selectedPkg ->
                customIconUri = "app://$selectedPkg"
                showAppIconSelector = false
            }
        )
    }

    if (showBuiltInIconSelector) {
        val currentIconName = if (customIconUri?.startsWith("builtin://") == true) {
            customIconUri!!.substringAfter("builtin://").substringBefore("#")
        } else {
            "default"
        }

        val currentColor = if (customIconUri?.startsWith("builtin://") == true) {
            customIconUri!!.substringAfter("#").toIntOrNull() ?: 0
        } else {
            0
        }

        IconSelector(
            currentIcon = currentIconName,
            currentColor = currentColor,
            onDismiss = {
                showBuiltInIconSelector = false
                if (customIconUri == null) selectedOption = -1
            },
            onIconSelected = { name, color ->
                customIconUri = "builtin://$name#$color"
                showBuiltInIconSelector = false
            }
        )
    }
}

private fun Color.darken(factor: Float = 0.8f): Color {
    return Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}