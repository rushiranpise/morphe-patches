package app.template.patches.hibernator

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.HIBERNATOR_COMPATIBILITY

/**
 * Unlocks Pro in Hibernator — Apps & Background Process (com.tafayor.hibernator).
 *
 * ## What's gated
 * - Closing system/background apps (pro-only feature)
 * - Pro badge and title in UI
 * - Ad display tied to non-pro status
 *
 * ## Patch strategy
 *
 * ### Layer 1 — App.isPro()Z
 *
 * Central gate method checked throughout the app. When FORCE_PRO is false it
 * delegates to SettingsHelper.getIsAppUpgraded(). Replacing its body with
 * `const/4 0x1; return` makes every call site see the app as pro.
 *
 * ### Layer 2 — SettingsHelper.getIsAppUpgraded()Z
 *
 * Called by App.isPro() as the SharedPreferences-backed fallback. Patching
 * this as well ensures the pro state survives if App.isPro() fingerprint ever
 * mismatches across version updates.
 */
@Suppress("unused")
val hibernatorUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all pro features in Hibernator.",
    default = true
) {
    compatibleWith(HIBERNATOR_COMPATIBILITY)

    execute {
        // ── Layer 1: App.isPro()Z — always return true ──────────────────────
        IsProFingerprint
            .match(classDefBy(IsProFingerprint.definingClass!!))
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

        // ── Layer 2: SettingsHelper.getIsAppUpgraded()Z — always return true ─
        GetIsAppUpgradedFingerprint
            .match(classDefBy(GetIsAppUpgradedFingerprint.definingClass!!))
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
    }
}
