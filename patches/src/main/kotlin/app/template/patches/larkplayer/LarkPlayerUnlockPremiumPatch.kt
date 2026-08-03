package app.template.patches.larkplayer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.LARK_PLAYER_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Unlocks Lark Player Premium by patching the app's billing-info-provider
 * implementation.
 *
 * Premium gate chain (class names below are obfuscated and rotate every
 * build — see Fingerprints.kt for how each target is located structurally
 * instead of by hardcoded name):
 *   hasPurchase()Z        — true if the cached PurchaseBean != null
 *   hasHistoryPurchase()Z — true if a cached PurchaseHistoryRecord != null
 *   isPermanent()Z        — feeds the premium-status computation directly
 *
 * All three must be forced true together. Forcing only hasPurchase/
 * hasHistoryPurchase is not sufficient: the app's premium-status computation
 * treats "has a purchase but isPermanent==false" as an *expired* subscription,
 * which immediately closes the in-app-purchase screen the instant it opens.
 */
@Suppress("unused")
val larkPlayerUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in app.",
) {
    compatibleWith(LARK_PLAYER_COMPATIBILITY)

    execute {
        // hasPurchase — active purchase check.
        HasPurchaseFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            )
        }

        // hasHistoryPurchase — any past purchase check.
        HasHistoryPurchaseFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            )
        }

        // isPermanent — required for premiumStatus to resolve as "permanent"
        // rather than "expired" (see class doc comment above).
        IsPermanentFingerprint.method.apply {
            clearBody()
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
