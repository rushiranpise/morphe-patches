package app.template.patches.tivimate

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TIVIMATE_COMPATIBILITY

@Suppress("unused")
val tivimateUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks TiviMate Premium.",
    default = true,
) {
    compatibleWith(TIVIMATE_COMPATIBILITY)

    execute {
        // Gate 1: BillingClient.isPurchased()Z — always return true
        IsPurchasedFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // Gate 2: b()V — skip SHA-256 cert check for re-signed builds (renamed from HyGokf in 5.3.3)
        SignatureCheckFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )
    }
}
