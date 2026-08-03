package app.template.patches.allreader

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// All Reader v3.2.4 uses the newer LicenseClient-based Pairip variant.
// There is no VMRunner, SignatureCheck, or StartupLauncher — those classes
// are absent from the DEX. The entire license check flow runs through
// LicenseClient and its nested LicenseCheckState enum.
//
// License flow (smali verified, classes.dex):
//
//   com.pairip.application.Application.attachBaseContext()
//     └─ LicenseClient.checkLicense(Context)          [static entry point]
//           └─ new LicenseClient(context)
//           └─ LicenseClient.initializeLicenseCheck()
//                 ├─ ordinal 0 (CHECK_REQUIRED)         → connectToLicensingService()
//                 │       → IPC to Play Store LVL
//                 │       → processResponse(responseCode, bundle)
//                 │             ├─ 0 (LICENSED)         → validateResponse()
//                 │             │       → LicenseResponseHelper.validateResponse()
//                 │             │         (RSA SHA256 JWS signature check)
//                 │             │       → scheduleRepeatedLicenseCheck / FULL_CHECK_OK
//                 │             ├─ 2 (NOT_LICENSED)     → startPaywallActivity()
//                 │             └─ other                → handleError()
//                 │                     └─ startErrorDialogActivity() (blocking)
//                 ├─ ordinal 1 (FULL_CHECK_OK)          → re-validateResponse()
//                 └─ ordinal 4 (REPEATED_CHECK_REQUIRED)→ connectToLicensingService()
//
// Bypass strategy:
//   initializeLicenseCheck() has a try-catch on LicenseCheckException. When the
//   method exits normally (return-void at the top), the state remains in its
//   initial value (CHECK_REQUIRED, ordinal 0), but none of the failure paths
//   (connectToLicensingService → NOT_LICENSED → paywall / error dialog) are ever
//   reached — the method simply does nothing and the app continues running.
//
// Single fingerprint, non-obfuscated Pairip SDK class/method — stable.
//
// Smali verified (v3.2.4, classes.dex):
//   .method public initializeLicenseCheck()V
//   .catch Lcom/pairip/licensecheck/LicenseCheckException; { :L1 .. :L2 } :L3
//   .registers 4
//   sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:...
//   invoke-virtual {v0}, LicenseCheckState;->ordinal()I
//   move-result v0
//   const/4 v1, 0
//   if-eqz v0, :L4       ← CHECK_REQUIRED → connect
//   const/4 v2, 1
//   if-eq v0, v2, :L1    ← FULL_CHECK_OK → re-validate
//   const/4 v2, 4
//   if-eq v0, v2, :L0    ← REPEATED → connect again
//   return-void           ← LOCAL_CHECK_OK / LOCAL_CHECK_REPORTED → no-op
internal object InitializeLicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    parameters = emptyList(),
)


// ─── Premium gate ────────────────────────────────────────────────────────────

