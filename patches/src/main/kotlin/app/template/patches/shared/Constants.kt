package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Call Recorder — Automatic by Catalina Group
    val CALLRECORDER_COMPATIBILITY = Compatibility(
        name = "Call Recorder - Automatic",
        packageName = "com.catalinagroup.callrecorder",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = "2.4.281"))
    )

    // Universal TV Remote Control by SensusTech
    val UNIVERSALTV_COMPATIBILITY = Compatibility(
        name = "Universal TV Remote Control",
        packageName = "sensustech.universal.tv.remote.control",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "1.8.1"))
    )

    // Citizen — Safety Alert by sp0n
    val CITIZEN_COMPATIBILITY = Compatibility(
        name = "Citizen - Safety Alert",
        packageName = "sp0n.citizen",
        appIconColor = 0x0066FF,
        targets = listOf(AppTarget(version = "0.1297.0"))
    )


    // Case Tracker — Immigration App by Saldous
    val CASETRACKER_COMPATIBILITY = Compatibility(
        name = "Case Tracker - Immigration",
        packageName = "com.saldous.casetracker",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "5.5.1"))
    )

    // Cloudflare WARP — 1.1.1.1
    val WARP_COMPATIBILITY = Compatibility(
        name = "1.1.1.1 + WARP",
        packageName = "com.cloudflare.onedotonedotonedotone",
        appIconColor = 0xF48120,
        targets = listOf(AppTarget(version = "6.38.7"))
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
        name = "Snipd: AI Podcast Player",
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
        targets = listOf(AppTarget(version = "4.18.6"))
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
        targets = listOf(AppTarget(version = "2.20"))
    )

    // CPU-Z by CPUID
    val CPUZ_COMPATIBILITY = Compatibility(
        name = "CPU-Z",
        packageName = "com.cpuid.cpu_z",
        appIconColor = 0x2A3B4C,
        targets = listOf(AppTarget(version = "1.57"))
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
        name = "NetShare - Hotspot & Wifi Direct",
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
        name = "Speedtest by Ookla",
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
        name = "Cashew - Budget & Finance App",
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
        name = "Waze - GPS, Maps & Traffic",
        packageName = "com.waze",
        appIconColor = 0x33CCFF,
        targets = listOf(AppTarget(version = "5.19.0.2"))
    )

    // AccuWeather — Weather Radar & Forecast
    val ACCUWEATHER_COMPATIBILITY = Compatibility(
        name = "AccuWeather: Weather Radar",
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

}
