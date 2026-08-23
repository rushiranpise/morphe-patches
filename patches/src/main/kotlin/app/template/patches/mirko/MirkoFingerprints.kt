package app.template.patches.mirko

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── Target 1: r4.c.e()Z — SharedPreferences ads/premium gate ────────────────
//
// Smali verified (0.10.0-152, classes.dex):
//   .method public e()Z  (.registers 4)
//   iget-object v0, p0, ->a:SharedPreferences
//   const-string v1, "key_ads_2"
//   const/4 v2, 1          ← default = true = has ads
//   invoke-interface SharedPreferences->getBoolean(String, Z)Z
//   move-result v0
//   return v0
//
// Callers on e()==true (has ads / not premium):
//   r5.c.run() case 2  → shows ad UI callback
//   ViewActivity.A()   → shows interstitial
//   ViewActivity.onResume() → loads ad
//
// Fix: returnEarly(false) = "no ads" = premium.
//
// Stable anchors: string("key_ads_2") uniquely identifies this method across
// ALL versions regardless of class/method renaming by R8. The string is the
// SharedPreferences key authored by the developer — never changes.
// No definingClass used — the class is R8-obfuscated (was z3.c, now r4.c).
internal val AdsStateFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        string("key_ads_2"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)

// ── Target 2: LicenseContentProvider.onCreate()Z — PairIP startup bypass ────
//
// Smali verified (0.10.0-152, classes.dex):
//   .method public onCreate()Z
//   invoke-virtual p0, ->getContext()Context
//   invoke-static {v0}, LicenseClient->checkLicense(Context)V
//   const/4 v0, 1
//   return v0
//
// ContentProviders init before any Activity. onCreate() calls
// LicenseClient.checkLicense() which connects to Play licensing service,
// fails on unsigned APK, and launches LicenseActivity → kills process.
//
// Fix: returnEarly(true) skips checkLicense entirely.
// Stable: definingClass is PairIP's own non-obfuscated SDK class.
internal val PairIpContentProviderFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseContentProvider;",
    name = "onCreate",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
)

// ── Target 3: LicenseResponseHelper.validateResponse(Bundle,String)V ─────────
//
// Smali verified (0.10.0-152): replaced old ResponseValidator, same signature.
// Performs SHA256withRSA verification + throws LicenseCheckException on failure.
// SHA256withRSA string is unique anchor; class name is stable PairIP SDK.
//
// Fix: return-void silently passes all responses.
// Stable: definingClass is PairIP SDK (non-obfuscated) + method name stable.
internal val PairIpValidateResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
)
