package app.template.patches.vizmanga

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.VIZMANGA_COMPATIBILITY

@Suppress("unused")
val vizMangaUnlockPremiumPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Remove in-app ads.",
    default = true
) {
    compatibleWith(VIZMANGA_COMPATIBILITY)

    execute {
        // Patch 1: f62.x(Context, String)Z — always return true (active subscription)
        runCatching {
            VmSubscriptionCheckFingerprint
                .match(classDefBy(VmSubscriptionCheckFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            if (implementation == null) return@apply
            val instrCount = instructions.size
            removeInstructions(0, instrCount)
            addInstructions(
                0,
                """
                const/4 p0, 0x1
                return p0
                """.trimIndent()
            )
        }

        // Patch 2: f62.u(Context, String)J — return a far-future expiry timestamp (year 2099)
        runCatching {
            VmSubscriptionExpiryFingerprint
                .match(classDefBy(VmSubscriptionExpiryFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            if (implementation == null) return@apply
            val instrCount = instructions.size
            removeInstructions(0, instrCount)
            addInstructions(
                0,
                """
                const-wide v0, 0x5F7B9CC000L
                return-wide v0
                """.trimIndent()
            )
        }
    }
}
