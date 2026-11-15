package app.template.patches.macrodroid.pro

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.template.patches.shared.Constants.MACRODROID_COMPATIBILITY
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// MacroDroid Pro Unlock (com.arlosoft.macrodroid v5.66.9+)
//
// Pro state resolver: j3d.a()Lb3d; (formerly ycc.j()Ltcc; in v5.65.9)
//   Checks in order (inlined by R8):
//     1. "vcp_count" int  (device-cap pro)         ← target branch (returns b3d$c)
//     2. "htt_count" int  (legacy serial pro)
//     3. Settings.y0()    (serial-key pro)
//     4. "pro_play_order_id" / LiveData<Boolean>   (Play Store purchase)
//     5. "utc_check_enabled"                       (trial/fallback)
//
//   Returns b3d$c (PRO) or b3d$b (FREE).
//
// Patch: Copies the Pro singleton load (sget-object v0, Lb3d$c;->a:Lb3d$c;)
// and return-object v0 instruction from the first branch to index 0 of j3d.a().
// This makes the resolver immediately return PRO without hardcoding obfuscated
// class names, bypassing Play Store and server validation entirely.
//
// Server validation (/v1/checkPro Retrofit API) still runs in background
// but only updates the locally cached preferences that the resolver reads —
// it does not revoke Pro status once the resolver returns early.

@Suppress("unused")
val macroDroidProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Pro for MacroDroid - Device Automation.",
) {
    compatibleWith(MACRODROID_COMPATIBILITY)

    dependsOn(
        spoofSignatureVerificationPatch,
        spoofInstallSourcePatch,
    )

    execute {
        val fieldReference = (IsProViaDeviceCapFingerprint.instructionMatches.last().instruction as ReferenceInstruction)
            .reference as FieldReference
        IsProViaDeviceCapFingerprint.method.addInstructions(0, "sget-object v0, $fieldReference\nreturn-object v0")
    }
}
