package app.template.patches.tradingview

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

private const val ULTIMATE_PLAN = "pro_premium_expert"
private const val YEARLY_BILLING = "1y"

@Suppress("unused")
val tradingViewUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlock premium features in app.",
) {
    compatibleWith(Constants.TRADINGVIEW_COMPATIBILITY)

    execute {
        listOf(
            PlanStringFingerprint,
            NextPlanStringFingerprint,
            WebChartUserPlanFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(0, "const-string v0, \"$ULTIMATE_PLAN\"\nreturn-object v0")
            }
        }

        BillingCycleFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"$YEARLY_BILLING\"\nreturn-object v0")
        }

        listOf(
            RenewalActiveFingerprint,
            ProPlanCheckFingerprint,
            ProPremiumOrHigherCheckFingerprint,
            CurrentUserPremiumFingerprint,
            CurrentUserUltimateFingerprint,
            CurrentUserAnnualFingerprint,
            CurrentUserAnnualUltimateFingerprint,
            ProfileServiceAnnualUltimateFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        listOf(
            GracePeriodFingerprint,
            HoldPeriodFingerprint,
            CurrentUserFreeFingerprint,
            CurrentUserMonthlyFingerprint,
            CurrentUserPaymentProblemsFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }

        PlanLevelFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlanLevel;->ULTIMATE:Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlanLevel;
                    return-object v0
                """,
            )
        }

        HiddenFeaturesFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    invoke-static {}, Lkotlin/collections/CollectionsKt;->emptyList()Ljava/util/List;
                    move-result-object v0
                    return-object v0
                """,
            )
        }

        PriorityBadgeFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Lcom/tradingview/tradingviewapp/feature/profile/model/user/UserBadge;
                    const-string v1, "pro:pro_premium_expert"
                    sget-object v2, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                    invoke-direct {v0, v1, v2}, Lcom/tradingview/tradingviewapp/feature/profile/model/user/UserBadge;-><init>(Ljava/lang/String;Ljava/lang/Boolean;)V
                    return-object v0
                """,
            )
        }

        SubscriptionTitleFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const v0, 0x7f130383
                    return v0
                """,
            )
        }
    }
}
