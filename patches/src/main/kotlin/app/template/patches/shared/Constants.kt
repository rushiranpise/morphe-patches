package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.SupportedAbi

object Constants {

val AAAD_COMPATIBILITY = Compatibility(
        name = "AAAD",
        packageName = "sksa.aa.customapps",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1A73E8,
        targets = listOf(
            AppTarget(version = "2.8.5", versionCode = 47)
        )
    )

val AAENABLER_COMPATIBILITY = Compatibility(
        name = "AAEnabler",
        packageName = "com.aaenabler.app",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1A73E8,
        targets = listOf(
            AppTarget(version = "1.0.2")
        )
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
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "21.1.15-3-rc", versionCode = 210115003))
    )

val ACE_EXPLORER_COMPATIBILITY = Compatibility(
        name = "Ace Ex File Manager",
        packageName = "com.ace.ex.file.manager",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.6.0.4", versionCode = 48))
    )

val ADGUARD_UNIFIED_COMPATIBILITY = Compatibility(
        name = "AdGuard",
        packageName = "com.adguard.android",
        appIconColor = 0x67B346,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "4.14.68", versionCode = 10330100)),
    )

val ADOBE_READER_COMPATIBILITY = Compatibility(
        name = "Adobe Acrobat",
        packageName = "com.adobe.reader",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xB30B00,
        targets = listOf(AppTarget(version = "26.7.0.47169"))
    )

val ADOBE_SCAN_COMPATIBILITY = Compatibility(
        name = "Adobe Scan",
        packageName = "com.adobe.scan.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xB30B00,
        targets = listOf(AppTarget(version = "26.08.01"))
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
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.2.4", versionCode = 53))
    )

val AMAZON_IN_COMPATIBILITY = Compatibility(
        name = "Amazon India",
        packageName = "in.amazon.mShop.android.shopping",
        appIconColor = 0xFF9900,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "32.15.0.300", versionCode = 1243210306))
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
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "7.3", versionCode = 81))
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
        targets = listOf(AppTarget(version = "1.0.958871038", versionCode = 65354))
    )

val ANDROPODS_COMPATIBILITY = Compatibility(
        name = "AndroPods",
        packageName = "pro.vitalii.andropods",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1DA1F2,
        targets = listOf(AppTarget(version = "1.5.28", versionCode = 84))
    )

val ANEXPLORER_COMPATIBILITY = Compatibility(
        name = "AnExplorer",
        packageName = "dev.dworks.apps.anexplorer",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "6.0.6", versionCode = 60604))
    )

val ANIME_DEPTH_WALLPAPERS_COMPATIBILITY = Compatibility(
        name = "Anime Depth Wallpapers",
        packageName = "com.jndapp.anime.depth.live.wallpaper",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x6A1B9A,
        targets = listOf(AppTarget(version = "1.1.2", versionCode = 6))
    )

val APKMIRROR_INSTALLER_COMPATIBILITY = Compatibility(
        name = "APKMirror Installer",
        packageName = "com.apkmirror.helper.prod",
        appIconColor = 0xFF9800,
        targets = listOf(AppTarget(version = "2.0.3 (41-d04e542)", versionCode = 41))
    )

val APPLOCKWATCHER_COMPATIBILITY = Compatibility(
        name = "AppLock Watcher",
        packageName = "com.domobile.applockwatcher",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1565C0,
        targets = listOf(
            AppTarget(version = "6.3.3", versionCode = 2026073001)
        )
    )

val ATHENA_COMPATIBILITY = Compatibility(
        name = "Athena",
        packageName = "com.kin.athena",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.80", versionCode = 306))
    )

val AUTOCURSOR_COMPATIBILITY = Compatibility(
        name = "Auto Cursor",
        packageName = "eu.toneiv.cursor",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x0D47A1,
        targets = listOf(
            AppTarget(version = "1.11.1", versionCode = 23164)
        )
    )

val AUTOMATE_COMPATIBILITY = Compatibility(
        name = "Automate",
        packageName = "com.llamalab.automate",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF6600,
        targets = listOf(AppTarget(version = "1.53.2", versionCode = 266))
    )

val AVIATE_COMPATIBILITY = Compatibility(
        name = "Aviate",
        packageName = "me.highest.aviate",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "1.1.0-beta.1", versionCode = 202))
    )

val AWAKE_COMPATIBILITY = Compatibility(
        name = "Awake",
        packageName = "com.mk.awake",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A237E,
        targets = listOf(AppTarget(version = "1.10.3", versionCode = 230020072))
    )

val BATTERYGURU_COMMUNITY_COMPATIBILITY = Compatibility(
        name = "Battery Guru",
        packageName = "com.paget96.batteryguru",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1B7080,
        targets = listOf(AppTarget(version = "2.5.0.7", versionCode = 728))
    )

val BATTERYGURU_COMPATIBILITY = Compatibility(
        name = "Battery Guru",
        packageName = "com.paget96.batteryguru",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1B7080,
        targets = listOf(AppTarget(version = "2.5.0.7", versionCode = 728))
    )

val BATTERYPODS_COMPATIBILITY = Compatibility(
        name = "BatteryPods",
        packageName = "com.sumyapplications.bluetooth.earphone",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.52", versionCode = 7520))
    )

