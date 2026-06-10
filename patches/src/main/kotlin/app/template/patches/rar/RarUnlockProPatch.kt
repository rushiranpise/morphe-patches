package app.template.patches.rar

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.RAR_COMPATIBILITY

/**
 * Removes ads and unlocks the no-ads subscription in RAR (com.rarlab.rar).
 *
 * ## What's gated
 * - Subscription-reminder dialog (AdsNotify) shown periodically while using the app
 * - "Buy" menu item visibility in MainActivity options menu (shown when not subscribed)
 *
 * ## Patch strategy
 *
 * ### Layer 1 — isSubsPurchased()Z
 *
 * `RarPurchase.isSubsPurchased()Z` is the sole gate method. It returns true only
 * when `purchaseState == PRESENT && !purchaseOneTime`. Its result is used by
 * MainActivity to control the "Buy" menu item visibility and is checked indirectly
 * through the purchase flow callbacks.
 *
 * Replacing the body with `const/4 0x1; return` makes every call site
 * see the subscription as purchased.
 *
 * ### Layer 2 — AdsNotify.show(AppCompatActivity)V
 *
 * `AdsNotify.show()` is the static entry point for the subscription-reminder
 * dialog. It enforces a 60-second cooldown, checks `activityPaused`, then
 * delegates to `showMessage()` which inflates the dialog fragment.
 * It is called from:
 *   - `RarPurchase.init()` when purchaseState == MISSING
 *   - `RarPurchase.lambda$showPurchaseReminder$0()` (timer-triggered)
 *
 * Prepending `return-void` silences all these call sites in one place.
 */
@Suppress("unused")
val rarUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Removes ads and unlocks the no-ads subscription in RAR.",
    default = true
) {
    compatibleWith(RAR_COMPATIBILITY)

    execute {
        // ── Layer 1: isSubsPurchased gate ────────────────────────────────────
        // RarPurchase.isSubsPurchased()Z — always return true.
        IsSubsPurchasedFingerprint
            .match(classDefBy(IsSubsPurchasedFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 2: Ad/reminder dialog entry point ──────────────────────────
        // AdsNotify.show(AppCompatActivity)V — suppress all reminder dialogs.
        AdsNotifyShowFingerprint
            .match(classDefBy(AdsNotifyShowFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }
    }
}