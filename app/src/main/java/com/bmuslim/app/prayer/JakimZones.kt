package com.bmuslim.app.prayer

data class JakimZone(
    val state: String,
    val code: String,
    val area: String,
) {
    val displayName: String get() = "$code · $area"
}

/**
 * Registry mirrored from the official e-Solat timetable selector.
 * Source: https://www.e-solat.gov.my/index.php?siteId=24&pageId=24
 */
object JakimZones {
    val all: List<JakimZone> = listOf(
        JakimZone("Johor", "JHR01", "Pulau Aur dan Pulau Pemanggil"),
        JakimZone("Johor", "JHR02", "Johor Bahru, Kota Tinggi, Mersing, Kulai"),
        JakimZone("Johor", "JHR03", "Kluang, Pontian"),
        JakimZone("Johor", "JHR04", "Batu Pahat, Muar, Segamat, Gemas Johor, Tangkak"),
        JakimZone("Kedah", "KDH01", "Kota Setar, Kubang Pasu, Pokok Sena"),
        JakimZone("Kedah", "KDH02", "Kuala Muda, Yan, Pendang"),
        JakimZone("Kedah", "KDH03", "Padang Terap, Sik"),
        JakimZone("Kedah", "KDH04", "Baling"),
        JakimZone("Kedah", "KDH05", "Bandar Baharu, Kulim"),
        JakimZone("Kedah", "KDH06", "Langkawi"),
        JakimZone("Kedah", "KDH07", "Puncak Gunung Jerai"),
        JakimZone("Kelantan", "KTN01", "Bachok, Kota Bharu, Machang, Pasir Mas, Pasir Puteh, Tanah Merah, Tumpat, Kuala Krai, Mukim Chiku"),
        JakimZone("Kelantan", "KTN02", "Gua Musang, Jeli, Jajahan Kecil Lojing"),
        JakimZone("Melaka", "MLK01", "Seluruh Negeri Melaka"),
        JakimZone("Negeri Sembilan", "NGS01", "Tampin, Jempol"),
        JakimZone("Negeri Sembilan", "NGS02", "Jelebu, Kuala Pilah, Rembau"),
        JakimZone("Negeri Sembilan", "NGS03", "Port Dickson, Seremban"),
        JakimZone("Pahang", "PHG01", "Pulau Tioman"),
        JakimZone("Pahang", "PHG02", "Kuantan, Pekan, Muadzam Shah"),
        JakimZone("Pahang", "PHG03", "Jerantut, Temerloh, Maran, Bera, Chenor, Jengka"),
        JakimZone("Pahang", "PHG04", "Bentong, Lipis, Raub"),
        JakimZone("Pahang", "PHG05", "Genting Sempah, Janda Baik, Bukit Tinggi"),
        JakimZone("Pahang", "PHG06", "Cameron Highlands, Genting Highlands, Bukit Fraser"),
        JakimZone("Pahang", "PHG07", "Zon Khas Daerah Rompin"),
        JakimZone("Perak", "PRK01", "Tapah, Slim River, Tanjung Malim"),
        JakimZone("Perak", "PRK02", "Kuala Kangsar, Sungai Siput, Ipoh, Batu Gajah, Kampar"),
        JakimZone("Perak", "PRK03", "Lenggong, Pengkalan Hulu, Gerik"),
        JakimZone("Perak", "PRK04", "Temengor, Belum"),
        JakimZone("Perak", "PRK05", "Kampung Gajah, Teluk Intan, Bagan Datuk, Seri Iskandar, Beruas, Parit, Lumut, Sitiawan, Pulau Pangkor"),
        JakimZone("Perak", "PRK06", "Selama, Taiping, Bagan Serai, Parit Buntar"),
        JakimZone("Perak", "PRK07", "Bukit Larut"),
        JakimZone("Perlis", "PLS01", "Kangar, Padang Besar, Arau"),
        JakimZone("Pulau Pinang", "PNG01", "Seluruh Negeri Pulau Pinang"),
        JakimZone("Sabah", "SBH01", "Bahagian Sandakan (Timur), Bandar Sandakan, Bukit Garam, Semawang, Temanggong, Tambisan, Sukau"),
        JakimZone("Sabah", "SBH02", "Beluran, Telupid, Pinangah, Terusan, Kuamut, Bahagian Sandakan (Barat)"),
        JakimZone("Sabah", "SBH03", "Lahad Datu, Silabukan, Kunak, Sahabat, Semporna, Tungku, Bahagian Tawau (Timur)"),
        JakimZone("Sabah", "SBH04", "Bandar Tawau, Balong, Merotai, Kalabakan, Bahagian Tawau (Barat)"),
        JakimZone("Sabah", "SBH05", "Kudat, Kota Marudu, Pitas, Pulau Banggi, Bahagian Kudat"),
        JakimZone("Sabah", "SBH06", "Gunung Kinabalu"),
        JakimZone("Sabah", "SBH07", "Kota Kinabalu, Ranau, Kota Belud, Tuaran, Penampang, Papar, Putatan, Bahagian Pantai Barat"),
        JakimZone("Sabah", "SBH08", "Pensiangan, Keningau, Tambunan, Nabawan, Bahagian Pedalaman (Atas)"),
        JakimZone("Sabah", "SBH09", "Beaufort, Kuala Penyu, Sipitang, Tenom, Long Pasia, Membakut, Weston, Bahagian Pedalaman (Bawah)"),
        JakimZone("Selangor", "SGR01", "Gombak, Petaling, Sepang, Hulu Langat, Hulu Selangor, Shah Alam"),
        JakimZone("Selangor", "SGR02", "Kuala Selangor, Sabak Bernam"),
        JakimZone("Selangor", "SGR03", "Klang, Kuala Langat"),
        JakimZone("Sarawak", "SWK01", "Limbang, Lawas, Sundar, Trusan"),
        JakimZone("Sarawak", "SWK02", "Miri, Niah, Bekenu, Sibuti, Marudi"),
        JakimZone("Sarawak", "SWK03", "Pandan, Belaga, Suai, Tatau, Sebauh, Bintulu"),
        JakimZone("Sarawak", "SWK04", "Sibu, Mukah, Dalat, Song, Igan, Oya, Balingian, Kanowit, Kapit"),
        JakimZone("Sarawak", "SWK05", "Sarikei, Matu, Julau, Rajang, Daro, Bintangor, Belawai"),
        JakimZone("Sarawak", "SWK06", "Lubok Antu, Sri Aman, Roban, Debak, Kabong, Lingga, Engkelili, Betong, Spaoh, Pusa, Saratok"),
        JakimZone("Sarawak", "SWK07", "Serian, Simunjan, Samarahan, Sebuyau, Meludam"),
        JakimZone("Sarawak", "SWK08", "Kuching, Bau, Lundu, Sematan"),
        JakimZone("Sarawak", "SWK09", "Zon Khas (Kampung Patarikan)"),
        JakimZone("Terengganu", "TRG01", "Kuala Terengganu, Marang, Kuala Nerus"),
        JakimZone("Terengganu", "TRG02", "Besut, Setiu"),
        JakimZone("Terengganu", "TRG03", "Hulu Terengganu"),
        JakimZone("Terengganu", "TRG04", "Dungun, Kemaman"),
        JakimZone("Wilayah Persekutuan", "WLY01", "Kuala Lumpur, Putrajaya"),
        JakimZone("Wilayah Persekutuan", "WLY02", "Labuan"),
    )

    private val byCode = all.associateBy(JakimZone::code)

    fun find(code: String): JakimZone? = byCode[code]
    fun contains(code: String): Boolean = code.matches(Regex("^[A-Z]{3}[0-9]{2}$")) && code in byCode
}