val BLEK_COMPATIBILITY = Compatibility(
        name = "Bluetooth Keyboard & Mouse",
        packageName = "io.appground.blek",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "6.23.1", versionCode = 257))
    )

val BLOCKBLAST_COMPATIBILITY = Compatibility(
        name = "Block Blast!",
        packageName = "com.block.juggle",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "10.4.5", versionCode = 10450))
    )

val BLOCKERHERO_COMPATIBILITY = Compatibility(
        name = "BlockerHero",
        packageName = "com.blockerhero",
        appIconColor = 0xFF5252,
        targets = listOf(AppTarget(version = "1.5.0"))
    )

val BLOCKING_SITES_COMPATIBILITY = Compatibility(
        name = "Blocking Sites",
        packageName = "com.blocking.sites",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xEF4444,
        targets = listOf(AppTarget(version = "4.0.19", versionCode = 331))
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
        targets = listOf(AppTarget(version = "5.1049.4.921", versionCode = 1921))
    )

val BLURWALL_COMPATIBILITY = Compatibility(
        name = "BlurWall",
        packageName = "apps.automan.blurwallpaper",
        appIconColor = 0x42A5F5,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "2.9.6", versionCode = 30))
    )

val BOLDVOICE_COMPATIBILITY = Compatibility(
        name = "BoldVoice",
        packageName = "com.wellocution.androidapp",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF5A36,
        targets = listOf(AppTarget(version = "4.6.1", versionCode = 410))
    )

val BOXBOX_COMPATIBILITY = Compatibility(
        name = "Box Box",
        packageName = "club.boxbox.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE10600,
        targets = listOf(AppTarget(version = "5.4.15", versionCode = 258))
    )

val BUBBLEUPNP_COMPATIBILITY = Compatibility(
        name = "BubbleUPnP",
        packageName = "com.bubblesoft.android.bubbleupnp",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "4.6.5.1", versionCode = 3000929))
    )

val BUZZCAST_COMPATIBILITY = Compatibility(
        name = "BuzzCast",
        packageName = "com.guochao.faceshow",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x7C3AED,
        targets = listOf(AppTarget(version = "3.2.85", versionCode = 3285))
    )

val CALIMOTO_COMPATIBILITY = Compatibility(
        name = "calimoto",
        packageName = "com.calimoto.calimoto",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "2026.08.2", versionCode = 625))
    )

val CALLRECORDER_COMPATIBILITY = Compatibility(
        name = "Cube ACR",
        packageName = "com.catalinagroup.callrecorder",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "2.4.281"))
    )

val CALM_COMPATIBILITY = Compatibility(
        name = "Calm",
        packageName = "com.calm.android",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4A90D9,
        targets = listOf(AppTarget(version = "6.102", versionCode = 4120456))
    )

val CALORY_COMPATIBILITY = Compatibility(
        name = "Calory",
        packageName = "com.funnmedia.calory",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "3.8", versionCode = 208))
    )

val CAMSCANNER_COMPATIBILITY = Compatibility(
        name = "CamScanner",
        packageName = "com.intsig.camscanner",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x19BCAA,
        targets = listOf(AppTarget(version = "7.22.5.2607250000", versionCode = 72252))
    )

val CANVA_COMPATIBILITY = Compatibility(
        name = "Canva",
        packageName = "com.canva.editor",
        appIconColor = 0x8B3DFF,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "2.374.0", versionCode = 29682599))
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
        targets = listOf(AppTarget(version = "2.76.5966", versionCode = 1633629252))
    )

val CASETRACKER_COMPATIBILITY = Compatibility(
        name = "Case Tracker",
        packageName = "com.saldous.casetracker",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.5.6", versionCode = 1059))
    )

val CASHEW_COMPATIBILITY = Compatibility(
        name = "Cashew",
        packageName = "com.budget.tracker_app",
        appIconColor = 0xFFB300,
        targets = listOf(AppTarget(version = "6.6.11", versionCode = 510))
    )

val CAST_TO_TV_COMPATIBILITY = Compatibility(
        name = "Cast to TV",
        packageName = "cast.video.screenmirroring.casttotv",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "2.5.3.1", versionCode = 253102))
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
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0066FF,
        targets = listOf(AppTarget(version = "0.1303.2", versionCode = 1138))
    )

val CITYMAPPER_COMPATIBILITY = Compatibility(
        name = "Citymapper",
        packageName = "com.citymapper.app.release",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x00A862,
        targets = listOf(AppTarget(version = "11.57.1", versionCode = 1157080))
    )

val CLUE_COMPATIBILITY = Compatibility(
        name = "Clue Period & Cycle Tracker",
        packageName = "com.clue.android",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE91E63,
        targets = listOf(AppTarget(version = "266.0", versionCode = 3185))
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
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "1.60", versionCode = 60))
    )

val CRIMERADAR_COMPATIBILITY = Compatibility(
        name = "Crime Radar",
        packageName = "com.newsbreak.crimeradar",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "26.33.1", versionCode = 26330103))
    )

