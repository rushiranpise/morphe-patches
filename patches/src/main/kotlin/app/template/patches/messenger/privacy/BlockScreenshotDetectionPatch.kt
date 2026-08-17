package app.template.patches.messenger.privacy

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.messenger.misc.messengerSignaturePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY

// Prevents Messenger from notifying other participants when you take a screenshot
// or screen recording in a conversation.
//
// When a screenshot is detected, ScreenshotContentObserver.onChange fires, reads
// the new file from MediaStore, and dispatches a "screenshot taken" event to all
// registered listeners. Those listeners send a notification to the other participant
// ("[User] took a screenshot") and display an in-app banner.
//
// Patching onChange → return-void at index 0 silences the entire detection and
// notification pipeline before any event is dispatched.
//
// Note: this patch alone does NOT remove the FLAG_SECURE window restriction
// (which prevents OS-level screen recording). For that, also apply
// AllowScreenCapturePatch, which patches the observer registration root.
// Together they fully disable both the restriction and the detection notification.
//
// Ported from MessengerAMD v1.0.9 "Block photo/video recording detection" feature.
//
// Verified against com.facebook.orca 573.0.0.44.88.
@Suppress("unused")
val messengerBlockScreenshotDetectionPatch = bytecodePatch(
    name = "Block screenshot detection",
    description = "Prevents Messenger from notifying other participants when you take a screenshot or screen recording.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        ScreenshotOnChangeFingerprint.method.replaceInstruction(0, "return-void")
    }
}
