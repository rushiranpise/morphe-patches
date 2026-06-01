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
}
