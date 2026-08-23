package app.template.patches.dooflix.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_DOOFLIX
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

@Suppress("unused")
val dooFlixAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Disables AdMob, Unity, IMA SDK, and WebView pre-roll ads in the player."
) {
    compatibleWith(COMPATIBILITY_DOOFLIX)

    dependsOn(
        spoofSignatureVerificationPatch,
        spoofInstallSourcePatch,
        spoofFirebaseCertHashPatch,
    )

    execute {
        AdMobBannerLoadFingerprint.method.returnEarly()
        AdMobInterstitialLoadFingerprint.method.returnEarly()
        UnityRewardedAdLoadFingerprint.method.returnEarly()

        ImaBannerAdShowFingerprint.method.returnEarly()
        ImaAdsShowFingerprint.method.returnEarly()
        ImaAdsShowWithCallbackFingerprint.method.returnEarly()
        ImaAdsShowWithBoolIntFingerprint.method.returnEarly()
        ImaAdsShowLandscapeFingerprint.method.returnEarly()

        // 4-param overloads with callback: fire onAdCompleted() immediately so video starts
        // p3 = ImaAdCallback; returnEarly() skips it → video never starts (movie logo freeze)
        ImaAdsShowLandscapeWithCallbackFingerprint.method.addInstructions(
            0,
            """
                if-eqz p3, :skip
                invoke-interface {p3}, Lcom/imasdk/lib/ImaAds${"$"}ImaAdCallback;->onAdCompleted()V
                :skip
                return-void
            """.trimIndent()
        )
        ImaAdsShowWithBoolIntCallbackFingerprint.method.addInstructions(
            0,
            """
                if-eqz p3, :skip
                invoke-interface {p3}, Lcom/imasdk/lib/ImaAds${"$"}ImaAdCallback;->onAdCompleted()V
                :skip
                return-void
            """.trimIndent()
        )

        // Preroll trigger — has 4 .catch blocks: use addInstructions(0) not returnEarly()
        // returnEarly() removes instructions → stale exception table → VerifyError at runtime
        PlayerPrerollAdTriggerFingerprintV97.methodOrNull?.addInstructions(0, "return-void")
        PlayerPrerollAdTriggerFingerprintV98.methodOrNull?.addInstructions(0, "return-void")

        // WebView loader — no .catch blocks, returnEarly() is safe
        PlayerWebViewAdLoaderFingerprint.method.returnEarly()

        // Ad show runnable Ly8/i;->run() — kills Ly8/i; path (0 .catch blocks)
        PlayerWebViewAdRunnableFingerprint.method.returnEarly()

        // WebView domain gate — m2606xb035fd54(String)Z in PlayerActivity
        // Always returns false → m2671 never called → kills ALL webview ad paths
        // (both Ly8/i; path AND RunnableC2742e.default token-resolve path)
        PlayerWebViewDomainCheckFingerprint.method.returnEarly(false)

    }
}
