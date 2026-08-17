package app.template.patches.hydrocoach.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── Hydro Coach v5.1.9 — premium fingerprints ────────────────────────────────
//
// Billing: Google Play Billing (BILLING permission) + RevenueCat SDK
// No Pairip, no native libs. 2 DEX files (9,076 + 7,708 smali files).
//
// Premium model:
//   a83 = UserState data class (R8-obfuscated class name, stable @JvmStatic method names)
//     field isSubscribedToPro:Boolean      — RC subscription flag
//     field purchases:HashMap<jp, lq2>     — one-time SKU purchases (jp = ProductId enum)
//     key IS_SUBSCRIBED_TO_PRO_KEY = "isSub"
//     key PURCHASES_KEY             = "purchs"
//
//   jp = ProductId enum (R8-obfuscated, 19 values):
//     jp.B = "pro_upgrade"    (one-time PRO)
//     jp.P = "pro_lifetime"   (lifetime PRO)
//     jp.S = monthly sub, jp.T = yearly sub
//     jp.D = "no_ads"         (no-ads only SKU)
//
//   Gate methods on a83 (all PUBLIC STATIC, Kotlin @JvmStatic, STABLE NAMES):
//     getHasProFeatures(a83)Z  — master gate: checks purchases OR subscription
//     getShowAds(a83)Z         — ads gate: getHasProFeatures || getPurchasedNoAds || promo
//     getPurchasedNoAds(a83)Z  — no-ads-only SKU check
//     getHasAllThemes(a83)Z    — delegates to getHasProFeatures
//
//   Call chain (getHasProFeatures):
//     getPurchasedPro()        — checks jp.B/P/Q/R/H/I/J/K/L/M/N/O in purchases map
//     getPurchasedUpgradeNoAdsToPro()
//     hasUnlockedPurchaseFromPromo(jp.B) + hasUnlockedPurchaseFromPromo(jp.C)
//     isSubscribedToPro()      — reads isSubscribedToPro:Boolean field
//
// PATCH STRATEGY — three targeted methods on a83:
//   Layer 1: getHasProFeatures(a83)Z → return true
//     Cascades to: getHasAllThemes, getShowAds (no-ads), all feature gates
//   Layer 2: getShowAds(a83)Z → return false
//     Belt+suspenders for ad removal (called independently in some places)
//   Layer 3: getPurchasedNoAds(a83)Z → return true
//     Covers the no-ads-only purchase path in getShowAds
//
//   All three are in the SAME class (a83). Found via:
//   classFingerprint: class containing both "getPurchasedPro" AND "isSubscribedToPro"
//   method names (only 1 class in 16,784 smali files matches — verified by scan)
//
// STABLE ANCHORS (zero obfuscated identifiers):
//   Method names "getPurchasedPro", "isSubscribedToPro", "getHasProFeatures",
//   "getShowAds", "getPurchasedNoAds" are Kotlin @JvmStatic compiler-generated
//   names — never obfuscated by R8 when using @JvmStatic.
//   Access flags: PUBLIC STATIC — verified in smali.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Locates the a83 UserState class via the presence of two stable method names.
 * The class has zero const-string instructions in any method body, so strings=
 * cannot be used. custom{} checks method names directly.
 * Verified unique: 1 class out of 16,784 smali files has both methods.
 */
private val userStateClassFingerprint = Fingerprint(
    custom = { _, classDef ->
        classDef.methods.any { it.name == "getPurchasedPro" } &&
        classDef.methods.any { it.name == "isSubscribedToPro" }
    },
)

/**
 * HasProFeatures — a83.getHasProFeatures(a83)Z — the master premium gate.
 *
 * Calls: getPurchasedPro → getPurchasedUpgradeNoAdsToPro →
 *        hasUnlockedPurchaseFromPromo (×2) → isSubscribedToPro.
 * Returns true → unlocks all pro features, themes, and removes ads.
 *
 * Fingerprint uses classFingerprint (stable method-name anchor) + method name.
 * Stable: @JvmStatic, PUBLIC STATIC, parameter (a83) is the class itself.
 *
 * Smali verified v5.1.9: a83.getHasProFeatures(La83;)Z, .registers 3
 */
val HasProFeaturesFingerprint = Fingerprint(
    classFingerprint = userStateClassFingerprint,
    name = "getHasProFeatures",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)

/**
 * ShowAds — a83.getShowAds(a83)Z — the ads gate.
 *
 * Calls getHasProFeatures first (if pro → return false = no ads).
 * Also checks getPurchasedNoAds and promo unlocks.
 * Belt+suspenders: patching this directly ensures ads are never shown
 * even if called before getHasProFeatures cascades through.
 *
 * Smali verified v5.1.9: a83.getShowAds(La83;)Z, .registers 3
 */
val ShowAdsFingerprint = Fingerprint(
    classFingerprint = userStateClassFingerprint,
    name = "getShowAds",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)

/**
 * PurchasedNoAds — a83.getPurchasedNoAds(a83)Z — no-ads SKU check.
 *
 * Reads jp.D ("no_ads") from the purchases map. Patching to true ensures
 * the no-ads path in getShowAds is always satisfied independently.
 *
 * Smali verified v5.1.9: a83.getPurchasedNoAds(La83;)Z, .registers 2
 */
val PurchasedNoAdsFingerprint = Fingerprint(
    classFingerprint = userStateClassFingerprint,
    name = "getPurchasedNoAds",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)
