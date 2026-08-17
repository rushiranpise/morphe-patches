package app.template.patches.aaenabler.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AAENABLER_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val aaEnablerPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium installation features by bypassing the Firestore license check."
) {
    compatibleWith(AAENABLER_COMPATIBILITY)

    execute {
        // getLicenseActive() is the sole read point for the license flag in MainUiState.
        // Returning true unconditionally bypasses the install gate in installWithShizuku()
        // and unlocks premium UI elements in the Compose screen.
        LicenseActiveFingerprint.method.returnEarly(true)
    }
}
