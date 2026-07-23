package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {

val ABRP_COMPATIBILITY = Compatibility(
        name = "ABRP - EV Trip Planner",
        packageName = "com.iternio.abrpapp",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1DA462,
        targets = listOf(AppTarget(version = "7.1.4", versionCode = 5924))
    )

val ACCUBATTERY_COMPATIBILITY = Compatibility(
        name = "AccuBattery",
        packageName = "com.digibites.accubattery",
        appIconColor = 0x00BCD4,
        targets = listOf(AppTarget(version = "2.1.8", versionCode = 201008))
    )

val ACCUWEATHER_COMPATIBILITY = Compatibility(
        name = "AccuWeather",
        packageName = "com.accuweather.android",
        appIconColor = 0xF25C1B,
        targets = listOf(AppTarget(version = "21.1.13-1-rc", versionCode = 210113001))
    )

val ACE_EXPLORER_COMPATIBILITY = Compatibility(
        name = "Ace Ex File Manager",
        packageName = "com.ace.ex.file.manager",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.6.0.4", versionCode = 48))
    )

val ACTIVITYLAUNCHER_COMPATIBILITY = Compatibility(
        name = "Activity Launcher",
        packageName = "de.szalkowski.activitylauncher",
        appIconColor = 0x3F51B5,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "2.5.0-dev", versionCode = 20500))
    )

val ADGUARD_COMPATIBILITY = Compatibility(
        name = "AdGuard Nightly",
        packageName = "com.adguard.android",
        appIconColor = 0x67B346,
        targets = listOf(AppTarget(version = "4.14.68"))
    )

val ADGUARD_VPN_COMPATIBILITY = Compatibility(
        name = "AdGuard VPN",
        packageName = "com.adguard.vpn",
        appIconColor = 0x67B346,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "2.17.0", versionCode = 740188))
    )

val AIDA64_COMPATIBILITY = Compatibility(
        name = "AIDA64",
        packageName = "com.finalwire.aida64",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.21", versionCode = 221))
    )

val AIO_STREAMER_COMPATIBILITY = Compatibility(
        name = "AIO Streamer",
        packageName = "com.streamdev.aiostreamer",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xD32F2F,
        targets = listOf(AppTarget(version = "6.6.4", versionCode = 6643))
    )

val AIOSTREAMER_COMPATIBILITY = Compatibility(
        name = "AIO Streamer",
        packageName = "com.streamdev.aiostreamer",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "6.6.4"))
    )

val AISCORE_COMPATIBILITY = Compatibility(
        name = "AiScore",
        packageName = "com.onesports.score",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = "4.2.7", versionCode = 292))
    )

val ALIGHT_MOTION_COMPATIBILITY = Compatibility(
        name = "Alight Motion",
        packageName = "com.alightcreative.motion",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE11D48,
        targets = listOf(AppTarget(version = "5.0.273.1028425", versionCode = 1028425))
    )

val ALLREADER_COMPATIBILITY = Compatibility(
        name = "All Reader",
        packageName = "alldocumentreader.office.viewer.filereader.pdfviewer",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.2.3", versionCode = 52))
    )

val AMAZON_IN_COMPATIBILITY = Compatibility(
        name = "Amazon India",
        packageName = "in.amazon.mShop.android.shopping",
        appIconColor = 0xFF9900,
        targets = listOf(AppTarget(version = "32.12.4.300", versionCode = 1241319416))
    )

val AMAZON_MUSIC_COMPATIBILITY = Compatibility(
        name = "Amazon Music",
        packageName = "com.amazon.mp3",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF9900,
        targets = listOf(AppTarget(version = "26.23.0", versionCode = 526230010))
    )

val AMAZON_SHOPPING_COMPATIBILITY = Compatibility(
        name = "Amazon Shopping",
        packageName = "com.amazon.mShop.android.shopping",
        appIconColor = 0xFF9900,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "32.13.2.100", versionCode = 1241320216))
    )

val AMBOSS_COMPATIBILITY = Compatibility(
        name = "AMBOSS",
        packageName = "com.amboss.medical.knowledge",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xCC1F3C,
        targets = listOf(AppTarget(version = "2.115.1.4408", versionCode = 4408))
    )

val AMOLEDPIX_COMPATIBILITY = Compatibility(
        name = "AmoledPix",
        packageName = "com.androholic.amoledpix",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "7.2", versionCode = 80))
    )

val AMPERE_COMPATIBILITY = Compatibility(
        name = "Ampere",
        packageName = "com.gombosdev.ampere",
        appIconColor = 0xFF9800,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "v4.37.0", versionCode = 292))
    )

val AMPEREFLOW_COMPATIBILITY = Compatibility(
        name = "AmpereFlow",
        packageName = "com.androxus.batterymeter",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x00C853,
        targets = listOf(AppTarget(version = "2.0.8", versionCode = 108))
    )

val ANATOMY_COMPATIBILITY = Compatibility(
        name = "Anatomy",
        packageName = "air.com.musclemotion.anatomy",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "3.2.1", versionCode = 1004185))
    )

val ANDROID_VERIFIER_COMPATIBILITY = Compatibility(
        name = "Android Developer Verifier",
        packageName = "com.google.android.verifier",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4285F4,
        targets = listOf(AppTarget(version = "1.0.943911795", versionCode = 65354))
    )

val ANDROPODS_COMPATIBILITY = Compatibility(
        name = "AndroPods",
        packageName = "pro.vitalii.andropods",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1DA1F2,
        targets = listOf(AppTarget(version = "1.5.28", versionCode = 84))
    )

val ANIME_DEPTH_WALLPAPERS_COMPATIBILITY = Compatibility(
        name = "Anime Depth Wallpapers",
        packageName = "com.jndapp.anime.depth.live.wallpaper",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x6A1B9A,
        targets = listOf(AppTarget(version = "1.0.4", versionCode = 5))
    )

val ANYSHARE_COMPATIBILITY = Compatibility(
        name = "SHAREit- Transfer,Share Files",
        packageName = "com.lenovo.anyshare.gps",
        appIconColor = 0xFF4B00,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "6.54.58_UD", versionCode = 4565458))
    )

val APKMIRROR_INSTALLER_COMPATIBILITY = Compatibility(
        name = "APKMirror Installer",
        packageName = "com.apkmirror.helper.prod",
        appIconColor = 0xFF9800,
        targets = listOf(AppTarget(version = "2.0.3 (41-d04e542)", versionCode = 41))
    )

val APPCLONER_COMPATIBILITY = Compatibility(
        name = "AppCloner",
        packageName = "com.applisto.appcloner",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x7B1FA2,
        targets = listOf(AppTarget(version = "3.6.8", versionCode = 26062918))
    )

val AUTOMATE_COMPATIBILITY = Compatibility(
        name = "Automate",
        packageName = "com.llamalab.automate",
        appIconColor = 0xFF6600,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "1.53.1", versionCode = 265))
    )

val AZRECORDER_COMPATIBILITY = Compatibility(
        name = "AZ Screen Recorder - No Root",
        packageName = "com.hecorat.screenrecorder.free",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "6.9.6", versionCode = 70384))
    )

val BALANCE_COMPATIBILITY = Compatibility(
        name = "Balance: Meditation & Sleep",
        packageName = "com.elevatelabs.geonosis",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4A90D9,
        targets = listOf(AppTarget(version = "1.204.0", versionCode = 990))
    )

val BATTERYGURU_COMPATIBILITY = Compatibility(
        name = "Battery Guru",
        packageName = "com.paget96.batteryguru",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1B7080,
        targets = listOf(AppTarget(version = "2.5.0.5", versionCode = 712))
    )

val BATTERYONE_COMPATIBILITY = Compatibility(
        name = "Battery One - Battery Monitor",
        packageName = "com.oneapps.batteryone",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.5.6", versionCode = 459))
    )

val BATTERYPODS_COMPATIBILITY = Compatibility(
        name = "BatteryPods",
        packageName = "com.sumyapplications.bluetooth.earphone",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.20", versionCode = 7200))
    )

val BEREAL_COMPATIBILITY = Compatibility(
        name = "BeReal.",
        packageName = "com.bereal.ft",
        appIconColor = 0x000000,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "3.88.1", versionCode = 3588172))
    )

val BETTERSLEEP_COMPATIBILITY = Compatibility(
        name = "BetterSleep: Relax and Sleep",
        packageName = "ipnossoft.rma.free",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x3F51B5,
        targets = listOf(AppTarget(version = "26.10", versionCode = 26458))
    )

val BIGO_LIVE_COMPATIBILITY = Compatibility(
        name = "BIGO LIVE",
        packageName = "sg.bigo.live",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = "6.51.3", versionCode = 4119))
    )

val BLACK_SCREEN_COMPATIBILITY = Compatibility(
        name = "Black Screen",
        packageName = "io.japp.blackscreen",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A1A1A,
        targets = listOf(AppTarget(version = "2.0.6", versionCode = 96))
    )

val BLAZAR_VPN_COMPATIBILITY = Compatibility(
        name = "Blazar VPN",
        packageName = "io.deveem.vpn.global",
        appIconColor = 0x2979FF,
        targets = listOf(AppTarget(version = "2.4.0(gl)", versionCode = 103))
    )

val BLOCKBLAST_COMPATIBILITY = Compatibility(
        name = "Block Blast!",
        packageName = "com.block.juggle",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "10.4.3", versionCode = 10430))
    )

val BLOCKERHERO_COMPATIBILITY = Compatibility(
        name = "BlockerHero",
        packageName = "com.blockerhero",
        appIconColor = 0xFF5252,
        targets = listOf(AppTarget(version = "1.5.0"))
    )

val BLOCKPUZZLE_COMPATIBILITY = Compatibility(
        name = "Block Puzzle",
        packageName = "game.puzzle.blockpuzzle",
        appIconColor = 0x3B82F6,
        targets = listOf(AppTarget(version = "6.0", versionCode = 60))
    )

val BLOCKWATCH_COMPATIBILITY = Compatibility(
        name = "BlockWatch",
        packageName = "com.blockwatch.blockwatchapp",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.0.5", versionCode = 24))
    )

val BLOOMBERG_COMPATIBILITY = Compatibility(
        name = "Bloomberg: The Financial App",
        packageName = "com.bloomberg.android.plus",
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "6.61.1", versionCode = 5530538))
    )

val BLUETOOTH_VOLUME_MANAGER_COMPATIBILITY = Compatibility(
        name = "Bluetooth Volume Manager",
        packageName = "eu.darken.bluemusic",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "3.4.3", versionCode = 30403000))
    )

val BLURAMS_COMPATIBILITY = Compatibility(
        name = "Blurams",
        packageName = "com.blurams.ipc",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.1049.4.908", versionCode = 1908))
    )

