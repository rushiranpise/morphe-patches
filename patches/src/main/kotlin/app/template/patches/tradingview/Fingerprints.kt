package app.template.patches.tradingview

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── Plan data class (gopro/model/plan/Plan) ──────────────────────────────────

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

/** Plan.isProPlan()Z — true if user has any paid plan; force true to gate pro-only UI */
object PlanIsProPlanFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isProPlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/** Plan.isAnnualPlan()Z — annual billing; force true (we spoof 1y billing cycle anyway) */
object PlanIsAnnualPlanFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isAnnualPlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/** Plan.isMonthlyPlan()Z — monthly billing; force false */
object PlanIsMonthlyPlanFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isMonthlyPlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/** Plan.isLitePlan2023()Z — lite/free 2023 tier; force false */
object PlanIsLitePlan2023Fingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isLitePlan2023",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/** Plan.isLitePlan2024()Z — lite/free 2024 tier; force false */
object PlanIsLitePlan2024Fingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isLitePlan2024",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/** Plan.isLitePlan2024Trial()Z — trial tier; force false */
object PlanIsLitePlan2024TrialFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isLitePlan2024Trial",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/** Plan.isPaymentsBanned()Boolean — nullable ban flag; force Boolean.FALSE */
object PlanIsPaymentsBannedFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/Plan;",
    name = "isPaymentsBanned",
    returnType = "Ljava/lang/Boolean;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── ProPlan$Companion ─────────────────────────────────────────────────────────

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

/** ProPlan$Companion.isTrial(String)Z — force false so app never shows trial UI */
object ProPlanIsTrialFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan\$Companion;",
    name = "isTrial",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── WebChart entity ───────────────────────────────────────────────────────────

object WebChartUserPlanFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/webchart/implementation/entity/UserPlanEntity;",
    name = "getUserPlan",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── Chart model ───────────────────────────────────────────────────────────────

object HiddenFeaturesFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/chart/model/FeatureInfo;",
    name = "getSupportedFeatures",
    returnType = "Ljava/util/List;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── CurrentUser ───────────────────────────────────────────────────────────────

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

// ── ProfileServiceImpl ────────────────────────────────────────────────────────

object ProfileServiceAnnualUltimateFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/impl/service/ProfileServiceImpl;",
    name = "userHasAnnualUltimatePlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ── Badge & menu ──────────────────────────────────────────────────────────────

object PriorityBadgeFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/BadgeDelegate;",
    name = "getPriorityBadge",
    returnType = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/UserBadge;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object SubscriptionTitleFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/menu/impl/presenter/mapper/MenuItemUiMapper;",
    name = "getSubscriptionTitleRes",
    returnType = "I",
    parameters = listOf("Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlan;"),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
)