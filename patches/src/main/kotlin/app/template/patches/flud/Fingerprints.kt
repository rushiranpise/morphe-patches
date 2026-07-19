package app.template.patches.flud

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Layer 1: AdViewHelper.loadAds() (s13.a) — loads both AdMob interstitial and banner.
// Unique: only method in the app containing the AdMob interstitial unit ID.
val AdLoaderFingerprint = Fingerprint(
    strings = listOf("ca-app-pub-8308447967239879/5050482671"),
    returnType = "V",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// Layer 2: AdViewHelper.maybeShowInterstitial() (s13.b) — decides when to show the
// interstitial and calls InterstitialAd.show(). Unique via "interstitial_minimum_triggers".
val InterstitialTriggerFingerprint = Fingerprint(
    strings = listOf("interstitial_minimum_triggers"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/google/android/gms/ads/interstitial/InterstitialAd;",
            name = "show",
        ),
    ),
)

// Layer 3: PairIP LicenseClient.checkLicense(Context) — static entry for license check.
val LicenseClientFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

// Layer 4: PairIP LicenseActivity kill-switch.
val LicenseActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "onCreate",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
