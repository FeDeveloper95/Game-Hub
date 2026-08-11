package com.fedeveloper95.games.elements.GameBubble

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fedeveloper95.games.R

@Composable
fun DismissAreaOverlay(isDragging: Boolean, isExpanded: Boolean, isHovering: Boolean, isDeleting: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = (isDragging || isDeleting) && !isExpanded,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200))
        ) {
            val scale by animateFloatAsState(
                targetValue = if (isDeleting) 0f else if (isHovering) 1.2f else 1f,
                animationSpec = tween(if (isDeleting) 300 else 200),
                label = "scale"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isDeleting) 0f else 1f,
                animationSpec = tween(if (isDeleting) 300 else 200),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .scale(scale)
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_eight_sided_cookie),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 1f),
                    modifier = Modifier
                        .size(112.dp)
                        .rotate(rotation)
                )
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 1f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}