val CUBESOLVER_COMPATIBILITY = Compatibility(
        name = "Cube Solver",
        packageName = "com.jeffprod.cubesolver",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF6600,
        targets = listOf(AppTarget(version = "5.0.4", versionCode = 10170))
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

val DRAMABOX_COMPATIBILITY = Compatibility(
        name = "DramaBox",
        packageName = "com.storymatrix.drama",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE53935,
        targets = listOf(
            AppTarget(version = "6.6.0", versionCode = 660)
        )
    )

val DRIVERLICENSE_COMPATIBILITY = Compatibility(
        name = "Zutobi",
        packageName = "com.driverlicenseapp",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(
            AppTarget(version = "4.5.78", versionCode = 102278)
        )
    )

val DRIVVO_COMPATIBILITY = Compatibility(
        name = "Drivvo",
        packageName = "br.com.ctncardoso.ctncar",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0077B6,
        targets = listOf(AppTarget(version = "9.6.1", versionCode = 242))
    )

val DUBOXDRIVE_COMPATIBILITY = Compatibility(
        name = "TeraBox",
        packageName = "com.dubox.drive",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x2EAAFF,
        targets = listOf(AppTarget(version = "4.23.5", versionCode = 680))
    )

val DUOLINGO_COMPATIBILITY = Compatibility(
        name = "Duolingo",
        packageName = "com.duolingo",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x58CC02,
        targets = listOf(AppTarget(version = "6.90.3", versionCode = 2422))
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
        targets = listOf(AppTarget(version = "16.0.20326.20034", versionCode = 200292347))
    )

val FEEM_COMPATIBILITY = Compatibility(
        name = "Feem",
        packageName = "com.feeperfect.airsend.android",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x4350AF,
        targets = listOf(
            AppTarget(version = "6.10.0", versionCode = 6010000)
        )
    )

val FILE_MANAGER_PLUS_COMPATIBILITY = Compatibility(
        name = "File Manager",
        packageName = "com.alphainventor.filemanager",
        appIconColor = 0xFF7043,
        targets = listOf(
            AppTarget(version = "3.8.2", versionCode = 2103082)
        )
    )

val FITBOD_COMPATIBILITY = Compatibility(
        name = "Fitbod",
        packageName = "com.fitbod.fitbod",
        appIconColor = 0xFF3D00,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "8.30.0-0", versionCode = 10830000))
    )

val FITIA_COMPATIBILITY = Compatibility(
        name = "Fitia",
        packageName = "com.nutrition.technologies.Fitia",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "25.1.12", versionCode = 1493))
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
        targets = listOf(AppTarget(version = "11.9.0", versionCode = 110900000))
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
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xF16522,
        targets = listOf(AppTarget(version = "2.0.15", versionCode = 100015352))
    )

val FUELIO_COMPATIBILITY = Compatibility(
        name = "Fuelio",
        packageName = "com.kajda.fuelio",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6B00,
        targets = listOf(AppTarget(version = "10.3.3", versionCode = 3437))
    )

val GENIUSSCAN_COMPATIBILITY = Compatibility(
        name = "Genius Scan",
        packageName = "com.thegrizzlylabs.geniusscan.free",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "7.41.0", versionCode = 7555))
    )

val GETCONTACT_COMPATIBILITY = Compatibility(
        name = "Getcontact",
        packageName = "app.source.getcontact",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x00BCD4,
        targets = listOf(AppTarget(version = "8.16.0", versionCode = 16471)),
    )

val GOOGLE_PHOTOS_COMPATIBILITY = Compatibility(
        name = "Google Photos",
        packageName = "com.google.android.apps.photos",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x4285F4,
        targets = listOf(AppTarget(version = "7.89.0.968035987", versionCode = 52244454))
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
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "2.57.0", versionCode = 5230))
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
        targets = listOf(AppTarget(version = "2.3.6-ad_mob", versionCode = 154))
    )

val HYDROCOACH_COMPATIBILITY = Compatibility(
        name = "Hydro Coach",
        packageName = "com.codium.hydrocoach",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0288D1,
        targets = listOf(AppTarget(version = "5.1.9", versionCode = 541))
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
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "2.3.50", versionCode = 739))
    )

val INSCODE_AUTOCLICKER_COMPATIBILITY = Compatibility(
        name = "Clickmate",
        packageName = "com.inscode.autoclicker",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.1.4", versionCode = 680))
    )

val INURE_COMPATIBILITY = Compatibility(
        name = "Inure App Manager",
        packageName = "app.simple.inure.play",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x6200EE,
        targets = listOf(AppTarget(version = "build107.2.1", versionCode = 10721))
    )

val INURE_GITHUB_COMPATIBILITY = Compatibility(
        name = "Inure App Manager (GitHub)",
        packageName = "app.simple.inure",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x6200EE,
        targets = listOf(AppTarget(version = "build107.2.1", versionCode = 10721))
    )

val JEFIT_COMPATIBILITY = Compatibility(
        name = "JEFIT",
        packageName = "je.fit",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "17.2.12", versionCode = 2048))
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
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "1.58.0", versionCode = 1000))
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
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x65AB1E,
        targets = listOf(AppTarget(version = "2026.34.2", versionCode = 263821002))
    )

val LARK_PLAYER_COMPATIBILITY = Compatibility(
        name = "Lark Player",
        packageName = "com.dywx.larkplayer",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1DB954,
        targets = listOf(AppTarget(version = "2026.12.5", versionCode = 2026120509))
    )

