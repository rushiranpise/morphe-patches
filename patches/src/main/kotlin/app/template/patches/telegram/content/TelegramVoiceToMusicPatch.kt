package app.template.patches.telegram.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.MessageObjectIsMusicFingerprint
import app.template.patches.telegram.MessageObjectIsVoiceFingerprint

// ════════════════════════════════════════════════════════════════════════════════
// Strategy
// ════════════════════════════════════════════════════════════════════════════════
//
// isVoice() → return false:
//   The voice note player UI checks isVoice() to decide whether to show the
//   waveform/speed controls. Returning false makes the app skip that branch.
//   isVoiceOnce() calls isVoice() internally — it will also return false, so
//   view-once logic is also suppressed for voice notes (use with AntiDisappearing).
//
// isMusic() → prepend isVoiceMessage() check:
//   isMusic() returns true for documents tagged as music files. We inject a
//   check at the top: if isVoiceMessage(messageOwner) is true, return true.
//   This makes voice notes appear in the full music player instead.
//
// Net effect: voice notes play in the music player with seek bar, speed control
// and background playback — same as the official Telegram Premium feature.
// ════════════════════════════════════════════════════════════════════════════════

@Suppress("unused")
val telegramVoiceToMusicPatch = bytecodePatch(
    name = "Voice to music",
    description = "Plays voice notes in the full music player with seek bar and background playback.",
    default = true,
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // isVoice() → always false: stops the voice-note UI from rendering
        MessageObjectIsVoiceFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // isMusic() → return true if isVoiceMessage(messageOwner) is true
        // Injected at index 0, before the original isMusicMessage() check.
        // Uses p0 (this), no label needed — falls through on false.
        MessageObjectIsMusicFingerprint.method.addInstructions(0, """
            iget-object v0, p0, Lorg/telegram/messenger/MessageObject;->messageOwner:Lorg/telegram/tgnet/TLRPC${'$'}Message;
            invoke-static { v0 }, Lorg/telegram/messenger/MessageObject;->isVoiceMessage(Lorg/telegram/tgnet/TLRPC${'$'}Message;)Z
            move-result v0
            if-eqz v0, :not_voice
            const/4 v0, 0x1
            return v0
            :not_voice
            nop
        """)
    }
}
