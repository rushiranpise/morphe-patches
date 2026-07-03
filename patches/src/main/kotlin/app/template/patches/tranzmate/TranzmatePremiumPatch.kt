package app.template.patches.tranzmate

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TRANZMATE_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
val tranzmatePremiumPatch = bytecodePatch(
    name = "Unlock Moovit+",
    description = "Unlocks Moovit+",
    default = true,
) {
    compatibleWith(TRANZMATE_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {
        MoovitApplicationOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static {}, Lapp/template/extension/extension/MoovitHelper;->init()V",
        )

        SubscriptionStateFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x1
                return p0
            """,
        )

        AdUnitResolverFingerprint.method.addInstructions(
            0,
            """
                const-string v0, ""
                return-object v0
            """,
        )

        MoovitAdViewSetSourceFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p0, v0}, Landroid/view/View;->setVisibility(I)V
                return-void
            """,
        )

        MoovitBannerAdViewSetSourceFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p0, v0}, Landroid/view/View;->setVisibility(I)V
                return-void
            """,
        )

        SubscriptionPackageStateFingerprint.method.addInstructions(
            0,
            """
                sget-object p0, Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;->ACTIVE:Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;
                return-object p0
            """,
        )

        SafeRideCalculateStateFingerprint.method.addInstructions(
            0,
            """
                sget-object p0, Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;->ACTIVE:Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;
                return-object p0
            """,
        )

        MoovitPlusActivityOnReadyFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroid/app/Activity;->finish()V
                return-void
            """,
        )

        PromoCellFragmentOnViewCreatedFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p1, v0}, Landroid/view/View;->setVisibility(I)V
                return-void
            """,
        )

        BlockPaywallGateFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x0
                return p0
            """,
        )

        BlockPaywallActivityOnReadyFingerprint.method.addInstructions(
            0,
            """
                invoke-direct {p0}, Lcom/moovit/app/plus/paywall/BlockPaywallActivity;->relaunchCallingActivity()V
                return-void
            """,
        )

        MoovitPlusOnboardingActivityFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Lcom/moovit/app/plus/onboarding/MoovitPlusOnboardingActivity;->P0()V
                return-void
            """,
        )

        MoovitPlusHelpCenterMenuItemFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )

        MoovitPlusMenuItemFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x8
                invoke-virtual {p1, v0}, Landroid/view/View;->setVisibility(I)V
                return-void
            """,
        )

        AdFreeMenuItemFingerprint.method.addInstructions(
            AdFreeMenuItemFingerprint.method.implementation!!.instructions.lastIndex,
            """
                const/16 p2, 0x8
                invoke-virtual {p1, p2}, Landroid/view/View;->setVisibility(I)V
            """,
        )

        val blockedSmartTipsIndex = ItineraryBlockedSmartTipsBannerFingerprint.method.implementation!!.instructions
            .indexOfFirst { instruction ->
                (instruction as? ReferenceInstruction)?.reference.toString()
                    .contains("view_blocked_smart_tips_banner") == true
            }
        check(blockedSmartTipsIndex > 0) { "Moovit blocked smart tips banner signature not found." }
        ItineraryBlockedSmartTipsBannerFingerprint.method.replaceInstruction(
            blockedSmartTipsIndex - 17,
            "const/4 v6, 0x0",
        )

        val goPremiumInstructions = MyMoovitPlusGoPremiumCardFingerprint.method.implementation!!.instructions
        val goPremiumStringIndex = goPremiumInstructions.indexOfFirst { instruction ->
            (instruction as? ReferenceInstruction)?.reference.toString().contains("goPremiumCard") == true
        }
        check(goPremiumStringIndex > 0) { "Moovit+ go premium card signature not found." }
        val goPremiumSetVisibilityIndex = goPremiumStringIndex +
            goPremiumInstructions.drop(goPremiumStringIndex).indexOfFirst { instruction ->
                (instruction as? ReferenceInstruction)?.reference.toString()
                    .contains("Landroid/view/View;->setVisibility(I)V") == true
            }
        check(goPremiumSetVisibilityIndex > goPremiumStringIndex) {
            "Moovit+ go premium card visibility call not found."
        }
        MyMoovitPlusGoPremiumCardFingerprint.method.replaceInstruction(
            goPremiumSetVisibilityIndex - 3,
            "move v9, v3",
        )

        MoovitPlusPackagePopupFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroidx/fragment/app/i;->dismiss()V
                return-void
            """,
        )

        MoovitPlusPurchaseFragmentFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )

        MoovitPlusPurchaseOffersFragmentFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )

        MoovitPlusOnboardingPrePurchaseFragmentFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )

        FreemiumPopupFragmentFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroidx/fragment/app/i;->dismiss()V
                return-void
            """,
        )
    }
}
