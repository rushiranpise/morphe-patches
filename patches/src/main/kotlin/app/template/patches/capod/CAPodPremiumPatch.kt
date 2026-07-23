package app.template.patches.capod

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CAPOD_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlocks the Pro upgrade in CAPod.
 *
 * ## Root cause of previous failure
 *
 * The previous patch targeted `UpgradeRepoExtensionsKt$isPro$1.invokeSuspend()`.
 * This is the coroutine continuation callback — it is called only after the billing
 * flow emits, at which point the actual isPro check (reading billingData->getProSku)
 * had already been evaluated inside the Info object. Our Boolean.TRUE return was
 * delivered to the outer coroutine but the billing state was not changed.
 *
 * ## Correct target: UpgradeRepoGplay$Info.isPro()Z
 *
 * Unlike BVM/SD Maid which use an `isUpgraded` boolean field, CAPod's Info class
 * exposes a direct `isPro()` method (stable, non-obfuscated path):
 *
 * ```
 * fun isPro(): Boolean {
 *     val sku = billingData?.let { UpgradeRepoGplay.getProSku(it) }
 *     if (sku != null) return true
 *     return gracePeriod
 * }
 * ```
 *
 * Every consumer that checks "is the user pro" calls this method on the Info object
 * emitted from the upgradeInfo SharedFlow. Returning true here covers all callers.
 *
 * ## SKUs
 *   - IAP: "eu.darken.capod.iap.upgrade.pro"
 *   - Sub: "upgrade.pro"
 */
@Suppress("unused")
val capodPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro.",
) {
    compatibleWith(CAPOD_COMPATIBILITY)

    execute {
        IsProFingerprint.method.returnEarly(true)
    }
}
