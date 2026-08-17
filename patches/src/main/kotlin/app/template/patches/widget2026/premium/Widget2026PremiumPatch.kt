package app.template.patches.widget2026.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WIDGET2026_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Widget 2026 / Aesthetic Widgets premium architecture:
//
// The app uses com.zipoapps.premiumhelper SDK wrapping Google Play Billing.
// Premium state is stored as SharedPreferences key "has_active_purchase" (boolean).
//
// Public API: d.b()Z — static method called from 34 sites across:
//   - MainActivity, ActivitySetting (show/hide premium UI)
//   - BaseProvider (controls widget rendering, gating premium widget types)
//   - All widget activities: clock, calendar, battery, notes, countdown, contacts,
//     photos, quotes, day counter, stickers, simple widgets
//   - ActivityStickerStore, ActivityQuotes, ActivityCustom* screens
//
// Root: yd/e.j()Z — reads SharedPreferences.getBoolean("has_active_purchase", false).
//   Patching here cascades through the entire PremiumHelper singleton chain.
//
// Ads: AppLovin (2776 classes), Meta Audience Network, AdMob — all gated by d.b()Z.
//   Patching d.b()Z → true also suppresses ad loading calls in the SDK layers.

@Suppress("unused")
val widget2026PremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium widgets, themes, and features in Widget 2026 by bypassing the PremiumHelper SDK subscription check at both the public API (d.b()) and SharedPreferences root (yd/e.j()).",
) {
    compatibleWith(WIDGET2026_COMPATIBILITY)

    execute {
        // Patch 1: Force yd/e.j() → true.
        // Root of the premium cascade — SharedPrefs "has_active_purchase" getter.
        // Cascades through the entire PremiumHelper singleton chain.
        HasActivePurchaseFingerprint.method.returnEarly(true)

        // Patch 2: Force d.b() → true.
        // Public static isPremium() API called from 34 call sites across all Activities.
        // Belt-and-suspenders alongside the root patch; also gates ad SDK initialization.
        IsPremiumStaticFingerprint.method.returnEarly(true)
    }
}
