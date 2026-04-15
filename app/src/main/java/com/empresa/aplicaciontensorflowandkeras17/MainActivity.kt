package com.empresa.aplicaciontensorflowandkeras17

import com.empresa.aplicaciontensorflowliteandkeras.AppNavigator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.empresa.aplicaciontensorflowliteandkeras.ui.theme.AplicacionTensorflowLiteAndKerasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AplicacionTensorflowLiteAndKeras17Theme {
                AppNavigator()
            }
        }
    }
}