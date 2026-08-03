package app.template.patches.monet.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Targets the BillingManager.setPremiumCached(boolean) method (La/km;->k(Z)V).
//
// This method is the single write-back point for premium state. It:
//   1. Sets the in-memory km.c field (is_premium_cached)
//   2. Persists "is_premium_cached" to "billing_prefs" SharedPreferences
//   3. Calls km.i() which emits the new boolean to the premium StateFlow
//   4. On true: dispatches a billing success event downstream
//
// Called from two sites:
//   - km.a() static billing event handler (after queryPurchases returns "premium_unlock" SKU)
//   - la/l.invokeSuspend() coroutine (purchase verification loop)
// Both sites pass the result of Purchase.getPurchaseState()==1 — so k(false) is called
// whenever no active "premium_unlock" purchase is found on a billing refresh.
//
// The patch forces p1=true at method entry, preventing false write-backs and ensuring
// the StateFlow always emits true, covering all feature gates in e82, fs1, x32, C0540oo.
//
// Smali (La/km;):
//   .method public final k(Z)V
//     iput-boolean p1, p0, La/km;->c:Z           <- sets in-memory flag
//     const-string v1, "billing_prefs"
//     invoke-virtual {...}, Context;->getSharedPreferences(...)
//     const-string v1, "is_premium_cached"
//     invoke-interface {...}, SharedPreferences$Editor;->putBoolean(...)
//     invoke-interface {...}, SharedPreferences$Editor;->apply()V
//     invoke-virtual {p0}, La/km;->i()V           <- emits to StateFlow
//
// Fingerprint uses stable string constants and SDK method calls only.
object SetPremiumCachedFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Z"),
    filters = listOf(
        string("billing_prefs"),
        methodCall(
            definingClass = "Landroid/content/Context;",
            name = "getSharedPreferences",
        ),
        string("is_premium_cached"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences\$Editor;",
            name = "putBoolean",
        ),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences\$Editor;",
            name = "apply",
        ),
    ),
)
