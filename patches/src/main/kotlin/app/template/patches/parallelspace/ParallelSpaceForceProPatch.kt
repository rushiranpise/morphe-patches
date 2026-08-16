package app.template.patches.parallelspace

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PARALLELSPACE_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Parallel Space Pro v4.0.9162 — Force Pro patch
//
// TWO-PATCH STRATEGY — covers all Pro check paths:
//
// Patch 1 (IsProGateFingerprint) — td.J()Z — BillingClient synchronized gate
//   instructionMatches[0] = MONITOR_ENTER at index 1.
//   Three guards sit at fixed offsets from monitor-enter:
//     [monitorIdx + 4] if-ne  v1, v2  (bpcs ≠ 2)
//     [monitorIdx + 6] if-eqz v1      (zzar == null)
//     [monitorIdx + 8] if-eqz v1      (dm1 == null)
//   Nop all three in reverse order → always falls to const/4 v3, 0x1 → return true.
//   monitor-enter/exit preserved — no deadlock.
//
// Patch 2 (IsProDirectFingerprint) — de.l()Z — direct SP read
//   returnEarly(true) — covers SplashActivity, HomeView, and other UI entry points
//   that bypass the billing gateway entirely.

@Suppress("unused")
val parallelSpaceForceProPatch = bytecodePatch(
    name = "Force Pro",
    description = "Unlocks Parallel Space Pro subscription features by bypassing all Pro status checks.",
) {
    compatibleWith(PARALLELSPACE_COMPATIBILITY)

    execute {
        // Patch 1: td.J()Z — nop three branch guards in reverse order
        val monitorIndex = IsProGateFingerprint.instructionMatches[0].index
        IsProGateFingerprint.method.replaceInstruction(monitorIndex + 8, "nop") // if-eqz dm1
        IsProGateFingerprint.method.replaceInstruction(monitorIndex + 6, "nop") // if-eqz zzar
        IsProGateFingerprint.method.replaceInstruction(monitorIndex + 4, "nop") // if-ne bpcs≠2

        // Patch 2: de.l()Z — always return true
        IsProDirectFingerprint.method.returnEarly(true)
    }
}
