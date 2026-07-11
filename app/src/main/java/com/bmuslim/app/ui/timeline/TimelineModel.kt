package com.bmuslim.app.ui.timeline

import java.time.LocalTime

enum class DeedType(val label: String) {
    Salah("Solat"),
    Dhikr("Zikir"),
    Quran("Quran"),
    Sawm("Puasa"),
    Adab("Adab"),
}

enum class Ruling(val label: String, val priority: Int) {
    Fardh("Fardhu", 0),
    Muakkadah("Sunnah Muakkadah", 1),
    Sunnah("Sunnah", 2),
    Mandub("Mandub", 3),
}

enum class RenderArchetype {
    Point,
    Window,
    DaySpan,
    Anytime,
}

/**
 * A compact native prototype model. Window and act duration remain separate so the dial can
 * distinguish "when it is available" from "how long it takes".
 */
data class TimelineDeed(
    val id: String,
    val title: String,
    val type: DeedType,
    val ruling: Ruling,
    val archetype: RenderArchetype,
    val windowLabel: String,
    val start: LocalTime,
    val end: LocalTime,
    val act: LocalTime = start,
    val actMinutes: Int = 10,
    val description: String,
    val citation: String,
    val grade: String = "Sahih",
    val arabic: String? = null,
    val translation: String? = null,
) {
    val startMinute: Int get() = start.hour * 60 + start.minute
    val endMinute: Int get() = end.hour * 60 + end.minute
    val actMinute: Int get() = act.hour * 60 + act.minute
}

data class DayState(
    val id: String,
    val title: String,
    val description: String,
    val ruling: Ruling,
)

/** A window may cross midnight, so it cannot always be represented by an IntRange. */
fun TimelineDeed.isActiveAt(minute: Int): Boolean {
    val value = ((minute % (24 * 60)) + (24 * 60)) % (24 * 60)
    return if (startMinute <= endMinute) {
        value in startMinute..endMinute
    } else {
        value >= startMinute || value <= endMinute
    }
}

fun activeDeedsAt(deeds: List<TimelineDeed>, minute: Int): List<TimelineDeed> = deeds
    .filter { it.isActiveAt(minute) }
    .sortedWith(compareBy<TimelineDeed> { it.ruling.priority }.thenBy { it.startMinute }.thenBy { it.id })

fun nextDeedAt(deeds: List<TimelineDeed>, minute: Int): TimelineDeed = deeds.minWith(
    compareBy<TimelineDeed> { forwardDistance(minute, it.startMinute) }
        .thenBy { it.ruling.priority }
        .thenBy { it.id },
)

fun obligatoryPrayerContextAt(deeds: List<TimelineDeed>, minute: Int): TimelineDeed {
    val prayers = deeds.filter { it.ruling == Ruling.Fardh }
    return activeDeedsAt(prayers, minute).firstOrNull() ?: nextDeedAt(prayers, minute)
}

/** The contextual deed excludes obligatory prayers so prayer and deed summaries never duplicate. */
fun contextualDeedAt(deeds: List<TimelineDeed>, minute: Int): TimelineDeed {
    val contextual = deeds.filter { it.ruling != Ruling.Fardh }
    return activeDeedsAt(contextual, minute).firstOrNull() ?: nextDeedAt(contextual, minute)
}

fun sampleDayStates(): List<DayState> = listOf(
    DayState(
        id = "thursday-fast",
        title = "Puasa sunat Khamis",
        description = "Cadangan sepanjang hari ini",
        ruling = Ruling.Sunnah,
    ),
)

private fun forwardDistance(from: Int, to: Int): Int {
    val day = 24 * 60
    return ((to - from) % day + day) % day
}