val LAWFULLY_COMPATIBILITY = Compatibility(
        name = "Lawfully",
        packageName = "com.lawfully.lawfully_ai_tracker",
        appIconColor = 0x0D47A1,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "6.8.1", versionCode = 545))
    )

val LEAP_FITNESS_WOMEN_COMPATIBILITY = Compatibility(
        name = "Women Workout",
        packageName = "women.workout.female.fitness",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "2.3.5", versionCode = 142))
    )

val LEAP_HEIGHTINCREASE_COMPATIBILITY = Compatibility(
        name = "Height Increase",
        packageName = "increaseheightworkout.heightincreaseexercise.tallerexercise",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "1.1.17", versionCode = 66))
    )

val LEAP_HOMEWORKOUT_COMPATIBILITY = Compatibility(
        name = "Home Workout",
        packageName = "homeworkout.homeworkouts.noequipment",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "1.7.8", versionCode = 152))
    )

val LEAP_LOSEWEIGHT_WOMEN_COMPATIBILITY = Compatibility(
        name = "Lose Weight App for Women",
        packageName = "loseweightapp.loseweightappforwomen.womenworkoutathome",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "2.4.0", versionCode = 107))
    )

val LIFE360_COMPATIBILITY = Compatibility(
        name = "Life360",
        packageName = "com.life360.android.safetymapd",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x5D2DE6,
        targets = listOf(AppTarget(version = "26.31.0", versionCode = 2914950))
    )

val LIVESCORE_COMPATIBILITY = Compatibility(
        name = "LiveScore",
        packageName = "com.livescore",
        appIconColor = 0xE30613,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "10.0", versionCode = 2136))
    )

val MACRODROID_COMPATIBILITY = Compatibility(
        name = "MacroDroid",
        packageName = "com.arlosoft.macrodroid",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "5.66.9", versionCode = 896600011))
    )

val MALWAREBYTES_COMPATIBILITY = Compatibility(
        name = "Malwarebytes Mobile Security",
        packageName = "org.malwarebytes.antimalware",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x0066FF,
        targets = listOf(
            AppTarget(version = "5.27.0+564", versionCode = 52700564)
        )
    )

val MANGA_PLUS_COMPATIBILITY = Compatibility(
        name = "MANGA Plus by SHUEISHA",
        packageName = "jp.co.shueisha.mangaplus",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "2.6.2", versionCode = 262))
    )

val MAPY_COMPATIBILITY = Compatibility(
        name = "Mapy.com",
        packageName = "cz.seznam.mapy",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xE03C31,
        targets = listOf(AppTarget(version = "26.8.0", versionCode = 26080001))
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
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xD9272E,
        targets = listOf(AppTarget(version = "16.11.1(262250408)(9a6c828835)", versionCode = 262250408))
    )

val MEOW_COMPATIBILITY = Compatibility(
        name = "Money Manager",
        packageName = "com.glgjing.money.manager.bookkeeping.meow",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6B9E,
        targets = listOf(AppTarget(version = "1.9.9", versionCode = 79))
    )

val MESSENGER_COMPATIBILITY = Compatibility(
        name = "Messenger",
        packageName = "com.facebook.orca",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x0084FF,
        targets = listOf(
            AppTarget(
                version = "575.0.0.48.90",
                versionCodes = mapOf(
                    SupportedAbi.ARMEABI_V7A to 344611781,
                    SupportedAbi.ARM64_V8A to 344611864, versionCode = 345012838)
            ),
        )
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

val MIKROTIK_COMPATIBILITY = Compatibility(
        name = "MikroTik Network Tools",
        packageName = "kha.prog.mikrotik",
        appIconColor = 0x0063B1,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "279", versionCode = 279))
    )

val MINDICATOR_COMPATIBILITY = Compatibility(
        name = "m-Indicator",
        packageName = "com.mobond.mindicator",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "18.0.364", versionCode = 364))
    )

val MINIMAL_WIDGETS_COMPATIBILITY = Compatibility(
        name = "Minimal Widgets",
        packageName = "com.jndapp.minimal.widgets",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x212121,
        targets = listOf(AppTarget(version = "2.1.02", versionCode = 13))
    )

val MIRKO_COMPATIBILITY = Compatibility(
        name = "Beta Maniac",
        packageName = "it.mirko.beta",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "0.10.0", versionCode = 152))
    )

val MLMANAGER_COMPATIBILITY = Compatibility(
        name = "ML Manager",
        packageName = "com.javiersantos.mlmanager",
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "5.0"))
    )

val MOBIOFFICE_COMPATIBILITY = Compatibility(
        name = "MobiOffice",
        packageName = "com.mobisystems.office",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "16.5.60515", versionCode = 60515))
    )

val MONET_COMPATIBILITY = Compatibility(
        name = "Monet",
        packageName = "com.klevico.monet",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF6D00,
        targets = listOf(AppTarget(version = "1.0.76", versionCode = 110))
    )

val MOVIEBOXTV_COMPATIBILITY = Compatibility(
        name = "MovieBox TV",
        packageName = "com.community.mbox.tv",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APK,
        targets = listOf(
            AppTarget(version = "1.1.9.0820.03", versionCode = 50040014),
        )
    )

val MYANIMELIST_COMPATIBILITY = Compatibility(
        name = "MyAnimeList",
        packageName = "net.myanimelist.app",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x2E51A2,
        targets = listOf(
            AppTarget(version = "2.3.16", versionCode = 2031623)
        )
    )

