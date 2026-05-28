package com.empresa.aplicaciontensorflowandkeras17

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.empresa.aplicaciontensorflowandkeras17.ui.Screen.AppNavigator
import com.empresa.aplicaciontensorflowandkeras17.ui.theme.AplicacionTensorFlowAndKeras17Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AplicacionTensorFlowAndKeras17Theme {
                AppNavigator()
            }
        }
    }
}
