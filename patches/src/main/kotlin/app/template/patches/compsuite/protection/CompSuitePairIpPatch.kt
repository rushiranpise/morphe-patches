package app.template.patches.compsuite.protection

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.COMPSUITE_COMPATIBILITY
import org.w3c.dom.Element

// ── Pairip scope in WearOS Toolset ───────────────────────────────────────────
//
// Lightweight Pairip integration — no VM, no native lib:
//   com.pairip.application.Application (extends android.app.Application)
//     └── attachBaseContext(Context)
//           └── LicenseClient.checkLicense(context)   ← sole enforcement point
//
//   com.pairip.licensecheck.LicenseClient   — Play LVL check + LicenseActivity
//   com.pairip.licensecheck.LicenseActivity — blocks UI or calls System.exit()
//
//   No VMRunner, no StartupLauncher, no libpairipcore.so.
//   arm64 split only contains libandroidx.graphics.path.so + libdatastore_shared_counter.so.
//
// ── Fix — two parts ───────────────────────────────────────────────────────────
//
//   PART 1 (manifest): swap android:name from
//     "com.pairip.application.Application" → "android.app.Application"
//
//     com.pairip.application.Application.attachBaseContext() calls
//     LicenseClient.checkLicense() → signature mismatch → LicenseActivity starts.
//     Swapping to android.app.Application skips that path entirely.
//     Safe to use the base class directly: no custom WearOS Toolset Application
//     subclass exists; all app init is lazy (BillingLibrary singleton, etc.).
//     Also removes LicenseActivity and CHECK_LICENSE permission.
//
//   PART 2 (bytecode): no-op LicenseClient.checkLicense(Context)
//     Belt-and-suspenders: if checkLicense() is ever reached through another path.
//
private val compSuitePairIpManifestPatch = resourcePatch(
    name = "WearOS Toolset Pairip manifest patch",
    description = "Swaps android:name to android.app.Application, " +
        "removing the Pairip attachBaseContext call.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->

            // Swap application class — Pairip attachBaseContext never runs
            val applicationElement =
                document.getElementsByTagName("application").item(0) as Element
            applicationElement.setAttribute(
                "android:name",
                "android.app.Application",
            )

            // Remove LicenseActivity — paywall screen can no longer start
            val activities = document.getElementsByTagName("activity")
            for (i in activities.length - 1 downTo 0) {
                val activity = activities.item(i) as Element
                if (activity.getAttribute("android:name")
                        .contains("LicenseActivity")) {
                    activity.parentNode.removeChild(activity)
                }
            }

            // Remove CHECK_LICENSE permission
            val permissions = document.getElementsByTagName("uses-permission")
            for (i in permissions.length - 1 downTo 0) {
                val permission = permissions.item(i) as Element
                if (permission.getAttribute("android:name")
                        .contains("CHECK_LICENSE")) {
                    permission.parentNode.removeChild(permission)
                }
            }
        }
    }
}

@Suppress("unused")
val compSuitePairIpPatch = bytecodePatch(
    name = "Bypass Pairip integrity check",
    description = "Bypasses Pairip DRM in WearOS Toolset by swapping the manifest " +
        "application class to android.app.Application (skipping LicenseClient " +
        "in attachBaseContext) and no-oping LicenseClient.checkLicense() directly. " +
        "Required by the unlock patch.",
    default = true,
) {
    compatibleWith(COMPSUITE_COMPATIBILITY)

    dependsOn(compSuitePairIpManifestPatch)

    execute {
        LicenseCheckFingerprint.method.addInstructions(0, "return-void")
    }
}
