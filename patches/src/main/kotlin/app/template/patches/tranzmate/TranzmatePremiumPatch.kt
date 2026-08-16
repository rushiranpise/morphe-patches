package app.template.patches.tranzmate

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TRANZMATE_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

/**
 * Tranzmate (Moovit+) premium patch — v5.197.1.1801
 *
 * ## Patch layers
 *
 * 1. **MoovitApplication.onCreate** — extension side-car initialiser.
 *
 * 2. **SubscriptionStateFingerprint** — b()Z in the "subscribed_skus"
 *    SharedPrefs wrapper; always returns true (subscribed).
 *
 * 3. **AdUnitResolverFingerprint** — f(AdSource)String (was g() in v5.197.0);
 *    returns "" to suppress all ad unit ID lookups.
 *    Fingerprinted by "is_ads_free_version" string, not method name.
 *
 * 4. **MoovitAdView / MoovitBannerAdView.setAdSource** — hides the views
 *    (GONE) before any ad loading occurs.
 *
 * 5. **SubscriptionPackageStateFingerprint** — always returns ACTIVE so every
 *    feature gate sees an active subscription.
 *
 * 6. **SafeRideCalculateStateFingerprint** — same: always returns ACTIVE.
 *
 * 7. **BlockPaywallGateFingerprint** — a(MoovitActivity)Z; returns false
 *    (paywall disabled). Class rename history: Ly81 (v5.196) → Lw81 (v5.197.0)
 *    → Ljh1 (v1801). Anchored by stable "block_paywall" string.
 *
 * 8. **BlockPaywallActivity.onReady** — calls relaunchCallingActivity() and
 *    exits immediately.
 *
 * 9. **MoovitPlusOnboardingActivity.onReady** — calls Q0() (finish+relaunch
 *    helper; was V0() in v5.197.0) then returns.
 *
 * 10. **Upgrade / purchase UI suppression** — finish() or GONE on:
 *     MoovitPlusActivity, HelpCenterMenuItemFragment, MenuItemFragment,
 *     AdFreeMenuItemFragment, MoovitSubscriptionsPromoCellFragment,
 *     MoovitPlusPackagePopupFragment, MoovitPlusPurchaseFragment,
 *     MoovitPlusPurchaseOffersFragment, MoovitPlusOnboardingPrePurchaseFragment,
 *     FreemiumPopupFragment.
 *
 * 11. **ItineraryBlockedSmartTipsBannerFingerprint** (Lwq7; in v1801, was Liy6;
 *     in v5.197.0) — replaces the MOVE_RESULT before view_blocked_smart_tips_banner
 *     sget with const/4 v6, 0x0, routing to the insight-banner path.
 *
 * 12. **MyMoovitPlusGoPremiumCardFingerprint** (Lr1b; in v1801, was Lbz9; in
 *     v5.197.0) — replaces "move v9, v2" (VISIBLE branch) with "move v9, v3"
 *     so the card is always GONE.
 */
