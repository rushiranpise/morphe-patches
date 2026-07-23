package app.template.patches.calm.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CALM_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Calm 6.99.1 — subscription bypass
//
// Gate flow:
//   Server API → Subscription.valid:Z → UserRepository.isSubscribed() → 20+ UI call sites
//   Per-content: PackItem.isUnlocked:Z / ActionData.isLocked:Z (server-set)
//   Display:     Subscription.planDuration / subscriptionPlan → lifetime label
//   Upsell:      IndividualSubscriptionKt shows SubscriptionUpgradeBanner when
//                SubscriptionDetails.type == Subscription.Type.Android
//
// 9 targets:
//
//   Access gates:
//   1. Subscription.getValid()              → returnEarly(true)
//   2. UserRepository.isSubscribed()        → returnEarly(true)
//   3. SubscriptionRefreshResponse.getValid() → returnEarly(true)
//   4. PackItem.isUnlocked()                → returnEarly(true)
//   5. ActionData.isLocked()                → returnEarly(false)
//
//   Lifetime display:
//   6. Subscription.getSubscriptionPlan()   → return "lifetime"
//   7. Subscription.getPlanDuration()       → return "lifetime"
//   8. Subscription.isLifetime()            → returnEarly(true)
//
//   Hide family plan upgrade upsell:
//   9. Subscription.getSubscriptionType()   → return Subscription.Type.Gift
//      The upgrade banner in IndividualSubscriptionKt only renders when
//      type == Subscription.Type.Android. Gift is never Android → banner hidden.

@Suppress("unused")
val calmPremiumPatch = bytecodePatch(
    name = "Calm Premium",
    description = "Unlocks Calm lifetime subscription",
) {
    compatibleWith(CALM_COMPATIBILITY)

    execute {
        // ── Access gates ─────────────────────────────────────────────────────

        SubscriptionGetValidFingerprint.method.returnEarly(true)

        UserRepositoryIsSubscribedFingerprint.method.returnEarly(true)

        SubscriptionRefreshGetValidFingerprint.method.returnEarly(true)

        PackItemIsUnlockedFingerprint.method.returnEarly(true)

        ActionDataIsLockedFingerprint.method.returnEarly(false)

        // ── Lifetime display ─────────────────────────────────────────────────

        SubscriptionGetPlanFingerprint.method.addInstructions(
            0,
            """
                const-string p0, "lifetime"
                return-object p0
            """,
        )

        SubscriptionGetPlanDurationFingerprint.method.addInstructions(
            0,
            """
                const-string p0, "lifetime"
                return-object p0
            """,
        )

        SubscriptionIsLifetimeFingerprint.method.returnEarly(true)

        // ── Hide family plan upsell ──────────────────────────────────────────
        // Return Subscription.Type.Gift — not Android, so the upgrade banner
        // in IndividualSubscriptionKt is never rendered.
        // getSubscriptionType() has .locals 2 — use v0.
        SubscriptionGetTypeFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/calm/android/data/subscription/Subscription${'$'}Type;->Gift:Lcom/calm/android/data/subscription/Subscription${'$'}Type;
                return-object v0
            """,
        )
    }
}
