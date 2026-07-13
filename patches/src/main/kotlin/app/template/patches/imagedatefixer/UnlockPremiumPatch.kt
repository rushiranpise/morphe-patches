package app.template.patches.imagedatefixer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.IMAGEDATEFIXER_COMPATIBILITY

/**
 * Unlocks premium in Image & Video Date Fixer (eu.duong.imagedatefixer).
 *
 * ## Gated features
 * - Unlimited files per batch
 * - All fix modes (EXIF, filename, folder-based, WhatsApp, etc.)
 * - Priority support access
 *
 * ## Patch strategy
 *
 * ### Layer 1 — gc.c0.g(Context)Z
 * Master isPremium gate: checks ispremium_sub → ispremium → ispremium_lifetime_v5.
 * Replace body with `const/4 0x1; return` → all call sites see premium=true.
 *
 * ### Layer 2 — gc.c0.d(Context)Z
 * Reads ispremium_lifetime_v5. Always returning true ensures lifetime unlock
 * even when g() delegates to this layer independently.
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features in Image & Video Date Fixer.",
    default = true,
) {
    compatibleWith(IMAGEDATEFIXER_COMPATIBILITY)

    execute {
        // ── Layer 1: gc.c0.g — master isPremium gate ─────────────────────────
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

        // ── Layer 2: gc.c0.d — ispremium_lifetime_v5 reader ──────────────────
        IsLifetimeV5Fingerprint
            .match(classDefBy(IsLifetimeV5Fingerprint.definingClass!!))
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