val MYRADAR_COMPATIBILITY = Compatibility(
        name = "MyRadar",
        packageName = "com.acmeaom.android.myradar",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1A6FBF,
        targets = listOf(AppTarget(version = "8.72.0", versionCode = 593))
    )

val NAVITIME_COMPATIBILITY = Compatibility(
        name = "NAVITIME",
        packageName = "com.navitime.inbound.walk",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x003087,
        targets = listOf(AppTarget(version = "12.1.0", versionCode = 374))
    )

val NETGUARD_COMPATIBILITY = Compatibility(
        name = "NetGuard",
        packageName = "eu.faircode.netguard",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "2.337", versionCode = 2026080101))
    )

val NETMONSTER_COMPATIBILITY = Compatibility(
        name = "NetMonster",
        packageName = "cz.mroczis.netmonster",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.4.1"))
    )

val NETWORKGURU_COMPATIBILITY = Compatibility(
        name = "Network Guru",
        packageName = "com.paget96.netspeedindicator",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.0", versionCode = 158))
    )

val NEWSBREAK_COMPATIBILITY = Compatibility(
        name = "NewsBreak",
        packageName = "com.particlenews.newsbreak",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xE31837,
        targets = listOf(AppTarget(version = "26.34.0", versionCode = 26340031))
    )

val NEWSBREAKLITE_COMPATIBILITY = Compatibility(
        name = "NewsBreak Lite",
        packageName = "com.particlenews.newsbreaklite",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE31837,
        targets = listOf(AppTarget(version = "26.33.0", versionCode = 26330013))
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
        targets = listOf(AppTarget(version = "6.38.0", versionCode = 6427092))
    )

val NZB360_COMPATIBILITY = Compatibility(
        name = "nzb360",
        packageName = "com.kevinforeman.nzb360",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "24.4.1", versionCode = 528))
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
        targets = listOf(AppTarget(version = "1.2.1-rc0", versionCode = 10201000))
    )

val OCTOPILAUNCHER_COMPATIBILITY = Compatibility(
        name = "Octopi Launcher",
        packageName = "com.otp.octopilauncher",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6600,
        targets = listOf(AppTarget(version = "1.88", versionCode = 2161))
    )

val OPERA_NEWS_COMPATIBILITY = Compatibility(
        name = "Opera News",
        packageName = "com.opera.app.news",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF1B2A,
        targets = listOf(AppTarget(version = "14.2.2254.84245", versionCode = 142084245))
    )

val OXYGENUPDATER_COMPATIBILITY = Compatibility(
        name = "Oxygen Updater",
        packageName = "com.arjanvlek.oxygenupdater",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xCC0C00,
        targets = listOf(AppTarget(version = "7.1.0", versionCode = 135))
    )

val PARALLELSPACE_COMPATIBILITY = Compatibility(
        name = "Parallel Space Pro",
        packageName = "com.parallel.space.pro",
        appIconColor = 0x00BCD4,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "4.0.9162", versionCode = 10934))
    )

val PARCELS_COMPATIBILITY = Compatibility(
        name = "Parcels — Package Tracker",
        packageName = "com.brightstripe.parcels",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "3.0.11", versionCode = 288))
    )

val PARK4NIGHT_COMPATIBILITY = Compatibility(
        name = "Park4Night",
        packageName = "fr.tramb.park4night",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.1.62", versionCode = 378))
    )

val PC_REMOTE_COMPATIBILITY = Compatibility(
        name = "PC Remote",
        packageName = "com.monect.portable",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "8.3.5", versionCode = 144))
    )

val PHOTOEDITOR_COMPATIBILITY = Compatibility(
        name = "Photo Editor",
        packageName = "com.iudesk.android.photo.editor",
        appIconColor = 0xFF6B9D,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "13.6", versionCode = 2026081100))
    )

val PIALYTIC_COMPATIBILITY = Compatibility(
        name = "Pialytic",
        packageName = "verbosus.pialytic",
        appIconColor = 0x2196F3,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "1.3.1", versionCode = 22))
    )

val PICSART_COMPATIBILITY = Compatibility(
        name = "PicsArt",
        packageName = "com.picsart.studio",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xE91E63,
        targets = listOf(AppTarget(version = "30.5.5", versionCode = 993830505))
    )

val PICTUREMUSHROOM_COMPATIBILITY = Compatibility(
        name = "Picture Mushroom",
        packageName = "com.glority.picturemushroom",
        appIconColor = 0x7A4A24,
        targets = listOf(AppTarget(version = "2.9.31", versionCode = 90))
    )

val PICTURETHIS_COMPATIBILITY = Compatibility(
        name = "PictureThis",
        packageName = "cn.danatech.xingseus",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.34.0", versionCode = 5091))
    )

val PILLO_COMPATIBILITY = Compatibility(
        name = "pillo",
        packageName = "xyz.rtrvr.pillo",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF69B4.toInt(),
        targets = listOf(
            AppTarget(version = "0.6.18", versionCode = 432)
        )
    )

val PIXEL_HABIT_TRACKER_COMPATIBILITY = Compatibility(
        name = "Pixel Habit Tracker",
        packageName = "com.pixel.al.pixelhabittracker",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "2.3.1", versionCode = 100083))
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
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x6A0DAD,
        targets = listOf(AppTarget(version = "3.1.17", versionCode = 236))
    )

