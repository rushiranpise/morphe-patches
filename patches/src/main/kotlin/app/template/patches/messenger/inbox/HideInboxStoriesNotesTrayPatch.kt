package app.template.patches.messenger.inbox

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import app.template.patches.messenger.misc.messengerSignaturePatch

// Hides the stories and notes horizontal tray at the top of the inbox.
//
// Target: classes2/X/1mq → method private A00()Z
// The method reads the FriendsInboxUnitKillSwitch MobileConfig flag and returns
// whether to display the friends-inbox / stories tray. Returning false unconditionally
// hides it regardless of the server-side flag value.
//
// Verified against com.facebook.orca 573.0.0.44.88.
@Suppress("unused")
val messengerHideInboxStoriesNotesTrayPatch = bytecodePatch(
    name = "Hide inbox stories and notes tray",
    description = "Hides the stories and notes horizontal tray at the top of the inbox.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        FriendsInboxTrayFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent()
        )
    }
}
