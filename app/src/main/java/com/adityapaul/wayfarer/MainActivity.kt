package com.adityapaul.wayfarer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adityapaul.wayfarer.ui.map.MapScreen
import com.adityapaul.wayfarer.ui.map.MapViewModel
import com.adityapaul.wayfarer.ui.settings.SettingsScreen
import com.adityapaul.wayfarer.ui.theme.WayfarerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MapViewModel = viewModel()
            val context = LocalContext.current
            val isSystemDark = isSystemInDarkTheme()

            LaunchedEffect(isSystemDark) {
                viewModel.initUserPreferences(context, isSystemDark)
            }

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            WayfarerTheme(
                themeMode = uiState.themeMode,
                palette = uiState.colorPalette,
                useDynamicColor = uiState.useDynamicColor
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MapScreen(viewModel = viewModel)

                    AnimatedVisibility(
                        visible = uiState.isSettingsOpen,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBackClick = { viewModel.toggleSettings() }
                        )
                    }
                }
            }
        }
    }
}
