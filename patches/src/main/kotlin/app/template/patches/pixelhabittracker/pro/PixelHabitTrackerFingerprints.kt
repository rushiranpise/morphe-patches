package app.template.patches.pixelhabittracker.pro

import app.morphe.patcher.Fingerprint

// ── PurchaseRepository (lk2) ──────────────────────────────────────────────────
//
// Class rename history:
//   v2.1.1 : hh2   field d: Lw43; (MutableStateFlow)
//   v2.2.2 : lk2   field d: Lk83; (MutableStateFlow — same role, renamed)
//
// Stable structure across versions (verified in v2.2.2 smali, classes.dex):
//   field a : Landroid/content/SharedPreferences; — "billing_prefs" prefs file
//   field c : Z                                   — in-memory pro boolean flag
//   field d : Lk83;                               — MutableStateFlow<Boolean>
//
// Fingerprint strategy: avoid definingClass (changes every update). Use the
// stable string constants "billing_prefs" + "pro_purchased" unique to this
// constructor, and "pro_purchased" unique to the f(Z)V setter. The custom
// predicate pins the method name to guard against false matches in other classes
// that happen to touch SharedPreferences with these keys.

// lk2.<init>(Context)V — reads "billing_prefs"/"pro_purchased" from SharedPrefs
// on construction. We pre-set field c = true before the getBoolean call so the
// in-memory flag is always true from the first tick.
//
// Smali verified (v2.2.2, classes.dex, Llk2;):
//   .method public constructor <init>(Landroid/content/Context;)V
//   const-string v0, "billing_prefs"
//   invoke-virtual {p1,v0,v1}, Context;->getSharedPreferences(String;I)SharedPreferences;
//   ...
//   const-string v2, "pro_purchased"
//   invoke-interface {v0,v2,v1}, SharedPreferences;->getBoolean(String;Z)Z
//   move-result v0
//   iput-boolean v0, p0, Llk2;->c:Z
internal object PurchaseRepositoryConstructorFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("billing_prefs", "pro_purchased"),
    custom = { method, _ -> method.name == "<init>" },
)

// lk2.f(Z)V — pro-state setter. Writes the boolean arg to SharedPrefs then
// emits to the MutableStateFlow via k83.i(null, newValue). We replace the
// body so it always writes true regardless of what the billing client sends.
//
// Smali verified (v2.2.2, classes.dex, Llk2;):
//   .method public final f(Z)V
//   iput-boolean p1, p0, Llk2;->c:Z
//   ... SharedPreferences.edit().putBoolean("pro_purchased", p1).apply()
//   iget-boolean p1, p0, Llk2;->c:Z
//   Boolean.valueOf(p1) → move-result-object p1
//   iget-object p0, p0, Llk2;->d:Lk83;
//   invoke-virtual {p0, v0, p1}, Lk83;->i(Object;Object;)Z   ← compareAndSet
//   return-void
internal object ProStateSetterFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Z"),
    strings = listOf("pro_purchased"),
    custom = { method, _ -> method.name == "f" },
)
