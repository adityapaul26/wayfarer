package com.adityapaul.wayfarer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adityapaul.wayfarer.ui.map.MapScreen
import com.adityapaul.wayfarer.ui.map.MapViewModel
import com.adityapaul.wayfarer.ui.theme.WayfarerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MapViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            WayfarerTheme(
                themeMode = uiState.themeMode,
                palette = uiState.colorPalette
            ) {
                MapScreen(viewModel = viewModel)
            }
        }
    }
}
