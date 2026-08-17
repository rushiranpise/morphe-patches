package app.template.patches.messenger.inbox

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import app.template.patches.messenger.misc.messengerSignaturePatch

// Hides the Home and Channels tabs between the active-now tray and the chat list.
//
// Target: classes2/X/2Je → run()V
// (InboxSubtabsItemSupplierImplementation$onSubscribe$1)
//
// run() body (v573):
//   [0] iget-object  v0, p0, A00
//   [1] iget-object  v1, v0, A05:AtomicBoolean
//   [2] const/4      v0, 0x1          ← replace with const/4 v0, 0x0
//   [3] invoke-virtual {v1, v0}, AtomicBoolean;->set(Z)V
//   [4] return-void
//
// Replacing index 2 with 0x0 sets the ready-flag to false → subtabs stay hidden.
//
// Verified against com.facebook.orca 573.0.0.44.88 (classes2/X/2Je.smali).
@Suppress("unused")
val messengerHideInboxSubtabsPatch = bytecodePatch(
    name = "Hide inbox subtabs",
    description = "Hides Home and Channels tabs between active now tray and chats.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        CreateInboxSubTabsFingerprint.method.replaceInstruction(2, "const/4 v0, 0x0")
    }
}
