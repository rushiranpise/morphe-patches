package app.template.patches.crimeradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CRIMERADAR_COMPATIBILITY

// Paywall gate methods confirmed from smali analysis:
//
//  RadarMapSubscriptionGateway (smali_classes8):
//    • isActiveNow()Z            — primary runtime gate for premium map UI
//    • isFeatureEnabled()Z       — TweakConfig feature-flag gate
//    • wasEverPremium()Z         — upsell suppression gate
//    • hasPaidEver()Z            — delegates to shouldSuppressPremiumPromotions();
//                                   queried independently by MapFollowedLocationActivity
//                                   and the Me tab to control upsell nag visibility.
//    • isPremiumEntryDismissed()Z — reads a per-session boolean that resets every
//                                   launch; premium entry cards re-appear each session
//                                   without this patch.
//
//  SubscriptionAccountHelper (smali_classes8):
//    • shouldSuppressPremiumPromotions()Z — controls premium entry card
//
//  hasPaidEver() smali body (before patch):
//    invoke-static {}, Lcom/.../SubscriptionAccountHelper;->shouldSuppressPremiumPromotions()Z
//    move-result v0
//    return v0
//
//  isPremiumEntryDismissed() smali body (before patch):
//    sget-boolean v0, Lcom/.../RadarMapSubscriptionGateway;->premiumEntryDismissedThisSession:Z
//    return v0

private const val GATEWAY_CLASS =
    "Lcom/particlemedia/feature/subscription/RadarMapSubscriptionGateway;"

private const val ACCOUNT_HELPER_CLASS =
    "Lcom/particlemedia/feature/subscription/SubscriptionAccountHelper;"

object IsActiveNowFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "isActiveNow"
)

object IsFeatureEnabledFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "isFeatureEnabled"
)

object WasEverPremiumFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "wasEverPremium"
)

object HasPaidEverFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "hasPaidEver"
)

object IsPremiumEntryDismissedFingerprint : Fingerprint(
    definingClass = GATEWAY_CLASS,
    name = "isPremiumEntryDismissed"
)

object ShouldSuppressPremiumPromotionsFingerprint : Fingerprint(
    definingClass = ACCOUNT_HELPER_CLASS,
    name = "shouldSuppressPremiumPromotions"
)

@Suppress("unused")
val bypassPaywallPatch = bytecodePatch(
    name = "Bypass Subscription Paywall",
    description = "Bypasses the subscription paywall in-app."
) {
    compatibleWith(CRIMERADAR_COMPATIBILITY)

    execute {
        val trueInstructions = """
            const/4 v0, 0x1
            return v0
        """.trimIndent()

        // Patch all boolean gate methods in RadarMapSubscriptionGateway
        for (fingerprint in listOf(
            IsActiveNowFingerprint,
            IsFeatureEnabledFingerprint,
            WasEverPremiumFingerprint,
            HasPaidEverFingerprint,
            IsPremiumEntryDismissedFingerprint
        )) {
            val method = fingerprint.match(classDefBy(GATEWAY_CLASS)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, trueInstructions)
        }

        // Patch shouldSuppressPremiumPromotions in SubscriptionAccountHelper
        val helperMethod = ShouldSuppressPremiumPromotionsFingerprint
            .match(classDefBy(ACCOUNT_HELPER_CLASS))
            .method
        helperMethod.removeInstructions(0, helperMethod.instructions.count())
        helperMethod.addInstructions(0, trueInstructions)
    }
}