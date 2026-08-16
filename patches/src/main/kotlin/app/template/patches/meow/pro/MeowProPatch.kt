package app.template.patches.meow.pro

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MEOW_COMPATIBILITY
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

// Money Manager - Budget & Meow (com.glgjing.money.manager.bookkeeping.meow v1.9.9)
//
// VIP type cascade (single source of truth = BillingConfig.d()):
//   d() → feeds BillingConfig.a StateFlow in <clinit> (initial UI state)
//   d() → called by e()Z (isVip gate): returns true if result != "sub_vip_none"
//   BillingConfig.a observed by pig/ui/dialog/b → displays member type label:
//     "sub_vip_permanent" → vip_type_permanent ("Life Member") ← what we return
//
// Patch 1: returnEarly("sub_vip_permanent") on BillingConfig.d() cascades to:
//   - e() returns true  → all VIP feature gates open
//   - BillingConfig.a StateFlow emits "sub_vip_permanent" on init → "Life Member" shown
//   - BillingManager$connect$1 sees KEY_VIP_PERMANENT_VERIFIED=false but calls e()
//     after queryPurchases — irrelevant since e() always returns true post-patch
//
// Patch 2: PairIP validateResponse → return-void (bypass RSA sig + package check)

@Suppress("unused")
val meowProPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks VIP and shows Life Member status for Money Manager - Budget & Meow.",
) {
    compatibleWith(MEOW_COMPATIBILITY)

    dependsOn(
        spoofSignatureVerificationPatch,
        spoofInstallSourcePatch,
    )

    execute {
        // Single-method patch that cascades to all VIP gates and UI display.
        VipTypeGetterFingerprint.method.returnEarly("sub_vip_permanent")

        // PairIP RSA signature verifier → no-op
        PairIpValidateResponseFingerprint.method.returnEarly()
    }
}
