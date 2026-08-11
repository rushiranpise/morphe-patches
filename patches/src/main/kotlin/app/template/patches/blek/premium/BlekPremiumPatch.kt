package app.template.patches.blek.premium

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLEK_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

// ─── Pairip bypass (hidden dependency) ───────────────────────────────────────

/**
 * Neutralises all three reachable Pairip DEX layers in this variant.
 * Blek ships the licensecheck variant only — no VMRunner/StartupLauncher/
 * SignatureCheck native VM (confirmed: no libpairipcore.so).
 *
 *  • LicenseClient.checkLicense(Context)       → return-void
 *  • LicenseResponseHelper.validateResponse()  → return-void
 *  • LicenseActivity.closeApp()                → return-void
 */
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
 * ## Billing architecture
 *
 * Google Play BillingClient → uy (C0790uy) stores one wf4<EnumC0200ey> StateFlow
 * per SKU in f20298d (HashMap). Ordinal 3 = PURCHASED_AND_ACKNOWLEDGED.
 *
 * The reactive chain driving all premium UI:
 *   wf4_premium_v1 / wf4_premium_yearly  (StateFlows in f20298d)
 *     → combine() in C0964zn.f24963e (no3<Boolean>)
 *     → fj3.isPremium (PremiumStatus) → feature gate dialogs
 *     → C0815vm.f20918d → bottom nav Upgrade tab visibility
 *
 * ## Five-layer patch
 *
 * ### Layer 1 — SkuStateInitFingerprint on uy.c(List)V
 * Replace move-result v1 (SharedPrefs.getInt return) with const/4 v1, 0x3.
 * Forces every wf4 StateFlow to initialise as PURCHASED_AND_ACKNOWLEDGED.
 * NOTE: v3 is dual-use (SharedPrefs default AND C0311hy mode int) — patching
 * const/4 v3 causes ClassCastException. Patching move-result v1 is safe.
 *
 * ### Layer 2 — IsPremiumFingerprint on ez.e()Z
 * returnEarly(true) — covers fj3 construction before StateFlow first emission.
 *
 * ### Layer 3 — SkuStateQueryFingerprint on uy.h(String)Z
 * returnEarly(true) — covers direct per-SKU boolean queries bypassing ez.e().
 *
 * ### Layer 4 — NavIsPremiumInitFingerprint on tu5.m8990e
 * Replace sget-object v1, Boolean.FALSE (collectAsState initial value for the
 * nav isPremium boolean) with sget-object v1, Boolean.TRUE.
 * Gates: C0815vm.f20918d → bottom nav Upgrade tab / "Unlock Pro Forever" entry.
 *
 * ### Layer 5 — SkuStateWriteFingerprint on uy.u(String, EnumC0200ey)V
 * returnEarly() — blocks ALL runtime wf4 StateFlow updates from BillingClient.
 *
 * ROOT CAUSE of the "PRO badge briefly disappears then returns" race:
 * After the BillingClient queries purchases and finds none, it calls:
 *   m9379b() → m9384j() → m9386u("premium_v1", SKU_STATE_UNPURCHASED)
 *   → wf4.h(null, UNPURCHASED) — overwrites the Layer 1 init value
 *   → combine() emits false → isPremium=false → all PRO badges reappear
 * returnEarly() on m9386u prevents this overwrite permanently.
 * Safe: SharedPrefs write is bypassed by Layer 1; wf4 already holds PURCHASED.
 */
@Suppress("unused")
val blekPremiumPatch = bytecodePatch(
    name = "Blek Premium",
    description = "Unlocks all premium features including fullscreen mode and removes the Upgrade navigation item.",
    default = true,
) {
    compatibleWith(BLEK_COMPATIBILITY)
    dependsOn(blekPairIpBypassPatch)

    execute {
        // ── Layer 1: Force wf4 StateFlow initial ordinal → PURCHASED_AND_ACKNOWLEDGED ──
        val getIntIndex = SkuStateInitFingerprint.instructionMatches[1].index
        val moveResultIndex = getIntIndex + 1
        val moveResultReg = SkuStateInitFingerprint.method
            .getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
        SkuStateInitFingerprint.method.replaceInstruction(
            moveResultIndex,
            "const/4 v$moveResultReg, 0x3",
        )

        // ── Layer 2: Force imperative isPremium gate → true ──
        IsPremiumFingerprint.method.returnEarly(true)

        // ── Layer 3: Force per-SKU direct boolean query → true ──
        SkuStateQueryFingerprint.method.returnEarly(true)

        // ── Layer 4: Force nav isPremium collectAsState initial value → TRUE ──
        // matches[2] = Lly;-><init> call; +1 = move-object; +2 = sget-object Boolean.FALSE
        val lyInitIndex = NavIsPremiumInitFingerprint.instructionMatches[2].index
        val falseIndex = lyInitIndex + 2
        val falseReg = NavIsPremiumInitFingerprint.method
            .getInstruction<OneRegisterInstruction>(falseIndex).registerA
        NavIsPremiumInitFingerprint.method.replaceInstruction(
            falseIndex,
            "sget-object v$falseReg, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        )

        // ── Layer 5: Block BillingClient from overwriting wf4 StateFlow ──
        // Prevents PURCHASED_AND_ACKNOWLEDGED from being overwritten with UNPURCHASED
        // when BillingClient reports no active purchases.
        SkuStateWriteFingerprint.method.returnEarly()
    }
}
