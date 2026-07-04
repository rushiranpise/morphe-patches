package app.template.patches.renameorganize

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.RENAMEORGANIZE_COMPATIBILITY

/**
 * Unlocks premium in Rename & Organize (eu.duong.picturemanager).
 *
 * ## Gated features
 * - Unlimited batch file renaming/organizing
 * - EXIF editor
 * - Duplicate finder
 * - GPX-based organization
 * - Scheduled batch jobs
 *
 * ## Patch strategy
 *
 * ### f5.c.n0(Context)Z — master isPremium gate
 * Checks ispremium_lifetime → static c:Z flag → ispremium → ispremium_lifetime_v2.
 * Replace body with `const/4 0x1; return` → all call sites see premium=true.
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features in Rename & Organize.",
    default = true,
) {
    compatibleWith(RENAMEORGANIZE_COMPATIBILITY)

    execute {
        // ── f5.c.n0 — master isPremium gate ───────────────────────────────────
        IsPremiumFingerprint
            .match(classDefBy(IsPremiumFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent(),
                )
            }
    }
}
