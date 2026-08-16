package app.template.patches.feem.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FEEM_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Verified against Feem 6.10.0 (versionCode 6010000).
//
// Architecture: Capacitor/Angular hybrid app. Premium logic lives in JS (main.js).
// Ad serving is done by the @capacitor-community/admob native plugin (classes3.dex).
// The JS calls these three @PluginMethod entry-points on the Java AdMob class:
//   - prepareInterstitial() — loads an interstitial ad
//   - showInterstitial()    — displays it
//   - showBanner()          — shows a banner ad
//
// Blocking these three methods at the native layer suppresses all AdMob ads
// regardless of what the JS layer requests. PluginCall.resolve() is called so
// the JS promise resolves cleanly (no error thrown, no retry loop).
//
// The premium (isPro) gate is server-side via backend5.feem.io license response;
// a client-side bypass is not feasible for that feature.
@Suppress("unused")
val feemDisableAdsPatch = bytecodePatch(
    name = "Disable Ads",
    description = "Disables AdMob interstitial and banner ads.",
    default = true,
) {
    compatibleWith(FEEM_COMPATIBILITY)

    execute {
        // Block interstitial loading — prepareInterstitial resolves immediately.
        // JS-side: Js.prepareInterstitial(t) resolves with no ad loaded.
        AdMobPrepareInterstitialFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p1}, Lcom/getcapacitor/PluginCall;->resolve()V
                return-void
            """.trimIndent(),
        )

        // Block interstitial display — showInterstitial resolves immediately.
        // JS-side: Js.showInterstitial() resolves as if an ad was shown.
        AdMobShowInterstitialFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p1}, Lcom/getcapacitor/PluginCall;->resolve()V
                return-void
            """.trimIndent(),
        )

        // Block banner ad display — showBanner resolves immediately.
        // JS-side: Js.showBanner(i) resolves; adMobHeight stays 0.
        AdMobShowBannerFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p1}, Lcom/getcapacitor/PluginCall;->resolve()V
                return-void
            """.trimIndent(),
        )
    }
}