@Suppress("unused")
val tranzmatePremiumPatch = bytecodePatch(
    name = "Unlock Moovit+",
    description = "Unlocks Moovit+ premium features, removes ads, and suppresses all upgrade paywalls and UI.",
    default = true,
) {
    compatibleWith(TRANZMATE_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {

        // 1. Extension initialiser.
        MoovitApplicationOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static {}, Lapp/template/extension/extension/MoovitHelper;->init()V",
        )

        // 2. Subscription state — always subscribed.
        SubscriptionStateFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x1
                return p0
            """.trimIndent(),
        )

        // 3. Ad unit resolver — return empty string (suppresses all ad unit lookups).
        //    Method renamed from g() to f() in v1801; fingerprinted by string, not name.
        AdUnitResolverFingerprint.method.addInstructions(
            0,
            """
                const-string v0, ""
                return-object v0
            """.trimIndent(),
        )

        // 4a. MoovitAdView — hide before ad loads.
        MoovitAdViewSetSourceFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p0, v0}, Landroid/view/View;->setVisibility(I)V
                return-void
            """.trimIndent(),
        )

        // 4b. MoovitBannerAdView — same.
        MoovitBannerAdViewSetSourceFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p0, v0}, Landroid/view/View;->setVisibility(I)V
                return-void
            """.trimIndent(),
        )

        // 5. Package state — always ACTIVE.
        SubscriptionPackageStateFingerprint.method.addInstructions(
            0,
            """
                sget-object p0, Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;->ACTIVE:Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;
                return-object p0
            """.trimIndent(),
        )

        // 6. SafeRide feature — always ACTIVE.
        SafeRideCalculateStateFingerprint.method.addInstructions(
            0,
            """
                sget-object p0, Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;->ACTIVE:Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;
                return-object p0
            """.trimIndent(),
        )

        // 7. Paywall gate — return false (paywall disabled).
        BlockPaywallGateFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x0
                return p0
            """.trimIndent(),
        )

        // 8. BlockPaywallActivity — re-launch calling activity and exit.
        BlockPaywallActivityOnReadyFingerprint.method.addInstructions(
            0,
            """
                invoke-direct {p0}, Lcom/moovit/app/plus/paywall/BlockPaywallActivity;->relaunchCallingActivity()V
                return-void
            """.trimIndent(),
        )

        // 9. Onboarding activity — call Q0() (finish+relaunch helper).
        //    Was V0() in v5.197.0; renamed to Q0() in v1801.
        MoovitPlusOnboardingActivityFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Lcom/moovit/app/plus/onboarding/MoovitPlusOnboardingActivity;->Q0()V
                return-void
            """.trimIndent(),
        )

        // 10. Upgrade / purchase UI suppression.
        MoovitPlusActivityOnReadyFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroid/app/Activity;->finish()V
                return-void
            """.trimIndent(),
        )

        MoovitPlusHelpCenterMenuItemFingerprint.method.addInstructions(0, "return-void")

        MoovitPlusMenuItemFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p1, v0}, Landroid/view/View;->setVisibility(I)V
                return-void
            """.trimIndent(),
        )

        // AdFreeMenuItem: hide after view inflation (insert before final return).
        AdFreeMenuItemFingerprint.method.addInstructions(
            AdFreeMenuItemFingerprint.method.implementation!!.instructions.lastIndex,
            """
                const/16 p2, 0x8
                invoke-virtual {p1, p2}, Landroid/view/View;->setVisibility(I)V
            """.trimIndent(),
        )

        PromoCellFragmentOnViewCreatedFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p1, v0}, Landroid/view/View;->setVisibility(I)V
                return-void
            """.trimIndent(),
        )

        MoovitPlusPackagePopupFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroidx/fragment/app/i;->dismiss()V
                return-void
            """.trimIndent(),
        )

        MoovitPlusPurchaseFragmentFingerprint.method.addInstructions(0, "return-void")

        MoovitPlusPurchaseOffersFragmentFingerprint.method.addInstructions(0, "return-void")

        MoovitPlusOnboardingPrePurchaseFragmentFingerprint.method.addInstructions(0, "return-void")

        FreemiumPopupFragmentFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroidx/fragment/app/i;->dismiss()V
                return-void
            """.trimIndent(),
        )

        // 11. Smart tips upsell banner — replace the MOVE_RESULT immediately
        //     before the view_blocked_smart_tips_banner sget with const/4 v6, 0x0.
        //     The fingerprint now uses fieldAccess to Li4d;->view_blocked_smart_tips_banner
        //     so instructionMatches[0] IS the sget. We step back to find the
        //     MOVE_RESULT that precedes it.
        val smartTipsSgetIndex =
            ItineraryBlockedSmartTipsBannerFingerprint.instructionMatches[0].index

        val smartTipsInstructions =
            ItineraryBlockedSmartTipsBannerFingerprint.method.implementation!!.instructions

        val smartTipsMoveResultIndex = smartTipsInstructions
            .subList(0, smartTipsSgetIndex)
            .indexOfLast { it.opcode == Opcode.MOVE_RESULT }
        check(smartTipsMoveResultIndex >= 0) { "MOVE_RESULT predecessor to smart tips sget not found." }

        ItineraryBlockedSmartTipsBannerFingerprint.method.replaceInstruction(
            smartTipsMoveResultIndex,
            "const/4 v6, 0x0",
        )

        // 13. Favorite location address search — flip c=false → c=true in
        //     FavoriteLocationEditorActivity.h1(). Instruction index 5 is
        //     "const/4 v5, 0x0" (p5 in AppSearchLocationCallback constructor
        //     = field c = addAddressProvider). When false, qn5 geocode provider
        //     is never registered — only transit stops appear in search results.
        //     "favorites_editor" string is unique to this method (verified v1801).
        FavoriteLocationAddressSearchFingerprint.method.replaceInstruction(
            5,
            "const/4 v5, 0x1",
        )

        // 12. Go Premium card — replace "move v9, v2" (VISIBLE branch) with
        //     "move v9, v3" so the card is permanently GONE.
        //     Navigates by finding setVisibility after the "goPremiumCard" string
        //     null-check marker, then steps back 3 instructions. (Lr1b; in v1801,
        //     was Lbz9; in v5.197.0.)
        val goPremiumInstructions =
            MyMoovitPlusGoPremiumCardFingerprint.method.implementation!!.instructions

        val goPremiumStringIndex = goPremiumInstructions.indexOfFirst { instruction ->
            (instruction as? ReferenceInstruction)?.reference.toString()
                .contains("goPremiumCard") == true
        }
        check(goPremiumStringIndex > 0) { "goPremiumCard null-check marker not found." }

        val setVisibilityOffset = goPremiumInstructions
            .drop(goPremiumStringIndex)
            .indexOfFirst { instruction ->
                (instruction as? ReferenceInstruction)?.reference.toString()
                    .contains("Landroid/view/View;->setVisibility(I)V") == true
            }
        check(setVisibilityOffset > 0) { "setVisibility call not found after goPremiumCard marker." }

        MyMoovitPlusGoPremiumCardFingerprint.method.replaceInstruction(
            goPremiumStringIndex + setVisibilityOffset - 3,
            "move v9, v3",
        )
    }
}