val BLURWALL_COMPATIBILITY = Compatibility(
        name = "BlurWall",
        packageName = "apps.automan.blurwallpaper",
        appIconColor = 0x42A5F5,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "2.9.4", versionCode = 29))
    )

val BOLDVOICE_COMPATIBILITY = Compatibility(
        name = "BoldVoice: American Accent",
        packageName = "com.wellocution.androidapp",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF5A36,
        targets = listOf(AppTarget(version = "4.4.3", versionCode = 392))
    )

val BOOSTCAMP_COMPATIBILITY = Compatibility(
        name = "Boostcamp: Workout & Gym Plan",
        packageName = "com.bpmhealth.boostcamp",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x00A6FF,
        targets = listOf(AppTarget(version = "262", versionCode = 353))
    )

val BOULTBOX_COMPATIBILITY = Compatibility(
        name = "BoultBox",
        packageName = "com.boultbox",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "1.2.6", versionCode = 20))
    )

val BOXBOX_COMPATIBILITY = Compatibility(
        name = "Box Box",
        packageName = "club.boxbox.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE10600,
        targets = listOf(AppTarget(version = "5.4.13", versionCode = 256))
    )

val BRAVE_COMPATIBILITY = Compatibility(
        name = "Brave Browser Nightly",
        packageName = "com.brave.browser_nightly",
        appIconColor = 0xFF6B35,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "1.94.85", versionCode = 429408504))
    )

val BUZZCAST_COMPATIBILITY = Compatibility(
        name = "BuzzCast",
        packageName = "com.guochao.faceshow",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x7C3AED,
        targets = listOf(AppTarget(version = "3.2.82", versionCode = 3282))
    )

val CALCULATOR_COMPATIBILITY = Compatibility(
        name = "All-In-One Calculator",
        packageName = "all.in.one.calculator",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.3.4", versionCode = 334))
    )

val CALIMOTO_COMPATIBILITY = Compatibility(
        name = "calimoto",
        packageName = "com.calimoto.calimoto",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "2026.07.3", versionCode = 620))
    )

val CALLRECORDER_COMPATIBILITY = Compatibility(
        name = "Cube ACR",
        packageName = "com.catalinagroup.callrecorder",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "2.4.281"))
    )

val CALM_COMPATIBILITY = Compatibility(
        name = "Calm: Sleep & Meditation",
        packageName = "com.calm.android",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4A90D9,
        targets = listOf(AppTarget(version = "6.100.1", versionCode = 4120448))
    )

val CALORY_COMPATIBILITY = Compatibility(
        name = "Calory - Calorie Counter & Diet",
        packageName = "com.funnmedia.calory",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "3.6.2", versionCode = 201))
    )

val CAMSCANNER_COMPATIBILITY = Compatibility(
        name = "CamScanner",
        packageName = "com.intsig.camscanner",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x19BCAA,
        targets = listOf(AppTarget(version = "7.20.5.2606250000", versionCode = 72051))
    )

val CANVA_COMPATIBILITY = Compatibility(
        name = "Canva",
        packageName = "com.canva.editor",
        appIconColor = 0x8B3DFF,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "2.369.0", versionCode = 29633241))
    )

val CAPCUT_COMPATIBILITY = Compatibility(
        name = "CapCut",
        packageName = "com.lemon.lvoverseas",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x111827,
        targets = listOf(AppTarget(version = "18.8.0", versionCode = 18800100))
    )

val CAPOD_COMPATIBILITY = Compatibility(
        name = "CAPod",
        packageName = "eu.darken.capod",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xF5A623,
        targets = listOf(AppTarget(version = "5.2.1", versionCode = 50201000))
    )

val CARBMANAGER_COMPATIBILITY = Compatibility(
        name = "Carb Manager",
        packageName = "com.wombatapps.carbmanager",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "13.2.0", versionCode = 202))
    )

val CARBON_COMPATIBILITY = Compatibility(
        name = "Carbon",
        packageName = "com.joincarbon.nutrition",
        appIconColor = 0x111111,
        targets = listOf(AppTarget(version = "2.76.5891", versionCode = 1633629177))
    )

val CAROUSELL_COMPATIBILITY = Compatibility(
        name = "Carousell",
        packageName = "com.thecarousell.Carousell",
        appIconColor = 0xE7392C,
        targets = listOf(AppTarget(version = "2.463.9", versionCode = 10820))
    )

val CASETRACKER_1P3A_COMPATIBILITY = Compatibility(
        name = "Case Tracker - Immigration Status",
        packageName = "com.onepointthreeacres.CaseTracker",
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = null))
    )

val CASETRACKER_COMPATIBILITY = Compatibility(
        name = "Case Tracker",
        packageName = "com.saldous.casetracker",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.5.3", versionCode = 1052))
    )

val CASHEW_COMPATIBILITY = Compatibility(
        name = "Cashew",
        packageName = "com.budget.tracker_app",
        appIconColor = 0xFFB300,
        targets = listOf(AppTarget(version = "6.6.11", versionCode = 510))
    )

val CASTLETV_COMPATIBILITY = Compatibility(
        name = "Castle TV",
        packageName = "com.gxnet.castle.indiatv",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "1.1.3", versionCode = 30))
    )

val CHAMET_COMPATIBILITY = Compatibility(
        name = "Chamet- Live Video Chat & Meet",
        packageName = "com.hkfuliao.chamet",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "4.4.4", versionCode = 2026070211))
    )

val CHARGEMETER_COMPATIBILITY = Compatibility(
        name = "Charge Meter",
        packageName = "dev.km.android.chargemeter",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.9.7", versionCode = 77))
    )

val CHATGPT_COMPATIBILITY = Compatibility(
        name = "ChatGPT",
        packageName = "com.openai.chatgpt",
        appIconColor = 0x10A37F,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "1.2026.195", versionCode = 2619512))
    )

val CHEFKOCH_COMPATIBILITY = Compatibility(
        name = "Chefkoch",
        packageName = "de.pixelhouse",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "8.0.0", versionCode = 404020215))
    )

val CHEGG_COMPATIBILITY = Compatibility(
        name = "Chegg Study",
        packageName = "com.chegg",
        appIconColor = 0xEB7100,
        targets = listOf(AppTarget(version = "15.37.0", versionCode = 22455))
    )

val CHORDIFY_COMPATIBILITY = Compatibility(
        name = "Chordify - Guitar, Piano Chords",
        packageName = "net.chordify.chordify",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0A8282,
        targets = listOf(AppTarget(version = null))
    )

val CITIZEN_COMPATIBILITY = Compatibility(
        name = "Citizen",
        packageName = "sp0n.citizen",
        appIconColor = 0x0066FF,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "0.1301.0", versionCode = 1135))
    )

val CITYMAPPER_COMPATIBILITY = Compatibility(
        name = "Citymapper",
        packageName = "com.citymapper.app.release",
        appIconColor = 0x00A862,
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "11.55.2", versionCode = 1155090))
    )

val CLOAKED_COMPATIBILITY = Compatibility(
        name = "Cloaked",
        packageName = "app.android.cloaked",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0F172A,
        targets = listOf(AppTarget(version = "2.88.3", versionCode = 183))
    )

val CLUE_COMPATIBILITY = Compatibility(
        name = "Clue Period & Cycle Tracker",
        packageName = "com.clue.android",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE91E63,
        targets = listOf(AppTarget(version = "262.0", versionCode = 3181))
    )

val COLORNOTE_COMPATIBILITY = Compatibility(
        name = "ColorNote",
        packageName = "com.socialnmobile.dictapps.notepad.color.note",
        appIconColor = 0xF2C200,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "4.8.6", versionCode = 2104860))
    )

val CONTACTS_SYNC_COMPATIBILITY = Compatibility(
        name = "Contacts Sync",
        packageName = "com.lb.contacts_sync",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "3.75", versionCode = 275))
    )

val COOKED_COMPATIBILITY = Compatibility(
        name = "Cooked",
        packageName = "wiki.cooked",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "4.3.0", versionCode = 119))
    )

val COUNTDOWN_WIDGET_COMPATIBILITY = Compatibility(
        name = "Countdown Widget",
        packageName = "me.gira.widget.countdown",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "3.2.0", versionCode = 306))
    )

val COURSEHERO_COMPATIBILITY = Compatibility(
        name = "Course Hero",
        packageName = "com.coursehero.coursehero",
        appIconColor = 0xFFB700,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "4.11.3", versionCode = 590))
    )

val CPUZ_COMPATIBILITY = Compatibility(
        name = "CPU-Z",
        packageName = "com.cpuid.cpu_z",
        appIconColor = 0x2A3B4C,
        targets = listOf(AppTarget(version = "1.59", versionCode = 59))
    )

val CRICHEROES_COMPATIBILITY = Compatibility(
        name = "CricHeroes",
        packageName = "com.cricheroes.cricheroes.alpha",
        appIconColor = 0x1B5E20,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "26.7.2", versionCode = 490))
    )

val CRIMERADAR_COMPATIBILITY = Compatibility(
        name = "Crime Radar",
        packageName = "com.newsbreak.crimeradar",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "26.29.0", versionCode = 26290002))
    )

val CSPDF_COMPATIBILITY = Compatibility(
        name = "CamScanner PDF",
        packageName = "com.intsig.cspdf",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "3.13.7.202604030000", versionCode = 31372))
    )

val CXFILEEXPLORER_COMPATIBILITY = Compatibility(
        name = "CX File Explorer",
        packageName = "com.cxinventor.file.explorer",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1976D2,
        targets = listOf(
            AppTarget(version = "2.7.6", versionCode = 276),
            AppTarget(version = "2.7.1", versionCode = 271)
        )
    )

val DAILYHUNT_COMPATIBILITY = Compatibility(
        name = "Dailyhunt",
        packageName = "com.eterno",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "34.0.4", versionCode = 10314))
    )

val DEEPSTASH_COMPATIBILITY = Compatibility(
        name = "Deepstash",
        packageName = "com.deepstash",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x22B776,
        targets = listOf(AppTarget(version = "31.0.0", versionCode = 4000259))
    )

val DEEZER_COMPATIBILITY = Compatibility(
        name = "Deezer",
        packageName = "deezer.android.app",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xA238FF,
        targets = listOf(AppTarget(version = "9.0.18.3", versionCode = 9001803))
    )

val DEFIT_COMPATIBILITY = Compatibility(
        name = "DeFit",
        packageName = "com.fitness.debugger",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "0.9.3", versionCode = 930))
    )

val DEPTH_LIVE_WALLPAPER_COMPATIBILITY = Compatibility(
        name = "Depth Live Wallpaper",
        packageName = "com.jndapp.depth.live.wallpaper",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.1.06", versionCode = 35))
    )