val POCKET_CASTS_COMPATIBILITY = Compatibility(
        name = "Pocket Casts",
        packageName = "au.com.shiftyjelly.pocketcasts",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xF43E37,
        targets = listOf(AppTarget(version = "8.16", versionCode = 9441))
    )

val POCKETPREP_BEHAVIORAL_HEALTH_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Behavioral Health",
        packageName = "com.pocketprep.android.behavioralhealth",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 433))
    )

val POCKETPREP_COMPATIBILITY = Compatibility(
        name = "Pocket Prep IT Cybersecurity",
        packageName = "com.pocketprep.android.itcybersecurity",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 433))
    )

val POCKETPREP_EMS_COMPATIBILITY = Compatibility(
        name = "Pocket Prep EMS",
        packageName = "com.pocketprep.android.ems",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 433))
    )

val POCKETPREP_ESSENTIALS_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Essentials",
        packageName = "com.pocketprep.android.essentials",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.1", versionCode = 429))
    )

val POCKETPREP_FITNESS_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Fitness",
        packageName = "com.pocketprep.android.fitness",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 433))
    )

val POCKETPREP_MAIN_COMPATIBILITY = Compatibility(
        name = "Pocket Prep",
        packageName = "com.pocketprep.android.pocketprep",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 433))
    )

val POCKETPREP_MEDICAL_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Medical",
        packageName = "com.pocketprep.android.medical",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 433))
    )

val POCKETPREP_NURSING_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Nursing",
        packageName = "com.pocketprep.android.nursing",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 433))
    )

val POCKETPREP_NURSING_SCHOOL_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Nursing School",
        packageName = "com.pocketprep.android.nursingschool",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.28.1", versionCode = 429))
    )

val POCKETPREP_PROFESSIONAL_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Professional",
        packageName = "com.pocketprep.android.professional",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 433))
    )

val POCKETPREP_SKILLED_TRADES_COMPATIBILITY = Compatibility(
        name = "Pocket Prep Skilled Trades",
        packageName = "com.pocketprep.android.automotive",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.29.0", versionCode = 433))
    )

val PODSLINK_COMPATIBILITY = Compatibility(
        name = "PodsLink",
        packageName = "net.podslink",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "1.3.9", versionCode = 86))
    )

val POLICESCANNER_COMPATIBILITY = Compatibility(
        name = "Police Scanner",
        packageName = "police.scanner.radio.broadcastify.citizen",
        appIconColor = 0x0D47A1,
        targets = listOf(AppTarget(version = "1.29.0-260420093"))
    )

val PORTDROID_COMPATIBILITY = Compatibility(
        name = "PortDroid",
        packageName = "com.stealthcopter.portdroid",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x1565C0,
        targets = listOf(
            AppTarget(version = "1.0.7", versionCode = 113)
        )
    )

val PROTON_PASS_COMPATIBILITY = Compatibility(
        name = "Proton Pass",
        packageName = "proton.android.pass",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x6D4AFF,
        targets = listOf(AppTarget(version = "1.40.3", versionCode = 14003373))
    )

val PROTONMAIL_COMPATIBILITY = Compatibility(
        name = "Proton Mail",
        packageName = "ch.protonmail.android",
        appIconColor = 0x6D4AFF,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "7.11.4", versionCode = 18315))
    )

val PROTONVPN_COMPATIBILITY = Compatibility(
        name = "Proton VPN",
        packageName = "ch.protonvpn.android",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x6D4AFF,
        targets = listOf(AppTarget(version = "5.19.78.0", versionCode = 605197800))
    )

val PROXYMAN_COMPATIBILITY = Compatibility(
        name = "Proxyman",
        packageName = "com.proxyman.proxymanandroid",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "1.21.0", versionCode = 48))
    )

val PSIPHON_COMPATIBILITY = Compatibility(
        name = "Psiphon Pro",
        packageName = "com.psiphon3.subscription",
        appIconColor = 0x6A1B9A,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "479", versionCode = 479))
    )

val QBITCONNECT_COMPATIBILITY = Compatibility(
        name = "qBitConnect",
        packageName = "com.bluematter.qbitconnect",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4FC3F7,
        targets = listOf(AppTarget(version = "2.0.6", versionCode = 66))
    )

val RAINBOW_WEATHER_COMPATIBILITY = Compatibility(
        name = "Rainbow Weather",
        packageName = "com.rainbowmeteo.weather.rainbow.ai",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x5C6BC0,
        targets = listOf(
            AppTarget(version = "3.24.0", versionCode = 261)
        )
    )

val RAR_COMPATIBILITY = Compatibility(
        name = "RAR",
        packageName = "com.rarlab.rar",
        appIconColor = 0x4CAF50,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "7.23.build134", versionCode = 134))
    )

val RECIPEBRO_COMPATIBILITY = Compatibility(
        name = "RecipeBro",
        packageName = "com.recipebro.cookingbuddy",
        appIconColor = 0xE65100,
        apkFileType = ApkFileType.APKS,
        targets = listOf(AppTarget(version = "1.7.21", versionCode = 1502))
    )

