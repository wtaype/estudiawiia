package com.estudiawii.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.estudiawii.app.feature.shell.EstudiaWiiApp
import com.estudiawii.app.feature.shell.EstudiaWiiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#FFF8D1")
        window.navigationBarColor = Color.WHITE
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContent {
            val viewModel: EstudiaWiiViewModel = viewModel()
            EstudiaWiiApp(viewModel)
        }
    }
}
