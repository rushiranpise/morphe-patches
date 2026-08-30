package app.template.patches.telegram.content

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.ChatActivityForwardMessagesFingerprint

/**
 * Telegram 12.10.1 Rich Message forwarding fix.
 *
 * When the selected message is a Rich Message, force the existing
 * SendMessagesHelper "fromMyName" argument to true. That causes the normal
 * forwarding request to set messages.forwardMessages.drop_author=true.
 *
 * This keeps the original message/content; it only removes the forwarded
 * sender attribution.
 *
 * NOTE: This is intentionally rich-message-only. Therefore ordinary text,
 * photo, video, etc. forwards retain Telegram's normal behavior.
 */
@Suppress("unused")
val telegramHideRichMessageForwardedSenderPatch = bytecodePatch(
    name = "Hide sender name",
    description = "Removes the forwarded channel attribution for Rich Messages while preserving the original message.",
) {
    compatibleWith(
        TELEGRAM_COMPATIBILITY,
        TELEGRAM_WEB_COMPATIBILITY,
        TELEGRAM_PLUS_COMPATIBILITY,
    )

    execute {
        ChatActivityForwardMessagesFingerprint.method.addInstructions(
            0,
            """
                if-eqz p1, :rich_forward_done
                invoke-virtual {p1}, Ljava/util/ArrayList;->isEmpty()Z
                move-result v0
                if-nez v0, :rich_forward_done
                const/4 v0, 0x0
                invoke-virtual {p1, v0}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;
                move-result-object v0
                check-cast v0, Lorg/telegram/messenger/MessageObject;
                iget-object v0, v0, Lorg/telegram/tgnet/TLRPC$Message;->rich_message:Lorg/telegram/tgnet/tl/TL_iv$RichMessage;
                if-eqz v0, :rich_forward_done
                const/4 p2, 0x1
            :rich_forward_done
            """,
        )
    }
}
