package app.template.patches.aaad.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AAAD_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * AAAD Premium Patch
 *
 * AAAD uses a dual licensing system:
 *  1. Stripe + Firebase Cloud Functions (europe-west1) — new subscriptions
 *     SubscriptionManager.getSubscriptionStatus() calls the Firebase Function,
 *     receives SubscriptionStatus(isActive, plan, expiresAt, ...) and caches
 *     the result in SharedPreferences for 1 hour.
 *
 *  2. Firebase Realtime Database (legacy) — old one-time purchases
 *     ProStatusHelper.isProUser(DataSnapshot) / isProValue(Object) read the
 *     Firebase RTDB node to determine pro status for accounts that pre-date Stripe.
 *
 * Patch layers:
 *  A) SubscriptionStatus.isActive() → return true
 *     Cascading: every consumer of getSubscriptionStatus() sees isActive=true.
 *
 *  B) hasActiveSubscription() → return boxed Boolean.TRUE immediately
 *     Skips the Firebase Functions network call entirely.
 *
 *  C) ProStatusHelper.isProUser() / isProValue() → return true
 *     Covers the legacy RTDB path used for old one-time purchase accounts.
 *
 * Note: SubscriptionManager.canLinkBackup() already returns true unconditionally
 * in the source (observed in smali), so no patch is needed for that method.
 */
@Suppress("unused")
val aaadPremiumPatch = bytecodePatch(
    name = "AAAD Premium",
    description = "Unlocks AAAD Pro subscription features by bypassing Stripe and Firebase subscription checks.",
) {
    compatibleWith(AAAD_COMPATIBILITY)

    execute {
        // Layer A: SubscriptionStatus.isActive() → always true
        // This cascades through the entire billing pipeline:
        //   hasActiveSubscription() → getSubscriptionStatus() → .isActive() → true
        //   canRecoverLicense() → getSubscriptionStatus() → .isActive() → true
        //   ProVersionActivity / AboutPaymentActivity UI state → true
        SubscriptionStatusIsActiveFingerprint.method.returnEarly(true)

        // Layer B: hasActiveSubscription() → return boxed Boolean.TRUE immediately
        // Skips the Firebase Cloud Function network call. The coroutine suspend machinery
        // still sets up correctly but the method returns before invoking getSubscriptionStatus().
        // We inject at index 0 to short-circuit before coroutine label checks.
        HasActiveSubscriptionFingerprint.method.addInstructions(
            0,
            """
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
            """.trimIndent(),
        )

        // Layer C: Legacy Firebase RTDB pro status checks → always true
        IsProUserFingerprint.method.returnEarly(true)
        IsProValueFingerprint.method.returnEarly(true)
    }
}
