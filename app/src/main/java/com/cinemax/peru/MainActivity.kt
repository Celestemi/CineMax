package com.cinemax.peru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cinemax.peru.ui.navigation.CineMaxApp
import com.cinemax.peru.ui.theme.CineMaxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineMaxTheme {
                CineMaxApp()
            }
        }
    }
}