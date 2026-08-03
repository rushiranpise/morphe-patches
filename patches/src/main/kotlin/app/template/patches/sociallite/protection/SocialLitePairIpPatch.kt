package app.template.patches.sociallite.protection

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.SOCIALLITE_COMPATIBILITY
import org.w3c.dom.Element

// ── Pairip scope in SocialLite ────────────────────────────────────────────────
//
// SocialLite bundles a lightweight Pairip integration — no VM, no native lib:
//
//   com.pairip.application.Application   (extends SocialLiteApplication)
//     └── attachBaseContext(Context)
//           └── LicenseClient.checkLicense(context)   ← the only enforcement point
//
//   com.pairip.licensecheck.LicenseClient     — Play LVL check + LicenseActivity start
//   com.pairip.licensecheck.LicenseActivity   — blocks UI / exits if not licensed
//   com.pairip.licensecheck.LicenseResponseHelper — RSA verification of LVL response
//   com.pairip.licensecheck.LicenseContentProvider — IPC with Play Store licensing service
//
//   NO VMRunner, NO StartupLauncher, NO libpairipcore.so, NO VmDecryptor.
//   The arm64 split only contains libandroidx.graphics.path.so.
//
// ── Root cause ────────────────────────────────────────────────────────────────
//
//   AndroidManifest.xml sets android:name="com.pairip.application.Application".
//   On every app start, Android instantiates this class and calls
//   attachBaseContext(), which unconditionally calls LicenseClient.checkLicense().
//   After Morphe re-signs the APK, the signature hash differs from the one
//   embedded in the Pairip SDK → the check fails → LicenseActivity starts and
//   blocks the UI (or calls System.exit() after timeout).
//
//   SocialLiteApplication (the real app Application subclass) has NO Pairip calls.
//   Its attachBaseContext() only applies a dark-mode configuration override.
//
// ── Fix — two parts ───────────────────────────────────────────────────────────
//
//   PART 1 (manifest): Swap android:name from
//     "com.pairip.application.Application" → "com.sociallite.android.SocialLiteApplication"
//
//     com.pairip.application.Application is never constructed, so
//     LicenseClient.checkLicense() is never called. SocialLiteApplication's
//     clean attachBaseContext() runs instead (dark-mode override only).
//     Also removes LicenseActivity from the manifest and CHECK_LICENSE permission.
//
//   PART 2 (bytecode): no-op LicenseClient.checkLicense(Context)
//
//     Belt-and-suspenders: if checkLicense() is ever reached through a path
//     other than the Application constructor (e.g. a service or broadcast),
//     the method immediately returns without performing any check or starting
//     LicenseActivity.
//
private val socialLitePairIpManifestPatch = resourcePatch(
    name = "SocialLite Pairip manifest patch",
    description = "Swaps android:name from com.pairip.application.Application to " +
        "com.sociallite.android.SocialLiteApplication, removing the Pairip " +
        "attachBaseContext call. Also removes LicenseActivity and CHECK_LICENSE.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->

            // Swap application class — Pairip's attachBaseContext never runs
            val applicationElement =
                document.getElementsByTagName("application").item(0) as Element
            applicationElement.setAttribute(
                "android:name",
                "com.sociallite.android.SocialLiteApplication",
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

            // Remove CHECK_LICENSE permission — no longer needed
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
val socialLitePairIpPatch = bytecodePatch(
    name = "Bypass Pairip integrity check",
    description = "Bypasses SocialLite's Pairip DRM: swaps the manifest " +
        "android:name to com.sociallite.android.SocialLiteApplication so " +
        "LicenseClient.checkLicense() is never called in attachBaseContext; " +
        "removes LicenseActivity and CHECK_LICENSE from the manifest; and " +
        "no-ops LicenseClient.checkLicense() as a secondary safety measure. " +
        "Required by all other SocialLite patches.",
    default = true,
) {
    compatibleWith(SOCIALLITE_COMPATIBILITY)

    dependsOn(socialLitePairIpManifestPatch)

    execute {
        // Belt-and-suspenders: no-op checkLicense() at the bytecode level
        LicenseCheckFingerprint.method.addInstructions(0, "return-void")
    }
}