val REDDIT_COMPATIBILITY = Compatibility(
        name = "Reddit",
        packageName = "com.reddit.frontpage",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF4500,
        targets = listOf(
            AppTarget(version = "2026.34.0", versionCode = 2634001))
    )

val RELANE_COMPATIBILITY = Compatibility(
        name = "Relane VPN",
        packageName = "xorsand.relane",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "2026073010.0.0", versionCode = 2026073010))
    )

val RELINK_COMPATIBILITY = Compatibility(
        name = "re-Link",
        packageName = "com.curiouscompany.relink",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6F00,
        targets = listOf(AppTarget(version = "2.0.13", versionCode = 34))
    )

val RENAMEORGANIZE_COMPATIBILITY = Compatibility(
        name = "Rename & Organize",
        packageName = "eu.duong.picturemanager",
        appIconColor = 0x1565C0,
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "8.30.0", versionCode = 8300000))
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
        targets = listOf(AppTarget(version = "2.3.3", versionCode = 45))
    )

val SCOOPZ_COMPATIBILITY = Compatibility(
        name = "Scoopz",
        packageName = "com.localaiapp.scoops",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APKM,
        targets = listOf(AppTarget(version = "3.34.0", versionCode = 3340002))
    )

val SCRL_COMPATIBILITY = Compatibility(
        name = "SCRL",
        packageName = "com.appostrophe.scrl",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xEFC67A,
        targets = listOf(AppTarget(version = "1.24.1", versionCode = 263))
    )

val SD_MAID_SE_COMPATIBILITY = Compatibility(
        name = "SD Maid SE",
        packageName = "eu.darken.sdmse",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.0.3-rc0", versionCode = 20003000))
    )

val SEND_FILES_TO_TV_COMPATIBILITY = Compatibility(
        name = "Send Files To TV",
        packageName = "com.yablio.sendfilestotv",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.4.22", versionCode = 69))
    )

val SERVER_AUDITOR_COMPATIBILITY = Compatibility(
        name = "Server Auditor",
        packageName = "com.server.auditor.ssh.client",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "7.8.1", versionCode = 934))
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
        targets = listOf(AppTarget(version = "2026.15", versionCode = 2559))
    )

val SNIPD_COMPATIBILITY = Compatibility(
        name = "Snipd",
        packageName = "ai.topicfinder.podcastdiscovery",
        appIconColor = 0x1CC29F,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "4.1.19", versionCode = 4119))
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

val SOCIALLITE_COMPATIBILITY = Compatibility(
        name = "SocialLite",
        packageName = "com.sociallite.android",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x5B5EA6,
        targets = listOf(AppTarget(version = "2.0.0.45", versionCode = 73))
    )

val SPEEDTEST_COMPATIBILITY = Compatibility(
        name = "Speedtest",
        packageName = "org.zwanoo.android.speedtest",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xD81F26,
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
        targets = listOf(AppTarget(version = "26.7.3", versionCode = 950))
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
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x00ADEF,
        targets = listOf(AppTarget(version = "3.37.0", versionCode = 1033700))
    )

val STRAVA_COMPATIBILITY = Compatibility(
        name = "Strava",
        packageName = "com.strava",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFC4C02,
        targets = listOf(AppTarget(version = "477.14", versionCode = 12399244))
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

val TEAMS_COMPATIBILITY = Compatibility(
        name = "Teams",
        packageName = "com.microsoft.teams",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x6264A7,
        targets = listOf(AppTarget(version = "1416/1.0.0.2026152302", versionCode = 2026152325))
    )

val TELEGRAM_COMPATIBILITY = Compatibility(
        name = "Telegram",
        packageName = "org.telegram.messenger",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x2CA5E0,
        targets = listOf(AppTarget(version = "12.10.0", versionCode = 70242))
    )

val TELEGRAM_PLUS_COMPATIBILITY = Compatibility(
        name = "Telegram Plus",
        packageName = "org.telegram.plus",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2CA5E0,
        targets = listOf(AppTarget(version = "12.9.2.0", versionCode = 22448))
    )

val TELEGRAM_WEB_COMPATIBILITY = Compatibility(
        name = "Telegram Web",
        packageName = "org.telegram.messenger.web",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2CA5E0,
        targets = listOf(AppTarget(version = "12.9.2", versionCode = 69919))
    )

val THE_ATHLETIC_COMPATIBILITY = Compatibility(
        name = "The Athletic",
        packageName = "com.theathletic",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1A1A1A,
        targets = listOf(AppTarget(version = "13.147.0", versionCode = 33625841))
    )

val THETRANSIT_COMPATIBILITY = Compatibility(
        name = "Transit",
        packageName = "com.thetransitapp.droid",
        appIconColor = 0x00B2A9,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "6.2.3", versionCode = 5126772))
    )

val THEWEATHERCHANNEL_COMPATIBILITY = Compatibility(
        name = "The Weather Channel",
        packageName = "com.weather.Weather",
        appIconColor = 0x1B6AC9,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "16.18.1", versionCode = 1080014064))
    )

val TIKTOK_LITE_COMPATIBILITY = Compatibility(
        name = "TikTok Lite",
        packageName = "com.tiktok.lite.go",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFE2C55,
        targets = listOf(
            AppTarget(version = "44.9.52", versionCode = 440952)
        )
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
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xDF1B12,
        targets = listOf(AppTarget(version = "3.6.320", versionCode = 1678697))
    )

