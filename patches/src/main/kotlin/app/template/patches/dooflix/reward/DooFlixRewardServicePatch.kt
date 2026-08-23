package app.template.patches.dooflix.reward

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_DOOFLIX
import app.template.patches.shared.returnEarly

@Suppress("unused")
val dooFlixRewardServicePatch = bytecodePatch(
    name = "Remove Reward Service",
    description = "Removes nodepointer background SDK and bypasses forced update screen."
) {
    compatibleWith(COMPATIBILITY_DOOFLIX)

    execute {
        // Kill boot receiver — prevents nodepointer restarting after reboot
        NodepointerBootReceiverFingerprint.method.returnEarly()

        // Kill foreground service onStartCommand — returns START_NOT_STICKY (0)
        NodepointerForegroundServiceFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent()
        )

        // Kill Firebase RC onComplete callback — this method ONLY handles:
        //   1. nodepointer init (Bridge.startNative/startForegroundService)
        //   2. Forced update gate (latestVersionCode comparison)
        // App config (SHOW_ADMOB, DNS_BLOCKER etc) comes from a separate API — safe to nop.
        UpdateGateFingerprint.method.returnEarly()
    }
}
