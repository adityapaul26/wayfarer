package com.adityapaul.wayfarer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
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
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            WayfarerTheme(
                themeMode = uiState.themeMode,
                palette = uiState.colorPalette,
                useDynamicColor = uiState.useDynamicColor
            ) {
                AnimatedContent(
                    targetState = uiState.isSettingsOpen,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenNavigation"
                ) { isSettings ->
                    if (isSettings) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBackClick = { viewModel.toggleSettings() }
                        )
                    } else {
                        MapScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
