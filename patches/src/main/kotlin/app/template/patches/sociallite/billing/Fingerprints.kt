package app.template.patches.sociallite.billing

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── IsPremiumActiveFingerprint ────────────────────────────────────────────────
//
// Targets: d8.b1.n()Z  (C1087b1.m2394n — isPremiumActive)
//
// Returns true when the user has a valid active subscription. Checked before
// any premium content is shown. Logic:
//   1. C() demo check → true
//   2. o() SharedPref "hasPaid" → false → return false
//   3. w() "subscriptionCancelled" → if false → return true
//      if true → check cancelAtTimestamp vs currentTimeMillis
//
// Smali (classes/d8/b1.smali line 2062):
//   .method public final n()Z
//   .registers 7
//   ...
//   const-string v1, "cancelAtTimestamp"       ← line 2078 — unique string in this method
//   invoke-interface {...}, SharedPreferences;->getLong(String;J)J
//   invoke-static { }, System;->currentTimeMillis()J
//   invoke-interface {...}, SharedPreferences;->getLong(String;J)J  ← second getLong
//
// Fingerprint: string "cancelAtTimestamp" + getLong + currentTimeMillis.
// Uniquely identifies n() among all Z-returning methods in b1.
//
object IsPremiumActiveFingerprint : Fingerprint(
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
    )
)

// ── HasProFeaturesFingerprint ─────────────────────────────────────────────────
//
// Targets: d8.b1.H()Z  (C1087b1.m2350H — hasProFeatures)
//
// The REAL gate for full Pro features. Separate from n() (isPremiumActive):
//   - n() = has any active subscription at all
//   - H() = has the "pro" or "parent" tier specifically
//
// Called from feature screens that require the full Pro plan. Logic:
//   if forceFreeModeDebug → false
//   if D() (test account) → true
//   if n() && (C() || y()=="pro" || y()=="parent") → true
//   else → false
//
// Smali (classes/d8/b1.smali line 920):
//   .method public final H()Z
//   ...
//   const-string v1, "pro"
//   invoke-virtual { v0, v1 }, Object;->equals(Object;)Z
//   const-string v1, "parent"
//   invoke-virtual { v0, v1 }, Object;->equals(Object;)Z
//
// Fingerprint: string "pro" + string "parent" in sequence, uniquely identifies H().
//
object HasProFeaturesFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("pro"),
        string("parent"),
    )
)

// ── SubscriptionTierFingerprint ───────────────────────────────────────────────
//
// Targets: d8.b1.y()Ljava/lang/String;  (C1087b1.m2416y — getSubscriptionTier)
//
// Returns the stored subscription tier string from SharedPreferences.
// Default: "free". Valid values observed: "free", "pro", "parent", "personal".
//
// Used directly in H() for tier-gated feature checks:
//   y().equals("pro")    → individual Pro plan
//   y().equals("parent") → family/parent plan
//
// Smali (classes/d8/b1.smali line 2407):
//   .method public final y()Ljava/lang/String;
//   .registers 4
//   ...
//   const-string v1, "subscriptionTier"
//   const-string v2, "free"
//   invoke-interface { v0, v1, v2 }, SharedPreferences;->getString(String;String;)String;
//
// Fingerprint: string "subscriptionTier" + string "free" + getString — uniquely
// identifies y() among all String-returning methods in b1.
//
object SubscriptionTierFingerprint : Fingerprint(
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
    )
)

// ── EntitlementSnapshotFingerprint ────────────────────────────────────────────
//
// Targets: d8.s.b()Ld8/x;  (C1127s.m2453b — getEntitlementSnapshot)
//
// Returns a RevenueCat EntitlementSnapshot (d8.x):
//   x.a (Z) = hasPaid
//   x.b (String) = purchaseToken / productIdentifier
//   x.c (Z) = willAutoRenew
//
// Called from F0 (server sync) to guard against server downgrading local Pro:
//   if (!serverHasPaid && snapshot.a && snapshot.c) → keep Pro, skip writes
//
// Smali (classes/d8/s.smali line 239):
//   .method public final b()Ld8/x;
//   ...
//   CustomerInfo;->getEntitlements()EntitlementInfos;
//   EntitlementInfos;->getActive()Map;
//   EntitlementInfo;->getProductIdentifier()String;
//   EntitlementInfo;->getWillRenew()Z
//
// d8.x constructor: <init>(String purchaseToken, Z hasPaid, Z willAutoRenew)
//
object EntitlementSnapshotFingerprint : Fingerprint(
    returnType = "Ld8/x;",
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
            name = "getProductIdentifier",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "getWillRenew",
        ),
    )
)
