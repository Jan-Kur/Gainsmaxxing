package com.gainsmaxxing

import android.app.Application
import com.gainsmaxxing.di.AppInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GainsmaxxingApp : Application() {
    @Inject lateinit var appInitializer: AppInitializer

    override fun onCreate() {
        super.onCreate()
        appInitializer.initialize()
    }
}
