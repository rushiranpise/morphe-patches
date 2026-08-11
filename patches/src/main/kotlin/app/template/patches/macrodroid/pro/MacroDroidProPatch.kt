package app.template.patches.macrodroid.pro

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MACRODROID_COMPATIBILITY
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

// MacroDroid Pro Unlock (com.arlosoft.macrodroid v5.65.9)
//
// Pro state resolver: ycc.j()Ltcc;
//   Checks in order:
//     1. Settings.c3()   → "vcp_count" int  (device-cap pro)   ← patch target
//     2. Settings.d3()   → "htt_count" int  (legacy serial pro)
//     3. Settings.m4()   → serial String    (serial-key pro)
//     4. LiveData<Boolean> from Play Store purchase
//     5. Settings.I2()   → "utc_check_enabled" (trial/fallback)
//
//   Returns tcc$c (PRO) or tcc$b (FREE).
//
// Patch: returnEarly(true) on Settings.c3() — the device-cap gate.
// This makes ycc.j() immediately return tcc$c (PRO) on the first branch,
// bypassing Play Store and server validation entirely.
//
// Server validation (/v1/checkPro Retrofit API) still runs in background
// but only updates the locally cached state that c3/d3 read — it does not
// revoke the app in the same session once we've patched the reader.

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
        IsProViaDeviceCapFingerprint.method.returnEarly(true)
    }
}
