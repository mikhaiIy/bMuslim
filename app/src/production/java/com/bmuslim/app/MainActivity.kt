package com.bmuslim.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bmuslim.app.prayer.JakimPrayerTimesRepository
import com.bmuslim.app.prayer.JakimAlmanacSchedule
import com.bmuslim.app.prayer.JakimZone
import com.bmuslim.app.prayer.JakimZones
import com.bmuslim.app.prayer.PrayerTimesRepository
import com.bmuslim.app.prayer.PrayerTimesResult
import com.bmuslim.app.truth.PrayerDayKey
import com.bmuslim.app.truth.TemporalState
import com.bmuslim.app.truth.TruthInputs
import com.bmuslim.app.truth.TruthResolution
import com.bmuslim.app.ui.theme.BMuslimTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal const val NotReadyHeading = "Panduan belum tersedia"
internal const val NotReadyExplanation =
    "Panduan yang disahkan untuk amalan dan dasar keutamaan belum tersedia. Jadual solat boleh dilihat secara berasingan."

internal fun productionTruthResolution(
    temporalState: TemporalState = TemporalState.Unconfigured,
): TruthResolution = TruthResolution.resolve(
    TruthInputs(
        contentRelease = null,
        temporalState = temporalState,
        rankingPolicy = null,
    ),
)

internal interface ZonePreference {
    fun read(): String?
    fun write(zoneCode: String)
}

private class SharedPreferencesZonePreference(context: Context) : ZonePreference {
    private val preferences = context.getSharedPreferences("prayer-zone", Context.MODE_PRIVATE)
    override fun read(): String? = preferences.getString("selected-zone", null)
    override fun write(zoneCode: String) {
        preferences.edit().putString("selected-zone", zoneCode).apply()
    }
}

internal enum class PrayerFailureKind { NO_CONNECTION, PROVIDER_REJECTED, INVALID_DATA }

internal sealed interface PrayerScreenState {
    data object Unconfigured : PrayerScreenState
    data class Loading(val zone: JakimZone) : PrayerScreenState
    data class Ready(
        val zone: JakimZone,
        val almanacSchedule: JakimAlmanacSchedule,
        val temporalState: TemporalState.Ready,
    ) : PrayerScreenState
    data class Failure(val zone: JakimZone, val kind: PrayerFailureKind) : PrayerScreenState
}

internal class PrayerTimesStateHolder(
    private val repository: PrayerTimesRepository,
    private val zonePreference: ZonePreference,
    private val scope: CoroutineScope,
    private val clock: Clock = Clock.system(ZoneId.of("Asia/Kuala_Lumpur")),
) {
    var state: PrayerScreenState by mutableStateOf(PrayerScreenState.Unconfigured)
        private set

    private var requestSequence = 0L
    private var activeRequest: Job? = null
    private var midnightRefresh: Job? = null
    private var isForeground = false

    init {
        zonePreference.read()?.let(JakimZones::find)?.let(::selectZone)
    }

    fun selectZone(zone: JakimZone) {
        zonePreference.write(zone.code)
        load(zone)
    }

    fun retry() {
        val zone = when (val current = state) {
            is PrayerScreenState.Failure -> current.zone
            is PrayerScreenState.Ready -> current.zone
            is PrayerScreenState.Loading -> current.zone
            PrayerScreenState.Unconfigured -> return
        }
        load(zone)
    }

    fun refreshIfDayChanged() {
        val current = state as? PrayerScreenState.Ready ?: return
        val today = LocalDate.now(clock.withZone(ZoneId.of("Asia/Kuala_Lumpur")))
        if (current.temporalState.requestedKey.civilDate != today) {
            load(current.zone)
        }
    }

    fun onForeground() {
        isForeground = true
        refreshIfDayChanged()
        (state as? PrayerScreenState.Ready)?.let(::scheduleMidnightRefresh)
    }

    fun onBackground() {
        isForeground = false
        midnightRefresh?.cancel()
        midnightRefresh = null
    }

    private fun load(zone: JakimZone) {
        midnightRefresh?.cancel()
        midnightRefresh = null
        val sequence = ++requestSequence
        activeRequest?.cancel()
        state = PrayerScreenState.Loading(zone)
        activeRequest = scope.launch {
            val date = LocalDate.now(clock.withZone(ZoneId.of("Asia/Kuala_Lumpur")))
            val result = repository.fetch(PrayerDayKey(zone.code, date))
            if (sequence != requestSequence) return@launch
            val resolvedState = when (result) {
                is PrayerTimesResult.Success -> PrayerScreenState.Ready(
                    zone,
                    result.almanacSchedule,
                    result.temporalState,
                )
                PrayerTimesResult.NoConnection -> PrayerScreenState.Failure(zone, PrayerFailureKind.NO_CONNECTION)
                PrayerTimesResult.ProviderRejected -> PrayerScreenState.Failure(zone, PrayerFailureKind.PROVIDER_REJECTED)
                is PrayerTimesResult.InvalidData -> PrayerScreenState.Failure(zone, PrayerFailureKind.INVALID_DATA)
            }
            state = resolvedState
            if (resolvedState is PrayerScreenState.Ready) scheduleMidnightRefresh(resolvedState)
        }
    }

    private fun scheduleMidnightRefresh(ready: PrayerScreenState.Ready) {
        if (!isForeground) return
        midnightRefresh?.cancel()
        val malaysiaZone = ZoneId.of("Asia/Kuala_Lumpur")
        val now = ZonedDateTime.now(clock.withZone(malaysiaZone))
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(malaysiaZone).toInstant()
        val delayMillis = Duration.between(clock.instant(), nextMidnight).toMillis().coerceAtLeast(1L)
        val scheduledDate = ready.temporalState.requestedKey.civilDate
        midnightRefresh = scope.launch {
            delay(delayMillis)
            val current = state as? PrayerScreenState.Ready ?: return@launch
            if (current.zone == ready.zone && current.temporalState.requestedKey.civilDate == scheduledDate) {
                refreshIfDayChanged()
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(5, 43, 35)),
        )
        setContent {
            BMuslimTheme {
                val scope = rememberCoroutineScope()
                val stateHolder = remember {
                    PrayerTimesStateHolder(
                        repository = JakimPrayerTimesRepository(),
                        zonePreference = SharedPreferencesZonePreference(this),
                        scope = scope,
                    )
                }
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner, stateHolder) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> stateHolder.onForeground()
                            Lifecycle.Event.ON_PAUSE -> stateHolder.onBackground()
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        stateHolder.onBackground()
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                ProductionApp(
                    state = stateHolder.state,
                    onSelectZone = stateHolder::selectZone,
                    onRetry = stateHolder::retry,
                )
            }
        }
    }
}

