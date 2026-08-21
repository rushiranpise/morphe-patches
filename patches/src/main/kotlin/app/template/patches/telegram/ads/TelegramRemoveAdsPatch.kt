package app.template.patches.telegram.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.telegram.AdsControllerAdsDisabledFingerprint
import app.template.patches.telegram.AdsInstanceLoadAdsFingerprint
import app.template.patches.telegram.AdsInstanceLoadNativeAdFingerprint
import app.template.patches.telegram.ChatActivityAddSponsoredMessagesFingerprint
import app.template.patches.telegram.ChatActivityGetSponsoredMessagesCountFingerprint
import app.template.patches.telegram.MessageObjectIsSponsoredFingerprint
import app.template.patches.telegram.MessagesControllerGetSponsoredMessagesFingerprint
import app.template.patches.telegram.MessagesControllerIsSponsoredDisabledFingerprint
import app.template.patches.telegram.VideoAdsLoadFingerprint

@Suppress("unused")
val telegramRemoveAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes sponsored messages and video ads from all chats and channels. " +
        "On Telegram Plus also blocks native banner and inline ads.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // Block sponsored messages from being injected into chat list
        ChatActivityAddSponsoredMessagesFingerprint.method.addInstructions(0, "return-void")

        // Return 0 count so UI never shows a sponsored message slot
        ChatActivityGetSponsoredMessagesCountFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Report sponsored as disabled at controller level
        MessagesControllerIsSponsoredDisabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Return null from getSponsoredMessages so no data is fetched
        MessagesControllerGetSponsoredMessagesFingerprint.methodOrNull?.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)

        // No message object is ever marked as sponsored
        MessageObjectIsSponsoredFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Prevent video ad preloading
        VideoAdsLoadFingerprint.method.addInstructions(0, "return-void")

        // Plus-only: AdsController.adsDisabled() → true (no-op on messenger/web)
        AdsControllerAdsDisabledFingerprint.methodOrNull?.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Plus-only: block Plus ad loading (no-op on messenger/web)
        AdsInstanceLoadAdsFingerprint.methodOrNull?.addInstructions(0, "return-void")

        // Plus-only: loadNativeAd — returns Z in Plus 12.9.0.1 (no-op on messenger/web)
        AdsInstanceLoadNativeAdFingerprint.methodOrNull?.apply {
            if (returnType == "Z") {
                addInstructions(0, """
                    const/4 v0, 0x0
                    return v0
                """)
            } else {
                addInstructions(0, "return-void")
            }
        }
    }
}
