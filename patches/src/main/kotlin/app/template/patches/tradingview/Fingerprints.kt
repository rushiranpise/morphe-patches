package app.template.patches.tradingview

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── Plan identity ──────────────────────────────────────────────────────────────

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

// ── Plan boolean flags → active subscription state ────────────────────────────

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

/**
 * Plan.isProPlan() — delegates to ProPlan$Companion.isPro()/isTrial().
 * The existing patch overrides getProPlan() string to "pro_premium_expert", so isPro()
 * and isTrial() are also affected — but we patch isProPlan() directly as belt-and-suspenders
 * for any call-sites that bypass getProPlan().
 */
object PlanIsProPlanFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isProPlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/**
 * Plan.isPaymentsBanned() — returns boxed Boolean (Ljava/lang/Boolean;), not primitive Z.
 * Called by BaseGoProInteractorImpl.fetchUserPlanInfo() and UniversalGoProInteractorImpl
 * userInfoFlow. When non-null and equal to Boolean.TRUE it constructs a BannedError
 * BlockingError object that locks the upgrade screen and can suppress feature access.
 * Patch: return Boolean.FALSE (the boxed constant).
 */
object PlanIsPaymentsBannedFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isPaymentsBanned",
    returnType = "Ljava/lang/Boolean;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── ProPlan companion checks ───────────────────────────────────────────────────

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

object PlanLevelFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan\$Companion;",
    name = "getPlanLevel",
    returnType = "Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlanLevel;",
    parameters = listOf("Ljava/lang/String;", "Z"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── CurrentUser plan flags ─────────────────────────────────────────────────────

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

// ── ProfileServiceImpl ─────────────────────────────────────────────────────────

object ProfileServiceAnnualUltimateFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/impl/service/ProfileServiceImpl;",
    name = "userHasAnnualUltimatePlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ── UserPlanInfo (constructed by fetchUserPlanInfo) ────────────────────────────

/**
 * UserPlanInfo.isFree() — backing getter on the value object built by
 * BaseGoProInteractorImpl.fetchUserPlanInfo(). Used by the universal GoPro screen
 * to decide whether to show upgrade CTAs and lock the subscription management UI.
 * Patch: return false → user treated as paid.
 */
object UserPlanInfoIsFreeFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/core/model/UserPlanInfo;",
    name = "isFree",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/**
 * UserPlanInfo.isPaymentsBanned() — primitive Z getter on the same value object.
 * Constructed from Plan.isPaymentsBanned() by fetchUserPlanInfo(); fed into
 * getBlockingErrorOrNull() which returns a BannedError that locks the paywall screen.
 * Patch: return false → no banned error emitted.
 */
object UserPlanInfoIsPaymentsBannedFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/core/model/UserPlanInfo;",
    name = "isPaymentsBanned",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── Feature/permission flags ───────────────────────────────────────────────────

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

// ── Menu subscription title ────────────────────────────────────────────────────

object SubscriptionTitleFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/menu/impl/presenter/mapper/MenuItemUiMapper;",
    name = "getSubscriptionTitleRes",
    returnType = "I",
    parameters = listOf("Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan;"),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
)

// ── Benefits root gate ────────────────────────────────────────────────────────

/**
 * BenefitsInteractorImpl.hasBenefit(BenefitName, BenefitPlanLevel, Continuation) — ROOT GATE.
 *
 * Suspend function called by EVERY native feature before allowing access:
 *   BAR_REPLAY_INTRADAY/DAILY/MINUTES/SECONDS/TICKS, INDICATORS_REPLAY,
 *   KAGI_RENKO, CUSTOM_INTERVALS, CUSTOM_RANGE_BARS, CUSTOM_FORMULAS,
 *   EXPORT_CHART_DATA, STUDY_ON_STUDY, VOLUME_FOOTPRINT, VOLUME_CANDLES,
 *   CHART_PATTERNS, SECONDS_INTERVALS, TICK_INTERVALS, TV_VOLUMEBYPRICE,
 *   HISTORICAL_BARS, BACKEND_CONNECTIONS, MULTIPLE_CHARTS, MULTIPLE_WATCHLISTS,
 *   SERVER_SIDE_ALERTS_PRIMITIVE/COMPLEX/WATCHLIST, ALERTS_MULTICONDITIONS,
 *   ALERTS_NO_EXPIRATION, SECOND_BASED_ALERTS, INVITE_ONLY_INDICATORS,
 *   TPO_CHART_STYLE, AD_FREE_CHARTS, AD_FREE_SOCIAL, FIRST_PRIORITY_SUPPORT,
 *   BUY_PRO_DATA, WATCHLIST_SYMBOLS, WATCHLIST_CUSTOM_COLUMNS, BACKTESTING,
 *   ACCESS_ACROSS_DEVICES, MOBILE_PUSH_ALERTS, CANDLESTICK_PATTERNS, and 50+ more.
 *
 * Returns Boolean (suspend). Patch: immediately return Boolean.TRUE.
 */
object BenefitsHasBenefitFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/benefits/impl/interactor/BenefitsInteractorImpl;",
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "Lcom/tradingview/tradingviewapp/benefits/api/model/BenefitName;",
        "Lcom/tradingview/tradingviewapp/benefits/api/model/BenefitPlanLevel;",
        "Lkotlin/coroutines/Continuation;",
    ),
    custom = { method, _ -> method.name == "hasBenefit" }
)

// ── Paywall / GoPro dispatch ──────────────────────────────────────────────────

/**
 * GoProTypeInteractorImpl.dispatchAction(BaseGoProAction) — legacy upgrade modal trigger.
 * Patch: return-void → upgrade dialog never shows.
 */
object GoProDispatchActionFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/core/interactor/GoProTypeInteractorImpl;",
    returnType = "V",
    parameters = listOf("Lcom/tradingview/tradingviewapp/gopro/api/model/BaseGoProAction;"),
    strings = listOf("action"),
    custom = { method, _ -> method.name == "dispatchAction" }
)

/**
 * PaywallInteractorImpl.dispatchPaywall(Paywall, Source, Params, Continuation) — new paywall.
 * Patch: return null (valid suspend no-op).
 */
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

/**
 * PaywallInteractorImpl.dispatchPaywall(String, Source, Params, Continuation) — string-key overload.
 * Patch: return null (no-op).
 */
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

object PlanTrialAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isTrialAvailable",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
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

// ── Lite plan / early-bird / trial-type gates (v1.20.77+) ────────────────────

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

object ProPlanIsTrialFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan\$Companion;",
    name = "isTrial",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)




