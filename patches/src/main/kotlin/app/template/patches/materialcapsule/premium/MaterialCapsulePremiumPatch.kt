package app.template.patches.materialcapsule.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MATERIAL_CAPSULE_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Material Capsule uses Google Play Billing (BillingManager) + Pairip license check.
// Pro status is persisted as an AES-encrypted boolean in DataStore via
// DataStoreRepositoryBilling.saveProStatus(isPro: Boolean).
//
// Flow:
//   createClientConnectAndRestorePurchase()
//     → isProAvailable() [suspend, queries Play Billing for inapp + subs purchases]
//     → saveProStatus(result) [suspend, encrypts and saves to DataStore]
//       → getProStatusFlow() / getProStatus1() [read back by MainAppViewModel.isPro]
//
// Patch: intercept saveProStatus() and override the isPro parameter (p1)
// to const/4 1 (true) before the coroutine body runs.
//
// saveProStatus is a coroutine — the real first instruction after the coroutine
// resume dispatch is where we inject. The method has .locals 7.
// p1 = isPro (Z), p2 = Continuation.
//
// The coroutine state machine begins with instance-of v0, p2, ...
// We inject BEFORE that — at index 0 — overwriting p1 with true.
// The coroutine machinery will then always proceed with isPro=true.

@Suppress("unused")
val materialCapsulePremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Material Capsule pro.",
) {
    compatibleWith(MATERIAL_CAPSULE_COMPATIBILITY)

    execute {

        CheckLicenseFingerprint.method.returnEarly()
        InitializeLicenseCheckFingerprint.method.returnEarly()
        ProcessLicenseResponseFingerprint.method.returnEarly()
        StartPaywallActivityFingerprint.method.returnEarly()

        // Inject at index 0: overwrite isPro parameter with true before
        // the coroutine dispatch reads or propagates it.
        // p1 = isPro:Z, overwrite with 1 (true).
        SaveProStatusFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x1",
        )
    }
}
