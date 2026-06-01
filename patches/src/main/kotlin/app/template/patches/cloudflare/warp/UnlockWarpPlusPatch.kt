package app.template.patches.cloudflare.warp

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WARP_COMPATIBILITY

/**
 * Forces WarpPlusState to UNLIMITED on every AccountData instance by intercepting
 * the primary constructor before the field is written.
 *
 * ── Why the previous approach crashed ──────────────────────────────────────────────
 * The original patch injected `iput-object` into WarpDataStore.p() (the getter):
 *
 *   sget-object v0, ...WarpPlusState;->UNLIMITED
 *   sget-object v1, ...AccountData;->j         // static default instance
 *   iput-object v0, v1, ...AccountData;->b     // ← VerifyError here
 *   return-object v1
 *
 * `AccountData.b` is a `final` Kotlin val. The Dalvik/ART verifier enforces
 * that final instance fields may ONLY be written inside the declaring class's
 * own <init> method. Writing it from WarpDataStore.p() — a foreign method —
 * causes a hard Dex VerifyError on class load, crashing the app at launch
 * before any activity is displayed.
 *
 * ── Correct approach ────────────────────────────────────────────────────────────
 * Intercept AccountData.<init> itself and overwrite parameter p2 (WarpPlusState)
 * with UNLIMITED *before* the constructor stores it into field `b`.
 */
@Suppress("unused")
val unlockWarpPlusPatch = bytecodePatch(
    name = "Spoof WARP+ Unlimited UI",
    description = "Unlocks WARP+ UI locally.",
    default = true
) {
    compatibleWith(WARP_COMPATIBILITY)

    execute {
        AccountDataConstructorFingerprint.method.addInstructions(
            0,
            """
                sget-object p2, Lcom/cloudflare/app/data/warpapi/WarpPlusState;->UNLIMITED:Lcom/cloudflare/app/data/warpapi/WarpPlusState;
            """
        )
    }
}