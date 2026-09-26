package app.template.patches.telegram.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.MessageObjectIsMusicFingerprint
import app.template.patches.telegram.signature.telegramSpoofDependency

/**
 * Plays voice notes through the music-player path while preserving
 * Telegram's native Raise-to-Listen / proximity detection.
 *
 * IMPORTANT:
 * MessageObject.isVoice() must NOT be overridden. Telegram's
 * MediaController uses isVoice() to decide whether proximity mode
 * should switch playback to the earpiece.
 */
@Suppress("unused")
val telegramVoiceToMusicPatch = bytecodePatch(
    name = "Voice to music",
    description = "Plays voice notes through the music-player path while preserving Raise-to-Listen.",
    default = true,
) {
    compatibleWith(
        TELEGRAM_COMPATIBILITY,
        TELEGRAM_PLUS_COMPATIBILITY,
        TELEGRAM_WEB_COMPATIBILITY,
    )

    dependsOn(telegramSpoofDependency())

    execute {
        // Keep MessageObject.isVoice() unchanged so Raise-to-Listen continues
        // to recognize voice messages.
        //
        // Only make voice messages qualify for the music-player path.
        MessageObjectIsMusicFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lorg/telegram/messenger/MessageObject;->messageOwner:Lorg/telegram/tgnet/TLRPC${'$'}Message;
                invoke-static { v0 }, Lorg/telegram/messenger/MessageObject;->isVoiceMessage(Lorg/telegram/tgnet/TLRPC${'$'}Message;)Z
                move-result v0
                if-eqz v0, :not_voice
                const/4 v0, 0x1
                return v0
                :not_voice
                nop
            """,
        )
    }
}
