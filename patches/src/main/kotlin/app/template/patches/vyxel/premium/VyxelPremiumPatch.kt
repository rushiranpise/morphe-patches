package app.template.patches.vyxel.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.VYXEL_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Vyxel Apps premium system overview:
// - Premium themes (Liquid Glass Light/Dark, Neon Punk, Cyberpunk) are gated behind
//   a Gumroad license key (server-side verification via api.gumroad.com).
// - On successful verification, AppViewModel.verifyGumroadKey() calls
//   PreferencesManager.saveLiquidGlassUnlocked(true) and saves the used key.
// - At startup, AppViewModel reads PreferencesManager.loadLiquidGlassUnlocked()
//   and stores the result in UiState.liquidGlassUnlocked.
// - The UI gates access to premium themes on liquidGlassUnlocked == true.
//
// Patch strategy:
// - Intercept loadLiquidGlassUnlocked() and always return true.
// - This makes all premium themes available unconditionally without requiring
//   a valid Gumroad license key or any network call.
// - The Gumroad verification flow still executes if the user submits a key,
//   but no key is needed for access.

@Suppress("unused")
val vyxelPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium themes (Liquid Glass, Neon Punk, Cyberpunk) by bypassing the Gumroad license key verification."
) {
    compatibleWith(VYXEL_COMPATIBILITY)

    execute {
        // loadLiquidGlassUnlocked() reads from EncryptedSharedPreferences ("lg_unlocked")
        // or plain SharedPreferences ("lg_unlocked_fb"). Returning true here makes the
        // app believe premium themes are permanently unlocked without a Gumroad key.
        LoadLiquidGlassUnlockedFingerprint.method.returnEarly(true)
    }
}
