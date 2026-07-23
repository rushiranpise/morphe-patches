package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {

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

val ADGUARD_COMPATIBILITY = Compatibility(
        name = "AdGuard Nightly",
        packageName = "com.adguard.android",
        appIconColor = 0x67B346,
        targets = listOf(AppTarget(version = "4.14.68"))
    )

val AIDA64_COMPATIBILITY = Compatibility(
        name = "AIDA64",
        packageName = "com.finalwire.aida64",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.21", versionCode = 221))
    )

val AISCORE_COMPATIBILITY = Compatibility(
        name = "AiScore",
        packageName = "com.onesports.score",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = "4.2.7", versionCode = 292))
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

val AMAZON_SHOPPING_COMPATIBILITY = Compatibility(
        name = "Amazon Shopping",
        packageName = "com.amazon.mShop.android.shopping",
        appIconColor = 0xFF9900,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "32.13.2.100", versionCode = 1241320216))
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

val APKMIRROR_INSTALLER_COMPATIBILITY = Compatibility(
        name = "APKMirror Installer",
        packageName = "com.apkmirror.helper.prod",
        appIconColor = 0xFF9800,
        targets = listOf(AppTarget(version = "2.0.3 (41-d04e542)", versionCode = 41))
    )

val BATTERYGURU_COMPATIBILITY = Compatibility(
        name = "Battery Guru",
        packageName = "com.paget96.batteryguru",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1B7080,
        targets = listOf(AppTarget(version = "2.5.0.5", versionCode = 712))
    )

val BATTERYPODS_COMPATIBILITY = Compatibility(
        name = "BatteryPods",
        packageName = "com.sumyapplications.bluetooth.earphone",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.20", versionCode = 7200))
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

val BOXBOX_COMPATIBILITY = Compatibility(
        name = "Box Box",
        packageName = "club.boxbox.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE10600,
        targets = listOf(AppTarget(version = "5.4.13", versionCode = 256))
    )

val BUZZCAST_COMPATIBILITY = Compatibility(
        name = "BuzzCast",
        packageName = "com.guochao.faceshow",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x7C3AED,
        targets = listOf(AppTarget(version = "3.2.82", versionCode = 3282))
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
        name = "Calory",
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

val CAPOD_COMPATIBILITY = Compatibility(
        name = "CAPod",
        packageName = "eu.darken.capod",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xF5A623,
        targets = listOf(AppTarget(version = "5.2.1", versionCode = 50201000))
    )

val CARBON_COMPATIBILITY = Compatibility(
        name = "Carbon",
        packageName = "com.joincarbon.nutrition",
        appIconColor = 0x111111,
        targets = listOf(AppTarget(version = "2.76.5891", versionCode = 1633629177))
    )

val CASETRACKER_COMPATIBILITY = Compatibility(
        name = "Case Tracker",
        packageName = "com.saldous.casetracker",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.5.4", versionCode = 1054))
    )

val CASHEW_COMPATIBILITY = Compatibility(
        name = "Cashew",
        packageName = "com.budget.tracker_app",
        appIconColor = 0xFFB300,
        targets = listOf(AppTarget(version = "6.6.11", versionCode = 510))
    )

val CHARGEMETER_COMPATIBILITY = Compatibility(
        name = "Charge Meter",
        packageName = "dev.km.android.chargemeter",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.9.7", versionCode = 77))
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

val COUNTDOWN_WIDGET_COMPATIBILITY = Compatibility(
        name = "Countdown Widget",
        packageName = "me.gira.widget.countdown",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "3.2.0", versionCode = 306))
    )

val CPUZ_COMPATIBILITY = Compatibility(
        name = "CPU-Z",
        packageName = "com.cpuid.cpu_z",
        appIconColor = 0x2A3B4C,
        targets = listOf(AppTarget(version = "1.59", versionCode = 59))
    )

val CRIMERADAR_COMPATIBILITY = Compatibility(
        name = "Crime Radar",
        packageName = "com.newsbreak.crimeradar",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "26.29.0", versionCode = 26290002))
    )

val DAILYHUNT_COMPATIBILITY = Compatibility(
        name = "Dailyhunt",
        packageName = "com.eterno",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "34.0.4", versionCode = 10314))
    )

