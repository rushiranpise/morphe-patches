package app.template.patches.parallelspace

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PARALLELSPACE_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Parallel Space Pro v4.0.9159 — Force Pro patch
//
// CHANGELOG FROM v4.0.9123:
//   Old: patched com/lbe/parallel/ad.k()Z (obfuscated class/method, broke on update)
//   New: patched com/lbe/parallel/ee.l()Z + ee.k()Z + le.g()V
//
// STABILITY IMPROVEMENTS:
//   - Three-patch strategy covers all Pro status check paths simultaneously.
//   - Fingerprints anchored on stable string("bpcs") + j11.c() call pair
//     rather than obfuscated class/method names alone.
//   - "bpcs" = SPConstant.BILLING_PURCHASE_CURRENT_STATE — a named constant
//     in the app's own code; unlikely to change across updates.
//   - j11 is the SharedPreferences singleton; its methods are stable.
//
// THREE-PATCH STRATEGY:
//
// Patch 1 (IsProRefreshFingerprint) — ee.l()Z → returnEarly(true)
//   Primary isPro getter that re-reads SP on each call.
//   Any code path that asks "is the user Pro right now?" hits this.
//
// Patch 2 (IsProCachedFingerprint) — ee.k()Z → returnEarly(true)
//   Cached isPro getter used in frequent UI checks (reads field ee.b).
//   Prevents the cached value from ever returning false.
//
// Patch 3 (IsProNotifierFingerprint) — le.g()V → nop if-ne at [bpcsCall + 3]
//   Billing state change notifier. Nop-ing the if-ne guard makes it always
//   call r50.e() (the Pro-status listener), notifying all registered UI
//   components that the user is Pro regardless of the actual bpcs value.
//   Uses replaceInstruction("nop") — no label resolution needed. ✓

@Suppress("unused")
val parallelSpaceForceProPatch = bytecodePatch(
    name = "Force Pro",
    description = "Unlocks Parallel Space Pro features by bypassing all Pro status checks.",
) {
    compatibleWith(PARALLELSPACE_COMPATIBILITY)

    execute {
        // Patch 1: ee.l()Z — always return true (isPro, re-reads SP)
        IsProRefreshFingerprint.method.returnEarly(true)

        // Patch 2: ee.k()Z — always return true (isPro, cached)
        IsProCachedFingerprint.method.returnEarly(true)

        // Patch 3: le.g()V — always notify listeners as Pro
        // string("bpcs") = instructionMatches[0].index = 32
        // methodCall(j11, "c") = instructionMatches[1].index = 33
        // if-ne v0, v1, :L2  = index 36 = [33] + 3
        val bpcsCallIndex = IsProNotifierFingerprint.instructionMatches[1].index
        IsProNotifierFingerprint.method.replaceInstruction(bpcsCallIndex + 3, "nop")
    }
}
