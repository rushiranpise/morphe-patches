package app.template.patches.telegram.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.MessagesControllerIsPremiumUserFingerprint
import app.template.patches.telegram.StoriesControllerIsPremiumFingerprint
import app.template.patches.telegram.UserConfigIsPremiumFingerprint

@Suppress("unused")
val telegramPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Telegram Premium features for the current account.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        UserConfigIsPremiumFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        MessagesControllerIsPremiumUserFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        StoriesControllerIsPremiumFingerprint.methodOrNull?.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
