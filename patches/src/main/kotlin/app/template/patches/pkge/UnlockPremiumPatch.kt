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
        // Forces PremiumPurchasedStatusBuffer to always emit Purchased
        premiumStatusSendFingerprint.method.addInstruction(0, "const/4 p1, 0x1")

        // Forces SubscriptionsHelperImpl.isPremiumPurchased(AdaptyProfile) to always return true
        // Covers auto-update and all direct isPremiumPurchased() calls bypassing the buffer
        isPremiumPurchasedAdaptyFingerprint.method.addInstruction(0, "const/4 p1, 0x1")
        isPremiumPurchasedAdaptyFingerprint.method.addInstruction(1, "return p1")
    }
}
