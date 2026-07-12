package app.template.patches.rsexplorer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.RS_EXPLORER_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Unlocks RS File Manager premium by patching the billing gate.
 *
 * Gate chain in BillingManager.p()Z:
 *   1. ij7.u()Z  — !isEmpty(subscriptionId token)  ← patched
 *   2. ij7.v()Z  — 3-day trial window ("key_p_r_encrypt_st") — skipped by p()=true
 *   3. r31.f(Context)Z — DO NOT PATCH: dual-purpose method.
 *        r31.f() first checks r31.b:Z (premium flag). If false, falls through to
 *        r31.e(Context) = isSystemDarkTheme() via UiModeManager.
 *        55+ callers use it as a theme/TV-layout detector — patching=true breaks all UI theming.
 *
 * BillingManager.p()=true short-circuits all three gates making r31.f() irrelevant.
 *
 * Lifetime VIP: product id "rs_file_lifetime_20250623" (one-time purchase via Play Billing).
 *   p()=true spoofs active premium; no separate lifetime method needed.
 */
@Suppress("unused")
val rsExplorerUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in app.",
) {
    compatibleWith(RS_EXPLORER_COMPATIBILITY)

    execute {
        // Main gate — short-circuits all sub-checks
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

        // Subscription token check — belt-and-suspenders
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
