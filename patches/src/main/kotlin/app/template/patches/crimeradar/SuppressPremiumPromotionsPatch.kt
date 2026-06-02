package app.template.patches.crimeradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CRIMERADAR_COMPATIBILITY

// Source: smali_classes8/com/particlemedia/feature/subscription/SubscriptionAccountHelper.smali
//
// isPremiumFeatureEnabled()Z:
//   Checks FlavorUtils.isLifeinfo() first (returns false for Crime Radar flavor),
//   then delegates to LE2/s.g() — the remote feature flag master switch.
//   Used to show/hide premium entry cards and feature promotion banners.
//   Forcing → true activates premium UI paths unconditionally.
//
// hasPaidEver()Z (on SubscriptionAccountHelper):
//   Reads SubscriptionManager.currentState().isActive(); if not active, also checks
//   subscriptionStatus string via hg/s.Q for a non-empty past-subscription signal.
//   Returning true suppresses "try premium" upsell nags for users who have never
//   subscribed.
//   NOTE: RadarMapSubscriptionGateway also has a hasPaidEver() method — that one
//   is patched separately in BypassPaywallPatch as HasPaidEverFingerprint. The
//   fingerprint object here is named AccountHasPaidEverFingerprint to avoid a
//   Kotlin redeclaration compile error between the two files in the same package.
//
// Note: shouldSuppressPremiumPromotions() is already covered in BypassPaywallPatch.

private const val ACCOUNT_HELPER_CLASS =
    "Lcom/particlemedia/feature/subscription/SubscriptionAccountHelper;"

object IsPremiumFeatureEnabledFingerprint : Fingerprint(
    definingClass = ACCOUNT_HELPER_CLASS,
    name = "isPremiumFeatureEnabled"
)

// Disambiguated from HasPaidEverFingerprint in BypassPaywallPatch.kt which targets
// the identically-named method on RadarMapSubscriptionGateway.
object AccountHasPaidEverFingerprint : Fingerprint(
    definingClass = ACCOUNT_HELPER_CLASS,
    name = "hasPaidEver"
)

@Suppress("unused")
val suppressPremiumPromotionsPatch = bytecodePatch(
    name = "Suppress Premium Promotions",
    description = "Supress all premium promotions in-app."
) {
    compatibleWith(CRIMERADAR_COMPATIBILITY)

    execute {
        val trueInstructions = """
            const/4 v0, 0x1
            return v0
        """.trimIndent()

        for (fingerprint in listOf(
            IsPremiumFeatureEnabledFingerprint,
            AccountHasPaidEverFingerprint
        )) {
            val method = fingerprint
                .match(classDefBy(ACCOUNT_HELPER_CLASS))
                .method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, trueInstructions)
        }
    }
}