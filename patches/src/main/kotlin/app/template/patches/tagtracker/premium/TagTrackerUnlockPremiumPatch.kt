package app.template.patches.tagtracker.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

// Tag Tracker uses Google Play Billing + PairIP license check.
// Premium state flows through a Kotlin MVI + DataStore architecture:
//
//   BillingClient.queryPurchasesAsync()
//   → nz3 callback: purchaseList.isEmpty() XOR 1 → isSubscribed:Z
//   → dx3.a(isSubscribed, continuation)       ← PATCH TARGET
//       logs "is_paid_user" / "is_lucky_user" Firebase events
//       → ra3 coroutine: DataStore.write("is_full_version", isSubscribed)
//       → kz3 reducer: reads Boolean from DataStore
//           → jz3.a(copy, isUpgraded=Boolean, bitmask=0x1FFE) updates SettingsUiState
//   → UI consumers (t6, bz3): iget-boolean jz3.a:Z gates every PRO feature
//
// Fix 1 — Premium unlock: inject const/4 p1, 1 at index 0 of dx3.a(Z, qj0).
//   p1 is the isSubscribed boolean. Overriding it to 1 before the coroutine state machine
//   runs means ra3 always writes is_full_version=true and kz3 always sets isUpgraded=true.
//   Fingerprint: strings "is_paid_user" + "is_lucky_user" are globally unique to dx3.a().
//
// Fix 2 — PairIP bypass: no-op initializeLicenseCheck() and scheduleAppShutdown().
//   Standard PairIP SDK — method names are never obfuscated.
//   initializeLicenseCheck() triggers the Play Integrity / license check that kills
//   re-signed builds; scheduleAppShutdown() is the deferred kill timer.
@Suppress("unused")
val tagTrackerUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Tag Tracker PRO: unlimited trackers, advanced detection, " +
        "full history, and ad-free experience.",
    default = true,
) {
    compatibleWith(Constants.TAGTRACKER_COMPATIBILITY)

    execute {
        // Fix 1: force p1 (isSubscribed) to true before the coroutine state machine
        // processes it. dx3.a() has .registers 13 so all registers are valid.
        SubscriptionStateWriterFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x1",
        )

        // Fix 2: PairIP bypass — standard SDK methods, no-op both entry points.
        PairIPLicenseCheckFingerprint.method.returnEarly()
        PairIPScheduleShutdownFingerprint.method.returnEarly()
    }
}
