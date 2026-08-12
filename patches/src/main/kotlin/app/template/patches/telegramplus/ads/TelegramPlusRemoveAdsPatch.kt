package app.template.patches.telegramplus.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.telegramplus.AdsControllerAdsDisabledFingerprint
import app.template.patches.telegramplus.AdsInstanceLoadAdsFingerprint
import app.template.patches.telegramplus.AdsInstanceLoadNativeAdFingerprint
import app.template.patches.telegram.MessagesControllerIsSponsoredDisabledFingerprint
import app.template.patches.telegram.MessageObjectIsSponsoredFingerprint
import app.template.patches.telegram.VideoAdsLoadFingerprint

@Suppress("unused")
val telegramPlusRemoveAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes Plus banner ads, native ads, Telegram sponsored messages, and video ads.",
) {
    compatibleWith(TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // Plus AdsController.adsDisabled() → true
        AdsControllerAdsDisabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Block Plus ad loading
        AdsInstanceLoadAdsFingerprint.method.addInstructions(0, "return-void")

        // loadNativeAd returns Z in this build — return false (no ad loaded)
        AdsInstanceLoadNativeAdFingerprint.method.apply {
            if (returnType == "Z") {
                addInstructions(0, """
                    const/4 v0, 0x0
                    return v0
                """)
            } else {
                addInstructions(0, "return-void")
            }
        }

        // Telegram sponsored messages
        MessagesControllerIsSponsoredDisabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
        MessageObjectIsSponsoredFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Video ads
        VideoAdsLoadFingerprint.method.addInstructions(0, "return-void")
    }
}
