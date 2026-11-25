package com.adityapaul.wayfarer.ui.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adityapaul.wayfarer.data.route.RouteResult
import com.adityapaul.wayfarer.data.route.TransportMode
import com.adityapaul.wayfarer.ui.map.LiveNavigationState
import com.adityapaul.wayfarer.ui.theme.TelemetryMedium
import com.adityapaul.wayfarer.ui.theme.TelemetrySmall
import kotlin.math.sin

import com.adityapaul.wayfarer.data.settings.SpeedUnit

@Composable
fun ActiveNavigationOverlay(
    activeRoute: RouteResult?,
    navigationState: LiveNavigationState,
    transportMode: TransportMode,
    currentSpeedFormatted: String,
    speedProgress: Float,
    speedUnit: SpeedUnit,
    onStopNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = navigationState.isNavigating && activeRoute != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        if (activeRoute != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top Turn-by-Turn Maneuver Banner (28.dp rounded card, deep violet / primaryContainer)
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        val modeIcon = when (transportMode) {
                            TransportMode.DRIVING -> Icons.Default.DirectionsCar
                            TransportMode.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
                            TransportMode.CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
                        }

                        // Oversized 48.dp turn arrow / transport icon badge with high contrast background
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = modeIcon,
                                    contentDescription = transportMode.displayName,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = navigationState.formattedRemainingDuration(),
                                    style = TelemetryMedium.copy(fontSize = 22.sp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = navigationState.formattedRemainingDistance(),
                                        style = TelemetrySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Navigating to ${activeRoute.destinationName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (navigationState.isOffRoute) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Off Route",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Re-routing",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onStopNavigation,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Navigation",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Floating Speedometer Pill during Navigation
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    SpeedometerPill(
                        currentSpeedFormatted = currentSpeedFormatted,
                        speedProgress = speedProgress,
                        speedUnit = speedUnit
                    )
                }

                // Bottom Expressive Wavy Progress Bar Card
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "TRIP PROGRESS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "${navigationState.formattedRemainingDuration()} left (${navigationState.formattedRemainingDistance()})",
                                style = TelemetrySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Calculate progress ratio (0.0 to 1.0)
                        val totalDist = activeRoute.distanceMeters.coerceAtLeast(1.0)
                        val remainingDist = navigationState.remainingDistanceMeters.coerceIn(0.0, totalDist)
                        val progressRatio = ((totalDist - remainingDist) / totalDist).toFloat().coerceIn(0f, 1f)

                        // Custom Wavy/Squiggly Progress Canvas (Android 14/15 Expressive Media Scrubber Style)
                        ExpressiveWavyProgressBar(
                            progress = progressRatio,
                            waveColor = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                            thumbColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveWavyProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.outlineVariant,
    thumbColor: Color = MaterialTheme.colorScheme.secondary
) {
    val phaseState = rememberInfiniteTransition(label = "WavePhase")
    val wavePhase by phaseState.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveAnimation"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(26.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val progressX = (width * progress.coerceIn(0f, 1f))

        val waveAmplitude = 3.5.dp.toPx()
        val waveFrequency = 18.dp.toPx()

        // Inactive track (remaining path)
        drawLine(
            color = trackColor,
            start = Offset(progressX, centerY),
            end = Offset(width, centerY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Active sine wave path (traveled path)
        if (progressX > 0) {
            val wavePath = Path().apply {
                moveTo(0f, centerY)
                var x = 0f
                while (x <= progressX) {
                    val y = centerY + sin((x / waveFrequency) * 2f * Math.PI.toFloat() + wavePhase) * waveAmplitude
                    lineTo(x, y)
                    x += 2f
                }
            }

            drawPath(
                path = wavePath,
                color = waveColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Active location thumb at progress position
        val thumbY = centerY + sin((progressX / waveFrequency) * 2f * Math.PI.toFloat() + wavePhase) * waveAmplitude
        drawCircle(
            color = thumbColor,
            radius = 8.dp.toPx(),
            center = Offset(progressX, thumbY)
        )
        drawCircle(
            color = Color(0xFF120D18),
            radius = 3.5.dp.toPx(),
            center = Offset(progressX, thumbY)
        )
    }
}
