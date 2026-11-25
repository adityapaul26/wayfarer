package com.adityapaul.wayfarer.ui.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adityapaul.wayfarer.ui.map.components.ActiveNavigationOverlay
import com.adityapaul.wayfarer.ui.map.components.MapControlsOverlay
import com.adityapaul.wayfarer.ui.map.components.RoutePreviewSheet
import com.adityapaul.wayfarer.ui.map.components.SearchOverlay

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchState = uiState.searchState
    val routeState = uiState.routeState
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(isGranted, context)
    }

    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()

    LaunchedEffect(isSystemDark) {
        viewModel.initUserPreferences(context, isSystemDark)
        viewModel.checkAndStartLocation(context)
        viewModel.initRecentSearchRepository(context)
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Full Screen Vector Map Canvas
            MapViewContainer(
                mapState = uiState,
                onCameraMoved = { lat, lng, zoom ->
                    viewModel.onCameraMoved(lat, lng, zoom)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Search Overlay (Search bar, multi-step route selector, search results / recent dropdown)
            SearchOverlay(
                searchState = searchState,
                routeState = routeState,
                hasUserLocation = uiState.userLocation != null,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onSubmitSearch = { viewModel.submitSearch() },
                onClearSearch = { viewModel.clearSearch() },
                onStartRoutePlanning = { viewModel.startRoutePlanning() },
                onCancelRoutePlanning = { viewModel.cancelRoutePlanning() },
                onSwapOriginAndDestination = { viewModel.swapOriginAndDestination() },
                onClearOrigin = { viewModel.clearOrigin() },
                onClearDestination = { viewModel.clearDestination() },
                onSelectOriginFromCurrentLocation = { viewModel.selectOriginFromCurrentLocation() },
                onTransportModeSelected = { viewModel.setTransportMode(it) },
                onSearchResultSelected = { viewModel.onSearchResultSelected(it) },
                onClearRecentSearches = { viewModel.clearRecentSearches() },
                onRemoveRecentSearch = { viewModel.removeRecentSearch(it) },
                onExpandSearchPanel = { viewModel.expandSearchPanel() },
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
            )

            // Active Navigation Overlay (Top Turn-by-Turn Banner & Speedometer)
            ActiveNavigationOverlay(
                activeRoute = routeState.activeRoute,
                navigationState = routeState.navigationState,
                transportMode = routeState.transportMode,
                currentSpeedFormatted = uiState.currentSpeedFormatted,
                speedProgress = uiState.speedProgress,
                speedUnit = uiState.speedUnit,
                onStopNavigation = { viewModel.stopLiveNavigation() },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            val hasBottomSheet = uiState.isGeekLogsOpen ||
                    routeState.isCalculatingRoute ||
                    routeState.activeRoute != null ||
                    uiState.selectedPinLocation != null

            val controlsBottomPadding = when {
                !hasBottomSheet -> 48.dp
                routeState.isCollapsed -> 100.dp
                else -> 260.dp
            }

            // Floating Map Controls (Recenter to GPS location & Settings FAB - Bottom Right Corner)
            MapControlsOverlay(
                isLocationPermissionGranted = uiState.isLocationPermissionGranted,
                isSettingsOpen = uiState.isSettingsOpen,
                onRecenterLocation = { viewModel.recenterToUserLocation() },
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onToggleSettings = { viewModel.toggleSettings() },
                showSpeedometer = uiState.showSpeedometerInFreeDrive && !routeState.navigationState.isNavigating,
                currentSpeedFormatted = uiState.currentSpeedFormatted,
                speedProgress = uiState.speedProgress,
                speedUnit = uiState.speedUnit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = controlsBottomPadding)
            )

            // Bottom Sheets: Logs for Geeks, Route Preview Sheet, or Selected Search Place Pin Sheet
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 38.dp)
            ) {
                when {
                    uiState.isGeekLogsOpen -> {
                        GeekLogsWindow(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    routeState.isCalculatingRoute || routeState.activeRoute != null -> {
                        RoutePreviewSheet(
                            routeState = routeState,
                            onExpandSheet = { viewModel.expandBottomSheet() },
                            onToggleCollapseSheet = { viewModel.toggleBottomSheetCollapse() },
                            onTransportModeSelected = { viewModel.setTransportMode(it) },
                            onClearRoute = { viewModel.clearRoute() },
                            onStartNavigation = { viewModel.startLiveNavigation() },
                            onStopNavigation = { viewModel.stopLiveNavigation() }
                        )
                    }

                    uiState.selectedPinLocation != null -> {
                        uiState.selectedPinLocation?.let { selected ->
                            Surface(
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 4.dp,
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(4.dp)
                                    ) {}

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = "Place",
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = selected.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                selected.category?.let { cat ->
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer
                                                    ) {
                                                        Text(
                                                            text = cat,
                                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = selected.address,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }

                                        IconButton(onClick = { viewModel.clearSelectedPin() }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close Pin",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            viewModel.startRoutePlanning()
                                            viewModel.selectDestination(selected)
                                        },
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Directions,
                                            contentDescription = "Route to Place",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Get Route Directions",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun GeekLogsWindow(
    uiState: MapState,
    viewModel: MapViewModel
) {
    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
        color = Color(0xFF0F172A),
        tonalElevation = 10.dp,
        shadowElevation = 12.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFF334155),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF334155),
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                ) {}
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Logs for Geeks",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Logs for Geeks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Real-time Developer Telemetry Stream",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFF38BDF8)
                    )
                }
                if (uiState.geekLogs.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearGeekLogs() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Logs",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { viewModel.closeGeekLogs() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Window",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.geekLogs.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Text(
                        text = "> Waiting for HTTP network, GPS telemetry, or OSRM route events...",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    items(uiState.geekLogs) { log ->
                        GeekLogLine(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun GeekLogLine(log: GeekLogEntry) {
    val tagColor = when (log.tag) {
        GeekLogType.NET -> Color(0xFF38BDF8)
        GeekLogType.GPS -> Color(0xFF34D399)
        GeekLogType.MAP -> Color(0xFFA78BFA)
        GeekLogType.ROUTE -> Color(0xFFFBBF24)
        GeekLogType.SYS -> Color(0xFFF472B6)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = tagColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "[${log.tag.name}]",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = tagColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = log.timestamp,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                ),
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = log.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFF8FAFC),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = log.details,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
