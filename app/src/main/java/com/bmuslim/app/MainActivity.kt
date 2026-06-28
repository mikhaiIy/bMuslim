package com.bmuslim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bmuslim.app.ui.theme.BMuslimTheme
import com.bmuslim.app.ui.timeline.TimelineScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BMuslimTheme {
                TimelineScreen()
            }
        }
    }
}
