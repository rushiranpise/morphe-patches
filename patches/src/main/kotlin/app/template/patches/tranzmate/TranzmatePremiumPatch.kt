package app.template.patches.tranzmate

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TRANZMATE_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

// Tranzmate (Moovit+) premium patch — v5.197.0.1799
//
// Patch layers:
//
//  1. MoovitHelper.init() — extension side-car for any runtime hooks.
//
//  2. SubscriptionStateFingerprint — b()Z in the "subscribed_skus" SharedPrefs
//     wrapper; always returns true (subscribed).
//
//  3. AdUnitResolverFingerprint — g(AdSource)String; returns "" to suppress
//     all ad unit ID lookups.
//
//  4. MoovitAdView / MoovitBannerAdView — setAdSource: hide the views
//     (GONE) before any ad loading occurs.
//
//  5. SubscriptionPackageStateFingerprint — always returns ACTIVE so every
//     feature-gate that checks package state sees an active subscription.
//
//  6. SafeRideCalculateStateFingerprint — same: always returns ACTIVE.
//
//  7. BlockPaywallGateFingerprint — a(MoovitActivity)Z; returns false
//     (paywall gate disabled). Class was Ly81; in v5.196, Lw81; in v5.197;
//     pinned by "block_paywall" string anchor in Fingerprints.kt.
//
//  8. BlockPaywallActivityOnReadyFingerprint — immediately calls
//     relaunchCallingActivity() so the blocking screen dismisses itself.
//
//  9. MoovitPlusOnboardingActivityFingerprint — calls V0() (the
//     finish+relaunch helper, renamed from P0() in v5.196) then returns.
//
// 10. Upgrade / purchase UI suppression — finish() or setVisibility(GONE) on
//     MoovitPlusActivity, HelpCenter, MenuItem, AdFreeMenuItem, PackagePopup,
//     PurchaseFragment, PurchaseOffersFragment, OnboardingPrePurchaseFragment,
//     FreemiumPopupFragment, PromoCellFragment.
//
// 11. ItineraryBlockedSmartTipsBannerFingerprint — replaces the MOVE_RESULT
//     feeding the isSmartTips boolean with const/4 v6, 0x0, routing the flow
//     to the insight-banner path and suppressing the smart tips upsell. Class
//     was Lmy6; in v5.196, Liy6; in v5.197; param Llx2; → Lix2;.
//
// 12. MyMoovitPlusGoPremiumCardFingerprint — replaces the VISIBLE branch's
//     "move v9, v2" (index 38) with "move v9, v3" so the card is always GONE.
//     Class was Lzy9; in v5.196, Lbz9; in v5.197; param Llx2; → Lix2;.

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

        // 3. Ad unit resolver — return empty string to suppress all ads.
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

        // 4b. MoovitBannerAdView — same treatment.
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

        // 7. Paywall gate — return false (no paywall).
        BlockPaywallGateFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x0
                return p0
            """.trimIndent(),
        )

        // 8. BlockPaywallActivity — re-launch the calling activity and exit.
        BlockPaywallActivityOnReadyFingerprint.method.addInstructions(
            0,
            """
                invoke-direct {p0}, Lcom/moovit/app/plus/paywall/BlockPaywallActivity;->relaunchCallingActivity()V
                return-void
            """.trimIndent(),
        )

        // 9. Onboarding — call V0() (renamed from P0() in v5.196) which reads
        //    "activity_to_start_on_finish" from the intent, starts that activity,
        //    then calls finish(). This mirrors the original dismiss flow.
        MoovitPlusOnboardingActivityFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Lcom/moovit/app/plus/onboarding/MoovitPlusOnboardingActivity;->V0()V
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

        MoovitPlusHelpCenterMenuItemFingerprint.method.addInstructions(
            0, "return-void",
        )

        MoovitPlusMenuItemFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p1, v0}, Landroid/view/View;->setVisibility(I)V
                return-void
            """.trimIndent(),
        )

        // AdFreeMenuItem: insert GONE + hide at the penultimate instruction
        // (just before the final return) so the view is hidden after inflation.
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

        MoovitPlusPurchaseFragmentFingerprint.method.addInstructions(
            0, "return-void",
        )

        MoovitPlusPurchaseOffersFragmentFingerprint.method.addInstructions(
            0, "return-void",
        )

        MoovitPlusOnboardingPrePurchaseFragmentFingerprint.method.addInstructions(
            0, "return-void",
        )

        FreemiumPopupFragmentFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroidx/fragment/app/i;->dismiss()V
                return-void
            """.trimIndent(),
        )

        // 11. Smart tips banner — replace the MOVE_RESULT before the
        //     view_blocked_smart_tips_banner sget with const/4 v6, 0x0.
        //     This sends the flow down the insight-banner path (opcode index 439).
        val smartTipsInstructions =
            ItineraryBlockedSmartTipsBannerFingerprint.method.implementation!!.instructions

        val smartTipsSgetIndex = smartTipsInstructions.indexOfFirst { instruction ->
            (instruction as? ReferenceInstruction)?.reference.toString()
                .contains("view_blocked_smart_tips_banner") == true
        }
        check(smartTipsSgetIndex > 0) {
            "Moovit blocked smart tips banner sget not found."
        }

        val smartTipsMoveResultIndex = smartTipsInstructions
            .subList(0, smartTipsSgetIndex)
            .indexOfLast { it.opcode == Opcode.MOVE_RESULT }
        check(smartTipsMoveResultIndex >= 0) {
            "Moovit blocked smart tips MOVE_RESULT predecessor not found."
        }

        ItineraryBlockedSmartTipsBannerFingerprint.method.replaceInstruction(
            smartTipsMoveResultIndex,
            "const/4 v6, 0x0",
        )

        // 12. GoPremiumCard — replace "move v9, v2" (VISIBLE branch, index 38)
        //     with "move v9, v3" so the card is permanently GONE.
        //     v2=0 (VISIBLE) and v3=8 (GONE) are set at indices 3 and 4 in the
        //     same method, so registers are stable regardless of the branch taken.
        val goPremiumInstructions =
            MyMoovitPlusGoPremiumCardFingerprint.method.implementation!!.instructions

        val goPremiumStringIndex = goPremiumInstructions.indexOfFirst { instruction ->
            (instruction as? ReferenceInstruction)?.reference.toString()
                .contains("goPremiumCard") == true
        }
        check(goPremiumStringIndex > 0) {
            "Moovit+ goPremiumCard null-check marker not found."
        }

        // Find the first setVisibility call after the goPremiumCard marker.
        val setVisibilityOffsetFromMarker = goPremiumInstructions
            .drop(goPremiumStringIndex)
            .indexOfFirst { instruction ->
                (instruction as? ReferenceInstruction)?.reference.toString()
                    .contains("Landroid/view/View;->setVisibility(I)V") == true
            }
        check(setVisibilityOffsetFromMarker > 0) {
            "Moovit+ goPremiumCard setVisibility call not found after marker."
        }

        val goPremiumSetVisibilityIndex = goPremiumStringIndex + setVisibilityOffsetFromMarker

        // The instruction 3 before setVisibility is "move v9, v2" (VISIBLE branch).
        // Replace it with "move v9, v3" to force GONE.
        MyMoovitPlusGoPremiumCardFingerprint.method.replaceInstruction(
            goPremiumSetVisibilityIndex - 3,
            "move v9, v3",
        )
    }
}
