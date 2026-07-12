package com.bmuslim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bmuslim.app.ui.theme.BMuslimTheme
import com.bmuslim.app.ui.timeline.TimelineScreen

internal const val DemoDisclosureText =
    "Demo reka bentuk — masa dan kandungan ialah data sampel, bukan panduan langsung."

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BMuslimTheme {
                Box(Modifier.fillMaxSize()) {
                    TimelineScreen()
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = DemoDisclosureText
                            },
                    ) {
                        Text(
                            text = DemoDisclosureText,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
