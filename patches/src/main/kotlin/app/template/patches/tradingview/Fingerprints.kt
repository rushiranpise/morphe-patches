package app.template.patches.tradingview

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

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

object WebChartUserPlanFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/webchart/implementation/entity/UserPlanEntity;",
    name = "getUserPlan",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object HiddenFeaturesFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/chart/model/FeatureInfo;",
    name = "getSupportedFeatures",
    returnType = "Ljava/util/List;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

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

object ProfileServiceAnnualUltimateFingerprint : Fingerprint(
    definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/impl/service/ProfileServiceImpl;",
    name = "userHasAnnualUltimatePlan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

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
