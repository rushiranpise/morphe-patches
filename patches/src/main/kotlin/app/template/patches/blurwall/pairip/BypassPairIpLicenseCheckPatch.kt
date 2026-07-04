package app.template.patches.blurwall.pairip

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLURWALL_COMPATIBILITY

// PairIP LicenseClient.performLocalInstallerCheck() bypass.
// Returns false for non-Play installs → connectToLicensingService() → remote check
// fails on re-signed APK → LicenseActivity (PAYWALL type) → "get app from Play Store".
// Patch: always return true (= installer check passed).

val bypassPairIpLicenseCheckPatch = bytecodePatch(
    name = "Bypass PairIP License Check",
    description = "Bypasses PairIP License check.",
    default = false,
) {
    compatibleWith(BLURWALL_COMPATIBILITY)

    execute {
        PerformLocalInstallerCheckFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
