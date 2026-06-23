package com.gainsmaxxing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gainsmaxxing.ui.navigation.AppNavigation
import com.gainsmaxxing.ui.theme.GainsmaxxingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GainsmaxxingTheme {
                AppNavigation()
            }
        }
    }
}
