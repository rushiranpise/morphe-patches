package app.template.patches.automate.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AUTOMATE_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlock Automate (1.51.1+)
 *
 * ## Licensing system — Google Play Billing + runtime state field
 *
 * Automate uses Google Play Billing (BillingClient) via the `F3.b` premium manager.
 * The result is cached in `AutomateService.Y1:I`, an instance field with three states:
 *  - `0` = initial / unknown
 *  - `1` = not premium (billing query returned no valid purchase)
 *  - `3` = premium verified (purchase state=PURCHASED + isAcknowledged=true)
 *
 * ## Feature gate
 *
 * `AutomateService.f(B0, BeginningStatement, Object, Z)Z` is called before each flow run:
 *  1. If `Y1 == 3` → `return true` (premium, unlimited flows)
 *  2. Query `runningStatementCount` — if count ≤ 30 → `return true` (free tier limit)
 *  3. If count > 30 AND not premium → shows `PremiumPurchaseActivity` → `return false`
 *
 * ## Patch strategy
 *
 * **Patch 1** — `returnEarly(true)` on the gate method `f()`. Every flow execution
 * is always allowed, bypassing both the 30-statement limit and the premium check.
 *
 * **Patch 2** — inject `Y1 = 3` at the top of `onQueryPremiumCompleted()`. This
 * ensures the premium status field is always set to verified before any billing
 * callback logic runs, so the UI (settings preference, premium indicator) also
 * reflects premium status correctly.
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
        // Patch 1: always allow flow execution (bypass count limit and premium check)
        AutomatePremiumGateFingerprint.method.returnEarly(true)

        // Patch 2: force premium state field Y1 = 3 before billing callback processes.
        // const/4 v0, 0x3  → iput v0, p0, AutomateService->Y1:I
        // Using p0 = this (AutomateService instance) as per smali convention.
        AutomatePremiumQueryFingerprint.method.addInstructions(
            0,
            """
            const/4 v0, 0x3
            iput v0, p0, Lcom/llamalab/automate/AutomateService;->Y1:I
            """.trimIndent()
        )
    }
}
