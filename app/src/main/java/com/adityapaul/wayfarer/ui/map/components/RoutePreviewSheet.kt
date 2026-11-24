package com.adityapaul.wayfarer.ui.map.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adityapaul.wayfarer.data.route.TransportMode
import com.adityapaul.wayfarer.ui.map.RouteState
import com.adityapaul.wayfarer.ui.theme.TelemetryLarge
import com.adityapaul.wayfarer.ui.theme.TelemetryMedium
import com.adityapaul.wayfarer.ui.theme.TelemetrySmall

@Composable
fun RoutePreviewSheet(
    routeState: RouteState,
    onExpandSheet: () -> Unit,
    onToggleCollapseSheet: () -> Unit,
    onTransportModeSelected: (TransportMode) -> Unit,
    onClearRoute: () -> Unit,
    onStartNavigation: () -> Unit,
    onStopNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCollapsed = routeState.isCollapsed

    Surface(
        shape = if (isCollapsed) CircleShape else RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
    ) {
        if (isCollapsed && routeState.activeRoute != null) {
            // Compact Navigation Pill View (Collapsed Mode)
            val route = routeState.activeRoute
            val displayDuration = if (routeState.navigationState.isNavigating) {
                routeState.navigationState.formattedRemainingDuration()
            } else {
                route.formattedDuration()
            }
            val displayDistance = if (routeState.navigationState.isNavigating) {
                routeState.navigationState.formattedRemainingDistance()
            } else {
                route.formattedDistance()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .clickable { onExpandSheet() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (routeState.navigationState.isNavigating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Navigation",
                            tint = if (routeState.navigationState.isNavigating) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$displayDuration • $displayDistance",
                        style = TelemetryMedium.copy(fontSize = 17.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = route.destinationName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onExpandSheet,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand Route Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Full Expanded Sheet View
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .clickable { onToggleCollapseSheet() }
                        .padding(vertical = 2.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Route Sheet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (routeState.isCalculatingRoute) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Calculating OSRM route...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else routeState.activeRoute?.let { route ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Transport Mode Selector in Route Sheet
                        TransportModeSelectorRow(
                            selectedMode = routeState.transportMode,
                            onSelectMode = onTransportModeSelected
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stacked Expressive Route Cards
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Secondary Alternative Card peeking out behind
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                                    .height(110.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Alternative Route Available",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Primary Selected Route Card (High contrast Acid Lime / Primary)
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val modeIcon = when (routeState.transportMode) {
                                            TransportMode.DRIVING -> Icons.Default.DirectionsCar
                                            TransportMode.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
                                            TransportMode.CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
                                        }

                                        // Badge Icon
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = modeIcon,
                                                    contentDescription = routeState.transportMode.displayName,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Pill Badges for Distance and Route Tag
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${route.formattedDistance()} • Fastest Route",
                                                style = TelemetrySmall,
                                                color = MaterialTheme.colorScheme.onSecondary,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        IconButton(
                                            onClick = onClearRoute,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear Route",
                                                tint = MaterialTheme.colorScheme.onSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Oversized 44.sp Extra-Bold Display Duration
                                    val displayDuration = if (routeState.navigationState.isNavigating) {
                                        routeState.navigationState.formattedRemainingDuration()
                                    } else {
                                        route.formattedDuration()
                                    }

                                    Text(
                                        text = displayDuration,
                                        style = TelemetryLarge.copy(color = MaterialTheme.colorScheme.onSecondary),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )

                                    Text(
                                        text = "${route.originName} → ${route.destinationName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Full-width 60.dp Tall Pill CTA Button
                        Button(
                            onClick = {
                                if (routeState.navigationState.isNavigating) {
                                    onStopNavigation()
                                } else {
                                    onStartNavigation()
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (routeState.navigationState.isNavigating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                contentColor = if (routeState.navigationState.isNavigating) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Icon(
                                imageVector = if (routeState.navigationState.isNavigating) Icons.Default.Close else Icons.Default.Navigation,
                                contentDescription = if (routeState.navigationState.isNavigating) "Stop Navigation" else "Start Navigation",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (routeState.navigationState.isNavigating) "Stop Live Navigation" else "Start Live Navigation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
