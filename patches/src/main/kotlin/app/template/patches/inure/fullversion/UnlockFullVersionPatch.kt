package app.template.patches.inure.fullversion

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.INURE_COMPATIBILITY

/**
 * Unlocks full version in Inure App Manager (app.simple.inure.play).
 *
 * ## What's gated
 * - All features behind 15-day trial expiry
 * - Paid purchase check via "is_full_version_" EncryptedSharedPreferences key
 * - Feature access through ScopedFragment.fullVersionCheck()
 *
 * ## Patch strategy
 *
 * ### Layer 1 — TrialPreferences.isFullVersion()Z
 * Direct purchase flag reader. Always returning true signals app is bought.
 *
 * ### Layer 2 — TrialPreferences.isAppFullVersionEnabled()Z
 * Combined flag+trial gate used across feature checks. Always returning true
 * unlocks all features regardless of purchase or elapsed time.
 *
 * ### Layer 3 — TrialPreferences.isWithinTrialPeriod()Z
 * Returns true while fewer than 15 days since install. Always returning true
 * prevents expiry-based restriction.
 *
 * ### Layer 4 — TrialPreferences.isTrialWithoutFull()Z
 * Returns true when trial expired without purchase (triggers nag dialogs).
 * Always returning false suppresses all purchase prompts.
 *
 * ### Layer 5 — BaseActivity.fullVersionCheck()Z
 * Inline duplicate of the gate in the Activity base class (no-arg overload).
 * Always returning true prevents the FullVersion dialog at activity level.
 *
 * ### Layer 6 — BaseActivity.fullVersionCheck(Function0)Z
 * Overload with onClose lambda; same inline gate. Always returning true
 * skips the dialog and lets the caller proceed.
 *
 * ### Layer 7 — SplashScreen.unlockStateChecker()V
 * Called on every launch. Checks for the companion unlocker APK
 * (app.simple.inureunlocker) and calls setFullVersion(false) when absent,
 * actively overwriting our patches. No-op prevents the false deactivation write.
 */
@Suppress("unused")
val unlockFullVersionPatch = bytecodePatch(
    name = "Unlock Full Version",
    description = "Unlocks all features in Inure App Manager.",
    default = true
) {
    compatibleWith(INURE_COMPATIBILITY)

    execute {
        // ── Layer 1: isFullVersion()Z — always return true ──────────────────
        IsFullVersionFingerprint
            .match(classDefBy(IsFullVersionFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 2: isAppFullVersionEnabled()Z — always return true ─────────
        IsAppFullVersionEnabledFingerprint
            .match(classDefBy(IsAppFullVersionEnabledFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 3: isWithinTrialPeriod()Z — always return true ─────────────
        IsWithinTrialPeriodFingerprint
            .match(classDefBy(IsWithinTrialPeriodFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 4: isTrialWithoutFull()Z — always return false ─────────────
        IsTrialWithoutFullFingerprint
            .match(classDefBy(IsTrialWithoutFullFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x0
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 5: BaseActivity.fullVersionCheck()Z — always return true ───
        BaseActivityFullVersionCheckFingerprint
            .match(classDefBy(BaseActivityFullVersionCheckFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 6: BaseActivity.fullVersionCheck(Function0)Z — always return true ─
        BaseActivityFullVersionCheckWithCallbackFingerprint
            .match(classDefBy(BaseActivityFullVersionCheckWithCallbackFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 7: SplashScreen.unlockStateChecker()V ──────────────────────
        // Checks for companion unlocker APK; calls setFullVersion(false) if absent.
        // Also the ONLY place that hides the daysLeft TextView — layout has it
        // visible by default with hardcoded trial text. Replace body with just
        // the ViewUtils.gone(daysLeft) call so the view is always hidden.
        UnlockStateCheckerFingerprint
            .match(classDefBy(UnlockStateCheckerFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    sget-object v0, Lapp/simple/inure/util/ViewUtils;->INSTANCE:Lapp/simple/inure/util/ViewUtils;
                    iget-object v1, p0, Lapp/simple/inure/ui/launcher/SplashScreen;->daysLeft:Lapp/simple/inure/decorations/typeface/TypeFaceTextView;
                    check-cast v1, Landroid/view/View;
                    invoke-virtual {v0, v1}, Lapp/simple/inure/util/ViewUtils;->gone(Landroid/view/View;)V
                    return-void
                    """.trimIndent()
                )
            }
    }
}
