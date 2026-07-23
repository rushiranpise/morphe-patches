package app.template.patches.tradingview

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TRADINGVIEW_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val tradingViewUnlockPremiumPatch = bytecodePatch(
    name = "TradingView Unlock Premium",
    description = "Unlocks Ultimate plan features, disables all paywalls and upgrade dialogs, " +
        "suppresses payment-banned blocking errors, and grants access to all benefits " +
        "including bar replay, custom intervals, multiple charts, study-on-study, " +
        "server-side alerts, and ad-free charts.",
) {
    compatibleWith(TRADINGVIEW_COMPATIBILITY)

    execute {

        // ── 1. Plan identity strings ─────────────────────────────────────────
        // Override plan string to "pro_premium_expert" (Ultimate).
        // ProPlan$Companion.isPro() / isProPremiumOrHigher() / getPlanLevel() all
        // key off this string, so they propagate automatically once it's set.
        PlanStringFingerprint.method.addInstructions(
            0, "const-string v0, \"pro_premium_expert\"\nreturn-object v0",
        )
        NextPlanStringFingerprint.method.addInstructions(
            0, "const-string v0, \"pro_premium_expert\"\nreturn-object v0",
        )
        BillingCycleFingerprint.method.addInstructions(
            0, "const-string v0, \"annual\"\nreturn-object v0",
        )

        // ── 2. WebChart user plan ────────────────────────────────────────────
        // The webchart WebView receives the plan string via UserPlanEntity.getUserPlan().
        // Most server-side limits (bar history, connections) are enforced server-side
        // and cannot be bypassed; however, the app also uses this locally to decide
        // which features to expose in the native → web bridge.
        WebChartUserPlanFingerprint.methodOrNull?.addInstructions(
            0, "const-string v0, \"pro_premium_expert\"\nreturn-object v0",
        )

        // ── 3. ProPlan companion checks ──────────────────────────────────────
        ProPlanCheckFingerprint.method.returnEarly(true)
        ProPremiumOrHigherCheckFingerprint.method.returnEarly(true)
        ProPlanIsTrialFingerprint.method.returnEarly(false)

        // ProPlanLevel.ULTIMATE — return the enum value directly
        PlanLevelFingerprint.method.addInstructions(
            0,
            """
            sget-object v0, Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlanLevel;->ULTIMATE:Lcom/tradingview/tradingviewapp/gopro/model/plan/ProPlanLevel;
            return-object v0
            """.trimIndent(),
        )

        // ── 4. Plan boolean flags ────────────────────────────────────────────
        PlanIsProPlanFingerprint.method.returnEarly(true)
        RenewalActiveFingerprint.method.returnEarly(true)
        PlanTrialAvailableFingerprint.method.returnEarly(false) // already "paid", no trial
        GracePeriodFingerprint.method.returnEarly(false)
        HoldPeriodFingerprint.method.returnEarly(false)
        IsLitePlan2023Fingerprint.method.returnEarly(false)
        IsLitePlan2024Fingerprint.method.returnEarly(false)
        IsLitePlan2024TrialFingerprint.method.returnEarly(false)
        IsEarlyBirdOfferAvailableFingerprint.method.returnEarly(false)

        // ── 5. Plan.isPaymentsBanned() — boxed Boolean return ────────────────
        // This method returns Ljava/lang/Boolean; (nullable boxed), NOT primitive Z.
        // When the server sets it to true, fetchUserPlanInfo() constructs a
        // UserPlanInfo with isPaymentsBanned=true, which feeds getBlockingErrorOrNull()
        // → BannedError, locking the payment management UI.
        // We return the static Boolean.FALSE constant (boxed false).
        PlanIsPaymentsBannedFingerprint.method.addInstructions(
            0,
            """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v0
            """.trimIndent(),
        )

        // ── 6. CurrentUser plan flags ────────────────────────────────────────
        CurrentUserFreeFingerprint.method.returnEarly(false)
        CurrentUserPremiumFingerprint.method.returnEarly(true)
        CurrentUserUltimateFingerprint.method.returnEarly(true)
        CurrentUserAnnualFingerprint.method.returnEarly(true)
        CurrentUserMonthlyFingerprint.methodOrNull?.returnEarly(false) // annual wins
        CurrentUserPaymentProblemsFingerprint.method.returnEarly(false)
        CurrentUserAnnualUltimateFingerprint.method.returnEarly(true)
        CurrentUserGooglePlayMerchantFingerprint.method.returnEarly(true)
        CurrentUserNonGooglePlayMerchantFingerprint.method.returnEarly(false)

        // ── 7. ProfileServiceImpl ────────────────────────────────────────────
        ProfileServiceAnnualUltimateFingerprint.method.returnEarly(true)

        // ── 8. UserPlanInfo value-object getters ─────────────────────────────
        // These are returned by BaseGoProInteractorImpl.fetchUserPlanInfo() and read
        // downstream by UniversalGoProInteractorImpl and BaseGoProPurchaseDelegateImpl.
        // Patching the Plan/CurrentUser methods above means fetchUserPlanInfo()
        // already builds the correct object — but we also lock the getters
        // directly in case the object was cached before patch took effect.
        UserPlanInfoIsFreeFingerprint.method.returnEarly(false)
        UserPlanInfoIsPaymentsBannedFingerprint.method.returnEarly(false)

        // ── 9. Benefits root gate ────────────────────────────────────────────
        // BenefitsInteractorImpl.hasBenefit() is a suspend function (returns Object).
        // In Kotlin coroutines the first return from a suspend method must be
        // either COROUTINE_SUSPENDED or the actual result.
        // We construct a Boolean.TRUE and return it directly, which is safe because
        // the suspension machinery will unwrap it at the call-site.
        BenefitsHasBenefitFingerprint.method.addInstructions(
            0,
            """
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
            """.trimIndent(),
        )

        // ── 10. Paywall / GoPro upgrade dialogs ──────────────────────────────
        GoProDispatchActionFingerprint.method.returnEarly()
        PaywallDispatchPaywallObjectFingerprint.method.addInstructions(
            0,
            """
            const/4 v0, 0x0
            return-object v0
            """.trimIndent(),
        )
        PaywallDispatchPaywallStringFingerprint.methodOrNull?.addInstructions(
            0,
            """
            const/4 v0, 0x0
            return-object v0
            """.trimIndent(),
        )

        // ── 11. Trial / offer suppression ────────────────────────────────────
        // Returning null means "no trial days available" — prevents the app from
        // showing trial-start prompts over the patched plan.
        TrialDaysFingerprint.methodOrNull?.addInstructions(
            0,
            """
            const/4 v0, 0x0
            return-object v0
            """.trimIndent(),
        )

        // ── 12. Native GoPro upgrade bottom-sheet ────────────────────────────
        NativeGoProAvailableFingerprint.methodOrNull?.addInstructions(
            0,
            """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v0
            """.trimIndent(),
        )
        NativeGoProFeatureToggleFingerprint.methodOrNull?.returnEarly(false)

        // ── 13. Watchlist/list permissions ───────────────────────────────────
        FlaggedListsPermissionsFullServiceFingerprint.methodOrNull?.returnEarly(true)
        FlaggedListsPermissionsRestrictedFingerprint.methodOrNull?.returnEarly(false)

        // ── 14. Menu subscription title ──────────────────────────────────────
        // getSubscriptionTitleRes(ProPlan) returns int (R.string resource ID).
        // We return the "you_are_ultimate" string resource so the side-menu
        // always shows "Ultimate" regardless of server account state.
        SubscriptionTitleFingerprint.methodOrNull?.addInstructions(
            0,
            """
            sget v0, Lcom/tradingview/tradingviewapp/core/locale/R${'$'}string;->info_menu_you_are_ultimate:I
            return v0
            """.trimIndent(),
        )
    }
}
