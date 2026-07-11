/* data.js — bMuslim sample day data (global, no module syntax)
 * Exposes: window.BM = { prayers, deeds, anytime, dayBanner, planner, strings, config }
 *
 * Sample day: Kuala Lumpur WLY01, Khamis (Thursday)
 * All Arabic texts and citations sourced ONLY from design/home-mockup.html.
 * Where the mockup has no Arabic text for a deed, arabic is set to null and
 * a short factual BM description is used.
 */
(function () {
  'use strict';

  /* ─── Config ──────────────────────────────────────────────────────────────── */
  var config = {
    zone:      'WLY01',
    city:      'Kuala Lumpur',
    dayName:   'Khamis',
    dateGreg:  'Khamis, 28 Jun 2026',
    dateHijri: '12 Muharram 1448',
    now:        15.00,    /* decimal hours, 15:00 */
    sleepTime:  23.00,
    wakeTime:    5.50,    /* 05:30 */
    dayStart:    3.00,    /* timeline start */
    dayEnd:     23.50,    /* timeline end */
    pxPerHour:  98
  };

  /* ─── Prayer times ────────────────────────────────────────────────────────── */
  /* All times as decimal hours. Syuruk is not a prayer but anchors deed windows. */
  var prayers = [
    { id: 'subuh',   name: 'Subuh',   time: 5.983  },  /* 05:59 */
    { id: 'syuruk',  name: 'Syuruk',  time: 7.183, isSyuruk: true }, /* 07:11 — anchor only */
    { id: 'zohor',   name: 'Zohor',   time: 13.35  },  /* 13:21 */
    { id: 'asar',    name: 'Asar',    time: 16.75  },  /* 16:45 */
    { id: 'maghrib', name: 'Maghrib', time: 19.483 },  /* 19:29 */
    { id: 'isyak',   name: 'Isyak',   time: 20.70  }   /* 20:42 */
  ];

  /* ─── Deeds ───────────────────────────────────────────────────────────────── */
  /*
   * Fields:
   *   id            string
   *   title         string — Bahasa Melayu display name
   *   type          string — Salah | Dhikr | Quran | Sadaqah | Adab (drives icon)
   *   ruling        string — fardh | muakkadah | sunnah | mandub
   *   windowStart   number — decimal hours, when the Window opens
   *   windowEnd     number — decimal hours, when the Window closes
   *   act           number — decimal hours, recommended act time (act-marker position)
   *   arabic        string | null — only values sourced from home-mockup.html
   *   transliteration string | null
   *   translation   string — Bahasa Melayu meaning
   *   description   string | null — short BM description when arabic is null
   *   citation      object | null
   */

  var deeds = [
    {
      id: 'tahajud',
      title: 'Solat Tahajud',
      type: 'Salah',
      ruling: 'muakkadah',
      windowStart: 3.50,   /* ~03:30 — last third of the night */
      windowEnd:   5.50,   /* 05:30 */
      act:         3.90,
      arabic: null,
      transliteration: null,
      translation: null,
      description: 'Solat sunat malam yang dilaksanakan pada sepertiga akhir malam, sebelum masuk waktu Subuh.',
      citation: null
    },
    {
      id: 'zikir-pagi',
      title: 'Zikir pagi',
      type: 'Dhikr',
      ruling: 'muakkadah',
      windowStart: 5.983,  /* Subuh */
      windowEnd:   7.183,  /* Syuruk */
      act:         6.15,
      /* Sourced verbatim from home-mockup.html detail screen */
      arabic: 'اللّٰهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُور',
      transliteration: 'Allahumma bika asbahna, wa bika amsayna, wa bika nahya wa bika namutu wa ilaikan-nushur',
      translation: 'Ya Allah, dengan-Mu kami memasuki waktu pagi, dan dengan-Mu kami memasuki waktu petang; dengan-Mu kami hidup dan dengan-Mu kami mati, dan kepada-Mu tempat kembali.',
      description: null,
      citation: {
        source: 'Sunan Abu Dawud',
        grade:  'Hasan',
        sunnahUrl: 'https://sunnah.com/abudawud:5068',
        fatwaUrl:  'https://www.e-fatwa.gov.my'
      }
    },
    {
      id: 'subuh-fardhu',
      title: 'Solat Subuh',
      type: 'Salah',
      ruling: 'fardh',
      windowStart: 5.983,
      windowEnd:   7.183,
      act:         5.983,
      arabic: null,
      transliteration: null,
      translation: null,
      description: 'Solat fardhu Subuh, dua rakaat sebelum terbit matahari.',
      citation: null
    },
    {
      id: 'dhuha',
      title: 'Solat Dhuha',
      type: 'Salah',
      ruling: 'sunnah',
      windowStart: 7.50,   /* ~07:30 */
      windowEnd:   13.35,  /* Zohor */
      act:         8.40,
      arabic: null,
      transliteration: null,
      translation: null,
      description: 'Solat sunat Dhuha, minimum dua rakaat, selepas matahari naik sepenggalah hingga sebelum Zohor.',
      citation: null
    },
    {
      id: 'zohor-fardhu',
      title: 'Solat Zohor',
      type: 'Salah',
      ruling: 'fardh',
      windowStart: 13.35,
      windowEnd:   16.75,
      act:         13.35,
      arabic: null,
      transliteration: null,
      translation: null,
      description: 'Solat fardhu Zohor, empat rakaat.',
      citation: null
    },
    {
      id: 'asar-fardhu',
      title: 'Solat Asar',
      type: 'Salah',
      ruling: 'fardh',
      windowStart: 16.75,
      windowEnd:   19.483,
      act:         16.75,
      arabic: null,
      transliteration: null,
      translation: null,
      description: 'Solat fardhu Asar, empat rakaat.',
      citation: null
    },
    {
      id: 'zikir-petang',
      title: 'Zikir petang',
      type: 'Dhikr',
      ruling: 'muakkadah',
      windowStart: 16.75,  /* Asar */
      windowEnd:   19.483, /* Maghrib */
      act:         17.10,
      arabic: null,
      transliteration: null,
      translation: null,
      description: 'Zikir dan doa petang, dibaca selepas Asar hingga sebelum Maghrib.',
      citation: null
    },
    {
      id: 'maghrib-fardhu',
      title: 'Solat Maghrib',
      type: 'Salah',
      ruling: 'fardh',
      windowStart: 19.483,
      windowEnd:   20.70,
      act:         19.483,
      arabic: null,
      transliteration: null,
      translation: null,
      description: 'Solat fardhu Maghrib, tiga rakaat selepas matahari terbenam.',
      citation: null
    },
    {
      id: 'isyak-fardhu',
      title: 'Solat Isyak',
      type: 'Salah',
      ruling: 'fardh',
      windowStart: 20.70,
      windowEnd:   23.50,
      act:         20.70,
      arabic: null,
      transliteration: null,
      translation: null,
      description: 'Solat fardhu Isyak, empat rakaat.',
      citation: null
    },
    {
      id: 'al-mulk',
      title: 'Baca Surah Al-Mulk',
      type: 'Quran',
      ruling: 'sunnah',
      windowStart: 20.70,  /* Isyak */
      windowEnd:   23.00,  /* tidur */
      act:         21.90,
      arabic: null,
      transliteration: null,
      translation: null,
      description: 'Membaca Surah Al-Mulk (surah ke-67) sebelum tidur.',
      citation: null
    }
  ];

  /* ─── Day banner ──────────────────────────────────────────────────────────── */
  /* Sawm (fasting) is a day-span state, shown as a banner not a Block */
  var dayBanner = {
    id:      'puasa-khamis',
    title:   'Puasa sunat Khamis',
    type:    'Sawm',
    ruling:  'sunnah',
    arabic:  null,
    description: 'Puasa sunat pada hari Khamis.',
    citation: null
  };

  /* ─── Anytime tray ────────────────────────────────────────────────────────── */
  /* No fixed Window; lived in the Anytime tray. No invented citations. */
  var anytime = [
    {
      id: 'istighfar',
      title: 'Istighfar',
      type: 'Dhikr',
      ruling: 'sunnah',
      arabic: null,
      description: 'Memohon keampunan pada bila-bila masa sepanjang hari.',
      citation: null
    },
    {
      id: 'selawat',
      title: 'Selawat ke atas Nabi',
      type: 'Dhikr',
      ruling: 'sunnah',
      arabic: null,
      description: 'Berselawat ke atas Nabi Muhammad SAW pada bila-bila masa.',
      citation: null
    },
    {
      id: 'sedekah',
      title: 'Sedekah',
      type: 'Sadaqah',
      ruling: 'sunnah',
      arabic: null,
      description: 'Memberi sedekah dalam apa jua bentuk.',
      citation: null
    }
  ];

  /* ─── Planner suggestions ─────────────────────────────────────────────────── */
  var planner = {
    weekDays: [
      { short: 'Ahd', num: 24, fast: false, active: false },
      { short: 'Isn', num: 25, fast: true,  active: false },
      { short: 'Sel', num: 26, fast: false, active: false },
      { short: 'Rab', num: 27, fast: false, active: false },
      { short: 'Kha', num: 28, fast: true,  active: true  },
      { short: 'Jum', num: 29, fast: true,  active: false },
      { short: 'Sab', num: 30, fast: true,  active: false }
    ],
    suggestions: [
      {
        id:    'puasa-isnin',
        title: 'Puasa sunat Isnin',
        desc:  'Isnin, 2 Julai',
        type:  'Sawm',
        icon:  'moon'
      },
      {
        id:    'ayyam-albidh',
        title: 'Ayyam al-Bidh (13, 14, 15 Muharram)',
        desc:  '29 Jun hingga 1 Julai · puasa 3 hari',
        type:  'Sawm',
        icon:  'moon'
      },
      {
        id:    'al-kahf',
        title: 'Baca Surah Al-Kahf',
        desc:  'Setiap Jumaat',
        type:  'Quran',
        icon:  'quran'
      },
      {
        id:    'dhuha-cadangan',
        title: 'Mula solat Dhuha',
        desc:  'Cadangan: 2 rakaat sehari',
        type:  'Salah',
        icon:  'salat'
      }
    ]
  };


  /* ─── JAKIM prayer-time zones ─────────────────────────────────────────────── */
  /* The published JAKIM e-Solat zone list. Both the onboarding select and the
     settings select are populated from this single dataset. */
  var zones = [
    { state: 'Wilayah Persekutuan', code: 'WLY01', area: 'Kuala Lumpur, Putrajaya' },
    { state: 'Wilayah Persekutuan', code: 'WLY02', area: 'Labuan' },
    { state: 'Selangor', code: 'SGR01', area: 'Gombak, Petaling, Sepang, Hulu Langat, Hulu Selangor, Shah Alam' },
    { state: 'Selangor', code: 'SGR02', area: 'Kuala Selangor, Sabak Bernam' },
    { state: 'Selangor', code: 'SGR03', area: 'Klang, Kuala Langat' },
    { state: 'Johor', code: 'JHR01', area: 'Pulau Aur, Pulau Pemanggil' },
    { state: 'Johor', code: 'JHR02', area: 'Johor Bahru, Kota Tinggi, Mersing, Kulai' },
    { state: 'Johor', code: 'JHR03', area: 'Kluang, Pontian' },
    { state: 'Johor', code: 'JHR04', area: 'Batu Pahat, Muar, Segamat, Gemas Johor, Tangkak' },
    { state: 'Kedah', code: 'KDH01', area: 'Kota Setar, Kubang Pasu, Pokok Sena' },
    { state: 'Kedah', code: 'KDH02', area: 'Kuala Muda, Yan, Pendang' },
    { state: 'Kedah', code: 'KDH03', area: 'Padang Terap, Sik' },
    { state: 'Kedah', code: 'KDH04', area: 'Baling' },
    { state: 'Kedah', code: 'KDH05', area: 'Bandar Baharu, Kulim' },
    { state: 'Kedah', code: 'KDH06', area: 'Langkawi' },
    { state: 'Kedah', code: 'KDH07', area: 'Puncak Gunung Jerai' },
    { state: 'Kelantan', code: 'KTN01', area: 'Kota Bharu, Bachok, Pasir Puteh, Tumpat, Pasir Mas, Tanah Merah, Machang, Kuala Krai, Mukim Chiku' },
    { state: 'Kelantan', code: 'KTN03', area: 'Gua Musang (Galas dan Bertam), Jeli, Jajahan Kecil Lojing' },
    { state: 'Melaka', code: 'MLK01', area: 'Seluruh Negeri Melaka' },
    { state: 'Negeri Sembilan', code: 'NGS01', area: 'Tampin, Jempol' },
    { state: 'Negeri Sembilan', code: 'NGS02', area: 'Jelebu, Kuala Pilah, Rembau' },
    { state: 'Negeri Sembilan', code: 'NGS03', area: 'Port Dickson, Seremban' },
    { state: 'Pahang', code: 'PHG01', area: 'Pulau Tioman' },
    { state: 'Pahang', code: 'PHG02', area: 'Kuantan, Pekan, Muadzam Shah' },
    { state: 'Pahang', code: 'PHG03', area: 'Jerantut, Temerloh, Maran, Bera, Chenor, Jengka' },
    { state: 'Pahang', code: 'PHG04', area: 'Bentong, Lipis, Raub' },
    { state: 'Pahang', code: 'PHG05', area: 'Genting Sempah, Janda Baik, Bukit Tinggi' },
    { state: 'Pahang', code: 'PHG06', area: 'Cameron Highlands, Genting Highlands, Bukit Fraser' },
    { state: 'Perak', code: 'PRK01', area: 'Tapah, Slim River, Tanjung Malim' },
    { state: 'Perak', code: 'PRK02', area: 'Kuala Kangsar, Sungai Siput, Ipoh, Batu Gajah, Kampar' },
    { state: 'Perak', code: 'PRK03', area: 'Lenggong, Pengkalan Hulu, Grik' },
    { state: 'Perak', code: 'PRK04', area: 'Temengor, Belum' },
    { state: 'Perak', code: 'PRK05', area: 'Kampung Gajah, Teluk Intan, Bagan Datuk, Seri Iskandar, Beruas, Parit, Lumut, Sitiawan, Pulau Pangkor' },
    { state: 'Perak', code: 'PRK06', area: 'Selama, Taiping, Bagan Serai, Parit Buntar' },
    { state: 'Perak', code: 'PRK07', area: 'Bukit Larut' },
    { state: 'Perlis', code: 'PLS01', area: 'Kangar, Padang Besar, Arau' },
    { state: 'Pulau Pinang', code: 'PNG01', area: 'Seluruh Negeri Pulau Pinang' },
    { state: 'Sabah', code: 'SBH01', area: 'Bahagian Sandakan (Timur), Bandar Sandakan, Bukit Garam, Semawang, Temanggong, Tambisan, Sukau' },
    { state: 'Sabah', code: 'SBH02', area: 'Beluran, Telupid, Pinangah, Terusan, Kuamut, Bahagian Sandakan (Barat)' },
    { state: 'Sabah', code: 'SBH03', area: 'Lahad Datu, Silabukan, Kunak, Sahabat, Semporna, Tungku, Bahagian Tawau (Timur)' },
    { state: 'Sabah', code: 'SBH04', area: 'Bandar Tawau, Balong, Merotai, Kalabakan, Bahagian Tawau (Barat)' },
    { state: 'Sabah', code: 'SBH05', area: 'Kudat, Kota Marudu, Pitas, Pulau Banggi, Bahagian Kudat' },
    { state: 'Sabah', code: 'SBH06', area: 'Gunung Kinabalu' },
    { state: 'Sabah', code: 'SBH07', area: 'Kota Kinabalu, Ranau, Kota Belud, Tuaran, Penampang, Papar, Putatan, Bahagian Pantai Barat' },
    { state: 'Sabah', code: 'SBH08', area: 'Pensiangan, Keningau, Tambunan, Nabawan, Bahagian Pedalaman (Atas)' },
    { state: 'Sabah', code: 'SBH09', area: 'Beaufort, Kuala Penyu, Sipitang, Tenom, Long Pasia, Membakut, Weston, Bahagian Pedalaman (Bawah)' },
    { state: 'Sarawak', code: 'SWK01', area: 'Limbang, Lawas, Sundar, Trusan' },
    { state: 'Sarawak', code: 'SWK02', area: 'Miri, Niah, Bekenu, Sibuti, Marudi' },
    { state: 'Sarawak', code: 'SWK03', area: 'Pandan, Belaga, Suai, Tatau, Sebauh, Bintulu' },
    { state: 'Sarawak', code: 'SWK04', area: 'Sibu, Mukah, Dalat, Song, Igan, Oya, Balingian, Kanowit, Kapit' },
    { state: 'Sarawak', code: 'SWK05', area: 'Sarikei, Matu, Julau, Rajang, Daro, Bintangor, Belawai' },
    { state: 'Sarawak', code: 'SWK06', area: 'Lubok Antu, Sri Aman, Roban, Debak, Kabong, Lingga, Engkilili, Betong, Spaoh, Pusa, Saratok' },
    { state: 'Sarawak', code: 'SWK07', area: 'Serian, Simunjan, Samarahan, Sebuyau, Meludam' },
    { state: 'Sarawak', code: 'SWK08', area: 'Kuching, Bau, Lundu, Sematan' },
    { state: 'Sarawak', code: 'SWK09', area: 'Zon Khas (Kampung Patarikan)' },
    { state: 'Terengganu', code: 'TRG01', area: 'Kuala Terengganu, Marang, Kuala Nerus' },
    { state: 'Terengganu', code: 'TRG02', area: 'Besut, Setiu' },
    { state: 'Terengganu', code: 'TRG03', area: 'Hulu Terengganu' },
    { state: 'Terengganu', code: 'TRG04', area: 'Dungun, Kemaman' }
  ];

  /* ─── Shared UI strings ────────────────────────────────────────────────────── */
  var strings = {
    /* Nav */
    navHariIni:   'Hari ini',
    navPerancang: 'Perancang',
    navTetapan:   'Tetapan',

    /* App header */
    salam:        'Assalamualaikum',
    wordmark:     'bMuslim',

    /* Timeline */
    timelineLabel:   'Garis masa hari ini',
    backToNow:       'Kembali ke sekarang',
    needleNow:       'Sekarang',
    noBlocksNeedle:  'Tiada amalan pada masa ini',

    /* Detail panel */
    detailNow:       'Sekarang',
    markDone:        'Tandai selesai',
    markUndone:      'Batal tandaan',
    selectorLabel:   'Amalan pada masa ini',
    citationLabel:   'Rujukan',
    meaningLabel:    'Maksud',
    windowLabel:     'Tempoh',

    /* Chip labels */
    typeSalah:       'Solat',
    typeDhikr:       'Zikir',
    typeQuran:       'Quran',
    typeSadaqah:     'Sedekah',
    typeAdab:        'Adab',
    typeSawm:        'Puasa',

    rulingFardh:      'Fardhu',
    rulingMuakkadah:  'Sunnah Muakkadah',
    rulingSunnah:     'Sunnah',
    rulingMandub:     'Mandub',

    /* Anytime tray */
    anytimeLabel:    'Bila-bila masa',
    anytimeDesc:     'Amalan tanpa waktu tetap',

    /* Progress ring */
    progressToday:   'hari ini',

    /* Citation links */
    viewSunnah:      'Lihat di sunnah.com',
    viewFatwa:       'Portal e-Fatwa',

    /* Planner */
    plannerMonth:    'Jun 2026',
    plannerHijri:    'Muharram 1448 H',
    suggestionsLabel: 'Cadangan mengikut sunnah',

    /* Settings */
    settingsZone:    'Zon waktu solat',
    settingsSleep:   'Waktu tidur',
    settingsWake:    'Waktu bangun',
    settingsWakeHelper: 'Menentukan tetingkap Tahajud dan Al-Mulk',
    settingsLevel:   'Tahap',
    settingsLevelBeginner: 'Pemula',
    settingsLevelAdvanced:  'Lanjutan',
    settingsLang:    'Bahasa',
    settingsNotif:   'Notifikasi',
    settingsNotifPrayer:    'Waktu solat',
    settingsNotifZikirPagi: 'Zikir pagi',
    settingsNotifZikirPetang: 'Zikir petang',
    settingsNotifAlMulk:    'Al-Mulk',
    settingsNotifSummary:   'Ringkasan harian',
    settingsSave:    'Simpan',

    /* Onboarding */
    onboardStep1Title: 'Bahasa',
    onboardStep2Title: 'Zon waktu solat',
    onboardStep3Title: 'Waktu tidur & bangun',
    onboardStep3Helper: 'Menentukan tetingkap Tahajud & Al-Mulk',
    onboardStep4Title: 'Tahap amalan',
    onboardBeginnerTitle: 'Pemula',
    onboardBeginnerDesc:  'Amalan asas harian: 5 waktu solat, zikir pagi & petang.',
    onboardAdvancedTitle: 'Lanjutan',
    onboardAdvancedDesc:  'Tambah Tahajud, Dhuha, puasa sunat, dan amalan lain.',
    onboardStart:  'Mula',
    onboardSkip:   'Langkau',

    /* Landing page */
    heroHeadline:   'Amalan terbaik untuk saat ini',
    heroSubtext:    'Garis masa harian berasaskan waktu solat. Tiada tekaan, hanya amalan.',
    heroCtaPrimary: 'Buka aplikasi',
    heroCtaSecondary: 'Lihat cara ia berfungsi',

    howTitle:       'Bagaimana ia berfungsi',
    howCaption1:    'Berpaut pada waktu solat',
    howCaption2:    'Tetingkap, bukan peringatan',
    howCaption3:    'Jarum menunjukkan sekarang',

    authTitle:      'Kandungan yang disahkan',
    authDesc:       'Setiap amalan disertakan rujukan hadith dan darjat kesahihannya. Tiada kandungan yang direka.',

    dayAwareTitle:  'Hari yang sedar',
    dayAwareDesc:   'Puasa sunat Isnin & Khamis, Ayyam al-Bidh, dan Al-Kahf pada Jumaat muncul secara automatik.',

    privacyTitle:   'Tiada akaun. Tiada awan.',
    privacyLine1:   'Tiada akaun diperlukan.',
    privacyLine2:   'Data anda tersimpan di peranti sahaja.',
    privacyLine3:   'Berfungsi sepenuhnya tanpa sambungan internet.',

    footerAttrib:   'Dibina untuk Malaysia',
    footerData:     'Data waktu solat: JAKIM',
    footerOnboard:  'Persediaan awal',

    /* CTA label — one label, one intent */
    ctaOpen: 'Buka aplikasi'
  };

  /* ─── Export ───────────────────────────────────────────────────────────────── */
  window.BM = {
    config:    config,
    prayers:   prayers,
    zones:     zones,
    deeds:     deeds,
    dayBanner: dayBanner,
    anytime:   anytime,
    planner:   planner,
    strings:   strings
  };
}());
