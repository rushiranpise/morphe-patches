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
        targets = listOf(AppTarget(version = null))
    )

    // Universal TV Remote Control by SensusTech
    val UNIVERSALTV_COMPATIBILITY = Compatibility(
        name = "Universal TV Remote Control",
        packageName = "sensustech.universal.tv.remote.control",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = null))
    )

    // Citizen — Safety Alert by sp0n
    val CITIZEN_COMPATIBILITY = Compatibility(
        name = "Citizen - Safety Alert",
        packageName = "sp0n.citizen",
        appIconColor = 0x0066FF,
        targets = listOf(AppTarget(version = null))
    )


    // Case Tracker — Immigration App by Saldous
    val CASETRACKER_COMPATIBILITY = Compatibility(
        name = "Case Tracker - Immigration",
        packageName = "com.saldous.casetracker",
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = null))
    )

    // Cloudflare WARP — 1.1.1.1
    val WARP_COMPATIBILITY = Compatibility(
        name = "1.1.1.1 + WARP",
        packageName = "com.cloudflare.onedotonedotonedotone",
        appIconColor = 0xF48120,
        targets = listOf(AppTarget(version = null))
    )

    // Crime Radar — Local Police & Safety by Newsbreak
    val CRIMERADAR_COMPATIBILITY = Compatibility(
        name = "Crime Radar",
        packageName = "com.newsbreak.crimeradar",
        appIconColor = 0xE53935,
        targets = listOf(AppTarget(version = null))
    )

    // Splitwise — Finance, bills and expenses
    val SPLITWISE_COMPATIBILITY = Compatibility(
        name = "Splitwise",
        packageName = "com.Splitwise.SplitwiseMobile",
        appIconColor = 0x1CC29F,
        targets = listOf(AppTarget(version = null))
    )

    // Greenify — App Hibernation & Battery Saver
    val GREENIFY_COMPATIBILITY = Compatibility(
        name = "Greenify",
        packageName = "com.oasisfeng.greenify",
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = null))
    )

    // Pialytic — LaTeX Editor (VerbTeX)
    val PIALYTIC_COMPATIBILITY = Compatibility(
        name = "Pialytic",
        packageName = "verbosus.pialytic",
        appIconColor = 0x2196F3,
        targets = listOf(AppTarget(version = "1.2.6"))
    )

    // Snipd — AI Podcast Player
    val SNIPD_COMPATIBILITY = Compatibility(
        name = "Snipd: AI Podcast Player",
        packageName = "ai.topicfinder.podcastdiscovery",
        appIconColor = 0x1CC29F,
        targets = listOf(AppTarget(version = null))
    )

    // TWT App — Astronomy & Sky Guide
    val TWTAPP_COMPATIBILITY = Compatibility(
        name = "TWT App",
        packageName = "com.twtapp",
        appIconColor = 0x1A1A2E,
        targets = listOf(AppTarget(version = null))
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
        name = "Beta by Mirko",
        packageName = "it.mirko.beta",
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "0.9.4"))
    )
}
