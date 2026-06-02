package app.template.patches.crimeradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CRIMERADAR_COMPATIBILITY

// Full Dalvik path confirmed from:
//   smali_classes8/com/particlemedia/feature/subscription/PremiumEntitlementHelper.smali
// Method: public static final isPremiumActive()Z
// Called by isAdFreeEnabled(), hasUnlimitedAudioPlayback(),
// hasUnlimitedReplayPlayback(), and maxSavedLocationCount().
object IsPremiumActiveFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/subscription/PremiumEntitlementHelper;",
    name = "isPremiumActive"
)

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium Features In the App."
) {
    compatibleWith(CRIMERADAR_COMPATIBILITY)

    execute {
        val method = IsPremiumActiveFingerprint
            .match(classDefBy(IsPremiumActiveFingerprint.definingClass!!))
            .method
        method.removeInstructions(0, method.instructions.count())
        method.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
            """.trimIndent()
        )
    }
}