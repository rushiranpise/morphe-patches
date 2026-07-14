package app.template.patches.countdownwidget.pro

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COUNTDOWN_WIDGET_COMPATIBILITY

// Countdown Widget (me.gira.widget.countdown) — Premium unlock
//
// Uses ZipoApps PremiumHelper library (v5.2.1).
// Premium state is stored as "has_active_purchase" boolean in SharedPreferences.
//
// Call chain:
//   PhUtils.a()Z
//     → PremiumHelper.getInstance().preferences.h()Z
//       → SharedPreferences.getBoolean("has_active_purchase", false)
//
// PhUtils.a() is the app's own thin wrapper, called from:
//   - SettingsActivity (backup/restore gating)
//   - BackupsActivity (cloud backup feature)
//   - Widget providers (custom themes per widget)
//
// Preferences.h()Z covers any direct internal library calls.
//
// Patch both layers so premium is always true regardless of billing state.

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features in Countdown Widget",
    default = true,
) {
    compatibleWith(COUNTDOWN_WIDGET_COMPATIBILITY)

    execute {
        // Layer 1: PhUtils.a()Z — app-side gate
        PhUtilsIsPremiumFingerprint
            .match(classDefBy(PhUtilsIsPremiumFingerprint.definingClass!!))
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

        // Layer 2: Preferences.h()Z — library-side gate
        PreferencesHasActivePurchaseFingerprint.method.apply {
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
