package app.template.patches.compsuite.billing

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── EvaluatePurchasesFingerprint ──────────────────────────────────────────────
//
// Targets: BillingLibrary.evaluatePurchasesToUnlockMode(List<Purchase>)UnlockMode
//          [classes4.dex — com/gs/complications/suite/utils/BillingLibrary.smali]
//
// Maps the user's active Google Play purchases to a UnlockMode enum value:
//   LIFETIME  → any of: wear_os_toolset_lifetime, toolset_lifetime_v2,
//                        toolset_lifetime_upgrade_v2, gs_complications_suite_unlock
//   YEARLY    → toolset_premium_yearly
//   MONTHLY   → toolset_premium_monthly
//   OLD_SUB   → wear_os_toolset_yearly_subscription, premium_subscription_v2
//   FREE      → (fallback, no matching purchase)
//
// Called from processAndAcknowledgePurchases() after every BillingClient query.
// The result flows into BillingLibrary.unlockMode (StateFlow<UnlockMode>) which
// drives the entire app's premium gate via UnlockMode.isFull():
//   isFull() = true for LIFETIME, YEARLY, MONTHLY, OLD_SUB (all except UNKNOWN/FREE)
//
// Smali evidence (classes4/com/gs/complications/suite/utils/BillingLibrary.smali line 644):
//   .method private final evaluatePurchasesToUnlockMode(Ljava/util/List;)Lcom/gs/complications/suite/utils/UnlockMode;
//   .registers 10
//   ...
//   const-string v0, "wear_os_toolset_lifetime"    ← line 659 — UNIQUE in BillingLibrary
//   ...
//   Purchase;->getProducts()Ljava/util/List;        ← called multiple times
//   ...
//   const-string v6, "toolset_premium_yearly"       ← line 750
//
// Fingerprint: string("wear_os_toolset_lifetime") — appears only at line 659 in the
// entire BillingLibrary class (line 134 is a field declaration, not in a method body).
// Combined with Purchase.getProducts() filter for safety.
// Access flags: PRIVATE FINAL — non-static instance method.
//
object EvaluatePurchasesFingerprint : Fingerprint(
    returnType = "Lcom/gs/complications/suite/utils/UnlockMode;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(
        string("wear_os_toolset_lifetime"),
        methodCall(
            definingClass = "Lcom/android/billingclient/api/Purchase;",
            name = "getProducts",
        ),
        string("toolset_premium_yearly"),
    )
)

// ── FallbackCacheFingerprint ──────────────────────────────────────────────────
//
// Targets: BillingLibrary.fallbackToTimeBoundedCache(UnlockData, UnlockMode)UnlockMode
//          [classes4.dex — com/gs/complications/suite/utils/BillingLibrary.smali]
//
// Called from observeDataStoreCache() when the DataStore has a cached UnlockData.
// Returns the cached UnlockMode if the cache is < 24 hours old, otherwise returns
// the fallback (FREE). This is a secondary path that bypasses BillingClient entirely
// on subsequent launches within 24h of a successful billing query.
//
// Without patching this method, the DataStore cache (which persists across restarts)
// would return FREE if the user never made a purchase — keeping the app in free mode
// even after evaluatePurchasesToUnlockMode() is patched, since the DataStore cache
// path runs independently and can override the billing result.
//
// Smali evidence (classes4/BillingLibrary.smali line 849):
//   .method private final fallbackToTimeBoundedCache(Lcom/gs/complications/suite/utils/UnlockData;Lcom/gs/complications/suite/utils/UnlockMode;)Lcom/gs/complications/suite/utils/UnlockMode;
//   .registers 7
//   ...
//   invoke-static { }, System;->currentTimeMillis()J
//   invoke-virtual { p1 }, UnlockData;->getTime()J      ← UNIQUE — only method reading UnlockData.getTime()
//   ...
//   invoke-static { p0, v2 }, DurationKt;->toDuration(ILkotlin/time/DurationUnit;)J
//
// Fingerprint: UnlockData.getTime() — appears only in this method in BillingLibrary.
// Combined with System.currentTimeMillis + DurationKt.toDuration for safety.
//
object FallbackCacheFingerprint : Fingerprint(
    returnType = "Lcom/gs/complications/suite/utils/UnlockMode;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    parameters = listOf(
        "Lcom/gs/complications/suite/utils/UnlockData;",
        "Lcom/gs/complications/suite/utils/UnlockMode;",
    ),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/System;",
            name = "currentTimeMillis",
        ),
        methodCall(
            definingClass = "Lcom/gs/complications/suite/utils/UnlockData;",
            name = "getTime",
        ),
        methodCall(
            definingClass = "Lkotlin/time/DurationKt;",
            name = "toDuration",
        ),
    )
)
