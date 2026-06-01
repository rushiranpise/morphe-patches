package app.template.patches.callrecorder

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CALLRECORDER_COMPATIBILITY

/**
 * Unlocks premium features in Call Recorder — Automatic (com.catalinagroup.callrecorder).
 *
 * The app has two independent premium gate systems:
 *
 * System A — IAB Google Play Billing (the REAL paywall)
 *   Every UI feature lock calls one of two methods on the obfuscated IAB
 *   billing manager class (e4/n at runtime):
 *     g()Z — hasPurchaseTokenOrProductId — MainActivity, SideBarView,
 *             RecordList, CallRecording, BackupSystemCell, PremiumPromo
 *     h()Z — hasPurchaseToken            — SideBarView, RecordList,
 *             TutorialPremiumOffer, PremiumPromo
 *   Both are forced to return true.
 *
 * System B — PremiumRewardState (video-reward time-based unlock)
 *   A secondary system where users earn premium time by watching rewarded
 *   video ads. Patched as belt-and-suspenders coverage.
 *     rewardIsActive()           → return true
 *     rewardIsActive(Context)    → return true
 *     isMaxViewsLimitReached()   → return false
 *     shouldDiscard()            → return false  (has try/catch — prepend only)
 *     shouldShowPromo()          → return null   (has try/catch — prepend only)
 */
@Suppress("unused")
val callRecorderUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium Features In the App.",
    default = true
) {
    compatibleWith(CALLRECORDER_COMPATIBILITY)

    execute {
        // ── System A: IAB purchase gates (primary paywall) ───────────────────

        // g()Z — hasPurchaseTokenOrProductId — no try/catch, safe to replace
        IabHasPurchaseFingerprint
            .match()
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // h()Z — hasPurchaseToken — no try/catch, safe to replace
        IabHasPurchaseTokenFingerprint
            .match()
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // ── System B: PremiumRewardState (belt-and-suspenders) ──────────────

        // rewardIsActive()Z (private instance) — no try/catch, safe to replace
        RewardIsActiveInstanceFingerprint
            .match(classDefBy(RewardIsActiveInstanceFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // rewardIsActive(Context)Z (public static) — no try/catch, safe to replace
        RewardIsActiveStaticFingerprint
            .match(classDefBy(RewardIsActiveStaticFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // isMaxViewsLimitReached(Context)Z — no try/catch, safe to replace
        IsMaxViewsLimitReachedFingerprint
            .match(classDefBy(IsMaxViewsLimitReachedFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }

        // shouldDiscard(Context, Preferences, long)Z — HAS try/catch: prepend only
        ShouldDiscardStaticFingerprint
            .match(classDefBy(ShouldDiscardStaticFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }

        // shouldShowPromo(Context, Preferences)Pair — HAS try/catch: prepend only
        ShouldShowPromoFingerprint
            .match(classDefBy(ShouldShowPromoFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
            }
    }
}