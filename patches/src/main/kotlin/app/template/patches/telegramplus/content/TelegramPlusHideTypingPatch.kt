package app.template.patches.telegramplus.content

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.telegramplus.PlusSendTypingFingerprint

@Suppress("unused")
val telegramPlusHideTypingPatch = bytecodePatch(
    name = "Hide typing indicator",
    description = "Prevents typing status from being sent in Telegram Plus.",
) {
    compatibleWith(TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // Plus has sendTyping(JJII)Z at the MessagesController dispatch layer.
        // Return false = not sent. Also apply needSendTyping matchAll for the UI layer.
        PlusSendTypingFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // UI layer: needSendTyping()V matchAll (same as Web patch)
        Fingerprint(
            name = "needSendTyping",
            returnType = "V",
            parameters = listOf(),
        ).matchAllOrNull()?.forEach { match ->
            if (match.method.implementation != null) {
                match.method.addInstructions(0, "return-void")
            }
        }
    }
}
