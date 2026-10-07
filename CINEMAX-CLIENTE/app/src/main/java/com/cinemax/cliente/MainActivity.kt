package com.cinemax.cliente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cinemax.cliente.data.local.DatabaseProvider
import com.cinemax.cliente.ui.navigation.CineMaxClienteApp
import com.cinemax.cliente.ui.theme.CineMaxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DatabaseProvider.obtener(applicationContext)
        enableEdgeToEdge()
        setContent {
            CineMaxTheme {
                CineMaxClienteApp()
            }
        }
    }
}
