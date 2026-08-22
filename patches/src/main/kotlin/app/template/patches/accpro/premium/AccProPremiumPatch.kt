package app.template.patches.accpro.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_ACCPRO
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.killPairIpFull
import app.template.patches.shared.pairIPManifestPatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

/**
 * Unlocks App Cache Cleaner Pro (com.a0soft.gphone.acc.free).
 *
 * ## Upgrade dialog call chains (two independent paths)
 *
 * ### Path A — DialogFragment (cold-start trigger)
 *   dul.轠(Z)V
 *     → yo.攦()Z                  [shows ox DialogFragment]
 *     → ox.ض(Bundle)Dialog         [builds AlertDialog via pu0]
 *   Blocked by: UpgradeDialogFragmentFingerprint → yo.攦().returnEarly()
 *
 * ### Path B — AlertDialog direct (feature-level trigger)
 *   dq.invokeSuspend() / 7 others
 *     → dvr.鷋(Context)V           [builds AlertDialog directly via had.س + pu0]
 *   Blocked by: UpgradeDialogFingerprint → dvr.鷋().returnEarly()
 *
 * ### Path C — LicWnd Activity (upgrade store screen)
 *   13 call sites → LicWnd.衊(Context)V → startActivity(LicWnd)
 *   Blocked by: LicWndShowFingerprint → LicWnd.衊().returnEarly()
 *
 * ## Other protections
 * - Pairip LVL: killPairIpFull() + pairIPManifestPatch(MainApp)
 * - Spoof patches: signature, install source, Firebase cert hash
 * - Process self-destruct: l20.糷()V — nop'd
 * - App-own signature check: cyq.罏() — nop'd
 * - isPro getter: dpx.鷋()Z — returnEarly(true)
 * - isAdFree gate: yo.衊(Z)Z — returnEarly(true)
 * - DexGuard: all string filters forbidden
 */
@Suppress("unused")
val accProPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks App Cache Cleaner Pro and removes ads by bypassing " +
        "Pairip LVL, signature checks, self-destruct, and all upgrade dialogs.",
) {
    compatibleWith(COMPATIBILITY_ACCPRO)

    dependsOn(
        pairIPManifestPatch(
            replacementAppClass = "com.a0soft.gphone.acc.free.MainApp",
        ),
        spoofSignatureVerificationPatch,
        spoofInstallSourcePatch,
        spoofFirebaseCertHashPatch,
    )

    execute {
        // 1. Full Pairip DEX kill.
        killPairIpFull()

        // 2. Nop process self-destruct (l20.糷()V).
        SelfDestructFingerprint.method.returnEarly()

        // 3. Nop app-own signature check + ipi state setter (cyq.罏(Context,List)V).
        SignatureCheckFingerprint.method.returnEarly()

        // 4. Force isPro = true (dpx.鷋()Z).
        IsProFingerprint.method.returnEarly(true)

        // 5. Force isAdFree = true (yo.衊(Z)Z).
        IsAdFreeFingerprint.method.returnEarly(true)

        // 6. Nop LicWnd Activity launcher (LicWnd.衊(Context)V) — Path C.
        LicWndShowFingerprint.method.returnEarly()

        // 7. Nop AlertDialog direct builder (dvr.鷋(Context)V) — Path B.
        UpgradeDialogFingerprint.method.returnEarly()

        // 8. Nop DialogFragment launcher (yo.攦()Z) — Path A. ROOT CAUSE of cold-start dialog.
        // yo.攦() calls getSupportFragmentManager().show(ox, "ox") where ox.ض(Bundle)
        // builds the AlertDialog with the upgrade message. Called from dul and deb
        // on startup, completely bypassing dvr.鷋 and the ipi state checks.
        UpgradeDialogFragmentFingerprint.method.returnEarly(false)
    }
}
