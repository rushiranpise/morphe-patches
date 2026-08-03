package app.template.patches.tradingview

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── Plan identity strings ──────────────────────────────────────────────────────
//
// NOTE (v1.20.79): Plan.getProPlan() is now overloaded:
//   getProPlan()Ljava/lang/String;                                 ← string alias
//   getProPlan()Lcom/.../ProPlan;                                 ← enum object (new)
//
// ProPlan was refactored from a String alias to a proper enum in v1.20.79.
// We target the String-returning overload for plan string injection because
// ProPlan$Companion.isPro/isProPremiumOrHigher/getPlanLevel all still accept
// String and will propagate the patched value through the full plan pipeline.
// The ProPlan enum object returned by the other overload is not patched directly;
// instead we patch isPro/isProPremiumOrHigher/getPlanLevel on ProPlan$Companion.

object PlanStringFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "getProPlan",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object NextPlanStringFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "getNextProPlan",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object BillingCycleFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "getBillingCycle",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object WebChartUserPlanFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/webchart/implementation/entity/UserPlanEntity;",
    name = "getUserPlan",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── Plan boolean flags ────────────────────────────────────────────────────────

object RenewalActiveFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isRenewalActive",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object GracePeriodFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isGracePeriod",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object HoldPeriodFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isHoldPeriod",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object PlanIsProPlanFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isProPlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// Plan.isPaymentsBanned() returns boxed Boolean (nullable), not primitive Z.
// Returning Boolean.FALSE prevents BannedError from locking the payment UI.
object PlanIsPaymentsBannedFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isPaymentsBanned",
    returnType = "Ljava/lang/Boolean;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── Lite / early-bird / trial-type gates ─────────────────────────────────────

object IsLitePlan2023Fingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isLitePlan2023",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object IsLitePlan2024Fingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isLitePlan2024",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object IsLitePlan2024TrialFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isLitePlan2024Trial",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object IsEarlyBirdOfferAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isEarlyBirdOfferAvailable",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object PlanTrialAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isTrialAvailable",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── ProPlan companion checks ──────────────────────────────────────────────────
//
// ProPlan$Companion still accepts String for all plan-level methods (v1.20.79).
// ProPlan itself is now an enum, but the Companion's public API is unchanged.

object ProPlanCheckFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan\$Companion;",
    name = "isPro",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object ProPremiumOrHigherCheckFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan\$Companion;",
    name = "isProPremiumOrHigher",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object ProPlanIsTrialFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan\$Companion;",
    name = "isTrial",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object PlanLevelFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan\$Companion;",
    name = "getPlanLevel",
    returnType = "Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlanLevel;",
    parameters = listOf("Ljava/lang/String;", "Z"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── CurrentUser plan flags ────────────────────────────────────────────────────

object CurrentUserFreeFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
    name = "isFree",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object CurrentUserPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
    name = "hasPremiumPlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object CurrentUserUltimateFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
    name = "hasUltimatePlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object CurrentUserAnnualFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
    name = "hasAnnualPlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object CurrentUserMonthlyFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
    name = "hasMonthlyPlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object CurrentUserPaymentProblemsFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
    name = "hasPaymentsProblems",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object CurrentUserAnnualUltimateFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
    name = "hasAnnualUltimatePlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object CurrentUserGooglePlayMerchantFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
    name = "hasGooglePlayMerchant",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object CurrentUserNonGooglePlayMerchantFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
    name = "hasNonGooglePlayMerchant",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── ProfileServiceImpl ────────────────────────────────────────────────────────

object ProfileServiceAnnualUltimateFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/impl/service/ProfileServiceImpl;",
    name = "userHasAnnualUltimatePlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ── Feature/permission flags ──────────────────────────────────────────────────

object FlaggedListsPermissionsFullServiceFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/Permissions\$FlaggedListsPermissions;",
    name = "isFullService",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object FlaggedListsPermissionsRestrictedFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/Permissions\$FlaggedListsPermissions;",
    name = "isRestricted",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── Menu subscription title ───────────────────────────────────────────────────
//
// NOTE (v1.20.79): getSubscriptionTitleRes now accepts the ProPlan enum object,
// not a String. The class descriptor is identical so the fingerprint is unchanged.

object SubscriptionTitleFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/menu/impl/presenter/mapper/MenuItemUiMapper;",
    name = "getSubscriptionTitleRes",
    returnType = "I",
    parameters = listOf("Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan;"),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
)

// ── Benefits root gate ────────────────────────────────────────────────────────
//
// BenefitsInteractorImpl.hasBenefit() is a suspend function called by EVERY
// native feature before granting access (bar replay, custom intervals, multiple
// charts, alerts, ad-free, study-on-study, etc.). Returning Boolean.TRUE
// immediately unlocks all features for the current coroutine call-site.

object BenefitsHasBenefitFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/benefits/impl/interactor/BenefitsInteractorImpl;",
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "Lcom/tradingview/tradingviewapp/benefits/api/model/BenefitName;",
        "Lcom/tradingview/tradingviewapp/benefits/api/model/BenefitPlanLevel;",
        "Lkotlin/coroutines/Continuation;",
    ),
    custom = { method, _ -> method.name == "hasBenefit" },
)

// ── Paywall / GoPro dispatch ──────────────────────────────────────────────────

// Legacy upgrade modal: return-void suppresses the dialog entirely.
object GoProDispatchActionFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/core/interactor/GoProTypeInteractorImpl;",
    returnType = "V",
    parameters = listOf("Lcom/tradingview/tradingviewapp/gopro/api/model/BaseGoProAction;"),
    strings = listOf("action"),
    custom = { method, _ -> method.name == "dispatchAction" },
)

// New paywall system: both overloads are suspend (return Object). Return null.
object PaywallDispatchPaywallObjectFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/paywalls/impl/interactor/PaywallInteractorImpl;",
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "Lcom/tradingview/paywalls/api/model/Paywall;",
        "Lcom/tradingview/paywalls/api/model/Paywall\$Source;",
        "Lcom/tradingview/paywalls/api/model/PaywallParams;",
        "Lkotlin/coroutines/Continuation;",
    ),
    accessFlags = listOf(AccessFlags.PUBLIC),
    custom = { method, _ -> method.name == "dispatchPaywall" },
)

object PaywallDispatchPaywallStringFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/paywalls/impl/interactor/PaywallInteractorImpl;",
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "Ljava/lang/String;",
        "Lcom/tradingview/paywalls/api/model/Paywall\$Source;",
        "Lcom/tradingview/paywalls/api/model/PaywallParams;",
        "Lkotlin/coroutines/Continuation;",
    ),
    accessFlags = listOf(AccessFlags.PUBLIC),
    custom = { method, _ -> method.name == "dispatchPaywall" },
)

// ── Trial / offer suppression ─────────────────────────────────────────────────

object TrialDaysFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/gopro/interactor/TrialPeriodInteractorImpl;",
    name = "getDaysOfTrialIfAvailable",
    returnType = "Ljava/lang/Integer;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ── Native GoPro availability (suppress native upgrade bottom-sheet) ──────────

object NativeGoProAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/gopro/interactor/NativeGoProAvailabilityInteractorImpl;",
    name = "isNativeGoProAvailable",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lkotlin/coroutines/Continuation;"),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

object NativeGoProFeatureToggleFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/gopro/interactor/NativeGoProAvailabilityInteractorImpl;",
    name = "isFeatureToggleEnabled",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)
