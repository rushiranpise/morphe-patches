package app.template.patches.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Call Recorder — Automatic by Catalina Group
    val CALLRECORDER_COMPATIBILITY = Compatibility(
        name = "Cube ACR",
        packageName = "com.catalinagroup.callrecorder",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "2.4.281"))
    )

    // Universal TV Remote Control by SensusTech
    val UNIVERSALTV_COMPATIBILITY = Compatibility(
        name = "Unimote",
        packageName = "sensustech.universal.tv.remote.control",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.8.1"))
    )

    // Citizen — Safety Alert by sp0n
    val CITIZEN_COMPATIBILITY = Compatibility(
        name = "Citizen",
        packageName = "sp0n.citizen",
        appIconColor = 0x0066FF,
        targets = listOf(AppTarget(version = "0.1298.0"))
    )


    // Case Tracker — Immigration App by Saldous
    val CASETRACKER_COMPATIBILITY = Compatibility(
        name = "Case Tracker",
        packageName = "com.saldous.casetracker",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.5.1"))
    )

    // Cloudflare WARP — 1.1.1.1
    val WARP_COMPATIBILITY = Compatibility(
        name = "1.1.1.1",
        packageName = "com.cloudflare.onedotonedotonedotone",
        appIconColor = 0xF48120,
        targets = listOf(AppTarget(version = "6.38.7", versionCode = 5311))
    )

    // Crime Radar — Local Police & Safety by Newsbreak
    val CRIMERADAR_COMPATIBILITY = Compatibility(
        name = "Crime Radar",
        packageName = "com.newsbreak.crimeradar",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "26.23.2"))
    )

    // Splitwise — Finance, bills and expenses
    val SPLITWISE_COMPATIBILITY = Compatibility(
        name = "Splitwise",
        packageName = "com.Splitwise.SplitwiseMobile",
        appIconColor = 0x1CC29F,
        targets = listOf(AppTarget(version = "26.5.5"))
    )

    // Greenify — App Hibernation & Battery Saver
    val GREENIFY_COMPATIBILITY = Compatibility(
        name = "Greenify",
        packageName = "com.oasisfeng.greenify",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "5.1.1"))
    )

    // Pialytic — LaTeX Editor (VerbTeX)
    val PIALYTIC_COMPATIBILITY = Compatibility(
        name = "Pialytic",
        packageName = "verbosus.pialytic",
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "1.2.8"))
    )

    // Snipd — AI Podcast Player
    val SNIPD_COMPATIBILITY = Compatibility(
        name = "Snipd",
        packageName = "ai.topicfinder.podcastdiscovery",
        appIconColor = 0x1CC29F,
        targets = listOf(AppTarget(version = "4.1.14"))
    )

    // TWT App — Astronomy & Sky Guide
    val TWTAPP_COMPATIBILITY = Compatibility(
        name = "Stargazing Hub",
        packageName = "com.twtapp",
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "3.2.1"))
    )

    // Photo Editor — iUdesk Photo Editor
    val PHOTOEDITOR_COMPATIBILITY = Compatibility(
        name = "Photo Editor",
        packageName = "com.iudesk.android.photo.editor",
        appIconColor = 0xFF6B9D,
        targets = listOf(AppTarget(version = "13.3"))
    )

    // ML Manager — APK Downloader & Backup
    val MLMANAGER_COMPATIBILITY = Compatibility(
        name = "ML Manager",
        packageName = "com.javiersantos.mlmanager",
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "5.0"))
    )

    // Beta by Mirko — App updates tracker
    val MIRKO_COMPATIBILITY = Compatibility(
        name = "Beta Maniac",
        packageName = "it.mirko.beta",
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "0.9.4"))
    )

    // Hibernator — Apps & Background Process
    val HIBERNATOR_COMPATIBILITY = Compatibility(
        name = "Hibernator",
        packageName = "com.tafayor.hibernator",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.56.10"))
    )

    // KillApps — Background Apps Killer
    val KILLAPPS_COMPATIBILITY = Compatibility(
        name = "KillApps",
        packageName = "com.tafayor.killall",
        appIconColor = 0xF44336,
        targets = listOf(AppTarget(version = "1.57.9"))
    )

    // RAR
    val RAR_COMPATIBILITY = Compatibility(
        name = "RAR",
        packageName = "com.rarlab.rar",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.20.build131"))
    )

    // m-Indicator — Mumbai Local Train
    val MINDICATOR_COMPATIBILITY = Compatibility(
        name = "m-Indicator",
        packageName = "com.mobond.mindicator",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "18.0.352"))
    )

    // Yatri — Train, Metro & Bus Ticketing
    val YATRI_COMPATIBILITY = Compatibility(
        name = "Yatri",
        packageName = "com.yatrirailways.yatri",
        appIconColor = 0xFF6B00,
        targets = listOf(AppTarget(version = "5.0.3"))
    )

    // Proxyman — Network Debugger
    val PROXYMAN_COMPATIBILITY = Compatibility(
        name = "Proxyman",
        packageName = "com.proxyman.proxymanandroid",
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "1.16.0"))
    )

    // SHAREit Premium
    val SHAREIT_COMPATIBILITY = Compatibility(
        name = "SHAREit Premium",
        packageName = "shareit.premium",
        appIconColor = 0xFF4B00,
        targets = listOf(AppTarget(version = "1.1.98"))
    )

    // NetMonster — Network Cell Info, Signal
    val NETMONSTER_COMPATIBILITY = Compatibility(
        name = "NetMonster",
        packageName = "cz.mroczis.netmonster",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.4.1"))
    )

    // Dubox Drive — Cloud Storage
    val DUBOXDRIVE_COMPATIBILITY = Compatibility(
        name = "TeraBox",
        packageName = "com.dubox.drive",
        appIconColor = 0x2EAAFF,
        targets = listOf(AppTarget(version = "4.20.1"))
    )

    // SAI — Split APKs Installer by MTV
    val SAI_COMPATIBILITY = Compatibility(
        name = "SAI",
        packageName = "com.mtv.sai",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.2.8"))
    )

    // BlockerHero — App Blocker & Focus Timer
    val BLOCKERHERO_COMPATIBILITY = Compatibility(
        name = "BlockerHero",
        packageName = "com.blockerhero",
        appIconColor = 0xFF5252,
        targets = listOf(AppTarget(version = "1.5.0"))
    )

    // nzb360 — Usenet & Torrent Manager
    val NZB360_COMPATIBILITY = Compatibility(
        name = "nzb360",
        packageName = "com.kevinforeman.nzb360",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "23.5"))
    )

    // Canva
    val CANVA_COMPATIBILITY = Compatibility(
        name = "Canva",
        packageName = "com.canva.editor",
        appIconColor = 0x8B3DFF,
        targets = listOf(AppTarget(version = "2.365.o"))
    )

    // Clickmate
    val INSCODE_AUTOCLICKER_COMPATIBILITY = Compatibility(
        name = "Clickmate",
        packageName = "com.inscode.autoclicker",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "7.1.4"))
    )

    // NetGuard — No-root Firewall by Marcel Bokhorst
    val NETGUARD_COMPATIBILITY = Compatibility(
        name = "NetGuard",
        packageName = "eu.faircode.netguard",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.335"))
    )

    // AdGuard — Ad Blocker & Privacy
    val ADGUARD_COMPATIBILITY = Compatibility(
        name = "AdGuard Nightly",
        packageName = "com.adguard.android",
        appIconColor = 0x67B346,
        targets = listOf(AppTarget(version = "4.14.68"))
    )

    // AIDA64 — System & Hardware Info by FinalWire
    val AIDA64_COMPATIBILITY = Compatibility(
        name = "AIDA64",
        packageName = "com.finalwire.aida64",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "2.21", versionCode = 221))
    )

    // CPU-Z by CPUID
    val CPUZ_COMPATIBILITY = Compatibility(
        name = "CPU-Z",
        packageName = "com.cpuid.cpu_z",
        appIconColor = 0x2A3B4C,
        targets = listOf(AppTarget(version = "1.59", versionCode = 59))
    )

    // HTTP Mock Tool — API Mock & Network Capture
    val HTTPMOCK_COMPATIBILITY = Compatibility(
        name = "HTTP Sniffer",
        packageName = "com.anetcapture.mock",
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "2.11.0-ad_mob"))
    )

    // MyPerm — App Permission Manager by darken
    val MYPERM_COMPATIBILITY = Compatibility(
        name = "Permission Pilot",
        packageName = "eu.darken.myperm",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "2.1.1-rc0"))
    )

    // Proton VPN
    val PROTONVPN_COMPATIBILITY = Compatibility(
        name = "Proton VPN",
        packageName = "ch.protonvpn.android",
        appIconColor = 0x6D4AFF,
        targets = listOf(AppTarget(version = "5.18.84.0"))
    )

    // NetShare — Mobile Hotspot / Subnet Router
    val NETSHARE_COMPATIBILITY = Compatibility(
        name = "NetShare",
        packageName = "kha.prog.mikrotik",
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "UI/link-274"))
    )

    // Shexa — Permission Manager by Shexa
    val SHEXA_COMPATIBILITY = Compatibility(
        name = "App Permission Manager",
        packageName = "com.shexa.permissionmanager",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.4.6.2"))
    )

    // Hola VPN Proxy Plus
    val HOLAVPN_COMPATIBILITY = Compatibility(
        name = "Hola VPN Proxy Plus",
        packageName = "org.hola.play",
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "AARCH64_1.248.400"))
    )

    // Psiphon Pro — Censorship Circumvention VPN
    val PSIPHON_COMPATIBILITY = Compatibility(
        name = "Psiphon Pro",
        packageName = "com.psiphon3.subscription",
        appIconColor = 0x6A1B9A,
        targets = listOf(AppTarget(version = "476"))
    )

    // Speedtest by Ookla
    val SPEEDTEST_COMPATIBILITY = Compatibility(
        name = "Speedtest",
        packageName = "org.zwanoo.android.speedtest",
        appIconColor = 0x141C4C,
        targets = listOf(AppTarget(version = "7.0.4"))
    )

    // Social Gamebox
    val SOCIALGAMEBOX_COMPATIBILITY = Compatibility(
        name = "Social Gamebox",
        packageName = "com.app.social_gamebox",
        appIconColor = 0x7C4DFF,
        targets = listOf(AppTarget(version = "1.1.3"))
    )

    // Flightradar24
    val FLIGHTRADAR_COMPATIBILITY = Compatibility(
        name = "Flightradar24",
        packageName = "com.flightradar24free",
        appIconColor = 0x327CB5,
        targets = listOf(AppTarget(version = "11.6.1"))
    )

    // Cashew — Budget & Finance Tracker by James Kokoska
    val CASHEW_COMPATIBILITY = Compatibility(
        name = "Cashew",
        packageName = "com.budget.tracker_app",
        appIconColor = 0xFFB300,
        targets = listOf(AppTarget(version = "6.5.9"))
    )

    // Rocket Money
    val ROCKETMONEY_COMPATIBILITY = Compatibility(
        name = "Rocket Money",
        packageName = "com.truebill",
        appIconColor = 0xDE3341,
        targets = listOf(AppTarget(version = "13.15.0"))
    )

    // Waze — GPS, Maps & Traffic
    val WAZE_COMPATIBILITY = Compatibility(
        name = "Waze",
        packageName = "com.waze",
        appIconColor = 0x33CCFF,
        targets = listOf(AppTarget(version = "5.19.0.2"))
    )

    // AccuWeather — Weather Radar & Forecast
    val ACCUWEATHER_COMPATIBILITY = Compatibility(
        name = "AccuWeather",
        packageName = "com.accuweather.android",
        appIconColor = 0xF25C1B,
        targets = listOf(AppTarget(version = "21.1.11-1-rc"))
    )

    // Windscribe VPN
    val WINDSCRIBE_COMPATIBILITY = Compatibility(
        name = "Windscribe VPN",
        packageName = "com.windscribe.vpn",
        appIconColor = 0x00AEEF,
        targets = listOf(AppTarget(version = "4.1.2274"))
    )

    // MovieBox TV by Transsion/Mbox
    val MOVIEBOXTV_COMPATIBILITY = Compatibility(
        name = "MovieBox TV",
        packageName = "com.community.oneroom",
        appIconColor = 0xE53935,
        targets = listOf(
            AppTarget(version = "3.0.15.0616.03", versionCode = 50020104))
    )

    // The Weather Channel — Weather Forecast & Alerts by The Weather Channel
    val THEWEATHERCHANNEL_COMPATIBILITY = Compatibility(
        name = "The Weather Channel",
        packageName = "com.weather.Weather",
        appIconColor = 0x1B6AC9,
        targets = listOf(AppTarget(version = "16.10.1", versionCode = 1080012982))
    )

    // Ninja VPN — Fast & Secure VPN Proxy
    val NINJVAPN_COMPATIBILITY = Compatibility(
        name = "Ninja VPN",
        packageName = "app.ninjavpn.android",
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "1.4.6", versionCode = 43))
    )

    // 1Tap Cleaner — App cache & history cleaner by a0soft
    val ONETAPCLEANER_COMPATIBILITY = Compatibility(
        name = "1Tap Cleaner",
        packageName = "com.a0soft.gphone.acc.free",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.18", versionCode = 240005189))
    )

    // Strength Training by Muscle Motion
    val STRENGTHTRAINING_COMPATIBILITY = Compatibility(
        name = "Strength Training",
        packageName = "air.com.musclemotion.strength.mobile",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "3.5.0", versionCode = 921))
    )

    // Windy — Weather Radar & Forecast by Windyty
    val WINDY_COMPATIBILITY = Compatibility(
        name = "Windy",
        packageName = "com.windyty.android",
        appIconColor = 0x0FA0EB,
        targets = listOf(AppTarget(version = "50.1.1"))
    )

    // TomTom GO Navigation
    val TOMTOMGO_COMPATIBILITY = Compatibility(
        name = "TomTom GO",
        packageName = "com.tomtom.gplay.navapp",
        appIconColor = 0xDF1B12,
        targets = listOf(AppTarget(version = "3.6.316-beta", versionCode = 1678657))
    )

    // Posture Correction by Muscle Motion
    val POSTURE_COMPATIBILITY = Compatibility(
        name = "Posture",
        packageName = "air.com.musclemotion.posture",
        appIconColor = 0x00897B,
        targets = listOf(AppTarget(version = "3.2.0", versionCode = 10040835))
    )

    // Kyphosis by Muscle Motion (legacy Adobe AIR architecture)
    val KYPHOSIS_COMPATIBILITY = Compatibility(
        name = "Kyphosis",
        packageName = "air.com.musclemotion.kyphosis",
        appIconColor = 0xE65100,
        targets = listOf(AppTarget(version = "1.4.9", versionCode = 156))
    )

    // Workout by Muscle Motion (legacy Adobe AIR architecture)
    val WORKOUT_COMPATIBILITY = Compatibility(
        name = "Workout",
        packageName = "air.com.musclemotion.workout",
        appIconColor = 0xC62828,
        targets = listOf(AppTarget(version = "1.2.0", versionCode = 153))
    )

    // Yoga by Muscle Motion
    val YOGA_COMPATIBILITY = Compatibility(
        name = "Yoga",
        packageName = "air.com.musclemotion.yoga",
        appIconColor = 0x7B1FA2,
        targets = listOf(AppTarget(version = "3.2.0", versionCode = 1004178))
    )

    // Anatomy by Muscle Motion
    val ANATOMY_COMPATIBILITY = Compatibility(
        name = "Anatomy",
        packageName = "air.com.musclemotion.anatomy",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.2.0", versionCode = 1004183))
    )

    // Citymapper
    val CITYMAPPER_COMPATIBILITY = Compatibility(
        name = "Citymapper",
        packageName = "com.citymapper.app.release",
        appIconColor = 0x00A862,
        targets = listOf(AppTarget(version = "11.55.1", versionCode = 1155080))
    )

    // Tranzmate — Public Transit by Moovit
    val TRANZMATE_COMPATIBILITY = Compatibility(
        name = "Moovit",
        packageName = "com.tranzmate",
        appIconColor = 0x0066FF,
        targets = listOf(AppTarget(version = "5.195.0.1789", versionCode = 1789))
    )

    // Transit — Bus, Train, Tracker
    val THETRANSIT_COMPATIBILITY = Compatibility(
        name = "Transit",
        packageName = "com.thetransitapp.droid",
        appIconColor = 0x00B2A9,
        targets = listOf(AppTarget(version = "6.1.12", versionCode = 5125980))
    )

    // UDisc Disc Golf
    val UDISC_COMPATIBILITY = Compatibility(
        name = "UDisc",
        packageName = "com.regasoftware.udisc",
        appIconColor = 0xF47C20,
        targets = listOf(AppTarget(version = "24.2.1", versionCode = 9928))
    )

    // Snow-Forecast.com
    val SNOWFORECAST_COMPATIBILITY = Compatibility(
        name = "Snow-Forecast.com",
        packageName = "com.snow_forecast.snowforecast",
        appIconColor = 0xCA0013,
        targets = listOf(AppTarget(version = "8.0.6", versionCode = 2139))
    )

    // SkinSort
    val SKINSORT_COMPATIBILITY = Compatibility(
        name = "SkinSort",
        packageName = "com.skinsort",
        appIconColor = 0x2F7D62,
        targets = listOf(AppTarget(version = "1.15", versionCode = 25))
    )

    // BlurWall — Blur Wallpaper by Automan
    val BLURWALL_COMPATIBILITY = Compatibility(
        name = "BlurWall",
        packageName = "apps.automan.blurwallpaper",
        appIconColor = 0x42A5F5,
        targets = listOf(AppTarget(version = "2.9.2", versionCode = 28))
    )

    // Carbon
    val CARBON_COMPATIBILITY = Compatibility(
        name = "Carbon",
        packageName = "com.joincarbon.nutrition",
        appIconColor = 0x111111,
        targets = listOf(AppTarget(version = "2.76.5784", versionCode = 1633629070))
    )

    // Scoopz — Local News & Community (com.localaiapp.scoops)
    val SCOOPZ_COMPATIBILITY = Compatibility(
        name = "Scoopz",
        packageName = "com.localaiapp.scoops",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "3.2.7.0"))
    )

    // NewsBreak
    val NEWSBREAK_COMPATIBILITY = Compatibility(
        name = "NewsBreak",
        packageName = "com.particlenews.newsbreak",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "26.27.0", versionCode = 26270016))
    )

    // Inmigreat — Immigration Guide & Case Tracker (com.changayaf.inmigreat)
    val INMIGREAT_COMPATIBILITY = Compatibility(
        name = "Inmigreat",
        packageName = "com.changayaf.inmigreat",
        appIconColor = 0x6344CC,
        targets = listOf(AppTarget(version = "2.2.83", versionCode = 679))
    )

    // Lawfully (com.lawfully.lawfully_ai_tracker)
    val LAWFULLY_COMPATIBILITY = Compatibility(
        name = "Lawfully",
        packageName = "com.lawfully.lawfully_ai_tracker",
        appIconColor = 0x0D47A1,
        targets = listOf(AppTarget(version = "6.6.2", versionCode = 532))
    )

    // MigraConnect — USCIS & EOIR Case Tracker (com.tecso.MigraConnect)
    val MIGRACONNECT_COMPATIBILITY = Compatibility(
        name = "MigraConnect",
        packageName = "com.tecso.MigraConnect",
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = "2.8.1", versionCode = 118))
    )

    // Police Scanner — Broadcastify (police.scanner.radio.broadcastify.citizen)
    val POLICESCANNER_COMPATIBILITY = Compatibility(
        name = "Police Scanner",
        packageName = "police.scanner.radio.broadcastify.citizen",
        appIconColor = 0x0D47A1,
        targets = listOf(AppTarget(version = null))
    )

    // CamScanner
    val CAMSCANNER_COMPATIBILITY = Compatibility(
        name = "CamScanner",
        packageName = "com.intsig.camscanner",
        appIconColor = 0x19BCAA,
        targets = listOf(AppTarget(version = "7.20.0.2606230000", versionCode = 72002))
    )

    // Image & Video Date Fixer by JD Apps
    val IMAGEDATEFIXER_COMPATIBILITY = Compatibility(
        name = "Image & Video Date Fixer",
        packageName = "eu.duong.imagedatefixer",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.7.1", versionCode = 30700100))
    )

    // Rename & Organize by JD Apps
    val RENAMEORGANIZE_COMPATIBILITY = Compatibility(
        name = "Rename & Organize",
        packageName = "eu.duong.picturemanager",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "8.25.1", versionCode = 8250101))
    )

    // Inure App Manager by Hamza Rizwan
    val INURE_COMPATIBILITY = Compatibility(
        name = "Inure App Manager",
        packageName = "app.simple.inure.play",
        appIconColor = 0x6200EE,
        targets = listOf(AppTarget(version = "build107.0.5", versionCode = 10705))
    )

    // Weawow: Weather & Widget
    val WEAWOW_COMPATIBILITY = Compatibility(
        name = "Weawow: Weather & Widget",
        packageName = "com.weawow",
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "7.1.7", versionCode = 717))
    )

    // SPIN Safe Browser
    val SPIN_COMPATIBILITY = Compatibility(
        name = "SPIN",
        packageName = "com.nationaledtech.spinbrowser",
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = "70.3.0", versionCode = 2025061004))
    )

    // Tracked
    val TRACKED_COMPATIBILITY = Compatibility(
        name = "Tracked",
        packageName = "com.tracked.mobile",
        appIconColor = 0x020617,
        targets = listOf(AppTarget(version = "7.0.0", versionCode = 125))
    )

    // Toxly
    val TOXLY_COMPATIBILITY = Compatibility(
        name = "Toxly",
        packageName = "com.mindful.code.studio.toxly.scanner",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "1.15.3", versionCode = 54))
    )
    
    // All Reader — PDF & Document Viewer
    val ALLREADER_COMPATIBILITY = Compatibility(
        name = "All Reader",
        packageName = "alldocumentreader.office.viewer.filereader.pdfviewer",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.2.3", versionCode = 52))
    )

    // Genius Scan — PDF Scanner by The Grizzly Labs
    val GENIUSSCAN_COMPATIBILITY = Compatibility(
        name = "Genius Scan",
        packageName = "com.thegrizzlylabs.geniusscan.free",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "7.40.0", versionCode = 7512))
    )

    // KineStop — Motion Stop & Screen Off by Urbandroid
    val KINESTOP_COMPATIBILITY = Compatibility(
        name = "KineStop",
        packageName = "com.urbandroid.kinestop",
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "5.1", versionCode = 101))
    )

    // pkge — Parcel & Package Tracker
    val PKGE_COMPATIBILITY = Compatibility(
        name = "pkge",
        packageName = "net.pkge.pkge",
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = "19.0.13", versionCode = 458))
    )

    // SpotAngels — Parking & Maps
    val SPOTANGELS_COMPATIBILITY = Compatibility(
        name = "SpotAngels",
        packageName = "com.spotangels.android",
        appIconColor = 0x1C9BE6,
        targets = listOf(AppTarget(version = "15.2.2", versionCode = 10323))
    )

    // TurboScan — Document & PDF Scanner by Piksoft Inc.
    val TURBOSCAN_COMPATIBILITY = Compatibility(
        name = "TurboScan",
        packageName = "com.piksoft.turboscan.free",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.7.3", versionCode = 123))
    )

    // Wavve Boating GPS — Marine Navigation & Charts
    val WAVVE_BOATING_COMPATIBILITY = Compatibility(
        name = "Wavve Boating",
        packageName = "com.wavve.boating.gps",
        appIconColor = 0x0077CC,
        targets = listOf(AppTarget(version = "5.6.9", versionCode = 3178))
    )
}