fun sampleDeeds(): List<TimelineDeed> = listOf(
    prayer("subuh", "Solat Subuh", 5, 59, 6, 19, 2),
    TimelineDeed(
        id = "zikir-pagi",
        title = "Zikir pagi",
        type = DeedType.Dhikr,
        ruling = Ruling.Muakkadah,
        archetype = RenderArchetype.Window,
        windowLabel = "Selepas Subuh hingga Syuruk",
        start = LocalTime.of(5, 59),
        end = LocalTime.of(7, 11),
        act = LocalTime.of(6, 20),
        actMinutes = 8,
        description = "Zikir dan doa pagi, dibaca selepas Subuh hingga matahari terbit.",
        citation = "Abu Dawud 5074",
        arabic = "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا",
        translation = "Ya Allah, dengan-Mu kami memasuki waktu pagi dan petang.",
    ),
    TimelineDeed(
        id = "dhuha",
        title = "Solat Dhuha",
        type = DeedType.Salah,
        ruling = Ruling.Sunnah,
        archetype = RenderArchetype.Point,
        windowLabel = "Selepas Syuruk hingga sebelum Zohor",
        start = LocalTime.of(7, 30),
        end = LocalTime.of(13, 21),
        act = LocalTime.of(8, 24),
        actMinutes = 12,
        description = "Solat sunat Dhuha, minimum dua rakaat, selepas matahari naik.",
        citation = "Sahih Muslim 720",
    ),
    prayer("zohor", "Solat Zohor", 13, 21, 13, 41, 4),
    prayer("asar", "Solat Asar", 16, 45, 17, 5, 4),
    TimelineDeed(
        id = "zikir-petang",
        title = "Zikir petang",
        type = DeedType.Dhikr,
        ruling = Ruling.Muakkadah,
        archetype = RenderArchetype.Window,
        windowLabel = "Selepas Asar hingga Maghrib",
        start = LocalTime.of(16, 45),
        end = LocalTime.of(19, 29),
        act = LocalTime.of(17, 10),
        actMinutes = 8,
        description = "Zikir perlindungan dan kesyukuran menjelang petang.",
        citation = "Abu Dawud 5074",
        arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ",
        translation = "Kami memasuki petang dan segala kerajaan milik Allah.",
    ),
    prayer("maghrib", "Solat Maghrib", 19, 29, 19, 49, 3),
    prayer("isyak", "Solat Isyak", 20, 42, 21, 2, 4),
    TimelineDeed(
        id = "al-mulk",
        title = "Baca Surah Al-Mulk",
        type = DeedType.Quran,
        ruling = Ruling.Sunnah,
        archetype = RenderArchetype.Window,
        windowLabel = "Selepas Isyak hingga sebelum tidur",
        start = LocalTime.of(20, 42),
        end = LocalTime.of(23, 0),
        act = LocalTime.of(21, 40),
        actMinutes = 10,
        description = "Membaca Surah Al-Mulk sebelum tidur.",
        citation = "Jami' at-Tirmidhi 2891",
        grade = "Hasan",
        arabic = "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ",
        translation = "Maha Berkat Dia yang menguasai segala kerajaan.",
    ),
    TimelineDeed(
        id = "tahajud",
        title = "Solat Tahajud",
        type = DeedType.Salah,
        ruling = Ruling.Muakkadah,
        archetype = RenderArchetype.Point,
        windowLabel = "Sepertiga akhir malam",
        start = LocalTime.of(3, 30),
        end = LocalTime.of(5, 30),
        act = LocalTime.of(4, 12),
        actMinutes = 16,
        description = "Solat sunat malam sebelum masuk waktu Subuh.",
        citation = "Sahih al-Bukhari 1145",
    ),
)

private fun prayer(
    id: String,
    title: String,
    hour: Int,
    minute: Int,
    endHour: Int,
    endMinute: Int,
    rakaat: Int,
) = TimelineDeed(
    id = id,
    title = title,
    type = DeedType.Salah,
    ruling = Ruling.Fardh,
    archetype = RenderArchetype.Point,
    windowLabel = "Waktu solat",
    start = LocalTime.of(hour, minute),
    end = LocalTime.of(endHour, endMinute),
    actMinutes = 10,
    description = "Dirikan ${title.removePrefix("Solat ")} fardhu, $rakaat rakaat, dengan tenang dan khusyuk.",
    citation = "Waktu solat JAKIM · WLY01",
    grade = "Tidak berkaitan",
)