val DEVCHECK_COMPATIBILITY = Compatibility(
        name = "DevCheck - Device & System Info",
        packageName = "flar2.devcheck",
        appIconColor = 0x1976D2,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(
            AppTarget(version = "6.46", versionCode = 646),
            AppTarget(version = "6.46", versionCode = 646),
        )
    )

val DICTIONARY_COMPATIBILITY = Compatibility(
        name = "Dictionary.com",
        packageName = "com.tfd.mobile.TfdSearch",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "16.0.2", versionCode = 187))
    )

val DISKWALA_COMPATIBILITY = Compatibility(
        name = "Diskwala",
        packageName = "com.diskwalaapp",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "23.0", versionCode = 313))
    )

val DUBOXDRIVE_COMPATIBILITY = Compatibility(
        name = "TeraBox",
        packageName = "com.dubox.drive",
        appIconColor = 0x2EAAFF,
        targets = listOf(AppTarget(version = "4.20.1"))
    )

val DUOLINGO_COMPATIBILITY = Compatibility(
        name = "Duolingo",
        packageName = "com.duolingo",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x58CC02,
        targets = listOf(AppTarget(version = "6.88.2", versionCode = 2412))
    )

val DYNAMICSPOT_COMPATIBILITY = Compatibility(
        name = "Dynamic Spot",
        packageName = "com.jamworks.dynamicspot",
        appIconColor = 0x1A1A1A,
        targets = listOf(AppTarget(version = "2.01", versionCode = 200142))
    )

val EARGASM_COMPATIBILITY = Compatibility(
        name = "EarGasm",
        packageName = "com.eargasm.search",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6B9D,
        targets = listOf(AppTarget(version = "1.1.4", versionCode = 11))
    )

val ELECTRON_COMPATIBILITY = Compatibility(
        name = "Electron",
        packageName = "com.mahersafadi.electron",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x29B6F6,
        targets = listOf(AppTarget(version = "3.0.3", versionCode = 106))
    )

val ES_EXPLORER_COMPATIBILITY = Compatibility(
        name = "ES File Explorer",
        packageName = "com.estrongs.android.pop",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "4.4.3.7", versionCode = 10353))
    )

val EV_CHARGING_COMPATIBILITY = Compatibility(
        name = "EV Charging Stations Map",
        packageName = "com.galsenstudio.teslasuperchargers",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x00B140,
        targets = listOf(AppTarget(version = "1.10.6", versionCode = 11006069))
    )

val EVERYDOLLAR_COMPATIBILITY = Compatibility(
        name = "EveryDollar",
        packageName = "com.everydollar.android",
        appIconColor = 0x3EA8FF,
        targets = listOf(AppTarget(version = "2026.6.190", versionCode = 3175))
    )

val EXCEL_COMPATIBILITY = Compatibility(
        name = "Excel",
        packageName = "com.microsoft.office.excel",
        appIconColor = 0x1B5E20,
        targets = listOf(AppTarget(version = "16.0.20131.20080", versionCode = 2005201435))
    )

val FACEBOOK_COMPATIBILITY = Compatibility(
        name = "Facebook",
        packageName = "com.facebook.katana",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1877F2,
        targets = listOf(
            AppTarget(version = "570.0.0.24.72", versionCode = 473134678),
            AppTarget(version = "566.0.0.48.73", versionCode = 472224650),
        )
    )

val FAKEGPS_COMPATIBILITY = Compatibility(
        name = "Fake GPS Location",
        packageName = "com.blogspot.newapphorizons.fakegps",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "5.8.5", versionCode = 91))
    )

val FEEDLY_COMPATIBILITY = Compatibility(
        name = "Feedly - Smart News Reader",
        packageName = "com.devhd.feedly",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x2BB24C,
        targets = listOf(AppTarget(version = "90.0.45", versionCode = 954))
    )

val FITBOD_COMPATIBILITY = Compatibility(
        name = "Fitbod",
        packageName = "com.fitbod.fitbod",
        appIconColor = 0xFF3D00,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "8.26.0-0", versionCode = 10826000))
    )

val FKM_COMPATIBILITY = Compatibility(
        name = "Franco Kernel Manager",
        packageName = "com.franco.kernel",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "7.0.48", versionCode = 1906262015))
    )

val FLIGHTAWARE_COMPATIBILITY = Compatibility(
        name = "FlightAware",
        packageName = "com.flightaware.android.liveFlightTracker",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "5.15.5", versionCode = 501500500))
    )

val FLIGHTRADAR_COMPATIBILITY = Compatibility(
        name = "Flightradar24",
        packageName = "com.flightradar24free",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x327CB5,
        targets = listOf(AppTarget(version = "11.7.0", versionCode = 110708450))
    )

val FLIGHTSKY_COMPATIBILITY = Compatibility(
        name = "Flightsky",
        packageName = "com.live.flight.tracker",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.7.1", versionCode = 23))
    )

val FLUD_COMPATIBILITY = Compatibility(
        name = "Flud",
        packageName = "com.delphicoder.flud",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xF16522,
        targets = listOf(AppTarget(version = "2.0.13-beta02", versionCode = 100013154))
    )

val FREEBIEALERTS_COMPATIBILITY = Compatibility(
        name = "Freebie Alerts",
        packageName = "com.rodolfogs.nextdooralerts",
        appIconColor = 0xFF6B00,
        targets = listOf(AppTarget(version = "3.9.5", versionCode = 213))
    )

val FREESTUFFFINDER_COMPATIBILITY = Compatibility(
        name = "FreeStuffFinder",
        packageName = "com.freestufffinder.app",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "6.3.2", versionCode = 234))
    )

val GENIUSFAX_COMPATIBILITY = Compatibility(
        name = "Genius Fax - Send Fax from Phone",
        packageName = "com.thegrizzlylabs.geniusfax",
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "1.11.0", versionCode = 3414))
    )

val GENIUSSCAN_COMPATIBILITY = Compatibility(
        name = "Genius Scan",
        packageName = "com.thegrizzlylabs.geniusscan.free",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "7.41.0", versionCode = 7555))
    )

val GLASSIFY_COMPATIBILITY = Compatibility(
        name = "Glassify",
        packageName = "com.mahmoudzadah.glassifywidgets",
        appIconColor = 0x38BDF8,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "1.4.5", versionCode = 247))
    )

val GLOBALVPN_COMPATIBILITY = Compatibility(
        name = "India VPN - GlobalVPN",
        packageName = "india.vpn.globalvpn",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "1.0.5"))
    )

val GOOGLE_DIALER_COMPATIBILITY = Compatibility(
        name = "Phone by Google",
        packageName = "com.google.android.dialer",
        appIconColor = 0x1A73E8,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "230.0.951104969", versionCode = 19915697))
    )

val GOOGLE_PHOTOS_COMPATIBILITY = Compatibility(
        name = "Google Photos",
        packageName = "com.google.android.apps.photos",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x4285F4,
        targets = listOf(AppTarget(version = "7.85.0.950510832", versionCode = 52095248))
    )

val GOOGLE_WALLET_COMPATIBILITY = Compatibility(
        name = "Google Wallet",
        packageName = "com.google.android.apps.walletnfcrel",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4285F4,
        targets = listOf(AppTarget(version = "26.28.944947626", versionCode = 933705337))
    )

val GPS_MAP_CAMERA_COMPATIBILITY = Compatibility(
        name = "GPS Map Camera- Geotag Photos",
        packageName = "com.gpsmapcamera.geotagginglocationonphoto",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "1.9.24", versionCode = 182))
    )

val GRADIENT_WEATHER_COMPATIBILITY = Compatibility(
        name = "Gradient Weather",
        packageName = "com.subtlesignals.gradientweather",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x5B9BD5,
        targets = listOf(AppTarget(version = "1.1.0", versionCode = 90))
    )

val GRAMMARLY_COMPATIBILITY = Compatibility(
        name = "Grammarly",
        packageName = "com.grammarly.android.keyboard",
        appIconColor = 0x15C39A,
        targets = listOf(AppTarget(version = "2.110.100044", versionCode = 20100044))
    )

val GREENIFY_COMPATIBILITY = Compatibility(
        name = "Greenify",
        packageName = "com.oasisfeng.greenify",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "5.1.1"))
    )

val HABITIFY_COMPATIBILITY = Compatibility(
        name = "Habitify",
        packageName = "co.unstatic.habitify",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "42.3.0", versionCode = 254))
    )

val HEALTHSYNC_COMPATIBILITY = Compatibility(
        name = "Health Sync",
        packageName = "nl.appyhapps.healthsync",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.9.1.3", versionCode = 1186))
    )

val HIBERNATOR_COMPATIBILITY = Compatibility(
        name = "Hibernator",
        packageName = "com.tafayor.hibernator",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.56.10"))
    )

val HISTORICALCALENDAR_COMPATIBILITY = Compatibility(
        name = "Historical Calendar",
        packageName = "com.alexandrucene.dayhistory",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "7.5.4", versionCode = 376))
    )

val HOLAVPN_COMPATIBILITY = Compatibility(
        name = "Hola VPN Proxy Plus",
        packageName = "org.hola.play",
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "AARCH64_1.248.400"))
    )

val HOME_WORKOUT_COMPATIBILITY = Compatibility(
        name = "Home Workout - No Equipment",
        packageName = "homeworkout.homeworkouts.noequipment",
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "1.7.6", versionCode = 150))
    )

val HOTUKDEALS_COMPATIBILITY = Compatibility(
        name = "hotukdeals - Deals & Vouchers",
        packageName = "com.tippingcanoe.hukd",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "8.16.00", versionCode = 81600))
		)

val HTTPMOCK_COMPATIBILITY = Compatibility(
        name = "HTTP Sniffer",
        packageName = "com.anetcapture.mock",
        appIconColor = 0x2196F3,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "2.11.7-ad_mob", versionCode = 139))
    )

val IAMSOBER_COMPATIBILITY = Compatibility(
        name = "I Am Sober",
        packageName = "com.thehungrywasp.iamsober",
        appIconColor = 0x4A90D9,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "8.7.2", versionCode = 1804312328)),
    )

val IMAGEDATEFIXER_COMPATIBILITY = Compatibility(
        name = "Image & Video Date Fixer",
        packageName = "eu.duong.imagedatefixer",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.7.1", versionCode = 30700100))
    )

val IMPLAYER_COMPATIBILITY = Compatibility(
        name = "iMPlayer- IPTV Player & Streamer",
        packageName = "com.myiptvonline.implayer",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "4.0.1.2", versionCode = 465))
    )

