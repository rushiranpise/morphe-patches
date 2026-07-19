package app.template.patches.pcremote

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PC_REMOTE_COMPATIBILITY

/**
 * Unlocks PC Remote premium (com.monect.portable 8.3.5).
 *
 * Gate: LoggedInUser.isPremium()Z
 *   Checks premiumExpirationDate > now() || rewardPremiumExpiration > now().
 *   Called via Config.isPremium()Z at 26 gate sites (ads, screen projector,
 *   file explorer, remote desktop, task manager, data cable, settings, etc.).
 *
 * Patch: always return true → all premium features unlocked, ads removed.
 */
@Suppress("unused")
val pcRemoteUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features after login.",
    default = true,
) {
    compatibleWith(PC_REMOTE_COMPATIBILITY)

    execute {
        LoggedInUserIsPremiumFingerprint
            .match(mutableClassDefBy(LoggedInUserIsPremiumFingerprint.definingClass!!))
            .method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent(),
                )
            }
    }
}
