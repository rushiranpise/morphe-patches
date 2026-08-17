package app.template.patches.blek.premium

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLEK_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

// ─── Pairip bypass ────────────────────────────────────────────────────────────

@Suppress("unused")
val blekPairIpBypassPatch = bytecodePatch {
    compatibleWith(BLEK_COMPATIBILITY)

    execute {
        LicenseCheckFingerprint.method.returnEarly()
        LicenseValidateResponseFingerprint.method.returnEarly()
        LicenseCloseAppFingerprint.method.returnEarly()
    }
}

// ─── Premium unlock ───────────────────────────────────────────────────────────

/**
 * Unlocks all premium features and removes the Upgrade navigation item in Blek.
 *
 * ## Billing architecture changes v6.22.0 → v6.23.1
 *
 *   uy (BillingManager)   → cz   (HashMap field: d → o)
 *   nz (isPremium helper) → nz   (unchanged; field v now holds cz not uy)
 *   wf4 (StateFlow)       → jh4  (CAS method h → i)
 *   Ley (SKU state enum)  → Lgy  (PURCHASED_AND_ACKNOWLEDGED field h → m)
 *   ez.e()Z               → nz.v()Z  (calls cz.q() ×2)
 *   uy.h(String)Z         → cz.q(String)Z
 *   uy.c(List)V           → cz.v(List)V
 *   uy.u(String,Ley)V     → cz.u(String,Lgy)V
 *   NavIsPremiumInit      → REMOVED: xn.<init>(Lnz;) now calls nz.v()Z directly
 *                           to seed the Compose nav LiveData initial value, so
 *                           patching nz.v()→true covers the nav gate automatically.
 *
 * ## Four-layer patch
 *
 * Layer 1 — SkuStateInitFingerprint on cz.v(List)V
 *   Replace move-result v1 (SharedPrefs.getInt ordinal) with const/4 v1, 0x3.
 *   Forces every jh4 StateFlow to start as PURCHASED_AND_ACKNOWLEDGED (ordinal 3).
 *
 * Layer 2 — IsPremiumFingerprint on nz.v()Z
 *   returnEarly(true) — imperative gate + nav initial value (via xn ViewModel).
 *
 * Layer 3 — SkuStateQueryFingerprint on cz.q(String)Z
 *   returnEarly(true) — per-SKU boolean queries bypassing nz.v().
 *
 * Layer 4 — SkuStateWriteFingerprint on cz.u(String,Lgy)V
 *   returnEarly() — blocks BillingClient from overwriting jh4 StateFlows with
 *   UNPURCHASED after it finds no active purchases.
 */
@Suppress("unused")
val blekPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features including fullscreen mode and removes the Upgrade navigation item.",
    default = true,
) {
    compatibleWith(BLEK_COMPATIBILITY)
    dependsOn(blekPairIpBypassPatch)

    execute {
        // Layer 1: force jh4 StateFlow initial ordinal → 3 (PURCHASED_AND_ACKNOWLEDGED)
        val getIntIndex = SkuStateInitFingerprint.instructionMatches[1].index
        val moveResultIndex = getIntIndex + 1
        val moveResultReg = SkuStateInitFingerprint.method
            .getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
        SkuStateInitFingerprint.method.replaceInstruction(
            moveResultIndex,
            "const/4 v$moveResultReg, 0x3",
        )

        // Layer 2: force isPremium gate → true (also seeds nav initial value via xn ViewModel)
        IsPremiumFingerprint.method.returnEarly(true)

        // Layer 3: force per-SKU direct boolean query → true
        SkuStateQueryFingerprint.method.returnEarly(true)

        // Layer 4: block BillingClient from overwriting jh4 StateFlows
        SkuStateWriteFingerprint.method.returnEarly()
    }
}
