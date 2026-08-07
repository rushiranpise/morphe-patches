package app.template.patches.relink.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── re-Link v2.0.13 — premium fingerprints ───────────────────────────────────
//
// Billing: Google Play Billing (BILLING permission present, product "relink_2_unlimited")
// No RevenueCat, no Pairip, no native libs.
//
// License model:
//   g2/e = LicenseManager (Hilt-injected singleton)
//     field b:SharedPreferences — persists "host_license" / "play_store_license"
//     field c:u4/M — SharedFlow<g2/h>  (internal state pair emitter)
//     field d:x4/v — StateFlow<g2/h>   (UI-observable license pair)
//
//   g2/a = LicenseState sealed class
//     g2/a$e = Licensed(key:String) — the "purchased" state
//     g2/a$b = NotLicensed (singleton)
//     g2/a$c = Error/Pending
//     g2/a$d = Loading (singleton)
//     g2/a$f = Unlicensed/Paywall (singleton)
//
//   g2/h = LicensePair(current:g2/a, previous:g2/a) — StateFlow value type
//   g2/g.a(g2/h) → g2/a — extracts the effective LicenseState from a pair
//     called by g2/e.x()Z and by the UI composable gating logic
//
//   g2/e.A(g2/a)V — posts a new LicenseState to the SharedFlow/StateFlow
//     called on every purchase/restore/check result
//
//   g2/e.x()Z — sync boolean: reads StateFlow → g2/g.a() → instanceof g2/a$e
//     called from ReLinkService coordinator (relink/c.smali)
//
// Feature gating (all paths):
//   UI path: StateFlow<g2/h> → g2/g.a() → instanceof g2/a$e → show/hide features
//            AbstractC3020b.C(g2/a, ...) checks instanceof at every composable render
//   Service path: g2/e.x()Z called from relink/c for background operations
//
// PATCH STRATEGY — two layers:
//
//   Layer 1 — g2/e.A(g2/a)V (LicenseStateUpdater):
//     Every state transition calls A(). Prepend construction of g2/a$e("relink_2_unlimited")
//     into p1 before the existing logging + SharedFlow emit runs.
//     Result: StateFlow ALWAYS carries a Licensed state → UI always shows premium.
//     Stable anchor: "PlayStore License changed: was " (unique, 1 match, 9,372 smali files)
//
//   Layer 2 — g2/e.x()Z (IsPurchasedSync):
//     Service sync check. Found via classDef of Layer 1 (proven pattern).
//     clearBody + return true — 2 instructions.
//     x()Z is the only PUBLIC FINAL ()Z method in g2/e (verified).
//
// Stable anchors (zero obfuscated identifiers):
//   "PlayStore License changed: was " — kotlinc log literal, 1 match
//   "relink_2_unlimited"              — product ID, in g2/e clinit (2 occurrences, same file)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * LicenseStateUpdater — g2/e.A(g2/a)V in v2.0.13.
 *
 * Called on every license state transition. Prepend construction of
 * g2/a$e("relink_2_unlimited") into p1 so every emitted state is Licensed.
 *
 * Stable anchor: "PlayStore License changed: was " — unique in codebase.
 * Parameters: omitted (Continuation obfuscation lesson) — strings alone sufficient.
 *
 * Smali verified v2.0.13: g2/e.A(Lg2/a;)V, .registers 11
 *   p0=v9 (this), p1=v10 (LicenseState param)
 *   v0..v8 are free scratch registers
 *   Prepend: new-instance p1 + const-string v0 + invoke-direct → p1 = Licensed
 *   Original body then logs and emits the (now Licensed) p1 unchanged.
 */
val LicenseStateUpdaterFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    strings = listOf("PlayStore License changed: was "),
)

/**
 * IsPurchasedSync — g2/e.x()Z in v2.0.13.
 *
 * Sync boolean check used by the service coordinator. The only PUBLIC FINAL
 * ()Z method in the LicenseManager class (verified by smali scan).
 * Found via classDef of LicenseStateUpdaterFingerprint (proven pattern).
 *
 * Patched: clearBody + const/4 v0, 0x1 + return v0 → always returns true.
 *
 * Smali verified v2.0.13: g2/e.x()Z, .registers 2
 *   reads d:Lx4/v → getValue() → cast g2/h → g2/g.a() → instanceof g2/a$e → return Z
 */
val IsPurchasedSyncFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
