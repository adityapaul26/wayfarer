package com.adityapaul.wayfarer.ui.map.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

import com.adityapaul.wayfarer.data.settings.SpeedUnit

@Composable
fun MapControlsOverlay(
    isLocationPermissionGranted: Boolean,
    isSettingsOpen: Boolean,
    onRecenterLocation: () -> Unit,
    onRequestPermission: () -> Unit,
    onToggleSettings: () -> Unit,
    modifier: Modifier = Modifier,
    showSpeedometer: Boolean = false,
    currentSpeedFormatted: String = "0",
    speedProgress: Float = 0f,
    speedUnit: SpeedUnit = SpeedUnit.KMH
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (showSpeedometer) {
            SpeedometerPill(
                currentSpeedFormatted = currentSpeedFormatted,
                speedProgress = speedProgress,
                speedUnit = speedUnit
            )
        }
        // Primary "Re-center / My Location" Button: Bold Squircle (18.dp) filled with vibrant secondary accent & dark high-contrast icon tint
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable {
                    if (isLocationPermissionGranted) {
                        onRecenterLocation()
                    } else {
                        onRequestPermission()
                    }
                }
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Secondary Controls Cluster Capsule (CircleShape, surfaceContainerHigh, 1.5.dp outlineVariant border)
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSettingsOpen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onToggleSettings() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (isSettingsOpen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
