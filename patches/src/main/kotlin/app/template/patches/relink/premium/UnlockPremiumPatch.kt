package app.template.patches.relink.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.RELINK_COMPATIBILITY
import app.template.patches.shared.clearBody

// ─────────────────────────────────────────────────────────────────────────────
// re-Link Premium unlock — two layers
//
// Layer 1 — g2/e.A(g2/a)V  (LicenseStateUpdater)
//
//   Every license state transition posts through A(). By replacing the incoming
//   g2/a param (p1) with a freshly constructed g2/a$e("relink_2_unlimited")
//   BEFORE the body logs/emits it, the StateFlow always carries a Licensed state.
//
//   g2/a$e constructor: <init>(String key)V
//     validates key != null, then calls super.<init>(0L, key, null)
//     Safe to construct with "relink_2_unlimited" — the same product ID used
//     by the real purchase flow (verified in g2/e clinit).
//
//   Register layout: .registers 11, non-static, 2 params
//     p0 = v9 (this), p1 = v10 (incoming g2/a LicenseState)
//     v0..v8 = free locals; v0 is used by body's first sget-object but
//     addInstructions(0) runs BEFORE the original body, so our v0 usage
//     completes before the original v0 sget — no conflict.
//
//   Patch: addInstructions(0, "new-instance p1 + const-string v0 + invoke-direct")
//     p1 ← new g2/a$e("relink_2_unlimited")
//     Original body then: logs "PlayStore License changed: was X and now is Licensed"
//     and emits the Licensed state to SharedFlow/StateFlow normally.
//
// Layer 2 — g2/e.x()Z  (IsPurchasedSync)
//
//   Sync boolean read by the service coordinator (relink/c.smali).
//   Original: reads StateFlow → g2/g.a() → instanceof g2/a$e → Z
//   Patched: clearBody + const/4 v0, 0x1 + return v0
//
//   Found via classDef of LicenseStateUpdaterFingerprint (the proven
//   Fingerprint.classDef.methods.firstOrNull{} pattern — avoids
//   Fingerprint.match() on methods without unique string anchors).
// ─────────────────────────────────────────────────────────────────────────────

// Licensed product ID — same value used in g2/e.clinit for the purchase flow.
private const val LICENSED_KEY = "relink_2_unlimited"

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features in re-Link by forcing the " +
        "license StateFlow to always emit a Licensed state and the sync " +
        "purchase check to always return true.",
    default = true,
) {
    compatibleWith(RELINK_COMPATIBILITY)

    execute {

        // ── Layer 1: LicenseStateUpdater — always emit Licensed state ─────────
        //
        // Prepend into g2/e.A(g2/a)V:
        //   new-instance p1, Lg2/a$e;
        //   const-string v0, "relink_2_unlimited"
        //   invoke-direct {p1, v0}, Lg2/a$e;-><init>(Ljava/lang/String;)V
        //
        // After patch the full body runs normally — it logs the state change
        // ("PlayStore License changed: was X and now is Licensed(relink_2_unlimited)")
        // and emits the Licensed state to the SharedFlow/StateFlow.
        LicenseStateUpdaterFingerprint.method.addInstructions(
            0,
            """
            new-instance p1, Lg2/a${'$'}e;
            const-string v0, "$LICENSED_KEY"
            invoke-direct {p1, v0}, Lg2/a${'$'}e;-><init>(Ljava/lang/String;)V
            """.trimIndent(),
        )

        // ── Layer 2: IsPurchasedSync — always return true ─────────────────────
        //
        // g2/e.x()Z — found via classDef of Layer 1 (same class g2/e).
        // x()Z is the only PUBLIC FINAL ()Z method in the class.
        val licenseManagerClass = LicenseStateUpdaterFingerprint.classDef

        val isPurchasedSync = licenseManagerClass.methods.firstOrNull { method ->
            method.name != "<init>" &&
            method.name != "<clinit>" &&
            method.returnType == "Z" &&
            method.parameters.isEmpty() &&
            method.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.PUBLIC.value != 0 &&
            method.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.FINAL.value != 0 &&
            method.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.STATIC.value == 0
        } ?: throw PatchException(
            "re-Link: could not find isPurchased()Z in ${licenseManagerClass.type}"
        )

        isPurchasedSync.apply {
            clearBody()
            addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
                """.trimIndent(),
            )
        }
    }
}
