package app.template.patches.feem.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Targets AdMob.showInterstitial(PluginCall) in classes3.dex.
// Anchored on:
//   - stable SDK definingClass  Lcom/getcapacitor/community/admob/interstitial/AdInterstitialExecutor;
//   - stable SDK method name    showInterstitial
//   - stable parameter types    PluginCall + BiConsumer (SDK types, never obfuscated)
// No obfuscated names referenced — these are Capacitor AdMob community plugin
// classes that ship as-is and are never renamed by R8.
object AdMobShowInterstitialFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Lcom/getcapacitor/PluginCall;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/getcapacitor/community/admob/interstitial/AdInterstitialExecutor;",
            name = "showInterstitial",
        ),
    ),
)

// Targets AdMob.prepareInterstitial(PluginCall) in classes3.dex.
// Anchored on the stable prepareInterstitial call in AdInterstitialExecutor.
object AdMobPrepareInterstitialFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Lcom/getcapacitor/PluginCall;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/getcapacitor/community/admob/interstitial/AdInterstitialExecutor;",
            name = "prepareInterstitial",
        ),
    ),
)

// Targets AdMob.showBanner(PluginCall) in classes3.dex.
// Anchored on the stable showBanner call in BannerExecutor.
object AdMobShowBannerFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Lcom/getcapacitor/PluginCall;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/getcapacitor/community/admob/banner/BannerExecutor;",
            name = "showBanner",
        ),
    ),
)
