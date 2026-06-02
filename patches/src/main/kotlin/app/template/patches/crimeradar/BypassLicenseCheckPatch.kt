package app.template.patches.crimeradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CRIMERADAR_COMPATIBILITY

// Full Dalvik path confirmed from:
//   smali_classes2/com/pairip/licensecheck/LicenseClient.smali
// Method: public static checkLicense(Landroid/content/Context;)V
// PairIP DRM entry point — NOT_LICENSED (0x2) triggers gracefulShutdown.
// No-op prevents forced shutdown on non-Play-licensed installs.
object CheckLicenseFingerprint : Fingerprint(
    name = "checkLicense",
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;"
)

@Suppress("unused")
val bypassLicenseCheckPatch = bytecodePatch(
    name = "Bypass License Check",
    description = "Bypasses PairIP DRM license verification to prevent forced app shutdown on non-Play-licensed installs."
) {
    compatibleWith(CRIMERADAR_COMPATIBILITY)

    execute {
        CheckLicenseFingerprint
            .match(classDefBy(CheckLicenseFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }
    }
}