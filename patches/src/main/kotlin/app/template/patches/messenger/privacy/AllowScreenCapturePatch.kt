package app.template.patches.messenger.privacy

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.messenger.misc.messengerSignaturePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY

// Allows screen recording and screenshots in all Messenger conversations,
// including end-to-end encrypted (secret) ones.
//
// Messenger uses two mechanisms to block screen capture in E2EE conversations:
//
//  1. Window.addFlags(FLAG_SECURE / 0x2000) — set via the Cp0 lambda chain,
//     which is triggered through the ScreenshotContentObserver registration path.
//
//  2. ScreenshotContentObserver — registered by 9Sa.A00()V against
//     MediaStore.Images.EXTERNAL_CONTENT_URI. When triggered, it dispatches
//     a "screenshot taken" event and triggers the FLAG_SECURE enforcement.
//
// Root fix: patch 9Sa.A00()V → return-void at index 0.
//   The ContentObserver is never registered, so:
//   • FLAG_SECURE is never applied to the conversation window
//   • Screenshot/recording events are never dispatched to listeners
//
// Defense-in-depth: patch ScreenshotContentObserver.onChange → return-void.
//   If the observer is registered through any other path, the callback is silenced.
//
// Ported from MessengerAMD v1.0.9 "Allow screen capture/recording" feature.
// AMD achieves the same result by replacing 9Sa and Cp0 with stub classes.
// Our approach patches the specific methods with stable fingerprints instead.
//
// Verified against com.facebook.orca 573.0.0.44.88.
@Suppress("unused")
val messengerAllowScreenCapturePatch = bytecodePatch(
    name = "Allow screen capture",
    description = "Allows screenshots and screen recording in all conversations including end-to-end encrypted ones.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        // 1. Stop ContentObserver registration — root fix
        ScreenshotObserverRegistrationFingerprint.method.replaceInstruction(0, "return-void")

        // 2. Silence onChange callback — defence-in-depth
        ScreenshotOnChangeFingerprint.method.replaceInstruction(0, "return-void")
    }
}
