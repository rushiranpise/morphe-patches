package app.template.patches.octi

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.OCTI_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlocks the Pro upgrade in Octi.
 *
 * ## Root cause of previous failure
 *
 * Previous patch targeted `UpgradeRepoExtensionsKt$isPro$1.invokeSuspend()`.
 * This coroutine continuation callback fires after the billing flow emits, at
 * which point `Trace.isPro()` (the state machine) had already read `Info.isPro:Z`
 * and returned the real billing result. Our Boolean.TRUE was never used.
 *
 * ## Correct target: UpgradeRepoGplay$Info.isPro()Z
 *
 * Octi's Info data class exposes a direct `isPro()` method that reads the
 * `isPro:Z` backing field set at construction time from billing state.
 * Every consumer of the upgrade status calls this method on the Info object
 * emitted from the upgradeInfo SharedFlow.
 *
 * Returning true here covers all callers without touching the coroutine machinery.
 *
 * ## SKUs
 *   - IAP: "eu.darken.octi.iap.upgrade.pro"
 *   - Sub: "upgrade.pro"
 */
@Suppress("unused")
val octiPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro upgrade in Octi.",
) {
    compatibleWith(OCTI_COMPATIBILITY)

    execute {
        IsProFingerprint.method.returnEarly(true)
    }
}
