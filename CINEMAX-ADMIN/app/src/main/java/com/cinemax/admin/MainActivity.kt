package com.cinemax.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cinemax.admin.data.local.DatabaseProvider
import com.cinemax.admin.ui.navigation.CineMaxAdminApp
import com.cinemax.admin.ui.theme.CineMaxAdminTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DatabaseProvider.obtener(applicationContext)
        enableEdgeToEdge()
        setContent {
            CineMaxAdminTheme {
                CineMaxAdminApp()
            }
        }
    }
}