@Composable
private fun ProductionApp(
    state: PrayerScreenState,
    onSelectZone: (JakimZone) -> Unit,
    onRetry: () -> Unit,
) {
    var selectorOpen by remember { mutableStateOf(false) }
    var researchPreviewOpen by remember { mutableStateOf(false) }
    val temporalState = (state as? PrayerScreenState.Ready)?.temporalState ?: TemporalState.Unconfigured
    val resolution = productionTruthResolution(temporalState)
    check(!resolution.isReady) { "Prayer times must not unlock unreviewed guidance" }

    if (state is PrayerScreenState.Ready) {
        ProductionAlmanacScreen(
            state = state,
        )
        if (selectorOpen) {
            ZoneSelector(
                onDismiss = { selectorOpen = false },
                onSelect = { zone ->
                    selectorOpen = false
                    onSelectZone(zone)
                },
            )
        }
        if (researchPreviewOpen) {
            ResearchPreviewDestination(onBack = { researchPreviewOpen = false })
        }
        return
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
        ) {
            Text(
                text = "Waktu solat hari ini",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .semantics { heading() }
                    .testTag("prayer-heading"),
            )
            when (state) {
                PrayerScreenState.Unconfigured -> UnconfiguredSchedule(onChooseZone = { selectorOpen = true })
                is PrayerScreenState.Loading -> LoadingSchedule(state.zone, onChangeZone = { selectorOpen = true })
                is PrayerScreenState.Ready -> Unit // Handled by the live instrument above.
                is PrayerScreenState.Failure -> FailureSchedule(
                    state = state,
                    onRetry = onRetry,
                    onChangeZone = { selectorOpen = true },
                )
            }
            HorizontalDivider()
            Text(
                text = NotReadyHeading,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = NotReadyExplanation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ResearchPreviewAction(
                onOpen = { researchPreviewOpen = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (selectorOpen) {
        ZoneSelector(
            onDismiss = { selectorOpen = false },
            onSelect = { zone ->
                selectorOpen = false
                onSelectZone(zone)
            },
        )
    }
    if (researchPreviewOpen) {
        ResearchPreviewDestination(onBack = { researchPreviewOpen = false })
    }
}

@Composable
private fun UnconfiguredSchedule(onChooseZone: () -> Unit) {
    Text("Pilih zon JAKIM anda untuk mendapatkan jadual rasmi bagi tarikh semasa di Malaysia.")
    Button(
        onClick = onChooseZone,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("choose-zone"),
    ) {
        Text("Pilih zon waktu solat")
    }
}

@Composable
private fun LoadingSchedule(zone: JakimZone, onChangeZone: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(32.dp)
                .semantics { contentDescription = "Sedang mendapatkan waktu solat" },
        )
        Text("Mendapatkan jadual untuk ${zone.code}…")
    }
    TextButton(
        onClick = onChangeZone,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) { Text("Tukar zon") }
}

@Composable
private fun FailureSchedule(
    state: PrayerScreenState.Failure,
    onRetry: () -> Unit,
    onChangeZone: () -> Unit,
) {
    val message = when (state.kind) {
        PrayerFailureKind.NO_CONNECTION -> "Tiada sambungan. Semak internet dan cuba lagi."
        PrayerFailureKind.PROVIDER_REJECTED -> "Portal e-Solat tidak dapat memberikan jadual sekarang."
        PrayerFailureKind.INVALID_DATA -> "Jadual diterima tetapi tarikh, zon atau waktunya tidak sah. Ia tidak dipaparkan."
    }
    Text(message, modifier = Modifier.testTag("prayer-error"))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) { Text("Cuba lagi") }
        TextButton(
            onClick = onChangeZone,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) { Text("Tukar zon") }
    }
}

@Composable
private fun ZoneSelector(onDismiss: () -> Unit, onSelect: (JakimZone) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih zon waktu solat", modifier = Modifier.semantics { heading() }) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 480.dp).testTag("zone-list")) {
                val grouped = JakimZones.all.groupBy(JakimZone::state)
                grouped.forEach { (state, zones) ->
                    item(key = "heading-$state") {
                        Text(
                            state,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                        )
                    }
                    items(zones, key = JakimZone::code) { zone ->
                        TextButton(
                            onClick = { onSelect(zone) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) {
                            Text(zone.displayName, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("Tutup") }
        },
    )
}
