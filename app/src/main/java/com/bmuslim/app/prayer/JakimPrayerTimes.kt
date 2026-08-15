package com.bmuslim.app.prayer

import com.bmuslim.app.truth.DateBearingPrayerValue
import com.bmuslim.app.truth.PermissionState
import com.bmuslim.app.truth.PrayerDayKey
import com.bmuslim.app.truth.PrayerDaySnapshot
import com.bmuslim.app.truth.PrayerSlot
import com.bmuslim.app.truth.SourceContract
import com.bmuslim.app.truth.TemporalProvenance
import com.bmuslim.app.truth.TemporalState
import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val JAKIM_PROVIDER_ID = "jakim-e-solat"
const val JAKIM_CONTRACT_VERSION = "takwimsolat-today-v2-eight-timings"
const val JAKIM_PORTAL_URL = "https://www.e-solat.gov.my/"
const val MALAYSIA_SCHEDULE_ZONE_ID = "Asia/Kuala_Lumpur"

data class JakimPrayerResponse(
    val status: String?,
    val zone: String?,
    val prayerTimes: List<JakimPrayerRecord>?,
)

data class JakimPrayerRecord(
    val date: String?,
    val imsak: String?,
    val fajr: String?,
    val syuruk: String?,
    val dhuha: String?,
    val dhuhr: String?,
    val asr: String?,
    val maghrib: String?,
    val isha: String?,
)

sealed interface JakimParseResult {
    data class Success(val response: JakimPrayerResponse) : JakimParseResult
    data object MalformedJson : JakimParseResult
}

