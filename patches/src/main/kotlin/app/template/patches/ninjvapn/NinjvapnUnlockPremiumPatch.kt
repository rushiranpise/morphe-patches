package app.template.patches.ninjvapn

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NINJVAPN_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Ninja VPN premium unlock.
 *
 * Premium state flow:
 *   1. La8/x;->f()Z  — checks if field `f` (order-id String) is non-null → isPremium
 *   2. Dashboard calls f() → if true → sets l0.r = Premium$Mode.PREMIUM
 *   3. Premium$Mode.get(Purchase) — maps billing purchaseState to PREMIUM enum
 *   4. l0.a(l0):Z — gate that opens SheetUpgrade when mode ≠ PREMIUM
 *
 * Patches:
 *   A) Force f() → true          → Dashboard always sets l0.r = PREMIUM on init
 *   B) Force get() → PREMIUM     → any billing callback also resolves to PREMIUM
 *   C) Force a() → false         → upgrade sheet never shown (returns false = no upsell)
 */
@Suppress("unused")
val ninjvapnUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Ninja VPN premium.",
    default = true
) {
    compatibleWith(NINJVAPN_COMPATIBILITY)

    execute {
        // A) La8/x;->f()Z — always return true
        isPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // B) Premium$Mode.get(Purchase) — always return PREMIUM enum constant
        getPremiumModeFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lapp/ninjavpn/android/app/revenue/subscription/Premium${'$'}Mode;->PREMIUM:Lapp/ninjavpn/android/app/revenue/subscription/Premium${'$'}Mode;
                    return-object v0
                """.trimIndent()
            )
        }

        // C) l0.a(l0):Z — return false so upgrade sheet is never shown
        showUpgradeSheetFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
    }
}
