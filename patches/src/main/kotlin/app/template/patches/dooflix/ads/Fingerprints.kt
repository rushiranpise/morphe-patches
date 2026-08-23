package app.template.patches.dooflix.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── AdMob Banner ─────────────────────────────────────────────────────────────

// Lt8/c; → Lu8/c; in v9.8 — fingerprint is class-agnostic
object AdMobBannerLoadFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;", "Landroid/widget/RelativeLayout;"),
    filters = listOf(
        methodCall(definingClass = "Lcom/google/android/gms/ads/AdView;", name = "<init>"),
        methodCall(definingClass = "Lcom/google/android/gms/ads/BaseAdView;", name = "setAdUnitId"),
        methodCall(definingClass = "Lcom/google/android/gms/ads/BaseAdView;", name = "loadAd"),
    )
)

// ── AdMob Interstitial ────────────────────────────────────────────────────────

// Lt8/s; → Lu8/s; in v9.8
object AdMobInterstitialLoadFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/app/Activity;"),
    filters = listOf(
        methodCall(definingClass = "Lcom/google/android/gms/ads/interstitial/InterstitialAd;", name = "load"),
    )
)

// ── Unity Rewarded Ad ─────────────────────────────────────────────────────────

// Lu8/s; in v9.8
object UnityRewardedAdLoadFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(definingClass = "Lcom/unity3d/ads/UnityAds;", name = "load"),
    )
)

// ── IMA SDK — stable non-obfuscated class names ───────────────────────────────

object ImaBannerAdShowFingerprint : Fingerprint(
    definingClass = "Lcom/imasdk/lib/IMABannerAd;",
    name = "show",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/app/Activity;", "Landroid/view/View;"),
)

object ImaAdsShowFingerprint : Fingerprint(
    definingClass = "Lcom/imasdk/lib/ImaAds;",
    name = "show",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/app/Activity;"),
)

object ImaAdsShowWithCallbackFingerprint : Fingerprint(
    definingClass = "Lcom/imasdk/lib/ImaAds;",
    name = "show",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/app/Activity;", "Lcom/imasdk/lib/ImaAds\$ImaAdCallback;"),
)

object ImaAdsShowWithBoolIntFingerprint : Fingerprint(
    definingClass = "Lcom/imasdk/lib/ImaAds;",
    name = "show",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/app/Activity;", "Z", "I"),
)

object ImaAdsShowLandscapeFingerprint : Fingerprint(
    definingClass = "Lcom/imasdk/lib/ImaAds;",
    name = "showLandscape",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/app/Activity;"),
)

// ImaAds.showLandscape(Activity, Z, I, Callback) — called from RunnableC6717a
// THIS IS THE ACTUAL CULPRIT: startActivity(ImaInterstitialActivity) with 10-sec skip
object ImaAdsShowLandscapeWithCallbackFingerprint : Fingerprint(
    definingClass = "Lcom/imasdk/lib/ImaAds;",
    name = "showLandscape",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(
        "Landroid/app/Activity;",
        "Z",
        "I",
        "Lcom/imasdk/lib/ImaAds\$ImaAdCallback;",
    ),
)

// ImaAds.show(Activity, Z, I, Callback) — completeness (4-param show overload)
object ImaAdsShowWithBoolIntCallbackFingerprint : Fingerprint(
    definingClass = "Lcom/imasdk/lib/ImaAds;",
    name = "show",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(
        "Landroid/app/Activity;",
        "Z",
        "I",
        "Lcom/imasdk/lib/ImaAds\$ImaAdCallback;",
    ),
)

// ── Player Pre-roll (WebView) ─────────────────────────────────────────────────

// v9.7: Lx8/y;  v9.8: Ly8/x;  — only 1 public final (I,String)V in each, no filters needed
// methodOrNull so v9.7 build silently skips the y8/x fingerprint and vice versa
object PlayerPrerollAdTriggerFingerprintV97 : Fingerprint(
    definingClass = "Lx8/y;",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("I", "Ljava/lang/String;"),
)

object PlayerPrerollAdTriggerFingerprintV98 : Fingerprint(
    definingClass = "Ly8/x;",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("I", "Ljava/lang/String;"),
)

// PlayerActivity static (WebView,String,String)V — loads ad URL into WebView
// v9.7 ✓  v9.8 ✓ (PlayerActivity unchanged)
object PlayerWebViewAdLoaderFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(
        "Landroid/webkit/WebView;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
    ),
    filters = listOf(
        methodCall(definingClass = "Landroid/webkit/WebView;", name = "loadUrl"),
    )
)


// ── Player WebView Ad Runnable ────────────────────────────────────────────────
//
// Targets Ly8/i; (RunnableC8595i) ->run()V
// This is the actual ad show runnable. run() does a packed-switch on int field:
//   case 0: setVisibility(VISIBLE) on ad FrameLayout + RelativeLayout → shows ad UI
//   default: calls m2607 (WebView URL loader) → loads 1xbet URL into WebView
// Patching run() with return-void kills both paths.
// Stable: definingClass="Ly8/i;" + name="run" — no filters needed, unique class.
// 0 .catch blocks in run() → returnEarly() is safe.
object PlayerWebViewAdRunnableFingerprint : Fingerprint(
    definingClass = "Ly8/i;",
    name = "run",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── WebView Domain Gate (kills ALL webview ad paths) ─────────────────────────

// Targets PlayerActivity static (String)Z — m2606xb035fd54
// Checks if URL host contains the server-configured webViewDomain (e.g. "1xbet.com")
// Returns true → triggers m2671 → shows ad via Ly8/i; or RunnableC2742e.default
// Returning false kills ALL webview ad paths regardless of how many callers exist
// Stable: only static (String)Z in PlayerActivity with Uri.getHost + String.contains
// 0 .catch blocks → returnEarly() safe
object PlayerWebViewDomainCheckFingerprint : Fingerprint(
    definingClass = "Lcom/dooflixv4/in/videoplayer/PlayerActivity;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(definingClass = "Landroid/net/Uri;", name = "parse"),
        methodCall(definingClass = "Landroid/net/Uri;", name = "getHost"),
        methodCall(definingClass = "Ljava/lang/String;", name = "contains"),
    )
)
