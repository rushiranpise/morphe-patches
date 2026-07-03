package app.template.patches.blurwall.pairip

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// LicenseClient.performLocalInstallerCheck()Z
// Checks getInstallSourceInfo().getInstallingPackageName() == "com.android.vending".
// Returns false for sideloaded/re-signed APKs → triggers Play Store paywall dialog.
object PerformLocalInstallerCheckFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PRIVATE),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("com.android.vending"),
        string("Local install check failed due to wrong installer."),
    ),
    custom = { method, _ -> method.name == "performLocalInstallerCheck" },
)
