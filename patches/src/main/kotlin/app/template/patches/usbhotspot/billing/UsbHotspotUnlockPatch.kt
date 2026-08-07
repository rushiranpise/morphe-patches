package app.template.patches.usbhotspot.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.USBHOTSPOT_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val usbHotspotUnlockPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Bypasses the Google Play Billing premium check, unlocking pro features."
) {
    compatibleWith(USBHOTSPOT_COMPATIBILITY)

    execute {
        // d1.h.e(Context)Z — SharedPreferences("pro").getBoolean("pro", false).
        // Return true unconditionally so all premium gates pass without a purchase.
        IsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
    }
}
