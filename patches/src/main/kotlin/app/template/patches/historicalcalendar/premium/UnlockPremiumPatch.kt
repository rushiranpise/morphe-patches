package app.template.patches.historicalcalendar.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.HISTORICALCALENDAR_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val historicalCalendarUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features in Historical Calendar by forcing the isPremium gate to return true.",
    default = true,
) {
    compatibleWith(HISTORICALCALENDAR_COMPATIBILITY)

    execute {
        // f00.a()Z is the synchronous isPremium getter called from 14 gate sites.
        // Returning true immediately bypasses the entire billing flow:
        //   xu1 (5x article/content gates), w (main nav), g7+f7 (startup gates),
        //   yg5 (2x offline download), yn2 (widget), rg4 (billing result handler).
        //
        // Fingerprint anchored to developer string "BillingManager" (log tag in
        // f00 constructor) — survives any R8 class rename. Never uses obfuscated
        // class/method names.
        IsPremiumFingerprint.method.returnEarly(true)
    }
}
