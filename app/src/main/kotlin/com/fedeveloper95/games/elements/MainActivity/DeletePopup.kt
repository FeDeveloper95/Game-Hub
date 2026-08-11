package com.fedeveloper95.games.elements.MainActivity

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import com.fedeveloper95.games.services.mainactivity.GameApp

@Composable
fun DeletePopup(
    game: GameApp,
    onConfirm: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit
) {
    val confirmInteractionSource = remember { MutableInteractionSource() }
    val isConfirmPressed by confirmInteractionSource.collectIsPressedAsState()
    val confirmCorner by animateIntAsState(
        targetValue = if (isConfirmPressed) 20 else 50,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "confirmCorner"
    )

    val uninstallInteractionSource = remember { MutableInteractionSource() }
    val isUninstallPressed by uninstallInteractionSource.collectIsPressedAsState()
    val uninstallCorner by animateIntAsState(
        targetValue = if (isUninstallPressed) 20 else 50,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "uninstallCorner"
    )

    val dismissInteractionSource = remember { MutableInteractionSource() }
    val isDismissPressed by dismissInteractionSource.collectIsPressedAsState()
    val dismissCorner by animateIntAsState(
        targetValue = if (isDismissPressed) 20 else 50,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dismissCorner"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(R.string.remove_game_title),
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.remove_game_desc, game.name),
                fontFamily = GoogleSansFlex,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 6.dp,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onUninstall,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(uninstallCorner),
                    interactionSource = uninstallInteractionSource
                ) {
                    Text(
                        text = stringResource(R.string.uninstall),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(confirmCorner),
                    interactionSource = confirmInteractionSource
                ) {
                    Text(
                        text = stringResource(R.string.remove),
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(dismissCorner),
                interactionSource = dismissInteractionSource
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}