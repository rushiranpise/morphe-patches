package app.template.patches.getcontact

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.GETCONTACT_COMPATIBILITY

/**
 * Getcontact — Unlock Premium (Tariff 10 / Premium Yearly)
 *
 * ## Complete entitlement map
 *
 * The "Try Premium" CTA shown in search detail is triggered when:
 *   isTrialUsed=false AND hasTariff=false → shows "Try Premium 30 Days" banner
 *   (fires analytics event view.searchDetail.tryPremium30Days)
 *
 * Full patch: 28 getters across 3 objects.
 *
 * ### SubscriptionModel — feature flags (pro*)
 *   proAds, proWho, proStats, proBusiness, proChatgpt, proTelco, proPriMod,
 *   proAIAssistantCalendar → all true (enable individual features)
 *
 * ### SubscriptionModel — premium UI / badge
 *   getPremiumType()        → BadgeType.PREMIUM
 *   getPremiumTypeName()    → "Premium"
 *   getShowWhoLookedMyProfile() → true
 *   getShowStatics()        → true
 *   getShowSubscriptionInfo() → true
 *   getShowSubscriptionPackages() → true
 *   getHasTariff()          → true
 *   isTrialUsed()           → true  ← suppresses "Try Premium 30 Days" CTA
 *   isMainSubscriptionMenuActive() → true  ← shows plan management menu
 *   isPremiumRequiredForOwnTags()  → false ← own-tag management requires no premium
 *   getShowTagUsage()       → false ← hides tag usage counter (premium = no limit)
 *   getShowTrustScoreUsage() → false ← hides trust-score counter
 *   getHideOwnTagCount()    → false ← tag count visible (premium behavior)
 *
 * ### SubscriptionModel — plan identity strings
 *   getLastPackageText()    → "Premium Yearly"
 *   getStoreProductId()     → "tariff10"
 *
 * ### UsageInfo — per-feature usage limit object
 *   getRemainingCount()     → Integer.MAX_VALUE (unlimited)
 *   isColorRed()            → false (no at-limit indicator)
 *   getShowOffer()          → false (no upgrade modal on exhaustion)
 *   getShowPackages()       → false (no package picker on exhaustion)
 */
@Suppress("unused")
val getContactUnlockPatch = bytecodePatch(
    name = "Unlock Premium Features",
    description = "Unlocks Getcontact Premium Yearly (Tariff 10) — patches all 28 subscription model getters including trial, usage limits, and plan identity.",
    default = true,
) {
    compatibleWith(GETCONTACT_COMPATIBILITY)

    execute {
        val returnTrue = """
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
        """.trimIndent()

        val returnFalse = """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v0
        """.trimIndent()

        // ── Feature-gate flags ────────────────────────────────────────────────
        GetProAdsFingerprint.method.addInstructions(0, returnTrue)
        GetProWhoFingerprint.method.addInstructions(0, returnTrue)
        GetProStatsFingerprint.method.addInstructions(0, returnTrue)
        GetProBusinessFingerprint.method.addInstructions(0, returnTrue)
        GetProChatgptFingerprint.method.addInstructions(0, returnTrue)
        GetProTelcoFingerprint.method.addInstructions(0, returnTrue)
        GetProPriModFingerprint.method.addInstructions(0, returnTrue)
        GetProAIAssistantCalendarFingerprint.method.addInstructions(0, returnTrue)

        // ── Premium UI badge / visibility ──────────────────────────────────────
        val badgeType = GetPremiumTypeFingerprint.method.returnType
        GetPremiumTypeFingerprint.method.addInstructions(
            0, "sget-object v0, $badgeType->PREMIUM:$badgeType\nreturn-object v0",
        )
        GetPremiumTypeNameFingerprint.method.addInstructions(
            0, "const-string v0, \"Premium\"\nreturn-object v0",
        )
        GetShowWhoLookedMyProfileFingerprint.method.addInstructions(0, returnTrue)
        GetShowStaticsFingerprint.method.addInstructions(0, returnTrue)
        GetShowSubscriptionInfoFingerprint.method.addInstructions(0, returnTrue)
        GetShowSubscriptionPackagesFingerprint.method.addInstructions(0, returnTrue)
        GetHasTariffFingerprint.method.addInstructions(0, returnTrue)

        // isTrialUsed=true → suppresses "Try Premium 30 Days" CTA in search detail
        // (shown when isTrialUsed=false AND hasTariff=false)
        IsTrialUsedFingerprint.method.addInstructions(0, returnTrue)

        // isMainSubscriptionMenuActive=true → shows plan management menu
        IsMainSubscriptionMenuActiveFingerprint.method.addInstructions(0, returnTrue)

        // isPremiumRequiredForOwnTags=false → own-tag management without premium gate
        IsPremiumRequiredForOwnTagsFingerprint.method.addInstructions(0, returnFalse)

        // Show*Usage=false → hides usage counters (premium = no visible limit)
        GetShowTagUsageFingerprint.method.addInstructions(0, returnFalse)
        GetShowTrustScoreUsageFingerprint.method.addInstructions(0, returnFalse)

        // getHideOwnTagCount=false → tag count visible on profile (premium behavior)
        GetHideOwnTagCountFingerprint.method.addInstructions(0, returnFalse)

        // ── Plan identity strings ──────────────────────────────────────────────
        GetLastPackageTextFingerprint.method.addInstructions(
            0, "const-string v0, \"Premium Yearly\"\nreturn-object v0",
        )
        GetStoreProductIdFingerprint.method.addInstructions(
            0, "const-string v0, \"tariff10\"\nreturn-object v0",
        )

        // ── UsageInfo: per-feature usage limit gates ───────────────────────────
        GetRemainingCountFingerprint.method.addInstructions(
            0,
            """
            const/high16 v0, 0x7fff0000
            invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v0
            return-object v0
            """.trimIndent(),
        )
        IsColorRedFingerprint.method.addInstructions(0, returnFalse)
        GetShowOfferFingerprint.method.addInstructions(0, returnFalse)
        GetShowPackagesFingerprint.method.addInstructions(0, returnFalse)
    }
}
