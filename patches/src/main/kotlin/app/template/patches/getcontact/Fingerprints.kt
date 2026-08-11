package app.template.patches.getcontact

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// SubscriptionModel feature-gate flags (all globally unique)
// ─────────────────────────────────────────────────────────────────────────────

object GetProAdsFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getProAds")
object GetProWhoFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getProWho")
object GetProStatsFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getProStats")
object GetProBusinessFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getProBusiness")
object GetProChatgptFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getProChatgpt")
object GetProTelcoFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getProTelco")
object GetProPriModFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getProPriMod")
object GetProAIAssistantCalendarFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getProAIAssistantCalendar")

// ─────────────────────────────────────────────────────────────────────────────
// SubscriptionModel premium UI / badge getters
// ─────────────────────────────────────────────────────────────────────────────

/**
 * getPremiumType()BadgeType — profile badge (FREE/PREMIUM/BUSINESS/…).
 * 3 classes have this — fieldAccess(premiumType) scopes to SubscriptionModel.
 */
object GetPremiumTypeFingerprint : Fingerprint(
    returnType = "Lapp/source/getcontact/subscription/domain/BadgeType;",
    name = "getPremiumType",
    filters = listOf(fieldAccess(opcode = Opcode.IGET_OBJECT, name = "premiumType")),
)

/**
 * getPremiumTypeName()String — badge text label ("Free" → "Premium").
 * 2 classes have this — fieldAccess(premiumTypeName) scopes to SubscriptionModel.
 */
object GetPremiumTypeNameFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    name = "getPremiumTypeName",
    filters = listOf(fieldAccess(opcode = Opcode.IGET_OBJECT, name = "premiumTypeName")),
)

/** getShowWhoLookedMyProfile()Boolean — Who Looked at Me list visibility. Globally unique. */
object GetShowWhoLookedMyProfileFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getShowWhoLookedMyProfile")

/** getShowStatics()Boolean — analytics panel gate. Globally unique. */
object GetShowStaticsFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getShowStatics")

/** getShowSubscriptionInfo()Boolean — "You are Premium" bar vs "Upgrade" CTA. Globally unique. */
object GetShowSubscriptionInfoFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getShowSubscriptionInfo")

/** getShowSubscriptionPackages()Boolean — plan selector vs upgrade flow. Globally unique. */
object GetShowSubscriptionPackagesFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getShowSubscriptionPackages")

/** getHasTariff()Boolean — any active paid plan flag (MainViewModel/StarterViewModel). Globally unique. */
object GetHasTariffFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getHasTariff")

// ─────────────────────────────────────────────────────────────────────────────
// SubscriptionModel plan display strings — "Premium Yearly" / "Tariff 10"
// ─────────────────────────────────────────────────────────────────────────────

/**
 * getLastPackageText()String — the active plan's display name shown in the subscription menu.
 * Inject "Premium Yearly" to match the user-visible plan label.
 * Globally unique.
 */
object GetLastPackageTextFingerprint : Fingerprint(returnType = "Ljava/lang/String;", name = "getLastPackageText")

/**
 * getStoreProductId()String — the Play Store product ID of the active subscription.
 * 4 classes have this — fieldAccess(storeProductId) scopes to SubscriptionModel.
 * Inject "tariff10" to match the Tariff 10 plan identifier.
 */
object GetStoreProductIdFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    name = "getStoreProductId",
    filters = listOf(fieldAccess(opcode = Opcode.IGET_OBJECT, name = "storeProductId")),
)

// ─────────────────────────────────────────────────────────────────────────────
// UsageInfo getters — per-feature usage limit object (accessgetGetCallInfoUseCasep)
// Controls search/tag/trust-score usage counters and limit-reached UI
// ─────────────────────────────────────────────────────────────────────────────

/**
 * UsageInfo.getRemainingCount()Integer — remaining uses for this feature.
 * Two occurrences: one returns Integer (SubscriptionModel usage objects), one returns int (lambda).
 * returnType=Ljava/lang/Integer; uniquely identifies the SubscriptionModel one.
 * Inject Integer.MAX_VALUE to show unlimited remaining.
 */
object GetRemainingCountFingerprint : Fingerprint(
    returnType = "Ljava/lang/Integer;",
    name = "getRemainingCount",
)

/**
 * UsageInfo.isColorRed()Boolean — turns the usage counter red when limit is reached.
 * Globally unique. Inject false to prevent red "limit reached" indicator.
 */
object IsColorRedFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "isColorRed")

/**
 * UsageInfo.getShowOffer()Boolean — shows the upgrade offer modal when usage is exhausted.
 * Globally unique. Inject false to suppress upgrade prompts.
 */
object GetShowOfferFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getShowOffer")

/**
 * UsageInfo.getShowPackages()Boolean — shows the package picker when usage is exhausted.
 * Only occurrence in accessgetGetCallInfoUseCasep (UsageInfo), not SubscriptionModel.
 * Inject false to suppress package picker.
 */
object GetShowPackagesFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getShowPackages")

// ─────────────────────────────────────────────────────────────────────────────
// Remaining SubscriptionModel Boolean getters — all globally unique
// ─────────────────────────────────────────────────────────────────────────────

/**
 * isTrialUsed()Boolean — whether the 30-day free trial has been used.
 *
 * When false AND hasTariff=false, the search detail screen shows the
 * "Try Premium 30 Days" CTA banner (fires view.searchDetail.tryPremium30Days event).
 * Patching to true suppresses the trial offer entirely, matching a paid subscriber.
 */
object IsTrialUsedFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "isTrialUsed")

/**
 * isMainSubscriptionMenuActive()Boolean — controls subscription menu visibility.
 *
 * When true, the main subscription management menu is shown (plan details, renewal date).
 * When false (free state), this menu is hidden or replaced with an upgrade prompt.
 */
object IsMainSubscriptionMenuActiveFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "isMainSubscriptionMenuActive")

/**
 * isPremiumRequiredForOwnTags()Boolean — own-tags access gate.
 *
 * When true, adding/managing your own tags requires premium.
 * Patching to false removes the premium requirement for tag management.
 */
object IsPremiumRequiredForOwnTagsFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "isPremiumRequiredForOwnTags")

/**
 * getShowTagUsage()Boolean — show tag-view usage counter.
 *
 * Controls whether the tag-view remaining count is displayed.
 * Patching to false hides the usage counter (premium users don't see limits).
 */
object GetShowTagUsageFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getShowTagUsage")

/**
 * getShowTrustScoreUsage()Boolean — show trust-score usage counter.
 *
 * Controls whether the trust-score remaining count is displayed.
 */
object GetShowTrustScoreUsageFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getShowTrustScoreUsage")

/**
 * getHideOwnTagCount()Boolean — hide own-tag count from others.
 *
 * When false (premium), your tag count is visible on your profile.
 * This is a cosmetic field; patching to false matches premium behavior.
 */
object GetHideOwnTagCountFingerprint : Fingerprint(returnType = "Ljava/lang/Boolean;", name = "getHideOwnTagCount")
