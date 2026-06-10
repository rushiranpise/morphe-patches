package app.template.patches.mlmanager

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MLMANAGER_COMPATIBILITY

/**
 * Unlocks pro features in ML Manager (com.javiersantos.mlmanager).
 *
 * ## Pro gate
 *
 * `j2/o.B(Context)Z` computes CRC32 of the app package name and compares
 * to 0x31e3c18b (the pro package). Three uses:
 *  1. AppsFragment — hides pro tabs when false
 *  2. z1/a0.H() — triggers LicenseChecker when true
 *  3. z1/a0.G() — triggers server validation when true
 *
 * Forcing B() true enables pro tabs but also fires the license/server
 * checks which call dontAllow/c() on failure → LicenseActivity.
 *
 * ## Fix: suppress the failure callbacks
 *
 * - `z1/a0.e(I)V` (dontAllow) → return-void: no LicenseActivity on check failure
 * - `z1/a0.c(ServerValidationsResponse)V` → return-void: no LicenseActivity on server error
 */
@Suppress("unused")
val mlManagerUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all pro features in ML Manager.",
    default = true
) {
    compatibleWith(MLMANAGER_COMPATIBILITY)

    execute {
        // Force pro package check to always return true → unlocks pro tabs
        ProPackageCheckFingerprint
            .match(classDefBy(ProPackageCheckFingerprint.definingClass!!))
            .method
            .apply {
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

        // Suppress dontAllow callback → no LicenseActivity on license check failure
        LicenseDontAllowFingerprint
            .match(classDefBy(LicenseDontAllowFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }

        // Suppress server validation failure callback → no LicenseActivity on server error
        ServerValidationCallbackFingerprint
            .match(classDefBy(ServerValidationCallbackFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }
    }
}
