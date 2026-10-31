package com.github.hwhaocool.pointbunny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.hwhaocool.pointbunny.ui.AppRoot
import com.github.hwhaocool.pointbunny.ui.theme.PointBunnyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PointBunnyTheme {
                AppRoot()
            }
        }
    }
}
