package app.template.patches.monet.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ---------------------------------------------------------------------------
// Layer 1 — BillingCallbackFingerprint
// ---------------------------------------------------------------------------
// Targets the billing-state write-back method (o(Z)V in La/iu; in 1.0.84,
// previously l(Z)V in La/hp; in 1.0.76). Class and method names are
// R8-obfuscated and change every build — NOT used as anchors.
//
// This is the single write-back point for premium state. It:
//   1. Compares the current cached premium boolean in the fk4 billing state holder
//   2. Emits "MonetBilling" log with "play premium granted" or "play premium revoked"
//   3. Writes p1 (boolean) into fk4.b:Z and re-emits via the MutableStateFlow
//   4. Persists p1 to "is_premium_cached" in "billing_prefs" SharedPrefs via Editor
//
// Called from two sites:
//   - queryPurchases callback (passes PURCHASED==1 for "premium_unlock" SKU)
//   - license-blob coroutine invokeSuspend (passes result of license validation)
// Both sites can pass false on billing refresh — patch forces p1=true at entry.
//
// Fingerprint anchors (stable, in smali instruction order):
//   string("play premium granted (live answer)")     — unique in entire DEX, developer log
//   string("is_premium_cached")                      — SharedPrefs key, never obfuscated
//   methodCall(SharedPreferences.Editor, putBoolean) — stable Android SDK call
//   methodCall(SharedPreferences.Editor, apply)      — stable Android SDK call
//
// Confirmed unique across classes.dex in 1.0.76 and 1.0.84.
//
// Smali evidence (1.0.84, La/iu;->o(Z)V):
//   if-eqz p1, :cond_1f
//   const-string v1, "play premium granted (live answer)"            <- filter 1
//   invoke-static {v1, v2}, Log;->i(String;String;)I
//   goto :goto_26
//   :cond_1f
//   const-string v1, "play premium revoked and persisted"
//   ...
//   const-string v3, "is_premium_cached"                             <- filter 2
//   invoke-interface {v2, v3, p1}, Editor;->putBoolean(String;Z)Editor;  <- filter 3
//   ...
//   invoke-interface {v2}, Editor;->apply()V                         <- filter 4
object BillingCallbackFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Z"),
    filters = listOf(
        string("play premium granted (live answer)"),
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
// Targets the BillingManager constructor (<init>(Context, re0)V in La/iu; in 1.0.84,
// previously (Context, s70)V in La/hp; in 1.0.76). Class name and second param
// type are R8-obfuscated and change every build — NOT used as anchors.
// The "L" wildcard in parameters absorbs second-param renames across builds.
//
// Architecture in 1.0.84: premium state is now encapsulated in a billing state
// holder object (La/fk4;) stored in field c of the BillingManager (La/iu;).
// Previously (1.0.76), state was stored as direct Z fields c:Z and d:Z on the
// BillingManager itself. The fk4 object is constructed with:
//   fk4(is_premium_cached:Z, has_license_blob:Z)
// and exposes:
//   fk4.b:Z  = is_premium_cached (from "is_premium_cached" SharedPref)
//   fk4.c:Z  = has_license_blob  (non-null "license_blob_v1" in SharedPrefs)
//   fk4.a()Z = isPremium formula: c || (b && (d || !e))
//   fk4.f    = MutableStateFlow<Boolean> seeded from a() at construction time
//   fk4.b(Z)V = update c field and re-emit into the StateFlow
//
// The patch injects before return-void to:
//   1. Retrieve the fk4 instance stored in the BillingManager's c field (dynamically)
//   2. Set fk4.b = true and fk4.c = true (both premium flags to true)
//   3. Call fk4.b(Z)V with true to re-emit into the MutableStateFlow immediately
//
// Fingerprint anchors (stable, in smali instruction order):
//   string("billing_prefs")     — SharedPrefs name, never obfuscated; appears first in ctor
//   string("is_premium_cached") — SharedPrefs key read via getBoolean
//   string("license_blob_v1")   — SharedPrefs key read via getString
//
// These three strings appear together in this exact order only in the BillingManager
// constructor across the entire DEX.
//
// Smali evidence (1.0.84, La/iu;-><init>(Landroid/content/Context;La/re0;)V):
//   const-string v1, "billing_prefs"          <- filter 1
//   invoke-virtual {p1,v1,v2}, Context;->getSharedPreferences(String;I)SharedPreferences;
//   const-string v3, "is_premium_cached"      <- filter 2
//   invoke-interface {v1,v3,v2}, SharedPreferences;->getBoolean(String;Z)Z
//   move-result v1                             (v1 = is_premium_cached value)
//   const-string v4, "license_blob_v1"        <- filter 3
//   invoke-interface {v3,v4,v5}, SharedPreferences;->getString(String;String;)String;
//   ...
//   invoke-direct {v0, v1, v3}, La/fk4;-><init>(ZZ)V
//   iput-object v0, p0, La/iu;->c:La/fk4;    <- fk4 stored here; found dynamically
//   ...
//   return-void                                <- injection point
object BillingManagerConstructorFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Landroid/content/Context;",
        "L",                              // obfuscated second param — "L" wildcard is stable
    ),
    filters = listOf(
        string("billing_prefs"),
        string("is_premium_cached"),
        string("license_blob_v1"),
    ),
)