class JakimResponseParser {
    fun parse(body: String): JakimParseResult {
        return try {
            JsonReader(StringReader(body)).use { reader ->
                reader.strictness = Strictness.STRICT
                if (reader.peek() != JsonToken.BEGIN_OBJECT) return JakimParseResult.MalformedJson
                var status: String? = null
                var zone: String? = null
                var prayerTimes: List<JakimPrayerRecord>? = null
                val seen = mutableSetOf<String>()
                reader.beginObject()
                while (reader.hasNext()) {
                    when (val name = reader.nextName()) {
                        "status" -> {
                            if (!seen.add(name)) return JakimParseResult.MalformedJson
                            status = reader.requiredString()
                                ?: return JakimParseResult.MalformedJson
                        }
                        "zone" -> {
                            if (!seen.add(name)) return JakimParseResult.MalformedJson
                            zone = reader.requiredString()
                                ?: return JakimParseResult.MalformedJson
                        }
                        "prayerTime" -> {
                            if (!seen.add(name) || reader.peek() != JsonToken.BEGIN_ARRAY) {
                                return JakimParseResult.MalformedJson
                            }
                            prayerTimes = buildList {
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    add(reader.readRecord() ?: return JakimParseResult.MalformedJson)
                                }
                                reader.endArray()
                            }
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                if (reader.peek() != JsonToken.END_DOCUMENT || seen != REQUIRED_ENVELOPE_FIELDS) {
                    return JakimParseResult.MalformedJson
                }
                JakimParseResult.Success(
                    JakimPrayerResponse(
                        status = status,
                        zone = zone,
                        prayerTimes = prayerTimes,
                    ),
                )
            }
        } catch (_: IOException) {
            JakimParseResult.MalformedJson
        } catch (_: IllegalStateException) {
            JakimParseResult.MalformedJson
        } catch (_: RuntimeException) {
            JakimParseResult.MalformedJson
        }
    }

    private fun JsonReader.readRecord(): JakimPrayerRecord? {
        if (peek() != JsonToken.BEGIN_OBJECT) return null
        val values = mutableMapOf<String, String>()
        beginObject()
        while (hasNext()) {
            val name = nextName()
            if (name in REQUIRED_RECORD_FIELDS) {
                if (name in values) return null
                values[name] = requiredString() ?: return null
            } else {
                skipValue()
            }
        }
        endObject()
        if (values.keys != REQUIRED_RECORD_FIELDS) return null
        return JakimPrayerRecord(
            date = values.getValue("date"),
            imsak = values.getValue("imsak"),
            fajr = values.getValue("fajr"),
            syuruk = values.getValue("syuruk"),
            dhuha = values.getValue("dhuha"),
            dhuhr = values.getValue("dhuhr"),
            asr = values.getValue("asr"),
            maghrib = values.getValue("maghrib"),
            isha = values.getValue("isha"),
        )
    }

    private fun JsonReader.requiredString(): String? =
        if (peek() == JsonToken.STRING) nextString() else null

    companion object {
        private val REQUIRED_ENVELOPE_FIELDS = setOf("status", "zone", "prayerTime")
        private val REQUIRED_RECORD_FIELDS = setOf(
            "date", "imsak", "fajr", "syuruk", "dhuha", "dhuhr", "asr", "maghrib", "isha",
        )
    }
}

enum class JakimAlmanacCategory { OBLIGATORY_PRAYER, TAKWIM_MARKER }

enum class JakimAlmanacSlot(
    val displayName: String,
    val category: JakimAlmanacCategory,
    val prayerSlot: PrayerSlot?,
) {
    IMSAK("Imsak", JakimAlmanacCategory.TAKWIM_MARKER, null),
    FAJR("Subuh", JakimAlmanacCategory.OBLIGATORY_PRAYER, PrayerSlot.DAWN),
    SYURUK("Syuruk", JakimAlmanacCategory.TAKWIM_MARKER, null),
    DHUHA("Dhuha", JakimAlmanacCategory.TAKWIM_MARKER, null),
    DHUHR("Zohor", JakimAlmanacCategory.OBLIGATORY_PRAYER, PrayerSlot.NOON),
    ASR("Asar", JakimAlmanacCategory.OBLIGATORY_PRAYER, PrayerSlot.AFTERNOON),
    MAGHRIB("Maghrib", JakimAlmanacCategory.OBLIGATORY_PRAYER, PrayerSlot.SUNSET),
    ISHA("Isyak", JakimAlmanacCategory.OBLIGATORY_PRAYER, PrayerSlot.NIGHT),
}

data class JakimAlmanacValue(
    val slot: JakimAlmanacSlot,
    val civilDate: LocalDate,
    val time: LocalTime,
)

data class JakimAlmanacSchedule(
    val exactKey: PrayerDayKey,
    val scheduleZoneId: String,
    val values: List<JakimAlmanacValue>,
    val provenance: TemporalProvenance,
)

sealed interface JakimValidationFailure {
    data object UnsupportedZone : JakimValidationFailure
    data object ProviderStatusRejected : JakimValidationFailure
    data object ZoneMismatch : JakimValidationFailure
    data object ScheduleCardinality : JakimValidationFailure
    data object WrongDate : JakimValidationFailure
    data object MalformedTime : JakimValidationFailure
    data object NonChronological : JakimValidationFailure
}

sealed interface JakimAdaptResult {
    data class Success(
        val almanacSchedule: JakimAlmanacSchedule,
        val temporalState: TemporalState.Ready,
    ) : JakimAdaptResult
    data class Failure(val reason: JakimValidationFailure) : JakimAdaptResult
}

class JakimPrayerAdapter {
    private val dateFormatter = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("dd-MMM-uuuu")
        .toFormatter(Locale.ENGLISH)
        .withResolverStyle(ResolverStyle.STRICT)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)
        .withResolverStyle(ResolverStyle.STRICT)

    fun adapt(
        requestedKey: PrayerDayKey,
        response: JakimPrayerResponse,
    ): JakimAdaptResult {
        if (!JakimZones.contains(requestedKey.zoneCode)) {
            return JakimAdaptResult.Failure(JakimValidationFailure.UnsupportedZone)
        }
        if (response.status != "OK!") {
            return JakimAdaptResult.Failure(JakimValidationFailure.ProviderStatusRejected)
        }
        if (response.zone != requestedKey.zoneCode) {
            return JakimAdaptResult.Failure(JakimValidationFailure.ZoneMismatch)
        }
        val records = response.prayerTimes
        if (records?.size != 1) {
            return JakimAdaptResult.Failure(JakimValidationFailure.ScheduleCardinality)
        }
        val record = records.singleOrNull()
            ?: return JakimAdaptResult.Failure(JakimValidationFailure.ScheduleCardinality)
        val responseDate = try {
            LocalDate.parse(record.date, dateFormatter)
        } catch (_: DateTimeParseException) {
            return JakimAdaptResult.Failure(JakimValidationFailure.WrongDate)
        } catch (_: NullPointerException) {
            return JakimAdaptResult.Failure(JakimValidationFailure.WrongDate)
        }
        if (responseDate != requestedKey.civilDate) {
            return JakimAdaptResult.Failure(JakimValidationFailure.WrongDate)
        }

        val rawValues = listOf(
            JakimAlmanacSlot.IMSAK to record.imsak,
            JakimAlmanacSlot.FAJR to record.fajr,
            JakimAlmanacSlot.SYURUK to record.syuruk,
            JakimAlmanacSlot.DHUHA to record.dhuha,
            JakimAlmanacSlot.DHUHR to record.dhuhr,
            JakimAlmanacSlot.ASR to record.asr,
            JakimAlmanacSlot.MAGHRIB to record.maghrib,
            JakimAlmanacSlot.ISHA to record.isha,
        )
        val values = try {
            rawValues.map { (slot, rawTime) ->
                JakimAlmanacValue(
                    slot = slot,
                    civilDate = responseDate,
                    time = LocalTime.parse(rawTime, timeFormatter),
                )
            }
        } catch (_: DateTimeParseException) {
            return JakimAdaptResult.Failure(JakimValidationFailure.MalformedTime)
        } catch (_: NullPointerException) {
            return JakimAdaptResult.Failure(JakimValidationFailure.MalformedTime)
        }
        if (!values.zipWithNext().all { (earlier, later) -> earlier.time < later.time }) {
            return JakimAdaptResult.Failure(JakimValidationFailure.NonChronological)
        }

        val contract = SourceContract(
            providerId = JAKIM_PROVIDER_ID,
            contractVersion = JAKIM_CONTRACT_VERSION,
            termsReference = JAKIM_PORTAL_URL,
            usePermission = PermissionState.PERMITTED,
            persistencePermission = PermissionState.UNKNOWN,
        )
        val provenance = TemporalProvenance(
            providerId = contract.providerId,
            contractVersion = contract.contractVersion,
            sourceReference = JakimPrayerTimesRepository.requestUrl(requestedKey.zoneCode),
        )
        val almanacSchedule = JakimAlmanacSchedule(
            exactKey = requestedKey,
            scheduleZoneId = MALAYSIA_SCHEDULE_ZONE_ID,
            values = values,
            provenance = provenance,
        )
        val prayerValues = values.mapNotNull { value ->
            value.slot.prayerSlot?.let { prayerSlot ->
                DateBearingPrayerValue(prayerSlot, value.civilDate, value.time)
            }
        }
        return JakimAdaptResult.Success(
            almanacSchedule = almanacSchedule,
            temporalState = TemporalState.Ready(
                requestedKey = requestedKey,
                sourceContract = contract,
                snapshot = PrayerDaySnapshot(
                    exactKey = requestedKey,
                    scheduleZoneId = MALAYSIA_SCHEDULE_ZONE_ID,
                    dateBearingValues = prayerValues,
                    provenance = provenance,
                ),
            ),
        )
    }
}

sealed interface JakimHttpResult {
    data class Success(val body: String) : JakimHttpResult
    data class HttpFailure(val statusCode: Int) : JakimHttpResult
    data object NoConnection : JakimHttpResult
    data object ResponseTooLarge : JakimHttpResult
}

fun interface JakimHttpClient {
    fun get(url: String): JakimHttpResult
}

class UrlConnectionJakimHttpClient(
    private val connectTimeoutMillis: Int = 10_000,
    private val readTimeoutMillis: Int = 10_000,
    private val maximumBodyBytes: Int = 128 * 1024,
    private val connectionFactory: HttpConnectionFactory = HttpConnectionFactory { url ->
        URL(url).openConnection() as HttpURLConnection
    },
) : JakimHttpClient {
    override fun get(url: String): JakimHttpResult {
        val connection = try {
            connectionFactory.open(url)
        } catch (_: IOException) {
            return JakimHttpResult.NoConnection
        }
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            connection.useCaches = false
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status !in 200..299) {
                return JakimHttpResult.HttpFailure(status)
            }
            val declaredLength = connection.contentLengthLong
            if (declaredLength > maximumBodyBytes) {
                return JakimHttpResult.ResponseTooLarge
            }
            connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > maximumBodyBytes) return JakimHttpResult.ResponseTooLarge
                    output.write(buffer, 0, count)
                }
                JakimHttpResult.Success(output.toString(StandardCharsets.UTF_8.name()))
            }
        } catch (_: SocketTimeoutException) {
            JakimHttpResult.NoConnection
        } catch (_: IOException) {
            JakimHttpResult.NoConnection
        } finally {
            connection.disconnect()
        }
    }
}

