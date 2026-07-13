package app.template.patches.aida64

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AIDA64_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val aida64RemoveAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Stubs all ad loading paths: banner/interstitial loaders, billing callbacks, and ad SDK init.",
    default = true,
) {
    compatibleWith(AIDA64_COMPATIBILITY)

    execute {
        // Stub loadBannerAd — called by showHideAds() when adMode=true
        AIDA64LoadBannerAdFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // Stub loadInterstitialAd — called by showHideAds() when adMode=true
        AIDA64LoadInterstitialAdFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // Stub onPurchasesUpdated — PurchasesUpdatedListener, prevents async billing resets
        AIDA64OnPurchasesUpdatedFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // Stub onQueryPurchasesResponse — this is the root cause:
        // when purchases.size==0 it sets adMode=true + showRemoveAds=true then calls showHideAds()
        // Stubbing prevents adMode from ever being flipped to true via the billing query path
        AIDA64OnQueryPurchasesResponseFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // Stub MobileAdsInitProvider.attachInfo — prevents ad SDK ContentProvider auto-init
        AIDA64MobileAdsInitProviderFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }
    }
}
