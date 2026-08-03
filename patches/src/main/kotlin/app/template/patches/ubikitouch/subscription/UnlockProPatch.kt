package app.template.patches.ubikitouch.subscription

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.UBIKITOUCH_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Patch summary for v1.17.7
// ─────────────────────────
// sw1.z()Z is the single authoritative premium-state getter. It reads an
// XOR-encoded boolean from MMKV (Tencent) keyed by the result of sw1.h().
// All 10+ premium UI and feature gates in the app call sw1.z() directly.
//
// Injecting return-true at offset 0 short-circuits the MMKV read entirely,
// making the app behave as if the one-time Play Billing purchase is already
// acknowledged — without touching BillingClient, the purchase record, or
// the MMKV store itself.
//
// PremiumPropagatorFingerprint (from v1.16.13) is intentionally removed:
// eu.toneiv.ubktouch.util.xwzp no longer contains the mpow propagation path.
// That call moved into lo1.w(Z)V, which is only invoked on an actual purchase
// confirmation and is not needed for a read-path bypass.

@Suppress("unused")
val ubikiTouchUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks UbikiTouch Pro features by overriding the premium state getter.",
) {
    compatibleWith(UBIKITOUCH_COMPATIBILITY)

    execute {
        // sw1.z()Z is the sole static reader of the IS_PURCHASED_PREF MMKV entry.
        // Forcing it to always return true propagates Pro status to every call site.
        IsPurchasedFingerprint.method.returnEarly(true)
    }
}
