package app.template.patches.aceexplorer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ACE_EXPLORER_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Unlocks Ace Ex File Manager premium by patching the subscription gate.
 *
 * Gate chain:
 *   SubscriptionManager.p()Z  ← all UI callers (55+ sites)
 *     → vn3.a()Z (test override, always false in prod) OR tq7.u()Z
 *   tq7.u()Z = !TextUtils.isEmpty(l())  ← l() returns cached subscriptionId
 *
 * Note: NOT a rebranded ES File Explorer (no es/zz4, es/fx4, FexApplication).
 *   Uses Yandex AppMetrica analytics + Firebase, separate codebase from ES/com.estrongs.android.pop.
 *   Uses Google Play Billing directly (no custom billing server).
 */
@Suppress("unused")
val aceExplorerUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in app.",
) {
    compatibleWith(ACE_EXPLORER_COMPATIBILITY)

    execute {
        // Main gate — covers all 55+ call sites
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

        // Underlying subscriptionId check — belt-and-suspenders
        SubscriptionIdCheckFingerprint.method.apply {
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