val TOOMICS_COMPATIBILITY = Compatibility(
        name = "Toomics",
        packageName = "com.toomics.global.google",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "1.6.8", versionCode = 107))
    )

val TOPWALLPAPERS_COMPATIBILITY = Compatibility(
        name = "HD UHD Live Wallpapers",
        packageName = "hd.uhd.live.wallpapers.topwallpapers",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "6.0.1", versionCode = 114))
    )

val TORRDROID_COMPATIBILITY = Compatibility(
        name = "TorrDroid",
        packageName = "intelligems.torrdroid",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "2.0.3", versionCode = 10272))
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
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "1.20.1", versionCode = 110))
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
        targets = listOf(
            AppTarget(version = "1.20.80.0.1002385", versionCode = 1002385),
        )
    )

val TRANZMATE_COMPATIBILITY = Compatibility(
        name = "Moovit",
        packageName = "com.tranzmate",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0066FF,
        targets = listOf(AppTarget(version = "5.197.1.1801", versionCode = 1801))
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
        targets = listOf(AppTarget(version = "3.4.1", versionCode = 3040100))
    )

val UBIKITOUCH_COMPATIBILITY = Compatibility(
        name = "UbikiTouch",
        packageName = "eu.toneiv.ubktouch",
        appIconColor = 0x0D47A1,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "1.17.8", versionCode = 78135))
    )

val UDISC_COMPATIBILITY = Compatibility(
        name = "UDisc",
        packageName = "com.regasoftware.udisc",
        appIconColor = 0xF47C20,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "24.2.10", versionCode = 20517))
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
        targets = listOf(AppTarget(version = "7.37", versionCode = 737))
    )

val USBHOTSPOT_COMPATIBILITY = Compatibility(
        name = "USB Hotspot",
        packageName = "kha.prog.usbhotspot",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0xFF6900,
        targets = listOf(AppTarget(version = "1.6", versionCode = 6))
    )

val VIZMANGA_COMPATIBILITY = Compatibility(
        name = "VIZ Manga",
        packageName = "com.vizmanga.android",
        appIconColor = 0xE53935,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "4.14.3", versionCode = 241))
    )

val VRADIO_COMPATIBILITY = Compatibility(
        name = "VRadio",
        packageName = "com.ilv.vradio",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.9.3", versionCode = 90209003))
    )

val VYXEL_COMPATIBILITY = Compatibility(
        name = "Vyxel Apps",
        packageName = "com.vythera.vyxelapps",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x00BFFF,
        targets = listOf(
            AppTarget(version = "1.0.7", versionCode = 7)
        )
    )

val WALLVERSE_COMPATIBILITY = Compatibility(
        name = "Wallverse",
        packageName = "com.wallverse.wallpapers",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x4F46E5,
        targets = listOf(AppTarget(version = "4.2", versionCode = 42))
    )

val WARP_COMPATIBILITY = Compatibility(
        name = "1.1.1.1",
        packageName = "com.cloudflare.onedotonedotonedotone",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xF48120,
        targets = listOf(AppTarget(version = "6.38.9", versionCode = 5641))
    )

val WAVVE_BOATING_COMPATIBILITY = Compatibility(
        name = "Wavve Boating",
        packageName = "com.wavve.boating.gps",
        appIconColor = 0x0077CC,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.7.8", versionCode = 3189))
    )

val WEAWOW_COMPATIBILITY = Compatibility(
        name = "Weawow",
        packageName = "com.weawow",
        appIconColor = 0x2196F3,
        apkFileType = ApkFileType.APK,
        targets = listOf(AppTarget(version = "7.1.8", versionCode = 718))
    )

val WIDGET2026_COMPATIBILITY = Compatibility(
        name = "Widget 2026",
        packageName = "com.remi.widget",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "85.1.4", versionCode = 116))
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
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0FA0EB,
        targets = listOf(AppTarget(version = "51.0.1", versionCode = 5100010))
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
        targets = listOf(AppTarget(version = "16.0.20326.20034", versionCode = 2005292347))
    )

val YATRI_COMPATIBILITY = Compatibility(
        name = "Yatri",
        packageName = "com.yatrirailways.yatri",
        appIconColor = 0xFF6B00,
        apkFileType = ApkFileType.XAPK,
        targets = listOf(AppTarget(version = "5.0.6", versionCode = 1013))
    )

val COMPATIBILITY_ACCPRO = Compatibility(
        name = "1Tap Cleaner",
        packageName = "com.a0soft.gphone.acc.free",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x2196F3,
        targets = listOf(
            AppTarget(version = "5.22", versionCode = 240005229)
        )
    )

val COMPATIBILITY_MYPERM = Compatibility(
        name = "Permission Pilot",
        packageName = "eu.darken.myperm",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x4CAF50,
        targets = listOf(
            AppTarget(version = "2.2.2-rc0", versionCode = 20202000)
        )
    )

val LEAP_FITNESS_ALL = arrayOf(
    LEAP_LOSEWEIGHT_WOMEN_COMPATIBILITY,
    LEAP_HEIGHTINCREASE_COMPATIBILITY,
    LEAP_HOMEWORKOUT_COMPATIBILITY,
    LEAP_FITNESS_WOMEN_COMPATIBILITY,

)
}
