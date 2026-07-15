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
        // ── Plan string spoofing ──────────────────────────────────────────────
        // Return "pro_premium_expert" (ULTIMATE) everywhere the plan string is read.
        listOf(
            PlanStringFingerprint,
            NextPlanStringFingerprint,
            WebChartUserPlanFingerprint,
        ).forEach { fp ->
            fp.method.apply {
                clearBody()
                addInstructions(0, "const-string v0, \"$ULTIMATE_PLAN\"\nreturn-object v0")
            }
        }

        // ── Billing cycle → annual ────────────────────────────────────────────
        BillingCycleFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"$YEARLY_BILLING\"\nreturn-object v0")
        }

        // ── Boolean gates → true (premium/active) ────────────────────────────
        listOf(
            RenewalActiveFingerprint,
            PlanIsProPlanFingerprint,
            PlanIsAnnualPlanFingerprint,
            ProPlanCheckFingerprint,
            ProPremiumOrHigherCheckFingerprint,
            CurrentUserPremiumFingerprint,
            CurrentUserUltimateFingerprint,
            CurrentUserAnnualFingerprint,
            CurrentUserAnnualUltimateFingerprint,
            ProfileServiceAnnualUltimateFingerprint,
        ).forEach { fp ->
            fp.method.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── Boolean gates → false (free/problem states) ───────────────────────
        listOf(
            GracePeriodFingerprint,
            HoldPeriodFingerprint,
            PlanIsMonthlyPlanFingerprint,
            PlanIsLitePlan2023Fingerprint,
            PlanIsLitePlan2024Fingerprint,
            PlanIsLitePlan2024TrialFingerprint,
            CurrentUserFreeFingerprint,
            CurrentUserMonthlyFingerprint,
            CurrentUserPaymentProblemsFingerprint,
            ProPlanIsTrialFingerprint,
        ).forEach { fp ->
            fp.method.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }

        // ── isPaymentsBanned → Boolean.FALSE ─────────────────────────────────
        PlanIsPaymentsBannedFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                    return-object v0
                """,
            )
        }

        // ── Plan level → ULTIMATE ─────────────────────────────────────────────
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

        // ── Hidden/locked features → empty list ──────────────────────────────
        // getSupportedFeatures() returns the list of features locked behind paywall;
        // returning empty list signals all features are unlocked.
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

        // ── Badge → Ultimate badge ────────────────────────────────────────────
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

        // ── Subscription title → "You are Ultimate" ──────────────────────────
        // R.string.info_menu_you_are_ultimate = 0x7f130383 (verified in v1.20.77)
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