val INMIGREAT_COMPATIBILITY = Compatibility(
        name = "Inmigreat",
        packageName = "com.changayaf.inmigreat",
        appIconColor = 0x6344CC,
        targets = listOf(AppTarget(version = "2.3.4", versionCode = 683))
    )

val INSCODE_AUTOCLICKER_COMPATIBILITY = Compatibility(
        name = "Clickmate",
        packageName = "com.inscode.autoclicker",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.1.4"))
    )

val INSPIRED_MINDS_COMPATIBILITY = Compatibility(
        name = "Inspired Minds - Early Childhood",
        packageName = "com.iteachtinyhumans.inspired_minds",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x43A047,
        targets = listOf(AppTarget(version = "5.6.0", versionCode = 142))
    )

val INURE_COMPATIBILITY = Compatibility(
        name = "Inure App Manager",
        packageName = "app.simple.inure.play",
        appIconColor = 0x6200EE,
        targets = listOf(AppTarget(version = "build107.0.5", versionCode = 10705))
    )

val ISS_LIVE_NOW_COMPATIBILITY = Compatibility(
        name = "ISS Live Now - Earth & Space",
        packageName = "com.nicedayapps.iss_free",
        appIconColor = 0x1A237E,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "8.5.3", versionCode = 853001))
    )

val JEFIT_COMPATIBILITY = Compatibility(
        name = "JEFIT",
        packageName = "je.fit",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "17.2.9", versionCode = 2020))
    )

val JOI_COMPATIBILITY = Compatibility(
        name = "Joi - Live Video Chat & Dating",
        packageName = "video.chat.joi",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE91E8C,
        targets = listOf(AppTarget(version = "2.7.0.2", versionCode = 104))
    )

val KAHOOT_COMPATIBILITY = Compatibility(
        name = "Kahoot!",
        packageName = "no.mobitroll.kahoot.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x46178F,
        targets = listOf(AppTarget(version = "6.6.7", versionCode = 3246))
    )

val KILLAPPS_COMPATIBILITY = Compatibility(
        name = "KillApps",
        packageName = "com.tafayor.killall",
        appIconColor = 0xF44336,
        targets = listOf(AppTarget(version = "1.57.9"))
    )

val KINEMASTER_COMPATIBILITY = Compatibility(
        name = "KineMaster",
        packageName = "com.nexstreaming.app.kinemasterfree",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1B54C8,
        targets = listOf(AppTarget(version = "8.1.13.36552.GP", versionCode = 36552))
    )

val KINESTOP_COMPATIBILITY = Compatibility(
        name = "KineStop",
        packageName = "com.urbandroid.kinestop",
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "5.1", versionCode = 101))
    )

val KOMOOT_COMPATIBILITY = Compatibility(
        name = "komoot",
        packageName = "de.komoot.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x65AB1E,
        targets = listOf(AppTarget(version = "2026.29.0", versionCode = 263710002))
    )

val KUSTOM_WIDGET_COMPATIBILITY = Compatibility(
        name = "KWGT Kustom Widget Maker",
        packageName = "org.kustom.widget",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.82b620310", versionCode = 382620310))
    )

val KYPHOSIS_COMPATIBILITY = Compatibility(
        name = "Kyphosis",
        packageName = "air.com.musclemotion.kyphosis",
        appIconColor = 0xE65100,
        targets = listOf(AppTarget(version = "1.4.9", versionCode = 156))
    )

val LABELESS_COMPATIBILITY = Compatibility(
        name = "Labeless",
        packageName = "com.bytes_and_pixels.food_buddy",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x5EAD34,
        targets = listOf(AppTarget(version = "4.2.0", versionCode = 416))
    )

val LARK_PLAYER_COMPATIBILITY = Compatibility(
        name = "Lark Player",
        packageName = "com.dywx.larkplayer",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1DB954,
        targets = listOf(AppTarget(version = "2026.10.5", versionCode = 2026100509))
    )

val LAWFULLY_COMPATIBILITY = Compatibility(
        name = "Lawfully",
        packageName = "com.lawfully.lawfully_ai_tracker",
        appIconColor = 0x0D47A1,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "6.7.2", versionCode = 536))
    )

val LIFE360_COMPATIBILITY = Compatibility(
        name = "Life360",
        packageName = "com.life360.android.safetymapd",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x5D2DE6,
        targets = listOf(AppTarget(version = "26.27.0", versionCode = 2906670))
    )

val LINKEDIN_COMPATIBILITY = Compatibility(
        name = "LinkedIn",
        packageName = "com.linkedin.android",
        appIconColor = 0x0A66C2,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "6.1.1", versionCode = 111206000))
    )

val LIVESCORE_COMPATIBILITY = Compatibility(
        name = "LiveScore",
        packageName = "com.livescore",
        appIconColor = 0xE30613,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "9.8", versionCode = 2088))
    )

val LOKLOK_COMPATIBILITY = Compatibility(
        name = "Loklok",
        packageName = "com.woden.celestis",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "3.27.1", versionCode = 3271))
    )

val LOSEIT_COMPATIBILITY = Compatibility(
        name = "Lose It! – Calorie Counter",
        packageName = "com.fitnow.loseit",
        appIconColor = 0x00C853,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "18.4.201", versionCode = 18905))
    )

val LOVE8_COMPATIBILITY = Compatibility(
        name = "Love8",
        packageName = "ltd.love8.couples.relationship",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF4081,
        targets = listOf(AppTarget(version = "3.7.0", versionCode = 210))
    )

val MANGA_PLUS_COMPATIBILITY = Compatibility(
        name = "MANGA Plus by SHUEISHA",
        packageName = "jp.co.shueisha.mangaplus",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "2.6.0", versionCode = 258))
    )

val MAPY_COMPATIBILITY = Compatibility(
        name = "Mapy",
        packageName = "cz.seznam.mapy",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE03C31,
        targets = listOf(AppTarget(version = "26.7.1", versionCode = 26070102))
    )

val MARINETRAFFIC_COMPATIBILITY = Compatibility(
        name = "MarineTraffic: Ship Tracking",
        packageName = "com.kpler.marinetraffic",
        appIconColor = 0x0055A4,
        targets = listOf(AppTarget(version = "5.7.0", versionCode = 5007000))
    )

val MATERIAL_CAPSULE_COMPATIBILITY = Compatibility(
        name = "Material Capsule",
        packageName = "com.pryshedko.mtisland",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x6750A4,
        targets = listOf(AppTarget(version = "15.5", versionCode = 155))
    )

val MATERIAL_PODS_COMPATIBILITY = Compatibility(
        name = "MaterialPods",
        packageName = "com.pryshedko.materialpods",
        appIconColor = 0x1B72E8,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "6.70", versionCode = 670))
    )

val MEGA_COMPATIBILITY = Compatibility(
        name = "MEGA",
        packageName = "mega.privacy.android.app",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xD9272E,
        targets = listOf(AppTarget(version = "16.10(261970902)(8daeddaf4d)", versionCode = 261970902))
    )

val METEOBLUE_COMPATIBILITY = Compatibility(
        name = "meteoblue Weather",
        packageName = "com.meteoblue.droid",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0077CC,
        targets = listOf(AppTarget(version = "Cirrus Uncinus 3.0.4", versionCode = 27024))
    )

val MIGRACONNECT_COMPATIBILITY = Compatibility(
        name = "MigraConnect",
        packageName = "com.tecso.MigraConnect",
        appIconColor = 0x2563EB,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "2.8.2", versionCode = 119))
    )

val MINDICATOR_COMPATIBILITY = Compatibility(
        name = "m-Indicator",
        packageName = "com.mobond.mindicator",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "18.0.356", versionCode = 356))
    )

val MINIMAL_WIDGETS_COMPATIBILITY = Compatibility(
        name = "Minimal Widgets",
        packageName = "com.jndapp.minimal.widgets",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x212121,
        targets = listOf(AppTarget(version = "1.3.01", versionCode = 11))
    )

val MIRKO_COMPATIBILITY = Compatibility(
        name = "Beta Maniac",
        packageName = "it.mirko.beta",
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "0.9.4"))
    )

val MLMANAGER_COMPATIBILITY = Compatibility(
        name = "ML Manager",
        packageName = "com.javiersantos.mlmanager",
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "5.0"))
    )

val MOBIOFFICE_COMPATIBILITY = Compatibility(
        name = "MobiOffice- Word, Excel, Slide",
        packageName = "com.mobisystems.office",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "16.4.60095", versionCode = 60095))
    )

val MONARCH_COMPATIBILITY = Compatibility(
        name = "Monarch Money",
        packageName = "com.monarchmoney.mobile",
        appIconColor = 0xE8A23A,
        targets = listOf(AppTarget(version = "2.0.100", versionCode = 13859))
    )

val MOONREADER_COMPATIBILITY = Compatibility(
        name = "Moon+ Reader",
        packageName = "com.flyersoft.moonreader",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "10.6", versionCode = 1006000))
    )

val MOTIONCAM_COMPATIBILITY = Compatibility(
        name = "MotionCam Pro",
        packageName = "com.motioncam",
        appIconColor = 0x1A1A2E,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.0.8-trial", versionCode = 3308))
    )

val MOVIEBASE_COMPATIBILITY = Compatibility(
        name = "Moviebase",
        packageName = "com.moviebase",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x0F172A,
        targets = listOf(AppTarget(version = "6.12.4", versionCode = 61204))
    )

val MOVIEBOX_COMPATIBILITY = Compatibility(
        name = "MovieBox",
        packageName = "com.community.oneroom",
        appIconColor = 0xE53935,
        targets = listOf(
            AppTarget(version = "3.0.16.0709.03", versionCode = 50020116),
        )
    )

val MOVIEBOX_IN_COMPATIBILITY = Compatibility(
        name = "MovieBox (India)",
        packageName = "com.community.mbox.in",
        appIconColor = 0xE53935,
        targets = listOf(
            AppTarget(version = "3.0.16.0707.03", versionCode = 50020115),
        )
    )

val MOVIEBOXTV_COMPATIBILITY = Compatibility(
        name = "MovieBox TV",
        packageName = "com.community.mbox.tv",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APK,
        targets = listOf(
            AppTarget(version = "1.1.5.0711.03", versionCode = 50040010),
        )
    )

val MTMANAGER_COMPATIBILITY = Compatibility(
        name = "MT Manager",
        packageName = "bin.mt.plus",
        appIconColor = 0x1976D2,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "2.26.7", versionCode = 26070303))
    )

val MULLVAD_COMPATIBILITY = Compatibility(
        name = "Mullvad VPN",
        packageName = "net.mullvad.mullvadvpn",
        appIconColor = 0xFFD524,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "2026.8", versionCode = 26089000))
    )

