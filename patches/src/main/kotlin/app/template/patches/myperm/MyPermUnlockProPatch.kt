package app.template.patches.myperm

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MYPERM_COMPATIBILITY

@Suppress("unused")
val myPermUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all Pro features in app.",
    default = true
) {
    compatibleWith(MYPERM_COMPATIBILITY)

    execute {
        // ── UpgradeRepoGplay$Info.isPro() → always true ───────────────────────
        // Bypasses billing data + grace period check; makes the app believe Pro is owned.
        runCatching {
            UpgradeRepoGplayIsProFingerprint
                .match(classDefBy(UpgradeRepoGplayIsProFingerprint.definingClass!!))
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
