package app.template.patches.clue

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CLUE_COMPATIBILITY

/**
 * Unlocks Clue Plus (com.clue.android 261.0).
 *
 * Two gates patched:
 *
 * Gate 1 – ProductTier.getHasCluePlus()Z → always true
 *   Root gate. Covers both launch path (InitializeResponseDto → UserPreference)
 *   and paywall path (PaywallResponse → ys1 subscription StateFlow).
 *
 * Gate 2 – UserPreference.getIsSubscribed()Z → always true
 *   Proto accessor read by the User domain mapper (mp9). Ensures y6h.isSubscribed
 *   is always true even if ProductTier object is null or bypassed.
 */
@Suppress("unused")
val clueUnlockPlusPatch = bytecodePatch(
    name = "Unlock Plus",
    description = "Forces Clue Plus subscription active, unlocking all premium features.",
    default = true,
) {
    compatibleWith(CLUE_COMPATIBILITY)

    execute {
        // Gate 1: ProductTier.getHasCluePlus() → always true
        ProductTierHasCluePlusFingerprint
            .match(mutableClassDefBy(ProductTierHasCluePlusFingerprint.definingClass!!))
            .method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // Gate 2: UserPreference.getIsSubscribed() → always true
        UserPreferenceIsSubscribedFingerprint
            .match(mutableClassDefBy(UserPreferenceIsSubscribedFingerprint.definingClass!!))
            .method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
    }
}
