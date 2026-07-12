package com.bmuslim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bmuslim.app.truth.TemporalState
import com.bmuslim.app.truth.TruthInputs
import com.bmuslim.app.truth.TruthResolution
import com.bmuslim.app.ui.theme.BMuslimTheme

internal const val NotReadyHeading = "Panduan belum tersedia"
internal const val NotReadyExplanation =
    "Panduan yang disahkan dan data jadual belum dikonfigurasikan untuk aplikasi ini."

internal fun productionTruthResolution(): TruthResolution = TruthResolution.resolve(
    TruthInputs(
        contentRelease = null,
        temporalState = TemporalState.Unconfigured,
        rankingPolicy = null,
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BMuslimTheme {
                ProductionApp(productionTruthResolution())
            }
        }
    }
}

@Composable
private fun ProductionApp(resolution: TruthResolution) {
    check(!resolution.isReady) { "Production entry has no approved truth inputs in this slice" }
    Surface(Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
        ) {
            Text(
                text = NotReadyHeading,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = NotReadyExplanation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
