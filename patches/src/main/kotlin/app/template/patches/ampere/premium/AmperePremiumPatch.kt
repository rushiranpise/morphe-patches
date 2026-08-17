package app.template.patches.ampere.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMPERE_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ── Purchase model ────────────────────────────────────────────────────────────
//
// Single one-time Google Play purchase: product ID "ampere_no_ads".
// No Pairip, no RevenueCat, no subscription tiers.
//
// Premium state pipeline:
//   Play Store response
//     → VerifyPurchase (RSA + "ampere_no_ads" product check)
//     → SharedPreferences.putBoolean("isProVersion", true)
//     → isProVersion() getter (SharedPrefs delegate → Boolean.booleanValue)
//     → SettingsData.isProVersion field (propagated via StateFlow to all UI)
//     → Payment gate (returns false = "no payment needed" when isPro = true)
//
// Patch strategy: two points only.
//
// PRIMARY: isProVersion() → true
//   Cascades to all consumers — SettingsData, all feature gates, payment dialog.
//   Fingerprinted on stable Kotlin stdlib calls (ReadWriteProperty.getValue,
//   Boolean.booleanValue) + the "isProVersion" string in the class clinit.
//   Zero obfuscated names.
//
// SECONDARY: verifyPurchase() → true
//   Bypasses RSA/Base64 signature check so any purchase event is accepted.
//   Fingerprinted on "ampere_no_ads" SKU string + ArrayList.contains() +
//   com.braintrapp.billing.iab.Base64DecoderException catch block.
//   Zero obfuscated names.
//
// The payment-needed gate and toolbar icon patches from the previous version
// are intentionally removed — they relied on obfuscated class names (kf, hf,
// z7) that renamed every build. The isProVersion() cascade already makes the
// payment gate return false and drives the SettingsData StateFlow that controls
// the toolbar icon state. Defence-in-depth via separate patches is not worth
// the maintenance cost when the anchors are unstable.
//
@Suppress("unused")
val amperePremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks the Remove Ads purchase in Ampere by bypassing the isProVersion getter and purchase verifier.",
) {
    compatibleWith(AMPERE_COMPATIBILITY)

    execute {
        // PRIMARY: isProVersion() → always true
        // Anchored on: Kotlin stdlib ReadWriteProperty.getValue + Boolean.booleanValue
        // + "isProVersion" string in class clinit. Survives all R8 renames.
        IsProVersionFingerprint.method.returnEarly(true)

        // SECONDARY: verifyPurchase() → always true
        // Anchored on: "ampere_no_ads" SKU + ArrayList.contains + braintrapp
        // Base64DecoderException catch. All stable, non-obfuscated.
        VerifyPurchaseFingerprint.method.returnEarly(true)
    }
}
