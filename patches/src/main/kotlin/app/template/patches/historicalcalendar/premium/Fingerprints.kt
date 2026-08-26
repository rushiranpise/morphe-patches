package app.template.patches.historicalcalendar.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── Architecture (v7.5.5) ─────────────────────────────────────────────────────
//
// Premium state is managed by a class we call "BillingManager" (f00 in v7.5.5).
// f00 holds:
//   f00.b : Lvr5;  = MutableStateFlow<Boolean>  (written by billing callback)
//   f00.c : Lsu4;  = derived read-only StateFlow (wraps .b via pe9.R)
//
// f00.a()Z = synchronous isPremium getter — reads .c→.b→getValue()→booleanValue()
// f00.b(Z)V = setPremium(boolean) — emits to MutableStateFlow .b
//
// All 14 isPremium gates across the app call f00.a()Z directly, including:
//   xu1.smali (5 call sites) — article/content access gates
//   w.smali   (1 call site)  — main navigation gate
//   g7.smali  (1 call site)  — ApplicationController startup gate
//   f7.smali  (1 call site)  — ApplicationController secondary gate
//   yg5.smali (2 call sites) — offline download gate
//   yn2.smali (1 call site)  — widget/notification gate
//   rg4.smali (1 call site)  — billing result handler
//
// Billing flow: r44 (BillingClient wrapper) → validates purchase productId=="premium"
// AND type=="inapp" (in s07 coroutine Callable) → calls wg5.b(ig5.e, Boolean.TRUE)
// → updates f00.b MutableStateFlow → f00.a() returns true.
//
// On a patched APK billing never completes successfully, so f00.a() always returns false.
// Fix: returnEarly(true) on f00.a() → all 14 gates see isPremium=true immediately.
//
// ── IsPremiumFingerprint ──────────────────────────────────────────────────────
//
// Targets: f00.a()Z  [classes.dex]
//
// Smali verified (v7.5.5, classes.dex, 8 instructions total):
//   .method public final a()Z
//     .registers 1
//     iget-object p0, p0, Lf00;->c:Lsu4;
//     iget-object p0, p0, Lsu4;->z:Lvr5;
//     invoke-virtual { p0 }, Lvr5;->getValue()Ljava/lang/Object;
//     move-result-object p0
//     check-cast p0, Ljava/lang/Boolean;
//     invoke-virtual { p0 }, Ljava/lang/Boolean;->booleanValue()Z
//     move-result p0
//     return p0
//   .end method
//
// Stable anchors (no obfuscated class names used):
//   classFingerprint: string("BillingManager") — developer log tag in f00.<init>
//     Unique across all 12822 smali files; survives class renames because the
//     string is author-written for logging and will not change.
//   Method: returnType=Z, accessFlags=PUBLIC|FINAL, parameters=[]
//   filter: methodCall(Boolean->booleanValue) — always present in isPremium pattern
//   custom: instruction count < 12 — eliminates all coroutine state machines
//     which are much larger (>100 instructions). The real isPremium getter
//     has exactly 8 instructions.
//
internal val IsPremiumClassFingerprint = Fingerprint(
    strings = listOf("BillingManager"),
)

internal val IsPremiumFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    classFingerprint = IsPremiumClassFingerprint,
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "booleanValue",
        ),
    ),
    custom = { method, _ ->
        // f00.a() has exactly 8 instructions. All coroutine state machines
        // that also call booleanValue() have 50+ instructions.
        (method.implementation?.instructions?.count() ?: Int.MAX_VALUE) < 12
    },
)
