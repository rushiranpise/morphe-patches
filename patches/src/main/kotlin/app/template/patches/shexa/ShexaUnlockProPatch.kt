package app.template.patches.shexa

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SHEXA_COMPATIBILITY

@Suppress("unused")
val shexaUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all Pro features in app.",
    default = true
) {
    compatibleWith(SHEXA_COMPATIBILITY)

    execute {
        // ── Layer 1: d.v()Z → "has_active_purchase" SharedPref → always true ──
        // Primary billing gate queried throughout the billing flow and ad decision logic.
        runCatching {
            PremiumHelperHasActivePurchaseFingerprint
                .match(classDefBy(PremiumHelperHasActivePurchaseFingerprint.definingClass!!))
                .method
        }.getOrNull()?.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
            """.trimIndent()
        )

        // ── Layer 2: a.u()Z → runtime isPremium field g:Z → always true ─────
        // Runtime flag set after billing validation; read by ad display logic.
        runCatching {
            PremiumHelperIsPremiumFingerprint
                .match(classDefBy(PremiumHelperIsPremiumFingerprint.definingClass!!))
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
