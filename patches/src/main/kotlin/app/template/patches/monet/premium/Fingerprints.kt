package app.template.patches.monet.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ---------------------------------------------------------------------------
// Layer 1 — BillingCallbackFingerprint
// ---------------------------------------------------------------------------
// Targets the billing-state write-back method (l(Z)V in La/hp; in 1.0.76,
// previously in La/km; → La/yo; → La/hp; across builds). Class and method
// names are R8-obfuscated and change every build — NOT used as anchors.
//
// This method is the single write-back point for premium state. It:
//   1. Reads/compares the current cached premium boolean (iget-boolean c:Z)
//   2. Emits "MonetBilling" log with "play premium granted" or "play premium revoked"
//   3. Writes p1 (boolean) to field c:Z and conditionally sets f:Z = true
//   4. Persists p1 to "is_premium_cached" in "billing_prefs" SharedPrefs via Editor
//   5. Calls i()V which emits the new boolean into the MutableStateFlow (field g)
//
// Called from two sites:
//   - queryPurchases callback (passes PURCHASED==1 for "premium_unlock" SKU)
//   - license-blob coroutine invokeSuspend (passes result of license validation)
// Both sites can pass false on billing refresh → patch forces p1=true at entry.
//
// Fingerprint anchors (stable, in smali instruction order):
//   string("play premium granted (live answer)")    — unique in entire DEX, developer log msg
//   string("is_premium_cached")                     — SharedPrefs key, never obfuscated
//   methodCall(SharedPreferences$Editor, putBoolean) — stable Android SDK call
//   methodCall(SharedPreferences$Editor, apply)      — stable Android SDK call
//
// "play premium granted (live answer)" appears at exactly one smali instruction
// across the entire classes.dex — confirmed in both 1.0.73 and 1.0.76.
// It is a developer-authored log message tied directly to the billing grant path,
// making it a stronger anchor than any SharedPrefs key (which could be renamed).
//
// Disambiguated from all other methods by:
//   returnType = "V", parameters = listOf("Z")
//   No other public final (Z)V method in the billing class contains this log string.
//
// Smali evidence (1.0.76, La/hp;->l(Z)V, .registers 6):
//   const-string v0, "MonetBilling"
//   if-eqz p1, :L0
//   const-string v1, "play premium granted (live answer)"     ← filter 1
//   invoke-static {v0, v1}, Log;->i(String;String;)I
//   goto :L1
//   :L0
//   const-string v1, "play premium revoked and persisted"
//   ...
//   :L1
//   iput-boolean p1, p0, La/hp;->c:Z
//   ...
//   const-string v1, "billing_prefs"
//   invoke-virtual {v0, v1, v2}, Context;->getSharedPreferences(String;I)SharedPreferences;
//   ...edit()...
//   const-string v1, "is_premium_cached"                      ← filter 2
//   invoke-interface {v0, v1, p1}, Editor;->putBoolean(String;Z)Editor;  ← filter 3
//   ...
//   invoke-interface {v0}, Editor;->apply()V                  ← filter 4
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
// Targets the BillingManager constructor (<init>(Context, s70)V in La/hp; in 1.0.76,
// previously (Context, u50)V in La/yo; in 1.0.73). Class name and second param
// type are R8-obfuscated and change every build — NOT used as anchors.
// The "L" wildcard in parameters absorbs second-param renames across builds.
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
// Fingerprint anchors (stable, in smali instruction order):
//   string("billing_prefs")     — SharedPrefs name, never obfuscated; first string in ctor
//   string("is_premium_cached") — SharedPrefs key read via getBoolean
//   string("license_blob_v1")   — SharedPrefs key read via getString
//
// These three strings appear together in this exact order only in the BillingManager
// constructor across the entire DEX. The constructor also uniquely takes
// (Landroid/content/Context; L) — no other constructor shares this signature
// with all three billing strings.
//
// Smali evidence (1.0.76, La/hp;-><init>(Landroid/content/Context;La/s70;)V, .registers 10):
//   [74]  const-string v0, "billing_prefs"       ← filter 1
//   [79]  invoke-virtual {p1,v0,v1}, Context;->getSharedPreferences(String;I)SharedPreferences;
//   [85]  const-string v3, "is_premium_cached"   ← filter 2
//   [88]  invoke-interface {v2,v3,v1}, SharedPreferences;->getBoolean(String;Z)Z
//   [94]  iput-boolean v2, p0, La/hp;->c:Z       ← patched: forced true at return-void
//   [103] const-string v2, "license_blob_v1"     ← filter 3
//   [108] invoke-interface {v0,v2,v3}, SharedPreferences;->getString(String;String;)String;
//   ...
//   [121] iput-boolean v0, p0, La/hp;->d:Z       ← patched: forced true at return-void
//   [178] iput-object v0, p0, La/hp;->g:La/hi4;  ← StateFlow seeded from c/d HERE (before patch)
//   ...
//   [442] return-void  ← injection point: const/4 v0,0x1 + iput c + iput d + invoke i()V
//                        i()V re-emits isPremium=true into g so UI reads true on first access
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
