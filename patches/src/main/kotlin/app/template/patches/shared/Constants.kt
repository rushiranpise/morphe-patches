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
}
