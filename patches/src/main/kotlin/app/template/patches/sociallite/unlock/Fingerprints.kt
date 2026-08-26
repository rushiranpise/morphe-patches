package app.template.patches.sociallite.unlock

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── IsPremiumActiveFingerprint ────────────────────────────────────────────────
//
// Targets: ib.l1.n()Z  [classes.dex]
//   (was d8.b1.n()Z in v2.0.0.45)
//
// Synchronous isPremiumActive() gate. Reads SharedPrefs "hasPaid" boolean.
// If cancelled subscription: also checks SharedPrefs "cancelAtTimestamp" vs
// System.currentTimeMillis() to allow grace period.
//
// Smali verified (v2.0.0.59, ib/l1.smali, method n()Z):
//   const-string "cancelAtTimestamp"       ← filter[0]
//   SharedPreferences->getLong(String,J)J  ← filter[1]
//   System->currentTimeMillis()J           ← filter[2]
//
// Stable anchors: developer-authored string "cancelAtTimestamp" + SDK method
// names. No obfuscated class names. Survived v2.0.0.45 → v2.0.0.59 rename ✓
//
internal object IsPremiumActiveFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("cancelAtTimestamp"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getLong",
        ),
        methodCall(
            definingClass = "Ljava/lang/System;",
            name = "currentTimeMillis",
        ),
    ),
)

// ── SubscriptionTierFingerprint ───────────────────────────────────────────────
//
// Targets: ib.l1.A()String  [classes.dex]
//   (was d8.b1.y()String in v2.0.0.45)
//
// Returns the subscription tier string from SharedPrefs (default: "free").
// Values: "free" | "pro" | "parent" | "personal".
// Read by HasProFeatures to decide if Pro features are unlocked.
//
// Smali verified (v2.0.0.59, ib/l1.smali, method A()Ljava/lang/String;):
//   const-string "subscriptionTier"            ← filter[0]
//   const-string "free"                         ← filter[1]
//   SharedPreferences->getString(String,String) ← filter[2]
//
// Stable anchors: developer-authored strings "subscriptionTier" and "free".
//
internal object SubscriptionTierFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("subscriptionTier"),
        string("free"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getString",
        ),
    ),
)

// ── HasProFeaturesFingerprint ─────────────────────────────────────────────────
//
// Targets: ib.l1.L()Z  [classes.dex]
//   (was d8.b1.H()Z in v2.0.0.45)
//
// The REAL gate for all Pro features. Logic:
//   if forceFreeModeDebug → false
//   if demo account → true
//   if isPremiumActive() && (isCohortC() || tier=="pro" || tier=="parent") → true
//   else → false
//
// Smali verified (v2.0.0.59, ib/l1.smali, method L()Z):
//   const-string "pro"    ← filter[0]
//   const-string "parent" ← filter[1]
//
// Stable anchors: developer-authored tier strings "pro" and "parent".
//
internal object HasProFeaturesFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("pro"),
        string("parent"),
    ),
)

// ── EntitlementSnapshotFingerprint ────────────────────────────────────────────
//
// Targets: the EntitlementSnapshot builder method  [classes.dex]
//   ib.t.b() in v2.0.0.59 / d8.s.b() in v2.0.0.45
//   Return type is an obfuscated data class — NOT used as anchor.
//
// Builds a snapshot of the RevenueCat entitlement state used by the server-sync
// guard (F0): if server says "free" but snapshot shows active+renewing → keep Pro.
// Without this patch, the 24h server sync overwrites "hasPaid"=false and
// "subscriptionTier"="free" in SharedPrefs, reverting to free on the next launch.
//
// Smali verified (v2.0.0.59, ib/t.smali, method b()):
//   CustomerInfo->getEntitlements()     ← filter[0]  non-obfuscated SDK
//   EntitlementInfos->getActive()       ← filter[1]  non-obfuscated SDK
//   EntitlementInfo->getExpirationDate()← filter[2]  non-obfuscated SDK
//   EntitlementInfo->getProductIdentifier() ← filter[3]
//   EntitlementInfo->getWillRenew()     ← filter[4]
//
// All five RevenueCat SDK method names are non-obfuscated — stable forever.
// The return type (obfuscated snapshot class) is intentionally not declared here:
// the 5-filter chain is globally unique across the entire DEX without it.
//
internal object EntitlementSnapshotFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
            name = "getEntitlements",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
            name = "getActive",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "getExpirationDate",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "getProductIdentifier",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "getWillRenew",
        ),
    ),
)

// ── LicenseCheckFingerprint ───────────────────────────────────────────────────
//
// Targets: com.pairip.licensecheck.LicenseClient.checkLicense(Context)V
//
// Called from com.pairip.application.Application.attachBaseContext().
// After Morphe re-signs the APK the signature doesn't match → LicenseActivity
// starts and blocks the UI.
// Primary fix: manifest android:name swap to SocialLiteApplication (resource patch).
// Secondary fix: no-op checkLicense() in bytecode as belt-and-suspenders.
//
// Stable: LicenseClient is PairIP SDK — class and method names never obfuscated.
//
internal object LicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)
