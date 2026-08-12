package app.template.patches.messenger.inputfield

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import app.template.patches.messenger.misc.messengerSignaturePatch

// Disables the typing indicator so other users cannot see when you are composing.
//
// Target: classes5/X/Ay7 → run()V
// (ConversationTypingContext$sendActiveStateRunnable$1)
//
// The Runnable is posted to a handler each time the composer text changes.
// Replacing instruction[0] with return-void prevents the typing state from
// ever being transmitted.
//
// Verified against com.facebook.orca 573.0.0.44.88 (classes5/X/Ay7.smali).
@Suppress("unused")
val messengerDisableTypingIndicatorPatch = bytecodePatch(
    name = "Disable typing indicator",
    description = "Disables the indicator while typing a message.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        SendTypingIndicatorFingerprint.method.replaceInstruction(0, "return-void")
    }
}
