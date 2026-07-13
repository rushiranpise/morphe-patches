package app.template.patches.greenify

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.GREENIFY_COMPATIBILITY

/**
 * Unlocks all donation/premium features in Greenify (com.oasisfeng.greenify).
 *
 * ## VerifyError note
 * W1.D() and W1.Q() both contain try-catch blocks. Removing all instructions
 * invalidates the exception table offsets → ART VerifyError on class load.
 * Fix: prepend-only (addInstructions at 0) so the return fires before the
 * try block; original bytecode (and its exception table) is unreachable but
 * structurally valid. W1.E() has no try-catch so full replacement is safe.
 */
@Suppress("unused")
val greenifyUnlockDonationPatch = bytecodePatch(
    name = "Unlock Donation Features",
    description = "Unlocks all premium donation features in Greenify.",
    default = true
) {
    compatibleWith(GREENIFY_COMPATIBILITY)

    execute {
        // ── Layer 1: W1.Q(Context, Z)I → return 2 ────────────────────────────
        // Has try-catch: prepend-only, do NOT remove instructions.
        PremiumStatusCheckFingerprint.method.addInstructions(
            0,
            """
            const/4 v0, 0x2
            return v0
            """.trimIndent()
        )

        // ── Layer 2: W1.D(Context)Z → return true ────────────────────────────
        // Has try-catch: prepend-only, do NOT remove instructions.
        DonationVerifierFingerprint.method.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
            """.trimIndent()
        )

        // ── Layer 3: W1.E(Context)Z → return true ────────────────────────────
        // No try-catch: safe to replace entire body.
        ChinaRegionCheckFingerprint.method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
                """.trimIndent()
            )
        }
    }
}
