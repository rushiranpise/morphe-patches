package app.template.patches.killapps

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.KILLAPPS_COMPATIBILITY

/**
 * Unlocks Pro in KillApps — Background Apps Killer (com.tafayor.killall).
 *
 * ## What's gated
 * - System apps killing (pro-only)
 * - Pro badge and UI elements
 * - Ad display tied to non-pro status
 *
 * ## Patch strategy
 *
 * ### Layer 1 — App.isPro()Z
 * Central gate method. Replacing body with `const/4 0x1; return` makes
 * every call site see the app as pro.
 *
 * ### Layer 2 — SettingsHelper.getIsAppUpgraded()Z
 * SharedPreferences fallback called by App.isPro(). Patching ensures
 * pro state persists across version updates.
 */
@Suppress("unused")
val killAppsUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all pro features in KillApps.",
    default = true
) {
    compatibleWith(KILLAPPS_COMPATIBILITY)

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
