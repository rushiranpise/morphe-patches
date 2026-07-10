package app.template.patches.fitbod.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Fitbod premium features.",
) {
    compatibleWith(Constants.FITBOD_COMPATIBILITY)

    execute {
        // IsUserSubscribedProvider.isUserSubscribed():Z → true
        // Drives FeatureAccessStateProvider.isUserUnlocked() and all subscription gates.
        IsUserSubscribedFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
    }
}