val MUSCLEWIKI_COMPATIBILITY = Compatibility(
        name = "MuscleWiki: Gym Workout & Fitness",
        packageName = "com.musclewiki.macro",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "3.2.0", versionCode = 32018))
    )

val MUSESCORE_COMPATIBILITY = Compatibility(
        name = "MuseScore: Sheet Music & Scores",
        packageName = "com.musescore.playerlite",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "2.14.37", versionCode = 2701548))
    )

val MUSIXMATCH_COMPATIBILITY = Compatibility(
        name = "Musixmatch: lyrics finder",
        packageName = "com.musixmatch.android.lyrify",
        appIconColor = 0xFF4B00,
        targets = listOf(AppTarget(version = "8.4.2", versionCode = 2026061901))
    )

val MUSWALL_COMPATIBILITY = Compatibility(
        name = "MusWall",
        packageName = "com.automan.albumwallpaper",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x6200EA,
        targets = listOf(AppTarget(version = "1.8.4", versionCode = 29))
    )

val MXPLAYER_COMPATIBILITY = Compatibility(
        name = "MX Player",
        packageName = "com.mxtech.videoplayer.ad",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1C1C1C,
        targets = listOf(AppTarget(version = "3.0.12", versionCode = 2001003512))
    )

val MYGATE_COMPATIBILITY = Compatibility(
        name = "MyGate",
        packageName = "com.mygate.user",
        appIconColor = 0x1A73E8,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "7.31.1", versionCode = 731010))
    )

val MYPERM_COMPATIBILITY = Compatibility(
        name = "Permission Pilot",
        packageName = "eu.darken.myperm",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "2.2.0-rc0", versionCode = 20200000))
    )

val MYPLANT_COMPATIBILITY = Compatibility(
        name = "MyPlant - Plant Identifier",
        packageName = "com.plant.identify.plantcare.identifier",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "4.4.0", versionCode = 58))
    )

val MYRADAR_COMPATIBILITY = Compatibility(
        name = "MyRadar",
        packageName = "com.acmeaom.android.myradar",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A6FBF,
        targets = listOf(AppTarget(version = "8.71.3", versionCode = 592))
    )

val NAVITIME_COMPATIBILITY = Compatibility(
        name = "NAVITIME",
        packageName = "com.navitime.inbound.walk",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x003087,
        targets = listOf(AppTarget(version = "12.0.5", versionCode = 367))
    )

val NETGUARD_COMPATIBILITY = Compatibility(
        name = "NetGuard",
        packageName = "eu.faircode.netguard",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.335"))
    )

val NETMONSTER_COMPATIBILITY = Compatibility(
        name = "NetMonster",
        packageName = "cz.mroczis.netmonster",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.4.1"))
    )

val NETSHARE_COMPATIBILITY = Compatibility(
        name = "NetShare",
        packageName = "kha.prog.mikrotik",
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "277", versionCode = 277))
    )

val NETWORKGURU_COMPATIBILITY = Compatibility(
        name = "Network Guru - Net Analyzer",
        packageName = "com.paget96.netspeedindicator",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.9-beta5", versionCode = 156))
    )

val NEWSBREAK_COMPATIBILITY = Compatibility(
        name = "NewsBreak",
        packageName = "com.particlenews.newsbreak",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "26.28.0", versionCode = 26280034))
    )

val NINJVAPN_COMPATIBILITY = Compatibility(
        name = "Ninja VPN",
        packageName = "app.ninjavpn.android",
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "1.4.7", versionCode = 44))
    )

val NOTESNOOK_COMPATIBILITY = Compatibility(
        name = "Notesnook- Private Notes App",
        packageName = "com.streetwriters.notesnook",
        appIconColor = 0x0F4C81,
        targets = listOf(AppTarget(version = "3.4.6", versionCode = 4197416))
    )

val NYT_GAMES_COMPATIBILITY = Compatibility(
        name = "NYT Games",
        packageName = "com.nytimes.crossword",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "6.35.0", versionCode = 6426547))
    )

val NZB360_COMPATIBILITY = Compatibility(
        name = "nzb360",
        packageName = "com.kevinforeman.nzb360",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "24.1", versionCode = 523))
    )

val OASIS_COMPATIBILITY = Compatibility(
        name = "Oasis",
        packageName = "com.liveoasis.oasis",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x22C55E,
        targets = listOf(AppTarget(version = "2.3.31", versionCode = 228))
    )

val OBD_ANDROID_COMPATIBILITY = Compatibility(
        name = "OBD Android",
        packageName = "ai.metaverselabs.obdandroid",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "4.9", versionCode = 133))
    )

val OCTI_COMPATIBILITY = Compatibility(
        name = "Octi",
        packageName = "eu.darken.octi",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6F00,
        targets = listOf(AppTarget(version = "1.0.4", versionCode = 10004000))
    )

val ODIALER_COMPATIBILITY = Compatibility(
        name = "ODialer",
        packageName = "com.oplus.dialer",
        appIconColor = 0x38C054,
        targets = listOf(AppTarget(version = "16.0.0", versionCode = 160000000))
    )

val OFFERUP_COMPATIBILITY = Compatibility(
        name = "OfferUp",
        packageName = "com.offerup",
        appIconColor = 0x00A6A6,
        targets = listOf(AppTarget(version = "2026.24.1", versionCode = 2026241001))
    )

val ONEDM_COMPATIBILITY = Compatibility(
        name = "1DM: Browser & Downloader",
        packageName = "idm.internet.download.manager",
        appIconColor = 0x4FC3F7,
        targets = listOf(AppTarget(version = "18.2", versionCode = 30249)),
    )

val ONETAPCLEANER_COMPATIBILITY = Compatibility(
        name = "1Tap Cleaner",
        packageName = "com.a0soft.gphone.acc.free",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.21", versionCode = 240005219))
    )

val ONRAILS_COMPATIBILITY = Compatibility(
        name = "On Rails",
        packageName = "com.intsoftdev.nationalrail",
        appIconColor = 0x003087,
        targets = listOf(AppTarget(version = "5.1.3", versionCode = 204))
    )

val ONX_OFFROAD_COMPATIBILITY = Compatibility(
        name = "onX Offroad: GPS Maps & Trails",
        packageName = "onxmaps.offroad",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x006B3A,
        targets = listOf(AppTarget(version = "26.24.0", versionCode = 1088))
    )

val OPENNOVEL_COMPATIBILITY = Compatibility(
        name = "OpenNovel",
        packageName = "co.opennovel.app",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "1.1.30", versionCode = 40))
    )

val OPERA_NEWS_COMPATIBILITY = Compatibility(
        name = "Opera News - Breaking & Local",
        packageName = "com.opera.app.news",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF1B2A,
        targets = listOf(AppTarget(version = "14.1.2254.83278", versionCode = 141083278))
    )

val OPLUS_MMS_COMPATIBILITY = Compatibility(
        name = "OPlus MMS",
        packageName = "com.android.mms",
        appIconColor = 0x1DA462,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "88.0.1.2", versionCode = 880001002))
    )

val OSMOSIS_COMPATIBILITY = Compatibility(
        name = "Osmosis",
        packageName = "org.osmosis.med",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "6.0.8-main", versionCode = 50410918))
    )

val PAPRIKA_COMPATIBILITY = Compatibility(
        name = "Paprika 3",
        packageName = "com.hindsightlabs.paprika.android.v3",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xD84315,
        targets = listOf(AppTarget(version = "3.3.10", versionCode = 50))
    )

val PARALLELSPACE_COMPATIBILITY = Compatibility(
        name = "Parallel Space Pro",
        packageName = "com.parallel.space.pro",
        appIconColor = 0x00BCD4,
        targets = listOf(AppTarget(version = "4.0.9123"))
    )

val PARCELS_COMPATIBILITY = Compatibility(
        name = "Parcels — Package Tracker",
        packageName = "com.brightstripe.parcels",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "3.0.11", versionCode = 288))
    )

val PC_REMOTE_COMPATIBILITY = Compatibility(
        name = "PC Remote - Desktop & Phone",
        packageName = "com.monect.portable",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "8.3.5", versionCode = 144))
    )

val PDFFILLER_COMPATIBILITY = Compatibility(
        name = "pdfFiller- PDF Editor & Form",
        packageName = "com.pdffiller",
        appIconColor = 0x0060FF,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "11.0.2", versionCode = 30003))
    )

val PHOTOEDITOR_COMPATIBILITY = Compatibility(
        name = "Photo Editor",
        packageName = "com.iudesk.android.photo.editor",
        appIconColor = 0xFF6B9D,
        targets = listOf(AppTarget(version = "13.4", versionCode = 2026070800))
    )

val PIALYTIC_COMPATIBILITY = Compatibility(
        name = "Pialytic",
        packageName = "verbosus.pialytic",
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "1.3.0", versionCode = 21))
    )

val PICSART_COMPATIBILITY = Compatibility(
        name = "Picsart",
        packageName = "com.picsart.studio",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x7C3AED,
        targets = listOf(AppTarget(version = "30.3.8", versionCode = 993830308))
    )

val PICTUREMUSHROOM_COMPATIBILITY = Compatibility(
        name = "Picture Mushroom - Mushroom ID",
        packageName = "com.glority.picturemushroom",
        appIconColor = 0x7A4A24,
        targets = listOf(AppTarget(version = "2.9.31", versionCode = 90))
    )

val PICTURETHIS_COMPATIBILITY = Compatibility(
        name = "PictureThis - Plant Identifier",
        packageName = "cn.danatech.xingseus",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "5.31.0", versionCode = 5086))
    )

val PIXEL_HABIT_TRACKER_COMPATIBILITY = Compatibility(
        name = "Pixel Habit Tracker",
        packageName = "com.pixel.al.pixelhabittracker",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "2.1.2", versionCode = 100063))
    )

val PKGE_COMPATIBILITY = Compatibility(
        name = "pkge",
        packageName = "net.pkge.pkge",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = "20.0.7", versionCode = 466))
    )

val PLAMFY_COMPATIBILITY = Compatibility(
        name = "Plamfy",
        packageName = "com.plamfy.platform",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF4081,
        targets = listOf(AppTarget(version = "1.8.5", versionCode = 1080500))
    )

val PLAYIT_COMPATIBILITY = Compatibility(
        name = "PLAYit",
        packageName = "com.playit.videoplayer",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "2.7.50.12", versionCode = 20750012))
    )

val PLUGSHARE_COMPATIBILITY = Compatibility(
        name = "PlugShare: EV Charging App",
        packageName = "com.xatori.Plugshare",
        appIconColor = 0x00B2D9,
        targets = listOf(AppTarget(version = "4.50.0", versionCode = 20397))
    )

