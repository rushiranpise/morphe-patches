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
        targets = listOf(AppTarget(version = "5.5.2", versionCode = 1036))
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
        targets = listOf(AppTarget(version = "26.6.3", versionCode = 945))
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
        targets = listOf(AppTarget(version = "3.3.1", versionCode = 3030100))
    )

    // Photo Editor — iUdesk Photo Editor
    val PHOTOEDITOR_COMPATIBILITY = Compatibility(
        name = "Photo Editor",
        packageName = "com.iudesk.android.photo.editor",
        appIconColor = 0xFF6B9D,
        targets = listOf(AppTarget(version = "13.4", versionCode = 2026070800))
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
        targets = listOf(AppTarget(version = "7.23.build133", versionCode = 133))
    )

    // m-Indicator — Mumbai Local Train
    val MINDICATOR_COMPATIBILITY = Compatibility(
        name = "m-Indicator",
        packageName = "com.mobond.mindicator",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "18.0.356", versionCode = 356))
    )

    // Yatri — Train, Metro & Bus Ticketing
    val YATRI_COMPATIBILITY = Compatibility(
        name = "Yatri",
        packageName = "com.yatrirailways.yatri",
        appIconColor = 0xFF6B00,
        targets = listOf(AppTarget(version = "5.0.4", versionCode = 317))
    )

    // Proxyman — Network Debugger
    val PROXYMAN_COMPATIBILITY = Compatibility(
        name = "Proxyman",
        packageName = "com.proxyman.proxymanandroid",
        appIconColor = 0xFF6B35,
        targets = listOf(AppTarget(version = "1.18.0", versionCode = 45))
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
        targets = listOf(AppTarget(version = "2.367.0", versionCode = 29613027))
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
        targets = listOf(AppTarget(version = "2.11.1-ad_mob", versionCode = 133))
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
        targets = listOf(AppTarget(version = "5.19.16.0", versionCode = 605191600))
    )

    // NetShare — Mobile Hotspot / Subnet Router
    val NETSHARE_COMPATIBILITY = Compatibility(
        name = "NetShare",
        packageName = "kha.prog.mikrotik",
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "277", versionCode = 277))
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
        targets = listOf(AppTarget(version = "479", versionCode = 479))
    )

    // Speedtest by Ookla
    val SPEEDTEST_COMPATIBILITY = Compatibility(
        name = "Speedtest",
        packageName = "org.zwanoo.android.speedtest",
        appIconColor = 0x141C4C,
        targets = listOf(AppTarget(version = "7.0.7", versionCode = 258530))
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
        targets = listOf(AppTarget(version = "11.7.0", versionCode = 110708450))
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
        targets = listOf(AppTarget(version = "5.21.0.0", versionCode = 1030711))
    )

    // AccuWeather — Weather Radar & Forecast
    val ACCUWEATHER_COMPATIBILITY = Compatibility(
        name = "AccuWeather",
        packageName = "com.accuweather.android",
        appIconColor = 0xF25C1B,
        targets = listOf(AppTarget(version = "21.1.13-1-rc", versionCode = 210113001))
    )

    // Windscribe VPN
    val WINDSCRIBE_COMPATIBILITY = Compatibility(
        name = "Windscribe VPN",
        packageName = "com.windscribe.vpn",
        appIconColor = 0x00AEEF,
        targets = listOf(AppTarget(version = "4.2.2326", versionCode = 2326))
    )

    // MovieBox (com.community.oneroom) + MovieBox TV (com.community.mbox.tv) by Transsion/Mbox
    val MOVIEBOX_COMPATIBILITY = Compatibility(
        name = "MovieBox",
        packageName = "com.community.oneroom",
        appIconColor = 0xE53935,
        targets = listOf(
            AppTarget(version = "3.0.16.0703.03", versionCode = 50020113),
        )
    )

    val MOVIEBOXTV_COMPATIBILITY = Compatibility(
        name = "MovieBox TV",
        packageName = "com.community.mbox.tv",
        appIconColor = 0xE53935,
        targets = listOf(
            AppTarget(version = "1.1.1.0702.03", versionCode = 50040006),
        )
    )

    // The Weather Channel — Weather Forecast & Alerts by The Weather Channel
    val THEWEATHERCHANNEL_COMPATIBILITY = Compatibility(
        name = "The Weather Channel",
        packageName = "com.weather.Weather",
        appIconColor = 0x1B6AC9,
        targets = listOf(AppTarget(version = "16.12.0", versionCode = 1080013138))
    )

    // Ninja VPN — Fast & Secure VPN Proxy
    val NINJVAPN_COMPATIBILITY = Compatibility(
        name = "Ninja VPN",
        packageName = "app.ninjavpn.android",
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = "1.4.7", versionCode = 44))
    )

    // 1Tap Cleaner — App cache & history cleaner by a0soft
    val ONETAPCLEANER_COMPATIBILITY = Compatibility(
        name = "1Tap Cleaner",
        packageName = "com.a0soft.gphone.acc.free",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.18", versionCode = 240005189))
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
        targets = listOf(AppTarget(version = "3.2.1", versionCode = 10040838))
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
        targets = listOf(AppTarget(version = "3.2.1", versionCode = 1004178))
    )

    // Anatomy by Muscle Motion
    val ANATOMY_COMPATIBILITY = Compatibility(
        name = "Anatomy",
        packageName = "air.com.musclemotion.anatomy",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "3.2.1", versionCode = 1004185))
    )

    // Strength Training by Muscle Motion
    val STRENGTHTRAINING_COMPATIBILITY = Compatibility(
        name = "Strength Training",
        packageName = "air.com.musclemotion.strength.mobile",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "3.5.1", versionCode = 924))
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
        targets = listOf(AppTarget(version = "5.195.2.1792", versionCode = 1792))
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
        targets = listOf(AppTarget(version = "3.28.0", versionCode = 3280007))
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
        targets = listOf(AppTarget(version = "6.7.1", versionCode = 534))
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
        targets = listOf(AppTarget(version = "1.29.0-260420093"))
    )

    // CamScanner
    val CAMSCANNER_COMPATIBILITY = Compatibility(
        name = "CamScanner",
        packageName = "com.intsig.camscanner",
        appIconColor = 0x19BCAA,
        targets = listOf(AppTarget(version = "7.20.5.2606250000", versionCode = 72051))
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
        targets = listOf(AppTarget(version = "1.16.6", versionCode = 61))
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

    // PictureThis — Plant Identifier by Glority Global Group
    val PICTURETHIS_COMPATIBILITY = Compatibility(
        name = "PictureThis - Plant Identifier",
        packageName = "cn.danatech.xingseus",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "5.29.0", versionCode = 5084))
    )

    // Picture Mushroom - Mushroom ID
    val PICTUREMUSHROOM_COMPATIBILITY = Compatibility(
        name = "Picture Mushroom - Mushroom ID",
        packageName = "com.glority.picturemushroom",
        appIconColor = 0x7A4A24,
        targets = listOf(AppTarget(version = "2.9.31", versionCode = 90))
    )

    // Amazon Shopping
    val AMAZON_SHOPPING_COMPATIBILITY = Compatibility(
        name = "Amazon Shopping",
        packageName = "com.amazon.mShop.android.shopping",
        appIconColor = 0xFF9900,
        targets = listOf(AppTarget(version = "32.12.4.100", versionCode = 1241319416))
    )

    // Amazon India Shop, Pay, miniTV
    val AMAZON_IN_COMPATIBILITY = Compatibility(
        name = "Amazon India",
        packageName = "in.amazon.mShop.android.shopping",
        appIconColor = 0xFF9900,
        targets = listOf(AppTarget(version = "32.12.4.300", versionCode = 1241319416))
    )

    // AmoledPix
    val AMOLEDPIX_COMPATIBILITY = Compatibility(
        name = "AmoledPix",
        packageName = "com.androholic.amoledpix",
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "7.2", versionCode = 80))
    )

    // APKMirror Installer
    val APKMIRROR_INSTALLER_COMPATIBILITY = Compatibility(
        name = "APKMirror Installer",
        packageName = "com.apkmirror.helper.prod",
        appIconColor = 0xFF9800,
        targets = listOf(AppTarget(version = "2.0.3 (41-d04e542)", versionCode = 41))
    )

    // Block Puzzle
    val BLOCKPUZZLE_COMPATIBILITY = Compatibility(
        name = "Block Puzzle",
        packageName = "game.puzzle.blockpuzzle",
        appIconColor = 0x3B82F6,
        targets = listOf(AppTarget(version = "6.0", versionCode = 60))
    )

    // ColorNote
    val COLORNOTE_COMPATIBILITY = Compatibility(
        name = "ColorNote",
        packageName = "com.socialnmobile.dictapps.notepad.color.note",
        appIconColor = 0xF2C200,
        targets = listOf(AppTarget(version = "4.8.4", versionCode = 2104840))
    )

     // Pocket Casts
    val POCKET_CASTS_COMPATIBILITY = Compatibility(
        name = "Pocket Casts",
        packageName = "au.com.shiftyjelly.pocketcasts",
        appIconColor = 0xF43E37,
        targets = listOf(AppTarget(version = "8.14", versionCode = 9435))
    )

    // Sticker Maker
    val STICKER_MAKER_COMPATIBILITY = Compatibility(
        name = "Sticker Maker",
        packageName = "com.marsvard.stickermakerforwhatsapp",
        appIconColor = 0x00A884,
        targets = listOf(AppTarget(version = "1.0.10-5", versionCode = 1001005))
    )

    // Subway Now
    val SUBWAYNOW_COMPATIBILITY = Compatibility(
        name = "Subway Now",
        packageName = "io.goodservice.theweekendest",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.2.8", versionCode = 79))
    )

    // TrackChecker Mobile — Parcel & Package Tracker by metalsoft
    val TRACKCHECKER_COMPATIBILITY = Compatibility(
        name = "TrackChecker Mobile",
        packageName = "com.metalsoft.trackchecker_mobile",
        appIconColor = 0x1E88E5,
        targets = listOf(AppTarget(version = "2.29.3", versionCode = 505))
    )

    // Fitbod — Workout & Fitness Coach
    val FITBOD_COMPATIBILITY = Compatibility(
        name = "Fitbod",
        packageName = "com.fitbod.fitbod",
        appIconColor = 0xFF3D00,
        targets = listOf(AppTarget(version = "8.24.0-1", versionCode = 10824001))
    )

    // Toomics
    val TOOMICS_COMPATIBILITY = Compatibility(
        name = "Toomics",
        packageName = "com.toomics.global.google",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "1.6.7", versionCode = 106))
    )

    // VIZ Manga — Read Manga Official by VIZ Media
    val VIZMANGA_COMPATIBILITY = Compatibility(
        name = "VIZ Manga",
        packageName = "com.vizmanga.android",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "4.14.0", versionCode = 232))
    )

    // Microsoft Excel
    val EXCEL_COMPATIBILITY = Compatibility(
        name = "Excel",
        packageName = "com.microsoft.office.excel",
        appIconColor = 0x1B5E20,
        targets = listOf(AppTarget(version = "16.0.20131.20080", versionCode = 2005201435))
    )

    // Microsoft Word
    val WORD_COMPATIBILITY = Compatibility(
        name = "Word",
        packageName = "com.microsoft.office.word",
        appIconColor = 0x1A237E,
        targets = listOf(AppTarget(version = "16.0.20131.20080", versionCode = 2005201435))
    )

    // AiScore
    val AISCORE_COMPATIBILITY = Compatibility(
        name = "AiScore",
        packageName = "com.onesports.score",
        appIconColor = 0x2563EB,
        targets = listOf(AppTarget(version = "4.2.4", versionCode = 289))
    )

    // AMBOSS Medical Knowledge
    val AMBOSS_COMPATIBILITY = Compatibility(
        name = "AMBOSS",
        packageName = "com.amboss.medical.knowledge",
        appIconColor = 0xCC1F3C,
        targets = listOf(AppTarget(version = "2.115.1.4408", versionCode = 4408))
    )

    // Blurams Home Pro — Security Camera App
    val BLURAMS_COMPATIBILITY = Compatibility(
        name = "Blurams",
        packageName = "com.blurams.ipc",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.1049.4.908", versionCode = 1908))
    )

    // Carousell — Buy & Sell Marketplace
    val CAROUSELL_COMPATIBILITY = Compatibility(
        name = "Carousell",
        packageName = "com.thecarousell.Carousell",
        appIconColor = 0xE7392C,
        targets = listOf(AppTarget(version = "2.461.8", versionCode = 10767))
    )

    val DUOLINGO_COMPATIBILITY = Compatibility(
        name = "Duolingo",
        packageName = "com.duolingo",
        appIconColor = 0x58CC02,
        targets = listOf(AppTarget(version = "6.87.6", versionCode = 2409))
    )

    val FLIGHTSKY_COMPATIBILITY = Compatibility(
        name = "Flightsky",
        packageName = "com.live.flight.tracker",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.7.1", versionCode = 23))
    )

    val FLIGHTAWARE_COMPATIBILITY = Compatibility(
        name = "FlightAware",
        packageName = "com.flightaware.android.liveFlightTracker",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.15.4", versionCode = 501500400))
    )

    // Historical Calendar — Day in History by Alexandru Cene
    val HISTORICALCALENDAR_COMPATIBILITY = Compatibility(
        name = "Historical Calendar",
        packageName = "com.alexandrucene.dayhistory",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "7.5.4", versionCode = 376))
    )

    // JEFIT — Workout Tracker & Gym Log
    val JEFIT_COMPATIBILITY = Compatibility(
        name = "JEFIT",
        packageName = "je.fit",
        appIconColor = 0x1A73E8,
        targets = listOf(AppTarget(version = "17.1.0", versionCode = 1966))
    )

    val LIVESCORE_COMPATIBILITY = Compatibility(
        name = "LiveScore",
        packageName = "com.livescore",
        appIconColor = 0xE30613,
        targets = listOf(AppTarget(version = "9.7.1", versionCode = 2072))
    )

    // MyRadar — Weather Radar & Forecast by ACME AtronOmatic
    val MYRADAR_COMPATIBILITY = Compatibility(
        name = "MyRadar",
        packageName = "com.acmeaom.android.myradar",
        appIconColor = 0x1A6FBF,
        targets = listOf(AppTarget(version = "8.71.2", versionCode = 591))
    )

    // Pocket Prep
    val POCKETPREP_COMPATIBILITY = Compatibility(
        name = "Pocket Prep",
        packageName = "com.pocketprep.android.itcybersecurity",
        appIconColor = 0x1D5CFF,
        targets = listOf(AppTarget(version = "3.27.2", versionCode = 424))
    )

     // RecipeBro — Cooking Buddy
    val RECIPEBRO_COMPATIBILITY = Compatibility(
        name = "RecipeBro",
        packageName = "com.recipebro.cookingbuddy",
        appIconColor = 0xE65100,
        targets = listOf(AppTarget(version = "1.3.74", versionCode = 1362))
    )

    val SCRL_COMPATIBILITY = Compatibility(
        name = "SCRL",
        packageName = "com.appostrophe.scrl",
        appIconColor = 0xEFC67A,
        targets = listOf(AppTarget(version = "1.21", versionCode = 253))
    )

    // Sticker.ly
    val STICKERLY_COMPATIBILITY = Compatibility(
        name = "Sticker.ly",
        packageName = "com.snowcorp.stickerly.android",
        appIconColor = 0x00ADEF,
        targets = listOf(AppTarget(version = "3.35.0", versionCode = 1033500))
    )

    val TRADINGVIEW_COMPATIBILITY = Compatibility(
        name = "TradingView",
        packageName = "com.tradingview.tradingviewapp",
        appIconColor = 0x2962FF,
        targets = listOf(AppTarget(version = "1.20.77.0.1002295", versionCode = 1002295))
    )

     val WOLFRAMALPHA_COMPATIBILITY = Compatibility(
        name = "WolframAlpha",
        packageName = "com.wolfram.android.alphapro",
        appIconColor = 0xDD1100,
        targets = listOf(AppTarget(version = "1.0.8.20260601651", versionCode = 117))
    )
}
