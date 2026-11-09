package com.adityapaul.wayfarer

import android.app.Application
import org.maplibre.android.MapLibre

class WayfarerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }
}
