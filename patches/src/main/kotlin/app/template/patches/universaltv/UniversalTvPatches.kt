package app.template.patches.universaltv

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.UNIVERSALTV_COMPATIBILITY

/**
 * Forces s8/f.e(Context)Z to always return true, bypassing all three
 * purchase gates (SharedPreferences isPremium, inapp SKU, and both
 * subscription SKUs) in a single no-try/catch method replacement.
 */
@Suppress("unused")
val universalTvUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium Features In the App.",
    default = true
) {
    compatibleWith(UNIVERSALTV_COMPATIBILITY)

    execute {
        IsPremiumFingerprint
            .match()
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        AppOpenAdPreloaderFingerprint
            .match()
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }

        CheckPremiumBroadcastFingerprint
            .match()
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }
    }
}