val DEPTH_LIVE_WALLPAPER_COMPATIBILITY = Compatibility(
        name = "Depth Live Wallpaper",
        packageName = "com.jndapp.depth.live.wallpaper",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.1.06", versionCode = 35))
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

val EXCEL_COMPATIBILITY = Compatibility(
        name = "Excel",
        packageName = "com.microsoft.office.excel",
        appIconColor = 0x1B5E20,
        targets = listOf(AppTarget(version = "16.0.20131.20080", versionCode = 2005201435))
    )

val FITBOD_COMPATIBILITY = Compatibility(
        name = "Fitbod",
        packageName = "com.fitbod.fitbod",
        appIconColor = 0xFF3D00,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "8.26.0-0", versionCode = 10826000))
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

val GENIUSSCAN_COMPATIBILITY = Compatibility(
        name = "Genius Scan",
        packageName = "com.thegrizzlylabs.geniusscan.free",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "7.41.0", versionCode = 7555))
    )

val GOOGLE_PHOTOS_COMPATIBILITY = Compatibility(
        name = "Google Photos",
        packageName = "com.google.android.apps.photos",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4285F4,
        targets = listOf(AppTarget(version = "7.85.0.952162352", versionCode = 52099290))
    )

val GREENIFY_COMPATIBILITY = Compatibility(
        name = "Greenify",
        packageName = "com.oasisfeng.greenify",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "5.1.1"))
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

val HTTPMOCK_COMPATIBILITY = Compatibility(
        name = "HTTP Sniffer",
        packageName = "com.anetcapture.mock",
        appIconColor = 0x2196F3,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "2.11.7-ad_mob", versionCode = 139))
    )

val IMAGEDATEFIXER_COMPATIBILITY = Compatibility(
        name = "Image & Video Date Fixer",
        packageName = "eu.duong.imagedatefixer",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.7.1", versionCode = 30700100))
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

val INURE_COMPATIBILITY = Compatibility(
        name = "Inure App Manager",
        packageName = "app.simple.inure.play",
        appIconColor = 0x6200EE,
        targets = listOf(AppTarget(version = "build107.0.5", versionCode = 10705))
    )

val JEFIT_COMPATIBILITY = Compatibility(
        name = "JEFIT",
        packageName = "je.fit",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "17.2.9", versionCode = 2020))
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

val LIVESCORE_COMPATIBILITY = Compatibility(
        name = "LiveScore",
        packageName = "com.livescore",
        appIconColor = 0xE30613,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "9.8", versionCode = 2088))
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

val MYPERM_COMPATIBILITY = Compatibility(
        name = "Permission Pilot",
        packageName = "eu.darken.myperm",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "2.2.0-rc0", versionCode = 20200000))
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
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF6F00,
        targets = listOf(AppTarget(version = "1.1.0-rc0", versionCode = 10100000))
    )

val ONETAPCLEANER_COMPATIBILITY = Compatibility(
        name = "1Tap Cleaner",
        packageName = "com.a0soft.gphone.acc.free",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.21", versionCode = 240005219))
    )

val OPERA_NEWS_COMPATIBILITY = Compatibility(
        name = "Opera News - Breaking & Local",
        packageName = "com.opera.app.news",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF1B2A,
        targets = listOf(AppTarget(version = "14.1.2254.83278", versionCode = 141083278))
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

val PLAYIT_COMPATIBILITY = Compatibility(
        name = "PLAYit",
        packageName = "com.playit.videoplayer",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "2.7.50.12", versionCode = 20750012))
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

val POCKETPREP_BEHAVIORAL_HEALTH_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Behavioral Health",
        packageName = "com.pocketprep.android.behavioralhealth",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_COMPATIBILITY = Compatibility(
        name = "Pocket Prep IT Cybersecurity",
        packageName = "com.pocketprep.android.itcybersecurity",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_EMS_COMPATIBILITY = Compatibility(
        name = "Pocket Prep EMS",
        packageName = "com.pocketprep.android.ems",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_ESSENTIALS_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Essentials",
        packageName = "com.pocketprep.android.essentials",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_FITNESS_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Fitness",
        packageName = "com.pocketprep.android.fitness",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_MAIN_COMPATIBILITY = Compatibility(
        name = "Pocket Prep",
        packageName = "com.pocketprep.android.pocketprep",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_MEDICAL_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Medical",
        packageName = "com.pocketprep.android.medical",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_NURSING_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Nursing",
        packageName = "com.pocketprep.android.nursing",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_NURSING_SCHOOL_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Nursing School",
        packageName = "com.pocketprep.android.nursingschool",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_PROFESSIONAL_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Professional",
        packageName = "com.pocketprep.android.professional",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val POCKETPREP_SKILLED_TRADES_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Skilled Trades",
        packageName = "com.pocketprep.android.automotive",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.0"))
    )

val PODSLINK_COMPATIBILITY = Compatibility(
        name = "PodsLink",
        packageName = "net.podslink",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "1.3.5", versionCode = 79))
    )

