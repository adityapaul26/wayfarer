package com.adityapaul.wayfarer.ui.map.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adityapaul.wayfarer.data.settings.SpeedUnit
import com.adityapaul.wayfarer.ui.theme.MonoFontFamily

@Composable
fun SpeedometerPill(
    currentSpeedFormatted: String,
    speedProgress: Float,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    var isCompactMode by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = speedProgress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "SpeedProgressAnimation"
    )

    // Dynamic Arc Color: Acid Lime -> Warm Coral at high speed (> 80%)
    val acidLime = MaterialTheme.colorScheme.secondary
    val warmCoral = MaterialTheme.colorScheme.error
    val targetColor = if (animatedProgress > 0.8f) {
        val fraction = ((animatedProgress - 0.8f) / 0.2f).coerceIn(0f, 1f)
        lerp(acidLime, warmCoral, fraction)
    } else {
        acidLime
    }

    val animatedArcColor by animateColorAsState(
        targetValue = targetColor,
        label = "SpeedArcColorAnimation"
    )

    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Surface(
        shape = if (isCompactMode) CircleShape else RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 6.dp,
        tonalElevation = 6.dp,
        modifier = modifier
            .clip(if (isCompactMode) CircleShape else RoundedCornerShape(28.dp))
            .clickable { isCompactMode = !isCompactMode }
    ) {
        if (isCompactMode) {
            // Compact Digital Pill Mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Speedometer",
                    tint = animatedArcColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$currentSpeedFormatted ${speedUnit.label}",
                    style = TextStyle(
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Radial Gauge Mode (88.dp widget)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(88.dp)
            ) {
                // 240° Canvas Radial Arc Scrubber
                Canvas(modifier = Modifier.size(76.dp)) {
                    val strokeWidth = 5.5.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    val startAngle = 150f
                    val totalSweep = 240f
                    val activeSweep = totalSweep * animatedProgress

                    // Inactive Track Arc
                    drawArc(
                        color = trackColor,
                        startAngle = startAngle,
                        sweepAngle = totalSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Progress Arc
                    if (activeSweep > 0f) {
                        drawArc(
                            color = animatedArcColor,
                            startAngle = startAngle,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Digital Readout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentSpeedFormatted,
                        style = TextStyle(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = (-1.0).sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = speedUnit.label,
                        style = TextStyle(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
