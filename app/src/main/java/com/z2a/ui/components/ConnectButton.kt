package com.z2a.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.z2a.data.models.ConnectionState
import com.z2a.ui.theme.Z2aGreen
import com.z2a.ui.theme.Z2aGreenGlow
import com.z2a.ui.theme.Z2aOnSurfaceDim
import com.z2a.ui.theme.Z2aRed
import com.z2a.ui.theme.Z2aRedGlow
import com.z2a.ui.theme.Z2aSurfaceVariant
import com.z2a.ui.theme.Z2aAmber

@Composable
fun ConnectButton(
    state: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val ringColor by animateColorAsState(
        targetValue = when (state) {
            ConnectionState.CONNECTED -> Z2aGreen
            ConnectionState.CONNECTING -> Z2aAmber
            ConnectionState.ERROR -> Z2aRed
            ConnectionState.DISCONNECTED -> Z2aOnSurfaceDim
        },
        animationSpec = tween(500),
        label = "ringColor"
    )

    val glowColor = when (state) {
        ConnectionState.CONNECTED -> Z2aGreenGlow
        ConnectionState.ERROR -> Z2aRedGlow
        else -> Color.Transparent
    }

    val iconColor by animateColorAsState(
        targetValue = when (state) {
            ConnectionState.CONNECTED -> Z2aGreen
            ConnectionState.CONNECTING -> Z2aAmber
            ConnectionState.ERROR -> Z2aRed
            ConnectionState.DISCONNECTED -> Z2aOnSurfaceDim
        },
        animationSpec = tween(500),
        label = "iconColor"
    )

    Box(
        modifier = modifier
            .size(200.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = size.minDimension / 2 - 8.dp.toPx()
            val innerRadius = outerRadius - 20.dp.toPx()

            // Outer glow (pulsing when connected)
            if (state == ConnectionState.CONNECTED || state == ConnectionState.ERROR) {
                drawCircle(
                    color = glowColor.copy(alpha = glowAlpha * 0.5f),
                    radius = outerRadius + 16.dp.toPx(),
                    center = center
                )
            }

            // Background circle
            drawCircle(
                color = Z2aSurfaceVariant,
                radius = innerRadius,
                center = center
            )

            // Outer ring
            if (state == ConnectionState.CONNECTING) {
                // Rotating gradient ring when connecting
                rotate(rotation) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Z2aAmber.copy(alpha = 0.3f),
                                Z2aAmber,
                                Z2aAmber.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
                    )
                }
            } else {
                drawCircle(
                    color = ringColor.copy(alpha = if (state == ConnectionState.CONNECTED) glowAlpha else 0.5f),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Power icon
            val iconSize = innerRadius * 0.4f
            val strokeWidth = 4.dp.toPx()

            // Vertical line (top of power icon)
            drawLine(
                color = iconColor,
                start = Offset(center.x, center.y - iconSize * 0.9f),
                end = Offset(center.x, center.y - iconSize * 0.2f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Arc (bottom of power icon)
            drawArc(
                color = iconColor,
                startAngle = -60f,
                sweepAngle = -240f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - iconSize, center.y - iconSize * 0.6f),
                size = androidx.compose.ui.geometry.Size(iconSize * 2, iconSize * 2)
            )
        }
    }
}
