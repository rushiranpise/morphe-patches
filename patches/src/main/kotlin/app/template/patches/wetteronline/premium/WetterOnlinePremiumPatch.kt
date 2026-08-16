package app.template.patches.wetteronline.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WETTERONLINE_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Unlocks premium and unblocks the app launch for WetterOnline.
//
// ROOT CAUSE (logo screen freeze):
//   WetterOnline calls BillingClient.startConnection() on startup.
//   On re-signed APKs, Play Integrity validation fails at the GMS/Finsky level
//   (HTTP 429, DF-DFERH-01 — certificate mismatch). The BillingClient
//   returns a non-OK responseCode to onBillingSetupFinished(), which emits
//   a SubscriptionException into the app's MutableStateFlow. The MainActivity
//   blocks on this StateFlow waiting for the OK state, causing the logo freeze.
//
// PATCHES APPLIED:
//
// 1. BillingSetupFinishedFingerprint → vpa.j(BillingResult)V
//    return-void immediately — skip emitting Success OR Error.
//    The StateFlow stays in its initial state. The app proceeds past the loading screen
//    because the premium gate (v45.d()Z) is already patched to return true unconditionally.
//
// 2. BillingServiceDisconnectedFingerprint → vpa.o()V
//    return-void — suppress the disconnect SubscriptionException emission.
//
// 3. IsFusedAccessProviderProFingerprint → v45.d()Z (FusedAccessProvider impl)
//    returnEarly(true) — primary isPro gate, bypasses all subscription checks.
//
// 4. IsProUseCaseFingerprint → n6.a()Z (IsProUseCase impl)
//    returnEarly(true) — DI-injected gateway called by all consumers.

@Suppress("unused")
val wetterOnlinePremiumPatch = bytecodePatch(
    name = "WetterOnline Premium",
    description = "Unlocks premium and unblocks logo screen freeze by bypassing billing callbacks and all local access checks.",
) {
    compatibleWith(WETTERONLINE_COMPATIBILITY)

    execute {
        // Fix logo freeze: suppress BillingClient result emissions
        BillingSetupFinishedFingerprint.method.returnEarly()
        BillingServiceDisconnectedFingerprint.method.returnEarly()

        // Premium gates: always return true
        IsFusedAccessProviderProFingerprint.method.returnEarly(true)
        IsProUseCaseFingerprint.method.returnEarly(true)
    }
}
