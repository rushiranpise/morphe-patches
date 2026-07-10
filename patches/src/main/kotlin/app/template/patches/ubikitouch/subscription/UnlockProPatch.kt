package app.template.patches.ubikitouch.subscription

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.UBIKITOUCH_COMPATIBILITY

@Suppress("unused")
val ubikiTouchUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks UbikiTouch Pro features in app.",
) {
    compatibleWith(UBIKITOUCH_COMPATIBILITY)

    execute {
        // gp1.y() is the sole static accessor for the IS_PURCHASED_PREF Paper preference.
        // All premium feature checks across the app (util/xwzp, settings screens) call this.
        // Forcing true makes the app behave as if the one-time purchase is already acknowledged.
        IsPurchasedFingerprint
            .match(classDefBy(IsPurchasedFingerprint.definingClass!!))
            .method
            .addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
    }
}
