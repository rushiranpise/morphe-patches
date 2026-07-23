package app.template.patches.ampere.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMPERE_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlocks the "ampere_no_ads" in-app purchase in Ampere.
 *
 * ## Purchase model
 *
 * Ampere sells a single one-time purchase via Google Play Billing:
 * product ID **"ampere_no_ads"** — removes all AdMob banner ads.
 * No RevenueCat, no Pairip, no subscription tiers.
 *
 * ## Premium state pipeline (fully traced, smali-verified)
 *
 * ```
 * Play Store response
 *   → ze.a(Purchase)Z          verifyPurchase: product contains "ampere_no_ads"
 *                               + RSA SHA1withRSA public key check
 *   → C3469we / C3136ne        coroutine sets z = (purchased && verified)
 *   → bz0.g(z)V                writes "isProVersion" = z to
 *                               PreferenceManager.getDefaultSharedPreferences()
 *   → bz0.f()Z                 isProVersion() reads it back
 *   → SettingsData.f2997c      propagated via StateFlow to all UI consumers
 *   → kf.d()Z                  checkIsPaymentNeeded gate (true = must pay)
 *   → BannerAdHandler3/c       ad display suppressed when isPro = true
 * ```
 *
 * ## Three-point bypass strategy
 *
 * ### 1. IsProVersionFingerprint → returnEarly(true)  [PRIMARY — cascades everywhere]
 *
 * `bz0.f()Z` is the canonical isPro getter. Returning `true` here makes every
 * downstream consumer (SettingsData, all UI screens, billing gate) see the user as
 * premium on every call. The cascade covers:
 *   - `kf.d()` immediately returns false (no payment needed)
 *   - `SettingsData.f2997c = true` → StateFlow emits true → BannerAdHandler3 sees adFree=true
 *   - All feature gates that call `bz0.f()` directly
 *
 * ### 2. VerifyPurchaseFingerprint → returnEarly(true)  [RSA bypass]
 *
 * `ze.a(Purchase)Z` verifies the RSA SHA1withRSA purchase receipt.
 * On a re-signed APK the Play billing token is valid but the RSA check uses
 * the app's own embedded key — returning true bypasses this so any purchase
 * event (including test purchases or restored purchases) is accepted.
 *
 * ### 3. CheckIsPaymentNeededFingerprint → returnEarly(false)  [defence-in-depth]
 *
 * `kf.d()Z` is the gate checked before showing a payment dialog.
 * Returning false ("payment not needed") ensures no paywall dialog ever appears,
 * even if IsProVersionFingerprint somehow fails to match after an update.
 */
@Suppress("unused")
val amperePremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks the Remove Ads purchase in Ampere by bypassing the isProVersion getter, purchase verifier, and payment-needed gate.",
) {
    compatibleWith(AMPERE_COMPATIBILITY)

    execute {
        // 1. isProVersion() → always true — cascades to all consumers
        IsProVersionFingerprint.method.returnEarly(true)

        // 2. verifyPurchase(Purchase) → always true — bypasses RSA SHA1 check
        VerifyPurchaseFingerprint.method.returnEarly(true)

        // 3. checkIsPaymentNeeded() → always false — no paywall dialog ever shows
        CheckIsPaymentNeededFingerprint.method.returnEarly(false)

        // 4. updateProKeyMenuItemState(EnumC2245hf) → return-void — hides key icon
        //
        // The PRO_KEY toolbar icon is driven by a billing StateFlow observer, NOT by
        // bz0.isProVersion(). On patched builds the billing state stays NOT_INITIALIZED
        // (ordinal 0 → visible=true, enabled=false) so the key icon remains visible.
        //
        // Returning void skips all branches. The icon retains the C2091d8 constructor
        // default z7(false, false) = hidden — identical to the PURCHASED branch outcome.
        UpdateProKeyMenuItemStateFingerprint.method.returnEarly()
    }
}
