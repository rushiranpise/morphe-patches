package app.template.patches.networkguru.license

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── Gate 2: w70.k(String) — Play Billing ownership check ─────────────────────
//
// w70.k(String productId) is the direct ownership check called during the
// IntroActivity subscription coroutine. It iterates over w70.c (ArrayList of
// owned Purchase objects), finds purchases whose product list contains productId,
// extracts purchaseState from the Purchase JSON, and returns true if
// purchaseState != 4 (PENDING). Returns false if empty or purchase not found.
//
// This is the REAL subscription gate. Patching returnEarly(true) here causes
// the coroutine (pr case 1) to set pr.v = true → r60(isSubscribed=true) →
// the "buy subscription" prompt is skipped.
//
// Smali (v2.0, Lw70;):
//   .method public final k(Ljava/lang/String;)Z
//     invoke-virtual {p1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
//     iget-object p0, p0, Lw70;->c:Ljava/util/ArrayList;
//     invoke-virtual {p0}, Ljava/util/ArrayList;->size()I
//     ...loop: Purchase.a().contains(productId)...
//     const-string p1, "purchaseState"
//     invoke-virtual {p0,p1,v0}, Lorg/json/JSONObject;->optInt(String;I)I
//     return v0  (1 = PURCHASED)
//
// Fingerprint anchors:
//   returnType = Z, accessFlags = PUBLIC | FINAL, parameters = [String]
//   methodCall(Purchase.a() — product list getter) + string("purchaseState")
val SubscriptionOwnershipCheckFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/android/billingclient/api/Purchase;",
            name = "a",
        ),
        string("purchaseState"),
    ),
)

// ── Gate 3 (secondary): t44.m(Boolean) — billing result StateFlow ────────────
//
// t44.m(Boolean)V writes to t44.f35687m (field `m`), the billing result StateFlow
// read by billing coroutine callbacks (s44/p44). Forcing TRUE here prevents any
// background billing query from overriding the subscribed state.
//
// Smali (v2.0, Lt44;):
//   .method public final m(Ljava/lang/Boolean;)V
//     iget-object p0, p0, Lt44;->m:Lkc5;
//     invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
//     invoke-virtual {p0, v0, p1}, Lkc5;->k(...)Z
//     return-void
val SubscriptionStateFlowSetterFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Boolean;"),
    filters = listOf(
        methodCall(definingClass = "Ljava/lang/Object;", name = "getClass"),
        methodCall(definingClass = "Lkc5;", name = "k"),
    ),
)
