package app.template.patches.mikrotik.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MIKROTIK_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val mikroTikUnlockPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Bypasses the Google Play Billing purchase check, unlocking all pro features."
) {
    compatibleWith(MIKROTIK_COMPATIBILITY)

    execute {
        // IAB.isPurchased(String, Context)Z — primary gate for all premium features.
        // Returns true so every SKU check (any, all, specific product IDs) passes.
        IsPurchasedFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Util.isPro(Context)Z — companion-app check via PackageManager.
        // Return true so no PackageManager lookup is performed.
        IsProFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
    }
}
