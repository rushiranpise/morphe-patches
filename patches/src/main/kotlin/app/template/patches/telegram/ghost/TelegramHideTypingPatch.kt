package app.template.patches.telegram.ghost

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.telegram.PlusSendTypingFingerprint

@Suppress("unused")
val telegramHideTypingPatch = bytecodePatch(
    name = "Hide typing indicator",
    description = "Hides your typing indicator from other users in all chats. " +
        "On Telegram Plus also silences the controller-level sendTyping dispatcher.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // needSendTyping()V — UI layer: called by ChatActivityEnterView when the user types.
        // Silencing all implementations prevents the typing TL request from being dispatched.
        Fingerprint(
            name = "needSendTyping",
            returnType = "V",
            parameters = listOf(),
        ).matchAllOrNull()?.forEach { match ->
            if (match.method.implementation != null) {
                match.method.addInstructions(0, "return-void")
            }
        }

        // Plus-only: MessagesController.sendTyping(JJII)Z — controller dispatch layer.
        // Return false = not sent. (no-op on messenger/web where this signature doesn't exist)
        PlusSendTypingFingerprint.methodOrNull?.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
    }
}
