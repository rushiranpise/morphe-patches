package app.template.patches.superfile

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SUPER_FILE_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Unlocks Super File premium / lifetime VIP.
 *
 * Gate chain (same developer family as Ace Ex / RS File Manager):
 *   SubscriptionManager.m()Z → frames/ih7.q()Z
 *   ih7.q() = !TextUtils.isEmpty(m("key_p_encrypt_st", "")) — subscription token present
 *
 * Lifetime product: "idesuper_lifetime" (one-time Play Billing).
 * m()=true covers both subscription and lifetime paths (all UI checks route through m()).
 * No pairip. Analytics: Firebase Crashlytics (AppMetrica SDK bundled but not app-activated).
 */
@Suppress("unused")
val superFileUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium/lifetime featuers in app.",
    default = true,
) {
    compatibleWith(SUPER_FILE_COMPATIBILITY)

    execute {
        // Main gate — 34 callers
        IsSubscribedFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent()
            )
        }

        // Underlying token check — belt-and-suspenders
        SubscriptionTokenCheckFingerprint.method.apply {
            clearBody()
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