// UtilsRepository.isPremiumUser()Z
// Non-obfuscated class in the app's own package — stable across updates.
// Simply reads a boolean field; returnEarly(true) covers all callers.
//
// Smali verified (v3.2.4, classes.dex):
//   .method public final isPremiumUser()Z
//   iget-boolean v0, p0, Lalldocumentreader/.../UtilsRepository;->isPremiumUser:Z
//   return v0
val IsPremiumUserFingerprint = Fingerprint(
    definingClass = "Lalldocumentreader/office/viewer/filereader/pdfviewer/respositories/UtilsRepository;",
    name = "isPremiumUser",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ─── SharedPreferences purchase gates ────────────────────────────────────────

// k.c.b()Z — reads SharedPref key "purchase"; false by default → paywall shown
//
// Smali verified (v3.2.4, classes.dex):
//   .method public final b()Z
//   const-string v0, "purchase"
//   const/4 v1, 0
//   iget-object v2, p0, Lk/c;->b:Landroid/content/SharedPreferences;
//   invoke-interface {v2, v0, v1}, SharedPreferences;->getBoolean(String;Z)Z
//   move-result v0 / return v0
val IsPurchasedFingerprint = Fingerprint(
    definingClass = "Lk/c;",
    name = "b",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("purchase"),
)

// k.c.a()Z — reads SharedPref key "firstLaunch"; true = first run → shows
// LanguageActivity onboarding. Returning false skips the onboarding screen.
//
// Smali verified (v3.2.4, classes.dex):
//   .method public final a()Z
//   const-string v0, "firstLaunch"
//   const/4 v1, 1
//   iget-object v2, p0, Lk/c;->b:Landroid/content/SharedPreferences;
//   invoke-interface {v2, v0, v1}, SharedPreferences;->getBoolean(String;Z)Z
//   move-result v0 / return v0
val IsFirstLaunchFingerprint = Fingerprint(
    definingClass = "Lk/c;",
    name = "a",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("firstLaunch"),
)

// ─── Interstitial ad gates ────────────────────────────────────────────────────

// InterstitialPreloadManager.showInterAd(Activity, c7/a) — static
// Shows a full-screen interstitial if not premium and an ad is ready.
// Strings "<this>" and "callBack" are Kotlin null-check annotation constants —
// stable as long as Kotlin compiles this class.
//
// Smali verified (v3.2.4, classes.dex):
//   .method public final static showInterAd(Landroid/app/Activity;Lc7/a;)V
//   const-string v0, "<this>"
//   ...
//   const-string v0, "callBack"
val ShowInterAdFingerprint = Fingerprint(
    definingClass = "Lcom/reader/office/ad/InterstitialPreloadManager;",
    name = "showInterAd",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;", "Lc7/a;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    strings = listOf("<this>", "callBack"),
)

// InterstitialPreloadManager.showPreloadTimeInter(Activity, c7/a) — instance
// Time-based interstitial variant; same callback interface pattern.
//
// Smali verified (v3.2.4, classes.dex):
//   .method public final showPreloadTimeInter(Landroid/app/Activity;Lc7/a;)V
//   const-string v0, "<this>"
//   ...
//   const-string v0, "callBack"
val ShowPreloadTimeInterFingerprint = Fingerprint(
    definingClass = "Lcom/reader/office/ad/InterstitialPreloadManager;",
    name = "showPreloadTimeInter",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;", "Lc7/a;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("<this>", "callBack"),
)

// ─── Native / banner ad loader ────────────────────────────────────────────────

// Z1.a.b(RemoteModel, Z1.b) — loads and inflates a native ad into a
// MaterialCardView container (field "e") and a FrameLayout (field "f").
// The string "default_ad_format" is a Kotlin null-check label for the second
// parameter; stable as long as this class compiles with Kotlin.
//
// NOTE: This target moved between versions:
//   v3.2.3: LK8/j;->u(RemoteModel, Z1/a)   field "c" for the card view
//   v3.2.4: LZ1/a;->b(RemoteModel, Z1/b)   field "e" for the card view
//
// Smali verified (v3.2.4, classes.dex, LZ1/a;->b):
//   .method public b(Lcom/admobads/data/RemoteModel;LZ1/b;)V
//   const-string v0, "default_ad_format"
//   invoke-static {p2, v0}, Ld7/k;->e(Object;String;)V
//   sget-boolean v0, LZ1/a;->i:Z
//   const/16 v1, 8                          ← GONE = 8
//   iget-object v2, p0, LZ1/a;->e:Object;  ← MaterialCardView
//   check-cast v2, MaterialCardView
//   if-eqz v0, :L0
//   invoke-virtual {v2, v1}, View;->setVisibility(I)V
//   return-void
val LoadNativeAdFingerprint = Fingerprint(
    definingClass = "LZ1/a;",
    name = "b",
    returnType = "V",
    parameters = listOf("Lcom/admobads/data/RemoteModel;", "LZ1/b;"),
    accessFlags = listOf(AccessFlags.PUBLIC),
    strings = listOf("default_ad_format"),
)
