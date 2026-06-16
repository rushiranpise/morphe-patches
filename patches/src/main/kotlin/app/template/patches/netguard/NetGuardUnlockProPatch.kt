package app.template.patches.netguard

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NETGUARD_COMPATIBILITY

@Suppress("unused")
val netGuardUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all pro features in NetGuard.",
    default = true
) {
    compatibleWith(NETGUARD_COMPATIBILITY)

    execute {
        // ── Layer 1: IAB.isPurchased(String, Context) → always true ──────────
        // Checks individual SKU keys (pro, pro1, support1, support2, donation, android.test.purchased).
        runCatching {
            IabIsPurchasedFingerprint
                .match(classDefBy(IabIsPurchasedFingerprint.definingClass!!))
                .method
        }.getOrNull()?.addInstructions(
            0,
            """
            const/4 p0, 0x1
            return p0
            """.trimIndent()
        )

        // ── Layer 2: IAB.isPurchasedAny(Context) → always true ───────────────
        // Top-level gate used in ActivitySettings and ServiceSinkhole.
        runCatching {
            IabIsPurchasedAnyFingerprint
                .match(classDefBy(IabIsPurchasedAnyFingerprint.definingClass!!))
                .method
        }.getOrNull()?.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
            """.trimIndent()
        )
    }
}