package app.template.patches.tradingview

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TRADINGVIEW_COMPATIBILITY
import app.template.patches.shared.returnEarly

// TradingView premium patch — v1.20.79
//
// Architecture changes since v1.20.78:
//   • ProPlan refactored from String alias → proper Kotlin enum with ULTIMATE,
//     EXPERT, PRO_PREMIUM, PRO_PLUS, PRO, PRO_LITE + *_TRIAL variants.
//   • Plan.getProPlan() is now overloaded:
//       getProPlan()String  — legacy string alias (still present, still patched)
//       getProPlan()ProPlan — new enum object (callers that use enum unaffected
//                             because we patch isPro/isProPremiumOrHigher directly)
//   • UserPlanInfo value class removed — the two UserPlanInfoIsFree /
//     UserPlanInfoIsPaymentsBanned fingerprints are dropped; their purpose is
//     fully covered by patching Plan.isPaymentsBanned() and CurrentUser.isFree().
//   • MenuItemUiMapper.getSubscriptionTitleRes() now accepts ProPlan enum,
//     not String — fingerprint param descriptor unchanged (same class path).
//
// Patch strategy: unchanged from v1.20.78 except for removals listed above.

@Suppress("unused")
val tradingViewUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Ultimate plan features, disables all paywalls and upgrade dialogs, " +
        "suppresses payment-banned blocking errors, and grants access to all benefits " +
        "including bar replay, custom intervals, multiple charts, study-on-study, " +
        "server-side alerts, and ad-free charts.",
) {
    compatibleWith(TRADINGVIEW_COMPATIBILITY)

    execute {

        // ── 1. Plan identity strings ─────────────────────────────────────────
        // Inject "pro_premium_expert" (Ultimate) into the String-returning overload.
        // ProPlan$Companion.isPro/isProPremiumOrHigher/getPlanLevel all accept String
        // and will propagate the correct level automatically.
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
        // UserPlanEntity.getUserPlan() feeds the native→web bridge used by the
        // WebView chart engine to decide which features to expose locally.
        WebChartUserPlanFingerprint.methodOrNull?.addInstructions(
            0, "const-string v0, \"pro_premium_expert\"\nreturn-object v0",
        )

        // ── 3. ProPlan companion checks ──────────────────────────────────────
        ProPlanCheckFingerprint.method.returnEarly(true)
        ProPremiumOrHigherCheckFingerprint.method.returnEarly(true)
        ProPlanIsTrialFingerprint.method.returnEarly(false)

        // Return the ULTIMATE enum constant from getPlanLevel().
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
        PlanTrialAvailableFingerprint.method.returnEarly(false)
        GracePeriodFingerprint.method.returnEarly(false)
        HoldPeriodFingerprint.method.returnEarly(false)
        IsLitePlan2023Fingerprint.method.returnEarly(false)
        IsLitePlan2024Fingerprint.method.returnEarly(false)
        IsLitePlan2024TrialFingerprint.method.returnEarly(false)
        IsEarlyBirdOfferAvailableFingerprint.method.returnEarly(false)

        // ── 5. Plan.isPaymentsBanned() — boxed Boolean ───────────────────────
        // Returns Ljava/lang/Boolean; (nullable). Boolean.TRUE causes BannedError
        // → locks the upgrade screen. We return Boolean.FALSE to suppress it.
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
        CurrentUserMonthlyFingerprint.methodOrNull?.returnEarly(false)
        CurrentUserPaymentProblemsFingerprint.method.returnEarly(false)
        CurrentUserAnnualUltimateFingerprint.method.returnEarly(true)
        CurrentUserGooglePlayMerchantFingerprint.method.returnEarly(true)
        CurrentUserNonGooglePlayMerchantFingerprint.method.returnEarly(false)

        // ── 7. ProfileServiceImpl ────────────────────────────────────────────
        ProfileServiceAnnualUltimateFingerprint.method.returnEarly(true)

        // ── 8. Benefits root gate ────────────────────────────────────────────
        // hasBenefit() is a Kotlin suspend function (returns Object). Returning
        // Boolean.TRUE immediately is safe — coroutine call-sites unwrap it.
        BenefitsHasBenefitFingerprint.method.addInstructions(
            0,
            """
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
            """.trimIndent(),
        )

        // ── 9. Paywall / GoPro upgrade dialogs ──────────────────────────────
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

        // ── 10. Trial / offer suppression ────────────────────────────────────
        // Null means "no trial days available" — prevents trial-start prompts.
        TrialDaysFingerprint.methodOrNull?.addInstructions(
            0,
            """
            const/4 v0, 0x0
            return-object v0
            """.trimIndent(),
        )

        // ── 11. Native GoPro upgrade bottom-sheet ────────────────────────────
        NativeGoProAvailableFingerprint.methodOrNull?.addInstructions(
            0,
            """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v0
            """.trimIndent(),
        )
        NativeGoProFeatureToggleFingerprint.methodOrNull?.returnEarly(false)

        // ── 12. Watchlist/list permissions ───────────────────────────────────
        FlaggedListsPermissionsFullServiceFingerprint.methodOrNull?.returnEarly(true)
        FlaggedListsPermissionsRestrictedFingerprint.methodOrNull?.returnEarly(false)

        // ── 13. Menu subscription title ──────────────────────────────────────
        // Returns the R.string ID for "You are Ultimate" so the side-menu always
        // shows Ultimate regardless of the server-side account state.
        SubscriptionTitleFingerprint.methodOrNull?.addInstructions(
            0,
            """
            sget v0, Lcom/tradingview/tradingviewapp/core/locale/R${'$'}string;->info_menu_you_are_ultimate:I
            return v0
            """.trimIndent(),
        )
    }
}
