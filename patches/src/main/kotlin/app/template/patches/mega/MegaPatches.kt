package app.template.patches.mega

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MEGA_COMPATIBILITY

/**
 * Unlock Pro — the single entitlement patch for MEGA.
 *
 * Fixes all account-type gates by patching two chokepoints:
 *
 * ① AccountTypeMapper.a(I) [static] — the sole mapping function called by
 *   DefaultAccountRepository whenever it constructs AccountLevelDetail or
 *   AccountSubscriptionDetail from SDK proLevel values. Forcing PRO_I here
 *   propagates to every consumer:
 *     • ShouldShowUpgradeAccountUseCase   → AccountLevelDetail.a != FREE → false (hide upgrade)
 *     • GetCurrentSubscriptionPlanUseCase → UserAccount.f = PRO_I (shows "PRO I" in UI)
 *     • TransferOverQuotaViewModel         → AccountLevelDetail.a != FREE → no quota banner
 *     • MyAccountHomeViewModel             → AccountLevelDetail.a != FREE → hides upgrade CTA
 *     • ScheduledMeetingManagementViewModel, RubbishBinViewModel, etc.
 *
 * ② AccountType.isPaid() — feature-gate helper on the AccountType enum.
 *   Returns false only for FREE and UNKNOWN. Forcing true unconditionally
 *   unlocks hidden nodes (Hide/Unhide toolbar & bottom-sheet items,
 *   HiddenNodesOnboarding screen, MonitorHiddenNodesEnabledUseCase),
 *   image preview Pro lock, and any other isPaid() call-site.
 *
 * Ads are removed as part of this patch (hidden under Pro) since MEGA only
 * shows ads to free accounts and uses the same entitlement pipeline.
 */
@Suppress("unused")
val megaUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all Pro features.",
) {
    compatibleWith(MEGA_COMPATIBILITY)

    execute {
        // ① Force AccountTypeMapper to always return PRO_I
        AccountTypeMapperFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lmega/privacy/android/domain/entity/AccountType;->PRO_I:Lmega/privacy/android/domain/entity/AccountType;
                return-object v0
            """,
        )

        // ② Force AccountType.isPaid() to always return true
        AccountTypeIsPaidFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // ③ Noop GoogleAdsManager.c() — prevents BannerAdRequest creation
        //    NewAdsContainer skips rendering when BannerAdRequest is null
        GoogleAdsManagerCreateAdRequestFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )

        // ④ Noop AdsViewModel.checkForAdsAvailability() — prevents isAdsFeatureEnabled
        //    from being written true, blocking the upstream trigger for ③
        AdsViewModelCheckAvailabilityFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lkotlin/Unit;->a:Lkotlin/Unit;
                return-object v0
            """,
        )
    }
}
