package app.template.patches.toxly

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── BillingRepositoryConstructorFingerprint ───────────────────────────────────
// kz.<init>(Context, Lh21;):V — the BillingRepository constructor.
// Initialises both MutableStateFlow fields (kz.e, kz.g) to Boolean.FALSE via
// Lp00;->e(Object):Lac4 and sets up the Play Billing client lifecycle.
//
// Smali (classes/kz.smali, method <init>):
//   .method public constructor <init>(Landroid/content/Context;Lh21;)V
//     .registers 10
//     ...BillingClient setup...
//     sget-object p1, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
//     invoke-static {p1}, Lp00;->e(Ljava/lang/Object;)Lac4;    ← kz.e initialised to FALSE
//     ...
//     invoke-virtual {v4, p1}, Lcz;->d(Lep3;)V
//     return-void                                                ← injection point
//
// We inject just before the first return-void. At that point:
//   p0 = this (Lkz;), p1 = null (overwritten), p2 = Lh21; (may be recycled)
//   v0–v4 are dead (their owning objects already stored to fields).
//   Safe register budget: v0, v1, v2 (well within .registers 10).
//
// Injection writes:
//   kz.e.k(null, Boolean.TRUE)   — unconditional set of the premium StateFlow
//   r5.c(kz.a, true)             — write "is_ad_free"=true to SharedPreferences
//
// Lac4.k(Object, Object):Z semantics: if p1==null, skips the CAS equality check
// and sets the value unconditionally (confirmed from ac4.smali line 2031).
//
// Stable anchors: CONSTRUCTOR (PUBLIC), (Context, Billing listener) param shape,
// string "Please provide a valid listener for purchases updates." (Google Play Billing
// error message — hardcoded in the library, never obfuscated).
internal val BillingRepositoryConstructorFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    filters = listOf(
        string("Please provide a valid listener for purchases updates."),
    ),
)

// ── BillingRepositoryOnPurchasesFingerprint ───────────────────────────────────
// kz.d(List):V — the onQueryPurchasesResponse callback (queryPurchasesAsync result).
// Iterates purchase list, looks for product "toxly_premium_sub", checks purchaseState
// and acknowledged, then calls kz.e.k(null, TRUE/FALSE) + r5.c(ctx, true/false).
// If the list is empty or the product is not found → kz.e.k(null, FALSE) + r5.c(ctx, false).
//
// Smali (classes/kz.smali, method d):
//   .method public final d(Ljava/util/List;)V
//     .registers 12
//     iget-object v0, p0, Lkz;->a:Landroid/content/Context;
//     iget-object v1, p0, Lkz;->e:Lac4;
//     const-string v2, "toxly_premium_sub"
//     ...iterates list, checks purchaseState + "acknowledged" + "purchaseToken"...
//     :goto_cf (not-licensed path):
//       invoke-virtual {v1, v5, p0}, Lac4;->k(...)Z   ← FALSE
//       invoke-static {v0, v4}, Lr5;->c(...)V          ← false
//
// We inject at index 0 (before any list iteration):
//   kz.e.k(null, TRUE)   — unconditionally activate the StateFlow
//   r5.c(kz.a, true)     — write SharedPreferences
//   return-void           — skip the entire list processing body
//
// At method entry: p0=Lkz; p1=List. v0–v11 are free.
//
// Stable anchors: PUBLIC FINAL, List parameter, three product/purchase strings
// ("toxly_premium_sub" = developer product ID, "purchaseState" + "acknowledged" =
// Google Play Billing JSON field names — none are obfuscated by R8).
internal val BillingRepositoryOnPurchasesFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(
        string("toxly_premium_sub"),
        string("purchaseState"),
        string("acknowledged"),
    ),
)
