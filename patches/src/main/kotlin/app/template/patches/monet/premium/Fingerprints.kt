package app.template.patches.monet.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ---------------------------------------------------------------------------
// Layer 1 — BillingCallbackFingerprint
// ---------------------------------------------------------------------------
// Targets the billing-state write-back method: (La/km;->k(Z)V in 1.0.67,
// La/yo;->l(Z)V in 1.0.73). Class and method names are R8-obfuscated and
// change every build — they are NOT used as anchors.
//
// This method is the single write-back point for premium state. It:
//   1. Persists p1 (boolean) to "is_premium_cached" in "billing_prefs" SharedPrefs
//   2. Calls the companion i()V which emits the new boolean into the MutableStateFlow
//   3. On true: removes "play_empty_answer_since_v1" key and dispatches billing event
//
// Called from two sites:
//   - queryPurchases callback (passes PURCHASED==1 for "premium_unlock" SKU)
//   - su2.invokeSuspend purchase-verification coroutine
// Both sites can pass false on billing refresh → patch forces p1=true at entry.
//
// Fingerprint anchors (stable, in smali instruction order):
//   string("billing_prefs")                       — SharedPrefs name, never obfuscated
//   methodCall(Context, getSharedPreferences)      — stable Android SDK call
//   string("is_premium_cached")                   — SharedPrefs key, never obfuscated
//   methodCall(SharedPreferences$Editor, putBoolean) — stable Android SDK call
//   methodCall(SharedPreferences$Editor, apply)    — stable Android SDK call
//
// Disambiguated from su2.invokeSuspend by:
//   returnType = "V", parameters = listOf("Z")
//   su2.invokeSuspend returns Object and takes Object — excluded.
//
// Smali evidence (1.0.73, La/yo;->l(Z)V, .registers 6):
//   const-string v1, "billing_prefs"
//   invoke-virtual {v0,v1,v2}, Context;->getSharedPreferences(String;I)SharedPreferences;
//   const-string v1, "is_premium_cached"
//   invoke-interface {v0,v1,p1}, SharedPreferences$Editor;->putBoolean(String;Z)SharedPreferences$Editor;
//   invoke-interface {v0}, SharedPreferences$Editor;->apply()V
object BillingCallbackFingerprint : Fingerprint(
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

// ---------------------------------------------------------------------------
// Layer 2 — BillingManagerConstructorFingerprint
// ---------------------------------------------------------------------------
// Targets the BillingManager constructor: (La/km;-><init>(Context,u10;)V in 1.0.67,
// La/yo;-><init>(Context,u50;)V in 1.0.73). Class name is obfuscated and changes
// per build — NOT used as anchor.
//
// The constructor reads two keys from "billing_prefs" SharedPrefs on startup:
//   - "is_premium_cached" (boolean) → written to field c:Z
//   - "license_blob_v1"  (String)   → non-null → field d:Z = true
// isPremium formula: d || (c && (f || !e))
//
// If "is_premium_cached" was never written true (fresh install, cleared data),
// the StateFlow starts emitting false until the first billing query returns.
// Patching the constructor ensures c=true and d=true from first frame, eliminating
// the startup flash of unlocked-feature gates appearing locked.
//
// Injection: before return-void (instruction index 120 in 1.0.73), inject:
//   const/4 v0, 0x1
//   iput-boolean v0, p0, La/<obf>;->c:Z    ← force is_premium_cached = true
//   iput-boolean v0, p0, La/<obf>;->d:Z    ← force has_license = true
//
// We locate the return-void via findInstructionIndicesReversed(RETURN_VOID)
// and the class field refs via scanning for iput-boolean after the getBoolean call
// matched by the fingerprint filters.
//
// Fingerprint anchors (stable, in smali instruction order):
//   string("billing_prefs")      — SharedPrefs name (index 3 in 1.0.73 constructor)
//   string("is_premium_cached")  — SharedPrefs key  (index 7)
//   string("license_blob_v1")    — SharedPrefs key  (index 13)
//
// First parameter Landroid/content/Context; distinguishes this from su2.invokeSuspend
// (su2 takes Object as first param and has is_premium_cached before billing_prefs).
//
// Smali evidence (1.0.73, La/yo;-><init>(Landroid/content/Context;La/u50;)V, .registers 10):
//   [3]  const-string v0, "billing_prefs"
//   [7]  const-string v3, "is_premium_cached"
//   [8]  invoke-interface {v2,v3,v1}, SharedPreferences;->getBoolean(String;Z)Z
//   [9]  move-result v2
//   [10] iput-boolean v2, p0, La/yo;->c:Z   ← overridden by patch
//   [13] const-string v2, "license_blob_v1"
//   [22] iput-boolean v0, p0, La/yo;->d:Z   ← overridden by patch
//   [120] return-void
object BillingManagerConstructorFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Landroid/content/Context;",
        "L",                              // second param is obfuscated — stable placeholder
    ),
    filters = listOf(
        string("billing_prefs"),
        string("is_premium_cached"),
        string("license_blob_v1"),
    ),
)
