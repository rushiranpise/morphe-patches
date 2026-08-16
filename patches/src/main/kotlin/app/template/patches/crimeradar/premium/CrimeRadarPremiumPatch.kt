package app.template.patches.crimeradar.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CRIMERADAR_COMPATIBILITY
import app.template.patches.shared.killPairIpFull
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

/**
 * Unlocks CrimeRadar premium subscription and removes ads.
 *
 * ## Billing Architecture
 *
 * CrimeRadar uses Google Play Billing via com.particlemedia.feature.subscription
 * (distinct from Zests' feature.billing module). The subscription model is:
 *
 *   SubscriptionState — data class with isActive: Z, subscriptionStatus: String,
 *   expireAt: J, sku: String and other server-populated fields.
 *
 * State is persisted by SubscriptionStateStore via SharedPreferences:
 *   "subscription_crimeradar_is_active" → SubscriptionState.isActive
 *   "subscription_crimeradar_status"    → SubscriptionState.subscriptionStatus
 *   (plus channel, sku, expireAt, autoRenew, etc.)
 *
 * All feature gates call subscriptionState.isActive() directly:
 *   - Map delegate layers: ParkSafe, PowerOutage, GasStation premium overlays
 *   - Replay playback paywall after login sync
 *   - Profile / inbox / settings premium UI sections
 *
 * ## Ad Architecture
 *
 * Identical to Zests: AdsPremium.isAdFreeEnabled() delegates through
 * AdsPremiumProvider$Companion$from$1.isAdFreeEnabled() via a Function0 lambda
 * (obfuscated as kh/a in this build) wired at AdsKit.start().
 *
 * ## Patch
 *
 * ### 1. Subscription unlock
 * IsActiveFingerprint targets SubscriptionState.isActive()Z — the universal
 * boolean getter read by every premium feature gate. Returning true
 * unconditionally covers all callers regardless of SharedPrefs cache or
 * server response state.
 *
 * ### 2. Ad removal
 * IsAdFreeEnabledFingerprint forces the AdsPremiumProvider lambda to return
 * true, suppressing all ad loading paths.
 *
 * ### 3. PairIP (Java-only variant — no libpairipcore.so)
 * LicenseClient is present in classes2. killPairIpFull() nops
 * initializeLicenseCheck, forces LOCAL_CHECK_OK, disables repeatedCheckEnabled,
 * and clears all Play LVL check methods.
 */
@Suppress("unused")
val crimeRadarPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks CrimeRadar premium subscription, raises followed locations limit, removes ads, and disables PairIP licence checks.",
) {
    compatibleWith(CRIMERADAR_COMPATIBILITY)

    dependsOn(
        spoofInstallSourcePatch,
        spoofSignatureVerificationPatch,
    )

    execute {
        // ── 1. Subscription unlock ─────────────────────────────────────────────
        IsActiveFingerprint.method.returnEarly(true)

        // ── 2. Followed locations limit ────────────────────────────────────────
        // Three paths set the adapter limit; all three must be patched:
        //
        // (a) SERVER path: updateSavedListFull(GLocationList) calls getLimit() and
        //     posts the result into GlobalLocationRepository.savedLimit LiveData.
        //     The Activity reads savedLimit FIRST — server sends 1 for free users,
        //     overriding any freeLimit() patch. Return INT_MAX to neutralise it.
        GLocationListGetLimitFingerprint.method.returnEarly(Int.MAX_VALUE)
        //
        // (b) ACCOUNT RESET path: onAccountChanged() hard-resets savedLimit to 1
        //     on every login/logout. Skip the entire method so the limit persists.
        OnAccountChangedFingerprint.method.returnEarly()
        //
        // (c) FALLBACK path: Activity uses freeLimit() when savedLimit is null.
        FreeLimitFingerprint.method.returnEarly(10)

        // ── 3. Ad removal ──────────────────────────────────────────────────────
        IsAdFreeEnabledFingerprint.method.returnEarly(true)

        // ── 4. PairIP kill ─────────────────────────────────────────────────────
        killPairIpFull()
    }
}