val POCKET_BARD_COMPATIBILITY = Compatibility(
        name = "Pocket Bard",
        packageName = "com.MojoFilterMediaLLC.RPGSoundSystem",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x6A0DAD,
        targets = listOf(AppTarget(version = "3.1.16", versionCode = 234))
    )

val POCKET_CASTS_COMPATIBILITY = Compatibility(
        name = "Pocket Casts",
        packageName = "au.com.shiftyjelly.pocketcasts",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xF43E37,
        targets = listOf(AppTarget(version = "8.14", versionCode = 9435))
    )

val POCKETFM_COMPATIBILITY = Compatibility(
        name = "Pocket FM",
        packageName = "com.radio.pocketfm",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "9.8.1", versionCode = 2091))
    )

val POCKETPREP_BEHAVIORAL_HEALTH_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Behavioral Health",
        packageName = "com.pocketprep.android.behavioralhealth",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = null))
    )

val POCKETPREP_COMPATIBILITY = Compatibility(
        name = "Pocket Prep",
        packageName = "com.pocketprep.android.itcybersecurity",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.27.2", versionCode = 424))
    )

val PODSLINK_COMPATIBILITY = Compatibility(
        name = "PodsLink",
        packageName = "net.podslink",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "1.3.5", versionCode = 79))
    )

val POLARR_COMPATIBILITY = Compatibility(
        name = "Polarr",
        packageName = "photo.editor.polarr",
        appIconColor = 0x00B7FF,
        signatures = setOf("12afe644a6c22f82e182722dd07430064cea7a262c92d4e040bb49d7b23279a8"),
        targets = listOf(AppTarget(version = "6.12.0", versionCode = 1770064968))
    )

val POLICESCANNER_COMPATIBILITY = Compatibility(
        name = "Police Scanner",
        packageName = "police.scanner.radio.broadcastify.citizen",
        appIconColor = 0x0D47A1,
        targets = listOf(AppTarget(version = "1.29.0-260420093"))
    )

val POLYCHAT_COMPATIBILITY = Compatibility(
        name = "PolyChat",
        packageName = "ventures.appliedai.polychat",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x6C63FF,
        targets = listOf(AppTarget(version = "5.0.4", versionCode = 201))
    )

val POSTURE_COMPATIBILITY = Compatibility(
        name = "Posture",
        packageName = "air.com.musclemotion.posture",
        appIconColor = 0x00897B,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "3.2.1", versionCode = 10040838))
    )

val POWERAMP_COMPATIBILITY = Compatibility(
        name = "Poweramp Music Player",
        packageName = "com.maxmpz.audioplayer",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "build-1027-uni", versionCode = 1027009))
    )

val POWERAMP_EQ_COMPATIBILITY = Compatibility(
        name = "Poweramp Equalizer",
        packageName = "com.maxmpz.equalizer",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "build-1021-bundle-play", versionCode = 1021004))
    )

val POWERPOINT_COMPATIBILITY = Compatibility(
        name = "Microsoft PowerPoint",
        packageName = "com.microsoft.office.powerpoint",
        appIconColor = 0xD04423,
        targets = listOf(AppTarget(version = "16.0.20131.20080", versionCode = 2005201435))
    )

val PROTONVPN_COMPATIBILITY = Compatibility(
        name = "Proton VPN",
        packageName = "ch.protonvpn.android",
        appIconColor = 0x6D4AFF,
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "5.19.43.0", versionCode = 605194300))
    )

val PROXYMAN_COMPATIBILITY = Compatibility(
        name = "Proxyman",
        packageName = "com.proxyman.proxymanandroid",
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "1.19.0", versionCode = 46))
    )

val PSIPHON_COMPATIBILITY = Compatibility(
        name = "Psiphon Pro",
        packageName = "com.psiphon3.subscription",
        appIconColor = 0x6A1B9A,
        targets = listOf(AppTarget(version = "479", versionCode = 479))
    )

val PUSHBULLET_COMPATIBILITY = Compatibility(
        name = "Pushbullet",
        packageName = "com.pushbullet.android",
        appIconColor = 0x4AB367,
        targets = listOf(AppTarget(version = "18.12.2", versionCode = 410))
    )

val QBITCONNECT_COMPATIBILITY = Compatibility(
        name = "qBitConnect",
        packageName = "com.bluematter.qbitconnect",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4FC3F7,
        targets = listOf(AppTarget(version = "2.0.5", versionCode = 65))
    )

val QUIZAI_COMPATIBILITY = Compatibility(
        name = "QuizAI",
        packageName = "com.intsig.camexam",
        appIconColor = 0x6C63FF,
        targets = listOf(AppTarget(version = "4.27.0.20260624", versionCode = 9427000))
    )

val RADARBOT_COMPATIBILITY = Compatibility(
        name = "Radarbot",
        packageName = "com.vialsoft.radarbot_free",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "9.35.4", versionCode = 423))
    )

val RADIOGARDEN_COMPATIBILITY = Compatibility(
        name = "Radio Garden",
        packageName = "com.jonathanpuckey.radiogarden",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "4.0.2", versionCode = 6194392))
    )

val RAR_COMPATIBILITY = Compatibility(
        name = "RAR",
        packageName = "com.rarlab.rar",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.23.build133", versionCode = 133))
    )

val RECIPEBRO_COMPATIBILITY = Compatibility(
        name = "RecipeBro",
        packageName = "com.recipebro.cookingbuddy",
        appIconColor = 0xE65100,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "1.4.22", versionCode = 1388))
    )

val REMOTEATV_COMPATIBILITY = Compatibility(
        name = "Remote ATV - Android TV Remote",
        packageName = "tech.simha.androidtvremote",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = null))
    )

val RENAMEORGANIZE_COMPATIBILITY = Compatibility(
        name = "Rename & Organize",
        packageName = "eu.duong.picturemanager",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "8.25.1", versionCode = 8250101))
    )

val REUTERS_COMPATIBILITY = Compatibility(
        name = "Reuters: Breaking News & More",
        packageName = "com.thomsonreuters.reuters",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF8000,
        targets = listOf(AppTarget(version = "7.42.0", versionCode = 1783014837))
    )

val REVPDF_COMPATIBILITY = Compatibility(
        name = "RevPDF - PDF Editor & Reader",
        packageName = "com.revpdf.editor",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "4.6.5", versionCode = 78))
    )

val ROCKETMONEY_COMPATIBILITY = Compatibility(
        name = "Rocket Money",
        packageName = "com.truebill",
        appIconColor = 0xDE3341,
        targets = listOf(AppTarget(version = "13.15.0"))
    )

val RS_EXPLORER_COMPATIBILITY = Compatibility(
        name = "RS File Manager",
        packageName = "com.rs.explorer.filemanager",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x0D47A1,
        targets = listOf(AppTarget(version = "2.3.0.4", versionCode = 239))
    )

val RUNNA_COMPATIBILITY = Compatibility(
        name = "Runna",
        packageName = "com.runbuddy.prod",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x00D4AA,
        targets = listOf(AppTarget(version = "8.44.0", versionCode = 2034))
    )

val SAI_COMPATIBILITY = Compatibility(
        name = "SAI",
        packageName = "com.mtv.sai",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.2.8"))
    )

val SAVVY_NAVVY_COMPATIBILITY = Compatibility(
        name = "Savvy Navvy",
        packageName = "com.savvy.navvy.android.app",
        appIconColor = 0x003366,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "2.2.23036", versionCode = 23036))
    )

val SBS_COMPATIBILITY = Compatibility(
        name = "SBS On Demand",
        packageName = "com.sbs.ondemand.android",
        appIconColor = 0xB4A034,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "6.2.5", versionCode = 16343))
    )

val SCANNERRADIO_COMPATIBILITY = Compatibility(
        name = "Scanner Radio Pro",
        packageName = "com.scannerradio",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = null))
    )

val SCOOPZ_COMPATIBILITY = Compatibility(
        name = "Scoopz",
        packageName = "com.localaiapp.scoops",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 3290002))
    )

val SCRL_COMPATIBILITY = Compatibility(
        name = "SCRL",
        packageName = "com.appostrophe.scrl",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xEFC67A,
        targets = listOf(AppTarget(version = "1.23.1", versionCode = 260))
    )

val SD_MAID_SE_COMPATIBILITY = Compatibility(
        name = "SD Maid SE",
        packageName = "eu.darken.sdmse",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "1.7.5-rc0", versionCode = 10705000))
    )

val SEND_FILES_TO_TV_COMPATIBILITY = Compatibility(
        name = "Send Files To TV",
        packageName = "com.yablio.sendfilestotv",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.4.22", versionCode = 69))
    )

val SHAREIT_COMPATIBILITY = Compatibility(
        name = "SHAREit Premium",
        packageName = "shareit.premium",
        appIconColor = 0xFF4B00,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "1.1.98"))
    )

val SHARK_TRACKER_COMPATIBILITY = Compatibility(
        name = "Shark Tracker",
        packageName = "org.ocearch.SharkTrackerAndroid",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "3.2.4", versionCode = 6000050))
    )

val SHEXA_COMPATIBILITY = Compatibility(
        name = "App Permission Manager",
        packageName = "com.shexa.permissionmanager",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.4.6.2"))
    )

val SHORTWAVE_COMPATIBILITY = Compatibility(
        name = "Shortwave Schedules",
        packageName = "com.msi.shortwave",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "7.6.0", versionCode = 760))
    )

val SHOUTTV_COMPATIBILITY = Compatibility(
        name = "Shout! TV - Movies & TV Shows",
        packageName = "com.shoutfactory.app",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "10.2.0", versionCode = 120))
    )

val SIMPLYPIANO_COMPATIBILITY = Compatibility(
        name = "SimplyPiano",
        packageName = "com.joytunes.simplypiano",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x6C3CE1,
        targets = listOf(AppTarget(version = "7.31.7", versionCode = 5275))
    )

val SKINSORT_COMPATIBILITY = Compatibility(
        name = "SkinSort",
        packageName = "com.skinsort",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x2F7D62,
        targets = listOf(AppTarget(version = "1.22", versionCode = 34))
    )

val SLOPES_COMPATIBILITY = Compatibility(
        name = "Slopes",
        packageName = "com.consumedbycode.slopes",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2026.14", versionCode = 2419))
    )

val SMARTRSS_COMPATIBILITY = Compatibility(
        name = "SmartRSS",
        packageName = "com.vinsonguo.flutter_rss_reader",
        appIconColor = 0xFF6600,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "1.2.36", versionCode = 371))
    )

val SMSBACKUPRESTORE_COMPATIBILITY = Compatibility(
        name = "SMS Backup & Restore",
        packageName = "com.riteshsahu.SMSBackupRestore",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "10.26.007", versionCode = 1026007))
    )

val SNAKEENGINE_COMPATIBILITY = Compatibility(
        name = "Snake Engine",
        packageName = "com.snake",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.2.2"))
    )

