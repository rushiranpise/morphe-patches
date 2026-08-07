package app.template.patches.telegram.ghost

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY

@Suppress("unused")
val telegramHideTypingPatch = bytecodePatch(
    name = "Hide typing indicator",
    description = "Hides your typing indicator from other users in all chats.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // needSendTyping()V is the delegate method called by ChatActivityEnterView
        // when the user types. Silencing all implementations prevents the typing
        // TL request from being dispatched — no sendRequestInternal hacks needed.
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
