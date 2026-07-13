package app.template.patches.larkplayer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.LARK_PLAYER_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Unlocks Lark Player Premium by patching the IBillingInfoProvide implementation (o/b15).
 *
 * Premium gate chain:
 *   x70.getIBillingInfoProvide() → j80.d (field) → b15
 *   b15 implements Lo/bw2; (IBillingInfoProvide interface):
 *     d()Z  — hasPurchase: returns true if active PurchaseBean != null
 *     J()Z  — hasHistoryPurchase: returns true if PurchaseHistoryRecord != null
 *
 * Patching both forces the app to always see an active + historic premium purchase,
 * satisfying all gating checks (settings UI, song list, player features).
 */
@Suppress("unused")
val larkPlayerUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in app.",
) {
    compatibleWith(LARK_PLAYER_COMPATIBILITY)

    execute {
        // Patch hasPurchase (active subscription check)
        HasPurchaseFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent()
            )
        }

        // Patch hasHistoryPurchase (any past purchase check)
        HasHistoryPurchaseFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent()
            )
        }

        // Patch isPermanent — critical fix for premiumStatus computation:
        // processMinePremiumData evaluates: if f()Z=true → premiumStatus=1 (permanent).
        // premiumStatus=1 blocks PayPremiumFragment from opening in SettingsFragment.
        // Without this: d()Z=true but e() returns null → premiumStatus=3 (expired) →
        //   PayPremiumFragment opens, sees d()Z=true, calls activity.finish() immediately.
        IsPermanentFingerprint.method.apply {
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
