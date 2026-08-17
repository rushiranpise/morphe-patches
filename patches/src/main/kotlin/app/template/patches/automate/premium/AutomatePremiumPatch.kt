package app.template.patches.automate.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AUTOMATE_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlock Automate (1.53.1+)
 *
 * ## Licensing system — Google Play Billing + runtime state field
 *
 * Automate uses Google Play Billing (BillingClient) via the `F3.b` premium manager.
 * The result is cached in `AutomateService.Z1:I` (was `Y1:I` in v1.51.1), with states:
 *  - `0` = initial / unknown
 *  - `1` = not premium
 *  - `3` = premium verified
 *
 * ## Feature gate
 *
 * `AutomateService.f(F0, BeginningStatement, Object, Z)Z` is called before each flow:
 *  1. If `Z1 == 3` → `return true` (premium, unlimited)
 *  2. Query `runningStatementCount` — if count ≤ 30 → `return true` (free tier)
 *  3. If count > 30 AND not premium → shows `PremiumPurchaseActivity` → `return false`
 *
 * ## Patch strategy
 *
 * **Patch 1** — `returnEarly(true)` on gate method `f()`. Every flow always allowed.
 * Fingerprinted by stable `"runningStatementCount"` + `"checkPremiumAllow"` strings.
 * First param uses `"L"` placeholder (obfuscated: was B0 in v1.51.1, F0 in v1.53.1).
 *
 * **Patch 2** — inject `Z1 = 3` at top of `AutomateService.onQueryPremiumCompleted()`.
 * Forces premium state field before billing callback processes. Field renamed Y1→Z1 in v1.53.1.
 *
 * **Patch 3** — inject `setPremiumPurchase(p1)` at top of `MainFragment.onQueryPremiumCompleted()`.
 * Forces settings UI to show "You got Premium" / "View order information".
 *
 * @package com.llamalab.automate
 */
@Suppress("unused")
val automatePremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Removes the 30 running statement limit and unlocks Automate premium features.",
    default = true
) {
    compatibleWith(AUTOMATE_COMPATIBILITY)

    execute {
        // Patch 1: always allow flow execution
        AutomatePremiumGateFingerprint.method.returnEarly(true)

        // Patch 2: force premium state field Z1 = 3 (was Y1 in v1.51.1)
        AutomatePremiumQueryFingerprint.method.addInstructions(
            0,
            """
            const/4 v0, 0x3
            iput v0, p0, Lcom/llamalab/automate/AutomateService;->Z1:I
            """.trimIndent()
        )

        // Patch 3: force settings UI to show "You got Premium"
        AutomateMainFragmentPremiumFingerprint.method.addInstructions(
            0,
            """
            invoke-direct {p0, p1}, Lcom/llamalab/automate/prefs/MainFragment;->setPremiumPurchase(Lcom/android/billingclient/api/Purchase;)V
            """.trimIndent()
        )
    }
}
