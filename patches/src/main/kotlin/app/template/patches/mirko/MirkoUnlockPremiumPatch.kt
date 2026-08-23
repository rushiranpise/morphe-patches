package app.template.patches.mirko

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MIRKO_COMPATIBILITY
import app.template.patches.shared.returnEarly
import app.template.patches.shared.disablePairIPLicenseCheckPatch

@Suppress("unused")
val mirkoUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium in Beta by Mirko by disabling the ads gate and PairIP license checks.",
    default = true,
) {
    compatibleWith(MIRKO_COMPATIBILITY)
    dependsOn(disablePairIPLicenseCheckPatch)

    execute {
        // ── Layer 1: ads/premium gate → return false (= no ads = premium) ────
        // r4.c.e()Z reads SharedPrefs "key_ads_2" (true = has ads, default).
        // Returning false makes all callers treat the user as premium:
        // - r5.c.run() skips ad UI
        // - ViewActivity.A() skips interstitial
        // - ViewActivity.onResume() skips ad load
        // Fingerprint anchored to string("key_ads_2") only — survives R8 class renames.
        AdsStateFingerprint.method.returnEarly(false)

        // ── Layer 2: PairIP ContentProvider startup bypass → return true ──────
        // LicenseContentProvider.onCreate() calls LicenseClient.checkLicense()
        // before any Activity starts. On an unsigned/patched APK this connects
        // to Play licensing, fails, and launches LicenseActivity → kills process.
        // Returning true immediately skips the entire license initialization.
        PairIpContentProviderFingerprint.method.returnEarly(true)

        // ── Layer 3: PairIP RSA validation bypass → return-void ───────────────
        // LicenseResponseHelper.validateResponse() performs SHA256withRSA signature
        // check and throws LicenseCheckException on failure.
        // return-void makes every response silently pass (defensive layer).
        PairIpValidateResponseFingerprint.method.returnEarly()
    }
}
