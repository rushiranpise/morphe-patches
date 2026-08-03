package app.template.patches.komoot.subscription

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.KOMOOT_COMPATIBILITY
import app.template.patches.shared.clearBody

// ── Patch strategy ───────────────────────────────────────────────────────────
//
// Four hooks:
//
// 1. UserIsPremiumFingerprint → UserV7.k0()Z
//    clearBody() + addInstructions: const/4 v0, 0x1 / return v0.
//
// 2. AppConfigPremiumFingerprint → AppConfigV3.n()Boolean
//    clearBody() + addInstructions: sget Boolean.TRUE / return-object p0.
//
// 3. OwnsWorldPackFingerprint → c2t.g()Z
//    replaceInstructions(0): const/4 p0, 0x1 / return p0.
//    .registers 1, non-static → p0 = this, no other registers needed.
//
// 4. OwnedRegionIsOwnedFingerprint.matchAll()[3] → vqo.n()Z
//    vqo has 4 PUBLIC FINAL Z-returning no-param methods (f, l, m, n).
//    R8 sorts alphabetically → index [3] = n()Z (the isOwned getter, reads field k:Z).
//    replaceInstructions(0): const/4 p0, 0x1 / return p0.
//    .registers 1, non-static → p0 = this.
//
// NOTE: getMethodCalled() was abandoned after two failures (StringBuilder, BaseBundle).
// The filter chain [getClass → void-call → Z-call → MOVE_RESULT → IF_EQZ] was not
// unique globally — other methods across the 7 DEX files matched first and their
// Z-returning call target was a system class absent from the DEX pool.
// matchAll()[3] with classFingerprint scoped to vqo is precise and avoids all
// system-class lookups.

private const val RETURN_TRUE = "const/4 p0, 0x1\nreturn p0"

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features and all map packs.",
    default = true,
) {
    compatibleWith(KOMOOT_COMPATIBILITY)

    execute {
        // Hook 1 — UserV7.k0()Z: primary premium check.
        // .registers 2 → v0 available after clearBody.
        UserIsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Hook 2 — AppConfigV3.n()Boolean: server-config premium flag.
        // .registers 1 → only p0; reuse as return value after clearBody.
        AppConfigPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object p0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    return-object p0
                """.trimIndent(),
            )
        }

        // Hook 3 — c2t.g()Z: ownsWorldPack source getter.
        // .registers 1 → p0 = this; replaceInstructions swaps the 2-instruction body.
        OwnsWorldPackFingerprint.method.replaceInstructions(0, RETURN_TRUE)

        // Hook 4 — vqo.n()Z: OwnedRegion.isOwned per-object getter.
        // classFingerprint scopes to vqo; matchAll() returns [f, l, m, n] in R8
        // alphabetical order; [3] = n()Z (reads field k:Z = isOwned).
        // .registers 1 → p0 = this.
        OwnedRegionIsOwnedFingerprint.matchAll()[3].method
            .replaceInstructions(0, RETURN_TRUE)
    }
}