fun interface HttpConnectionFactory {
    fun open(url: String): HttpURLConnection
}

sealed interface PrayerTimesResult {
    data class Success(
        val almanacSchedule: JakimAlmanacSchedule,
        val temporalState: TemporalState.Ready,
    ) : PrayerTimesResult
    data object NoConnection : PrayerTimesResult
    data object ProviderRejected : PrayerTimesResult
    data class InvalidData(val reason: String) : PrayerTimesResult
}

interface PrayerTimesRepository {
    suspend fun fetch(key: PrayerDayKey): PrayerTimesResult
}

class JakimPrayerTimesRepository(
    private val httpClient: JakimHttpClient = UrlConnectionJakimHttpClient(),
    private val parser: JakimResponseParser = JakimResponseParser(),
    private val adapter: JakimPrayerAdapter = JakimPrayerAdapter(),
) : PrayerTimesRepository {
    override suspend fun fetch(key: PrayerDayKey): PrayerTimesResult = withContext(Dispatchers.IO) {
        if (!JakimZones.contains(key.zoneCode)) {
            return@withContext PrayerTimesResult.InvalidData("unsupported-zone")
        }
        val url = requestUrl(key.zoneCode)
        when (val http = httpClient.get(url)) {
            is JakimHttpResult.Success -> when (val parsed = parser.parse(http.body)) {
                JakimParseResult.MalformedJson -> PrayerTimesResult.InvalidData("malformed-json")
                is JakimParseResult.Success -> when (val adapted = adapter.adapt(key, parsed.response)) {
                    is JakimAdaptResult.Success -> PrayerTimesResult.Success(
                        adapted.almanacSchedule,
                        adapted.temporalState,
                    )
                    is JakimAdaptResult.Failure -> when (adapted.reason) {
                        JakimValidationFailure.ProviderStatusRejected -> PrayerTimesResult.ProviderRejected
                        else -> PrayerTimesResult.InvalidData(adapted.reason.toString())
                    }
                }
            }
            is JakimHttpResult.HttpFailure -> PrayerTimesResult.ProviderRejected
            JakimHttpResult.NoConnection -> PrayerTimesResult.NoConnection
            JakimHttpResult.ResponseTooLarge -> PrayerTimesResult.InvalidData("response-too-large")
        }
    }

    companion object {
        private const val ENDPOINT = "https://www.e-solat.gov.my/index.php"

        fun requestUrl(zoneCode: String): String {
            val encodedZone = URLEncoder.encode(zoneCode, StandardCharsets.UTF_8.name())
            return "$ENDPOINT?r=esolatApi/TakwimSolat&period=today&zone=$encodedZone"
        }
    }
}
