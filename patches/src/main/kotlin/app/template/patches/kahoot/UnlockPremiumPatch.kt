package app.template.patches.kahoot

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.KAHOOT_COMPATIBILITY

// Kahoot! v6.6.7 — no.mobitroll.kahoot.android
//
// TWO-LAYER PATCH:
//
// Layer 1 — Feature flags (fixes feature access):
//   hasFeature(Feature)Z → true
//   hasFeature(String)Z  → true
//   Unlocks all 140+ Feature enum values (QUICK_CREATE, SLIDE_BLOCK, STUDY_BUDDY, etc.)
//
// Layer 2 — Subscription plan display (fixes "Free" badge + "7 day trial" CTA):
//   getProductFromMostPremiumStandardSubscription() → UNIVERSAL_GOLD
//     Shows "Kahoot! Plus Gold" in profile, plan pages, compare-plans screen.
//   hasActiveStandardSubscription() → true
//     Hides "Start 7 day trial" CTA on home/profile; enables paid-user UI flows.
//   hasActiveStandardSubscriptionMatchingAppAndDeviceAppStore() → true
//     Store-filtered variant used by billing manager and trial gate.
//   getPlanLogoType() → "gold"
//     Sets the plan logo badge to gold tier.

private const val PRODUCT_CLASS = "Lno/mobitroll/kahoot/android/account/billing/Product;"
private const val UNIVERSAL_GOLD = "UNIVERSAL_GOLD"

@Suppress("unused")
val kahootUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Gold",
    description = "Unlocks Kahoot! Plus Gold features in app.",
    default = true,
) {
    compatibleWith(KAHOOT_COMPATIBILITY)

    execute {
        val returnTrue = """
            const/4 v0, 0x1
            return v0
        """

        // ── Layer 1: Feature flags ────────────────────────────────────────────
        HasFeatureFingerprint.method.addInstructions(0, returnTrue)
        HasFeatureByNameFingerprint.method.addInstructions(0, returnTrue)

        // ── Layer 2: Subscription plan display ────────────────────────────────

        // getProductFromMostPremiumStandardSubscription() → UNIVERSAL_GOLD
        GetProductFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, $PRODUCT_CLASS->$UNIVERSAL_GOLD:$PRODUCT_CLASS
                return-object v0
            """,
        )

        // hasActiveStandardSubscription() → true
        HasActiveSubscriptionFingerprint.method.addInstructions(0, returnTrue)

        // hasActiveStandardSubscriptionMatchingAppAndDeviceAppStore() → true
        HasActiveSubscriptionMatchingStoreFingerprint.method.addInstructions(0, returnTrue)

        // getPlanLogoType() → "gold"
        GetPlanLogoTypeFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "gold"
                return-object v0
            """,
        )
    }
}
