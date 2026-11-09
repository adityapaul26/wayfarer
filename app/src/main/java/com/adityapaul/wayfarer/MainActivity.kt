package com.adityapaul.wayfarer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.adityapaul.wayfarer.ui.map.MapScreen
import com.adityapaul.wayfarer.ui.theme.WayfarerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WayfarerTheme {
                MapScreen()
            }
        }
    }
}
