package app.template.patches.telegram.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.SendScreenshotMessageSecretFingerprint
import app.template.patches.telegram.SendScreenshotMessageUserFingerprint

@Suppress("unused")
val telegramAntiScreenshotNotificationPatch = bytecodePatch(
    name = "Anti-screenshot notification",
    description = "Blocks screenshot notifications from being sent to the other user.",
    default = true,
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // Suppress screenshot notification in regular chats
        SendScreenshotMessageUserFingerprint.method.addInstructions(0, "return-void")

        // Suppress screenshot notification in secret chats
        SendScreenshotMessageSecretFingerprint.method.addInstructions(0, "return-void")
    }
}
