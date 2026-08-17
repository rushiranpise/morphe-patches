package app.template.patches.messenger.linkhandling

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import app.template.patches.messenger.misc.messengerSignaturePatch

// Forces all links to open in the system browser instead of the in-app browser.
//
// Target: classes9/X/KU2 → method public A0H(Uri, FbUserSession)Z
// The method returns true to use the in-app browser, false to skip it.
// Inserting "const/4 v0, 0x0 / return v0" at index 0 always returns false,
// making Messenger hand every URL off to the default external browser.
//
// Verified against com.facebook.orca 573.0.0.44.88 (classes9/X/KU2.smali).
@Suppress("unused")
val messengerOpenLinksExternallyPatch = bytecodePatch(
    name = "Open links externally",
    description = "Opens links in the external browser instead of the in-app browser.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        ShouldOpenInAppBrowserFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )
    }
}