val POLICESCANNER_COMPATIBILITY = Compatibility(
        name = "Police Scanner",
        packageName = "police.scanner.radio.broadcastify.citizen",
        appIconColor = 0x0D47A1,
        targets = listOf(AppTarget(version = "1.29.0-260420093"))
    )

val PROTONVPN_COMPATIBILITY = Compatibility(
        name = "Proton VPN",
        packageName = "ch.protonvpn.android",
        appIconColor = 0x6D4AFF,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.19.61.0", versionCode = 605196100))
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

val QBITCONNECT_COMPATIBILITY = Compatibility(
        name = "qBitConnect",
        packageName = "com.bluematter.qbitconnect",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4FC3F7,
        targets = listOf(AppTarget(version = "2.0.5", versionCode = 65))
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

val RENAMEORGANIZE_COMPATIBILITY = Compatibility(
        name = "Rename & Organize",
        packageName = "eu.duong.picturemanager",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "8.25.1", versionCode = 8250101))
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

val SAI_COMPATIBILITY = Compatibility(
        name = "SAI",
        packageName = "com.mtv.sai",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.3.1"))
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

val SHEXA_COMPATIBILITY = Compatibility(
        name = "App Permission Manager",
        packageName = "com.shexa.permissionmanager",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.4.6.2"))
    )

val SLOPES_COMPATIBILITY = Compatibility(
        name = "Slopes",
        packageName = "com.consumedbycode.slopes",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2026.14", versionCode = 2419))
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

val SPOTANGELS_COMPATIBILITY = Compatibility(
        name = "SpotAngels",
        packageName = "com.spotangels.android",
        appIconColor = 0x1C9BE6,
        targets = listOf(AppTarget(version = "15.2.2", versionCode = 10323))
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

val STRAVA_COMPATIBILITY = Compatibility(
        name = "Strava",
        packageName = "com.strava",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFC4C02,
        targets = listOf(AppTarget(version = "471.11", versionCode = 12398607))
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

val TAGTRACKER_COMPATIBILITY = Compatibility(
        name = "Tag Tracker",
        packageName = "com.makeevapps.tagtracker",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "1.3.2", versionCode = 19))
    )

val THE_ATHLETIC_COMPATIBILITY = Compatibility(
        name = "The Athletic",
        packageName = "com.theathletic",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A1A1A,
        targets = listOf(AppTarget(version = "13.142.0", versionCode = 33625571))
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
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.3.3", versionCode = 233))
    )

val TOXLY_COMPATIBILITY = Compatibility(
        name = "Toxly",
        packageName = "com.mindful.code.studio.toxly.scanner",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "1.17.1", versionCode = 71))
    )

val TRACKCHECKER_COMPATIBILITY = Compatibility(
        name = "TrackChecker Mobile",
        packageName = "com.metalsoft.trackchecker_mobile",
        appIconColor = 0x1E88E5,
        targets = listOf(AppTarget(version = "2.29.3", versionCode = 505))
    )

val TRACKERDETECT_COMPATIBILITY = Compatibility(
        name = "Tracker Detect",
        packageName = "com.apple.trackerdetect",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "1.2", versionCode = 9))
    )

val TRADINGVIEW_COMPATIBILITY = Compatibility(
        name = "TradingView",
        packageName = "com.tradingview.tradingviewapp",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x2962FF,
        targets = listOf(AppTarget(version = "1.20.79.0.1002355", versionCode = 1002355))
    )

val TRANZMATE_COMPATIBILITY = Compatibility(
        name = "Moovit",
        packageName = "com.tranzmate",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0066FF,
        targets = listOf(AppTarget(version = "5.196.0.1794", versionCode = 1794))
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

val WAZE_COMPATIBILITY = Compatibility(
        name = "Waze",
        packageName = "com.waze",
        appIconColor = 0x33CCFF,
        targets = listOf(AppTarget(version = "5.21.90.800", versionCode = 1030712))
    )

val WEAWOW_COMPATIBILITY = Compatibility(
        name = "Weawow",
        packageName = "com.weawow",
        appIconColor = 0x2196F3,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "7.1.8", versionCode = 718))
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

val YATRI_COMPATIBILITY = Compatibility(
        name = "Yatri",
        packageName = "com.yatrirailways.yatri",
        appIconColor = 0xFF6B00,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.0.5", versionCode = 1003))
    )
}
