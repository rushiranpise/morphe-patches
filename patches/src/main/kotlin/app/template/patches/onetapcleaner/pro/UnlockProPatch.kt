package app.template.patches.onetapcleaner.pro

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.onetapcleaner.ads.disableAdsPatch
import app.template.patches.onetapcleaner.antitamper.disableAntiTamperPatch
import app.template.patches.onetapcleaner.gma.disableGmaPcamPatch
import app.template.patches.onetapcleaner.license.disablePairIPLicenseCheckPatch
import app.template.patches.onetapcleaner.licwnd.disableLicWndPatch
import app.template.patches.shared.Constants
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch
import com.android.tools.smali.dexlib2.AccessFlags

// 1Tap Cleaner's pro state in v5.22 flows through:
//
//   Feature gate: amg.m1173()
//     return billingManager != null && billingManager.m5236()
//
//   amg.m1173() has 116 call sites — the single central isPro gate for
//   every locked feature in the app (history export, app-group filters,
//   unlimited cache targets, etc.).
//
//   BillingManager.m5236():
//     return egm.f13916 || egm.m7660()
//     f13916 is only set to true by Google Play billing after a valid purchase.
//     On a re-signed build the billing session fails or returns no purchases →
//     m5236() returns false → amg.m1173() returns false → all feature gates fail.
//
//   com.a0soft.gphone.playbilling.BillingManager is a stable non-obfuscated class.
//   Patching m5236() to always return true propagates through all 116 isPro sites.
//
//   Selector: non-static ()Z method in BillingManager — the only one in the class.
//   FINAL flag excluded from the predicate: the class is declared final,
//   which may or may not set the FINAL flag on individual methods at the DEX level.
//
// Architecture change from v5.21:
//   v5.21 used a static singleton with four AtomicBoolean fields (no longer present).
//   v5.22 reads pro status from BillingManager.m5236() at each check site.
//
// Dependencies:
//   spoofSignatureVerificationPatch — satisfies signature checks in the billing path
//     (Google Play Billing validates installer certificate; re-signed APK fails without it)
//   spoofInstallSourcePatch — reports Play Store as installer so BillingClient
//     considers the install source valid
//   spoofFirebaseCertHashPatch — prevents Firebase/Dynamic Links from rejecting the
//     re-signed APK via cert hash mismatch, which can block feature unlock flows
//
@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks 1Tap Cleaner PRO features: history export, app-group filters, " +
        "unlimited cache targets, and ad removal.",
    default = true,
) {
    dependsOn(
        spoofSignatureVerificationPatch,
        spoofInstallSourcePatch,
        spoofFirebaseCertHashPatch,
        disablePairIPLicenseCheckPatch,
        disableGmaPcamPatch,
        disableAntiTamperPatch,
        disableLicWndPatch,
        disableAdsPatch,
    )
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        // BillingManager.m5236() — isPro check called by amg.m1173() and all 116 feature gates.
        val billingManagerClass = mutableClassDefBy(
            "Lcom/a0soft/gphone/playbilling/BillingManager;",
        )

        // The only non-static ()Z method in BillingManager.
        val isProMethod = billingManagerClass.methods.firstOrNull { method ->
            method.returnType == "Z" &&
                method.parameterTypes.isEmpty() &&
                !AccessFlags.STATIC.isSet(method.accessFlags)
        } ?: throw PatchException(
            "1Tap Cleaner: BillingManager isPro method not found — " +
                "expected a non-static ()Z method in BillingManager.",
        )

        isProMethod.returnEarly(true)
    }
}