val SNAPTUBE_COMPATIBILITY = Compatibility(
        name = "SnapTube",
        packageName = "com.snaptube.premium",
        appIconColor = 0xFF6B00,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "7.62.0.76230210", versionCode = 76230210))
    )

val SNIPD_COMPATIBILITY = Compatibility(
        name = "Snipd",
        packageName = "ai.topicfinder.podcastdiscovery",
        appIconColor = 0x1CC29F,
        targets = listOf(AppTarget(version = "4.1.14"))
    )

val SNOWFORECAST_COMPATIBILITY = Compatibility(
        name = "Snow-Forecast.com",
        packageName = "com.snow_forecast.snowforecast",
        appIconColor = 0xCA0013,
        targets = listOf(AppTarget(version = "8.0.8", versionCode = 2145))
    )

val SOCIALGAMEBOX_COMPATIBILITY = Compatibility(
        name = "Social Gamebox",
        packageName = "com.app.social_gamebox",
        appIconColor = 0x7C4DFF,
        targets = listOf(AppTarget(version = "1.1.3"))
    )

val SPEEDTEST_COMPATIBILITY = Compatibility(
        name = "Speedtest",
        packageName = "org.zwanoo.android.speedtest",
        appIconColor = 0x141C4C,
        targets = listOf(AppTarget(version = "7.0.7", versionCode = 258530))
    )

val SPIN_COMPATIBILITY = Compatibility(
        name = "SPIN",
        packageName = "com.nationaledtech.spinbrowser",
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = "70.3.0", versionCode = 2025061004))
    )

val SPLITWISE_COMPATIBILITY = Compatibility(
        name = "Splitwise",
        packageName = "com.Splitwise.SplitwiseMobile",
        appIconColor = 0x1CC29F,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "26.7.2", versionCode = 949))
    )

val SPORTZX_COMPATIBILITY = Compatibility(
        name = "SportzX TV",
        packageName = "com.sportzxtv.luvefootball",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.0.0", versionCode = 1))
    )

val SPOTANGELS_COMPATIBILITY = Compatibility(
        name = "SpotAngels",
        packageName = "com.spotangels.android",
        appIconColor = 0x1C9BE6,
        targets = listOf(AppTarget(version = "15.2.2", versionCode = 10323))
    )

val SPOTIFY_COMPATIBILITY = Compatibility(
        name = "Spotify: Music and Podcasts",
        packageName = "com.spotify.music",
        appIconColor = 0x1DB954,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "9.1.64.1676", versionCode = 143666644))
    )

val STICKER_MAKER_COMPATIBILITY = Compatibility(
        name = "Sticker Maker",
        packageName = "com.marsvard.stickermakerforwhatsapp",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x00A884,
        targets = listOf(AppTarget(version = "1.0.10-5", versionCode = 1001005))
    )

val STICKERLY_COMPATIBILITY = Compatibility(
        name = "Sticker.ly",
        packageName = "com.snowcorp.stickerly.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x00ADEF,
        targets = listOf(AppTarget(version = "3.35.0", versionCode = 1033500))
    )

val STORYGRAPH_COMPATIBILITY = Compatibility(
        name = "StoryGraph",
        packageName = "com.thestorygraph.thestorygraph",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "1.29", versionCode = 38))
    )

val STRAVA_COMPATIBILITY = Compatibility(
        name = "Strava",
        packageName = "com.strava",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFC4C02,
        targets = listOf(AppTarget(version = "471.11", versionCode = 12398607))
    )

val STREAMFREE_COMPATIBILITY = Compatibility(
        name = "StreamFree",
        packageName = "com.streamfree.app",
        appIconColor = 0x9146FF,
        targets = listOf(AppTarget(version = "1.0.0", versionCode = 9))
    )

val STRENGTHTRAINING_COMPATIBILITY = Compatibility(
        name = "Strength Training",
        packageName = "air.com.musclemotion.strength.mobile",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "3.5.1", versionCode = 924))
    )

val STRONG_COMPATIBILITY = Compatibility(
        name = "Strong - Workout Tracker",
        packageName = "io.strongapp.strong",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "v6.2.1", versionCode = 602002))
    )

val STUDOCU_COMPATIBILITY = Compatibility(
        name = "Studocu",
        packageName = "com.studocu.app",
        appIconColor = 0x00AEEF,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "7.154.1", versionCode = 5668))
    )

val SUBWAYNOW_COMPATIBILITY = Compatibility(
        name = "Subway Now",
        packageName = "io.goodservice.theweekendest",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "1.2.10", versionCode = 81))
    )

val SUPER_FILE_COMPATIBILITY = Compatibility(
        name = "Super File",
        packageName = "com.esuper.file.explorer",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.5.6.3", versionCode = 77))
    )

val SUPERCHINESE_COMPATIBILITY = Compatibility(
        name = "SuperChinese - Learn Chinese",
        packageName = "com.superchinese",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "6.7.1", versionCode = 397))
    )

val SURFLINE_COMPATIBILITY = Compatibility(
        name = "Surfline",
        packageName = "com.surfline.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x005CE6,
        targets = listOf(AppTarget(version = "12.7.0", versionCode = 15743))
    )

val SVPLAYER_COMPATIBILITY = Compatibility(
        name = "SVPlayer",
        packageName = "com.svpteam.svp",
        appIconColor = 0x2D7DD2,
        targets = listOf(AppTarget(version = "1.8.0", versionCode = 80))
    )

val TAGTRACKER_COMPATIBILITY = Compatibility(
        name = "Tag Tracker: Find Lost Items",
        packageName = "com.makeevapps.tagtracker",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "1.3.2", versionCode = 19))
    )

val TANGO_COMPATIBILITY = Compatibility(
        name = "Tango- Live Stream Video Chat",
        packageName = "com.sgiggle.production",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "9.69.7", versionCode = 1751043051))
    )

val TAP2FREE_COMPATIBILITY = Compatibility(
        name = "India VPN - Tap2Free",
        packageName = "india.vpn_tap2free",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.164", versionCode = 164))
    )

val TASKER_COMPATIBILITY = Compatibility(
        name = "Tasker",
        packageName = "net.dinglisch.android.taskerm",
        appIconColor = 0x263238,
        targets = listOf(AppTarget(version = "6.6.20"))
    )

val TELEVIZO_COMPATIBILITY = Compatibility(
        name = "Televizo",
        packageName = "com.ottplay.ottplay",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "1.9.9.31-g", versionCode = 614))
    )

val TEMPMAIL_COMPATIBILITY = Compatibility(
        name = "TempMail",
        packageName = "com.tempmail",
        appIconColor = 0xFF6B6B,
        targets = listOf(AppTarget(version = null))
    )

val TEMPNUMBER_COMPATIBILITY = Compatibility(
        name = "Temp Number",
        packageName = "com.tempnumber.Temp_Number.Temp_Number",
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "2.0.10", versionCode = 78))
    )

val TETRD_COMPATIBILITY = Compatibility(
        name = "Tetrd",
        packageName = "com.robskie.tether",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.3.2", versionCode = 70))
    )

val TFIND_COMPATIBILITY = Compatibility(
        name = "TFind",
        packageName = "com.dreamteammobile.tagtracker",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.7.1", versionCode = 49))
    )

val THE_ATHLETIC_COMPATIBILITY = Compatibility(
        name = "The Athletic",
        packageName = "com.theathletic",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A1A1A,
        targets = listOf(AppTarget(version = "13.141.0", versionCode = 33625540))
    )

val THE_BUDGETING_APP_COMPATIBILITY = Compatibility(
        name = "The Budgeting App",
        packageName = "com.thebudgetingapp.thebudgetingapp",
        appIconColor = 0x2E7D32,
        targets = listOf(AppTarget(version = "6.3.9", versionCode = 518))
    )

val THETRANSIT_COMPATIBILITY = Compatibility(
        name = "Transit",
        packageName = "com.thetransitapp.droid",
        appIconColor = 0x00B2A9,
        targets = listOf(AppTarget(version = "6.1.12", versionCode = 5125980))
    )

val THEWEATHERCHANNEL_COMPATIBILITY = Compatibility(
        name = "The Weather Channel",
        packageName = "com.weather.Weather",
        appIconColor = 0x1B6AC9,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "16.14.1", versionCode = 1080013465))
    )

val TIDAL_COMPATIBILITY = Compatibility(
        name = "TIDAL Music",
        packageName = "com.aspiro.tidal",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "2.201.0", versionCode = 10005))
    )

val TIME_SQUARED_COMPATIBILITY = Compatibility(
        name = "Time Squared: Time Tracker",
        packageName = "co.timesquared.timetracker",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "3.4.1730", versionCode = 1730))
    )

val TIMEUNTIL_COMPATIBILITY = Compatibility(
        name = "Time Until",
        packageName = "com.brunoschalch.timeuntil",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.4.2", versionCode = 165))
    )

val TINYCAM_COMPATIBILITY = Compatibility(
        name = "tinyCam Monitor",
        packageName = "com.alexvas.dvr",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "18.1.1 - Google Play", versionCode = 68760))
    )

val TIVIMATE_COMPATIBILITY = Compatibility(
        name = "TiviMate IPTV Player",
        packageName = "ar.tvplayer.tv",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.3.3", versionCode = 5332))
    )

val TODAYWEATHER_COMPATIBILITY = Compatibility(
        name = "Today Weather",
        packageName = "mobi.lockdown.weather",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "2.5.0-6", versionCode = 756))
    )

val TOMTOMGO_COMPATIBILITY = Compatibility(
        name = "TomTom GO",
        packageName = "com.tomtom.gplay.navapp",
        appIconColor = 0xDF1B12,
        targets = listOf(AppTarget(version = "3.6.316-beta", versionCode = 1678657))
    )

val TOOMICS_COMPATIBILITY = Compatibility(
        name = "Toomics",
        packageName = "com.toomics.global.google",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "1.6.7", versionCode = 106))
    )

val TORRDROID_COMPATIBILITY = Compatibility(
        name = "TorrDroid",
        packageName = "intelligems.torrdroid",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "2.0.0", versionCode = 10266))
    )

val TORRENTSEARCH_COMPATIBILITY = Compatibility(
        name = "Torrent Search Revolution V2",
        packageName = "torrent.search.revolutionv2",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.3.2", versionCode = 232))
    )

val TOXLY_COMPATIBILITY = Compatibility(
        name = "Toxly",
        packageName = "com.mindful.code.studio.toxly.scanner",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "1.17.1", versionCode = 71))
    )

val TRACK17_COMPATIBILITY = Compatibility(
        name = "17TRACK- Package Tracker",
        packageName = "yqtrack.app",
        appIconColor = 0x1E88E5,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "3.1.6986", versionCode = 10486))
    )

