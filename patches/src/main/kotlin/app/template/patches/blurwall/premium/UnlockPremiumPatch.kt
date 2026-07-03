package app.template.patches.blurwall.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLURWALL_COMPATIBILITY

// BlurWall v2.9.2 — RevenueCat onReceived bypass.
//
// onReceived() checks activeSubscriptions/nonSubscriptionTransactions and writes
// the result to SharedPreferences key "start_blur" via an obfuscated unicode method.
// Instead of calling the unicode method (which may fail in patcher), we return-void
// early so no FALSE is ever written. The liccheck patch (DisableLicCheckPatch) then
// intercepts the SharedPreferences read and returns TRUE for "start_blur".

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlock Premium Features in app.",
) {
    compatibleWith(BLURWALL_COMPATIBILITY)

    execute {
        OnReceivedFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )
    }
}
