package app.template.patches.messenger.chatheads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.messenger.misc.messengerSignaturePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY

// Enables chat heads (chat bubbles) for Messenger regardless of device API level
// or RAM class.
//
// On stock Messenger, chat heads are gated by LX/2NA.A00()Z which returns true
// only when API >= 30 AND the device is not low-RAM. Devices that fail either
// check never see the "Open as chat head" option in thread settings or
// notifications.
//
// This patch overrides the gate to always return true, making chat heads
// available on all supported devices unconditionally.
//
// Target: classes2/X/2NA → method public final A00()Z
// Fingerprinted via stable SDK references:
//   • Landroid/os/Build$VERSION;->SDK_INT (SGET)
//   • Landroid/app/ActivityManager;->isLowRamDevice()
//
// Note: Android OS requires API >= 29 for the Bubbles API. This patch only
// removes Messenger's own extra restriction (API >= 30 + non-low-RAM check).
// Devices running Android 9 or below will still not have system bubble support.
//
// Ported from NeonOrbit/ChatHeadEnabler (Xposed module). ChatHeadEnabler hooks
// the same method at runtime via DexFetcher reflection. This patch achieves the
// same result statically at patch time with no runtime library required.
//
// Verified against com.facebook.orca 573.0.0.44.88 (classes2/X/2NA.smali).
@Suppress("unused")
val messengerEnableChatHeadsPatch = bytecodePatch(
    name = "Enable chat heads",
    description = "Enables chat heads (bubbles) for all devices regardless of API level or RAM class.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        BubblesEligibilityGateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        )
    }
}
