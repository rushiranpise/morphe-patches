package app.template.patches.dailyhunt.subscription

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.DAILYHUNT_COMPATIBILITY

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features after login.",
) {
    compatibleWith(DAILYHUNT_COMPATIBILITY)

    execute {
        IsPremiumSubscribedFingerprint.method
            .addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
    }
}
