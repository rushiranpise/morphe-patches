package app.template.patches.nytgames

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NYT_GAMES_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val nytGamesUnlockSubscriptionPatch = bytecodePatch(
    name = "Unlock subscription",
    description = "Unlocks subscription in app",
    default = true,
) {
    compatibleWith(NYT_GAMES_COMPATIBILITY)

    execute {
        // isSubscribed gate — SubauthEntitlementClientImpl.r()
        // Single source of subscription truth. Propagates through full delegate chain.
        IsSubscribedFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }
    }
}
