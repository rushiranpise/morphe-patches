package app.template.patches.shareit

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SHAREIT_COMPATIBILITY

@Suppress("unused")
val shareitUnlockLifetimePatch = bytecodePatch(
    name = "Unlock Lifetime",
    description = "Unlocks SHAREit lifetime premium."
) {
    compatibleWith(SHAREIT_COMPATIBILITY)

    execute {
        // Layer 1: main isPurchased() gate in PurchaseManager (hepler/b.d()Z)
        PurchaseManagerIsPurchasedFingerprint
            .match(classDefBy(PurchaseManagerIsPurchasedFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    const/4 p0, 0x1
                    return p0
                    """.trimIndent()
                )
            }

        // Layer 2: per-SKU purchase check (ank.a(String)Z)
        AnkSkuPurchasedFingerprint
            .match(classDefBy(AnkSkuPurchasedFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    const/4 p1, 0x1
                    return p1
                    """.trimIndent()
                )
            }
    }
}