val TRACKCHECKER_COMPATIBILITY = Compatibility(
        name = "TrackChecker Mobile",
        packageName = "com.metalsoft.trackchecker_mobile",
        appIconColor = 0x1E88E5,
        targets = listOf(AppTarget(version = "2.29.3", versionCode = 505))
    )

val TRACKED_COMPATIBILITY = Compatibility(
        name = "Tracked",
        packageName = "com.tracked.mobile",
        appIconColor = 0x020617,
        targets = listOf(AppTarget(version = "7.1.1", versionCode = 127))
    )

val TRACKERDETECT_COMPATIBILITY = Compatibility(
        name = "Tracker Detect",
        packageName = "com.apple.trackerdetect",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "1.2", versionCode = 9))
    )

val TRACKERDETECTPRO_COMPATIBILITY = Compatibility(
        name = "Tracker Detect Pro",
        packageName = "com.dylan.airtag.detector.pro",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "9.9.6", versionCode = 909060))
    )

val TRADINGVIEW_COMPATIBILITY = Compatibility(
        name = "TradingView",
        packageName = "com.tradingview.tradingviewapp",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x2962FF,
        targets = listOf(AppTarget(version = "1.20.79.0.1002355", versionCode = 1002355))
    )

val TRANSPARENT_CLOCK_WEATHER_COMPATIBILITY = Compatibility(
        name = "Transparent clock & weather",
        packageName = "com.droid27.transparentclockweather",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "9.12.0", versionCode = 1410))
    )

val TRANZMATE_COMPATIBILITY = Compatibility(
        name = "Moovit",
        packageName = "com.tranzmate",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0066FF,
        targets = listOf(AppTarget(version = "5.196.0.1794", versionCode = 1794))
    )

val TRIPVIEW_LITE_COMPATIBILITY = Compatibility(
        name = "TripView Lite",
        packageName = "com.grofsoft.tripview.lite",
        appIconColor = 0x0066CC,
        targets = listOf(AppTarget(version = "4.5.6", versionCode = 689))
    )

val TRUEPHONE_COMPATIBILITY = Compatibility(
        name = "True Phone Dialer & Contacts",
        packageName = "com.hb.dialer.free",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "2.0.24-pt-2026-01-15", versionCode = 124))
    )

val TSR_COMPATIBILITY = Compatibility(
        name = "Torrent Search Revolution V2",
        packageName = "torrent.search.revolutionv2",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "2.3.2", versionCode = 232))
    )

val TUNEIN_COMPATIBILITY = Compatibility(
        name = "TuneIn Radio",
        packageName = "tunein.player",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x14D8CC,
        targets = listOf(AppTarget(version = "42.0", versionCode = 290035))
    )

val TURBOSCAN_COMPATIBILITY = Compatibility(
        name = "TurboScan",
        packageName = "com.piksoft.turboscan.free",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.7.3", versionCode = 123))
    )

val TWTAPP_COMPATIBILITY = Compatibility(
        name = "Stargazing Hub",
        packageName = "com.twtapp",
        appIconColor = 0x1A1A2E,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "3.3.2", versionCode = 3030200))
    )

val UBIKITOUCH_COMPATIBILITY = Compatibility(
        name = "UbikiTouch",
        packageName = "eu.toneiv.ubktouch",
        appIconColor = 0x0D47A1,
        targets = listOf(AppTarget(version = "1.16.13", versionCode = 73441))
    )

val UDISC_COMPATIBILITY = Compatibility(
        name = "UDisc",
        packageName = "com.regasoftware.udisc",
        appIconColor = 0xF47C20,
        targets = listOf(AppTarget(version = "24.2.3", versionCode = 9935))
    )

val UNIVERSALTV_COMPATIBILITY = Compatibility(
        name = "Unimote",
        packageName = "sensustech.universal.tv.remote.control",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.8.1"))
    )

val UPNOTE_COMPATIBILITY = Compatibility(
        name = "UpNote - Notes, Journal, Diary",
        packageName = "com.getupnote.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4A90D9,
        targets = listOf(AppTarget(version = "9.18.10", versionCode = 342))
    )

val UPTODOWN_COMPATIBILITY = Compatibility(
        name = "Uptodown App Store",
        packageName = "com.uptodown",
        appIconColor = 0x1E88E5,
        targets = listOf(AppTarget(version = "7.34", versionCode = 734))
    )

val VIZMANGA_COMPATIBILITY = Compatibility(
        name = "VIZ Manga",
        packageName = "com.vizmanga.android",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "4.14.2", versionCode = 240))
    )

val VPHONEGAGA_COMPATIBILITY = Compatibility(
        name = "VPhoneGaga",
        packageName = "com.yoyo.snake.rush",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x6C63FF,
        targets = listOf(AppTarget(version = "5.0.8", versionCode = 6092))
    )

val VPNIFY_COMPATIBILITY = Compatibility(
        name = "vpnify",
        packageName = "com.vpn.free.hotspot.secure.vpnify",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4A90D9,
        targets = listOf(AppTarget(version = "2.2.9.2", versionCode = 2290056))
    )

val WARP_COMPATIBILITY = Compatibility(
        name = "1.1.1.1",
        packageName = "com.cloudflare.onedotonedotonedotone",
        appIconColor = 0xF48120,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "6.38.8", versionCode = 5431))
    )

val WAVVE_BOATING_COMPATIBILITY = Compatibility(
        name = "Wavve Boating",
        packageName = "com.wavve.boating.gps",
        appIconColor = 0x0077CC,
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "5.7.4", versionCode = 3183))
    )

val WAYPOINT_COMPATIBILITY = Compatibility(
        name = "Waypoint",
        packageName = "com.greycastel.waypoint",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x171717,
        targets = listOf(AppTarget(version = "1.6.2", versionCode = 60))
    )

val WAZE_COMPATIBILITY = Compatibility(
        name = "Waze",
        packageName = "com.waze",
        appIconColor = 0x33CCFF,
        targets = listOf(AppTarget(version = "5.21.90.800", versionCode = 1030712))
    )

val WEATHERBUG_COMPATIBILITY = Compatibility(
        name = "WeatherBug- Weather Forecasts",
        packageName = "com.aws.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xF57C00,
        targets = listOf(AppTarget(version = "6.3.1-1", versionCode = 2106000550))
    )

val WEATHERWISE_COMPATIBILITY = Compatibility(
        name = "WeatherWise",
        packageName = "com.interactiveweather.weatherwise",
        appIconColor = 0x00A3A3,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "1.8.2", versionCode = 115))
    )

val WEAWOW_COMPATIBILITY = Compatibility(
        name = "Weawow: Weather & Widget",
        packageName = "com.weawow",
        appIconColor = 0x2196F3,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "7.1.8", versionCode = 718))
    )

val WEBNOVEL_COMPATIBILITY = Compatibility(
        name = "WebNovel",
        packageName = "com.qidian.Int.reader",
        appIconColor = 0x1976D2,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "8.21.1", versionCode = 1211))
    )

val WHATSAPP_COMPATIBILITY = Compatibility(
        name = "WhatsApp",
        packageName = "com.whatsapp",
        appIconColor = 0x25D366,
        targets = listOf(AppTarget(version = null))
    )

val WINDSCRIBE_COMPATIBILITY = Compatibility(
        name = "Windscribe VPN",
        packageName = "com.windscribe.vpn",
        appIconColor = 0x00AEEF,
        targets = listOf(AppTarget(version = "4.2.2328", versionCode = 2328))
    )

val WINDY_COMPATIBILITY = Compatibility(
        name = "Windy",
        packageName = "com.windyty.android",
        appIconColor = 0x0FA0EB,
        targets = listOf(AppTarget(version = "50.1.1"))
    )

val WINDYAPP_COMPATIBILITY = Compatibility(
        name = "Windy: Weather Radar & Forecast",
        packageName = "co.windyapp.android",
        appIconColor = 0x0FA0EB,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "93.2.2", versionCode = 872))
    )

val WOLFRAMALPHA_COMPATIBILITY = Compatibility(
        name = "WolframAlpha",
        packageName = "com.wolfram.android.alphapro",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xDD1100,
        targets = listOf(AppTarget(version = "1.0.8.20260601651", versionCode = 117))
    )

val WORD_COMPATIBILITY = Compatibility(
        name = "Word",
        packageName = "com.microsoft.office.word",
        appIconColor = 0x1A237E,
        targets = listOf(AppTarget(version = "16.0.20131.20080", versionCode = 2005201435))
    )

val WORKOUT_COMPATIBILITY = Compatibility(
        name = "Workout",
        packageName = "air.com.musclemotion.workout",
        appIconColor = 0xC62828,
        targets = listOf(AppTarget(version = "1.2.0", versionCode = 153))
    )

val WPS_OFFICE_COMPATIBILITY = Compatibility(
        name = "WPS Office- Word, Docs, PDF",
        packageName = "cn.wps.moffice_eng",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xE84C3D,
        targets = listOf(AppTarget(version = "26.7.2", versionCode = 1612))
    )

val WPS_SCAN_COMPATIBILITY = Compatibility(
        name = "WPS 扫描",
        packageName = "cn.wps.scanhost",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE84C3D,
        targets = listOf(AppTarget(version = "1.0.1", versionCode = 1905))
    )

val YANDEX_MAPS_COMPATIBILITY = Compatibility(
        name = "Yandex Maps",
        packageName = "ru.yandex.yandexmaps",
        appIconColor = 0xFF0000,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "30.0.1", versionCode = 739523110))
    )

val YATRI_COMPATIBILITY = Compatibility(
        name = "Yatri",
        packageName = "com.yatrirailways.yatri",
        appIconColor = 0xFF6B00,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.0.5", versionCode = 1003))
    )

val YOGA_COMPATIBILITY = Compatibility(
        name = "Yoga",
        packageName = "air.com.musclemotion.yoga",
        appIconColor = 0x7B1FA2,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "3.2.1", versionCode = 1004178))
    )

val YUKA_COMPATIBILITY = Compatibility(
        name = "Yuka",
        packageName = "io.yuka.android",
        appIconColor = 0x5C8F3B,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.0.0", versionCode = 859))
    )

val ZEDGE_COMPATIBILITY = Compatibility(
        name = "Zedge",
        packageName = "net.zedge.android",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x00B140,
        targets = listOf(AppTarget(version = "9.30.1", versionCode = 93000100))
    )

val ZOOMEARTH_COMPATIBILITY = Compatibility(
        name = "Zoom Earth",
        packageName = "com.neave.zoomearth",
        appIconColor = 0x1A6FBF,
        targets = listOf(AppTarget(version = null))
    )
}
