package app.template.patches.pkge

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PKGE_COMPATIBILITY

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlock Premium features in app.",
) {
    compatibleWith(PKGE_COMPATIBILITY)

    execute {
        // p1 is the boolean argument (true = purchased, false = not purchased).
        // Overwriting it with 1 before the branch means the buffer always emits PremiumStatus.Purchased.
        premiumStatusSendFingerprint.method.addInstruction(0, "const/4 p1, 0x1")
    }
}
