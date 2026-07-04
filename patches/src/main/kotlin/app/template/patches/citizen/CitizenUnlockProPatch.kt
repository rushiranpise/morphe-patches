package app.template.patches.citizen

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CITIZEN_COMPATIBILITY

private fun dismissActivity(@Suppress("UNUSED_PARAMETER") cls: String) =
    "invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n" +
    "invoke-super {p0}, Landroid/app/Activity;->finish()V\n" +
    "return-void"

// Uses p1 (param register) to avoid requiring .locals >= 1
private val returnKotlinUnit =
    "sget-object p1, Lkotlin/Unit;->a:Lkotlin/Unit;\n" +
    "return-object p1"

@Suppress("unused")
val citizenUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all Citizen Plus/Protect features: Safety Network, Safety Center, Zones, Live Agent, Offender alerts, Clarity crime map, incident video, and more.",
    default = true
) {
    compatibleWith(CITIZEN_COMPATIBILITY)

    execute {
        // Layer 2
        listOf(
            CitizenPlusInfoGetActiveFingerprint,
            CitizenProtectInfoGetActiveFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // Layer 3
        SuperwallSetSubscriptionStatusFingerprint
            .match(classDefBy(SuperwallSetSubscriptionStatusFingerprint.definingClass!!))
            .method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }

        // Layer 4
        listOf(
            PrivateUserIsPlusActiveFingerprint,
            PrivateUserIsProtectActiveFingerprint,
            PrivateUserIsProtectActiveOrInSetupFingerprint,
            CitizenProtectInfoDomainGetActiveFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // Layer 5
        ShowPaywallUseCaseAFingerprint
            .match(classDefBy(ShowPaywallUseCaseAFingerprint.definingClass!!))
            .method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 p1, 0x1\nreturn p1")
            }

        listOf(
            ShowPaywallUseCaseCFingerprint,
            ShowPaywallUseCaseDFingerprint,
            PrivateUserIsPaidFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // Layer 6
        SafetyCenterPaywallVMGateFingerprint
            .match(classDefBy(SafetyCenterPaywallVMGateFingerprint.definingClass!!))
            .method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }

        // Layer 7
        SafetyNetworkRemoveExpiredFingerprint
            .match(classDefBy(SafetyNetworkRemoveExpiredFingerprint.definingClass!!))
            .method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
            }

        // Layer 10
        ClarityEntrypointVisibleFingerprint
            .match(classDefBy(ClarityEntrypointVisibleFingerprint.definingClass!!))
            .method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;\nreturn-object p1")
            }

        // Layer 11
        MonoSubscriptionGetEnabledFingerprint
            .match(classDefBy(MonoSubscriptionGetEnabledFingerprint.definingClass!!))
            .method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // Layer 12
        runCatching {
            OnboardingOverridePaywallOnCreateFingerprint
                .match(classDefBy(OnboardingOverridePaywallOnCreateFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, dismissActivity(OnboardingOverridePaywallOnCreateFingerprint.definingClass!!))
                }
        }

        runCatching {
            InAppOverridePaywallOnCreateFingerprint
                .match(classDefBy(InAppOverridePaywallOnCreateFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, dismissActivity(InAppOverridePaywallOnCreateFingerprint.definingClass!!))
                }
        }

        // Layers 13/14/15
        MonoSubscriptionIsSafetyToolAvailableFingerprint
            .match(classDefBy(MonoSubscriptionIsSafetyToolAvailableFingerprint.definingClass!!))
            .method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        MonoSubscriptionGetHidePremiumOnboardingFingerprint
            .match(classDefBy(MonoSubscriptionGetHidePremiumOnboardingFingerprint.definingClass!!))
            .method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        listOf(
            MonoSubscriptionGetShowPlusToPremiumEducationFingerprint,
            MonoSubscriptionGetShowPlusToPremiumProfileBannerFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }

        // Layer 17
        listOf(
            ClarityMapTooltipUpsellEnabledFingerprint,
            ClarityRadioClipsUpsellEnabledFingerprint,
            ClaritySettingsUpsellEnabledFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
            }
        }

        // Layer 18
        listOf(
            PlusV1NeighborhoodTrendsEnabledFingerprint,
            PlusV1RadioClipsEnabledFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
            }
        }

        // Layer 19
        runCatching {
            SuperwallPaywallActivityOnCreateFingerprint
                .match(classDefBy(SuperwallPaywallActivityOnCreateFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, dismissActivity(SuperwallPaywallActivityOnCreateFingerprint.definingClass!!))
                }
        }

        // Layer 20
        listOf(
            PrivateUserIsProtectEligibleFingerprint,
            PrivateUserIsProtectSubscriberFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
            }
        }

        // Removed in v0.1298.0 — runCatching for compat
        runCatching {
            MonoSubscriptionIsSafetyNetworkAvailableFingerprint
                .match(classDefBy(MonoSubscriptionIsSafetyNetworkAvailableFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }

        runCatching {
            SafetyNetworkPaywallVMGateFingerprint
                .match(classDefBy(SafetyNetworkPaywallVMGateFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                }
        }

        // Layer 21
        runCatching {
            ClarityProfileEntrypointEnabledFingerprint
                .match(classDefBy(ClarityProfileEntrypointEnabledFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }

        // Layer 22
        listOf(
            ClarityPaywallActivityOnCreateFingerprint,
            ComparePlansActivityOnCreateFingerprint,
            CarouselPaywallActivityOnCreateFingerprint,
            PromoOfferPaywallActivityOnCreateFingerprint,
            PremiumEducationalPaywallActivityOnCreateFingerprint,
            SuperwallOnboardingWrapperActivityOnCreateFingerprint,
            SubscriptionCenterActivityOnCreateFingerprint,
            SafetyCenterPaywallActivityOnCreateFingerprint,
            SafetyNetworkEducationActivityOnCreateFingerprint,
            FamilyPlanBenefitActivityOnCreateFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, dismissActivity(fp.definingClass!!))
                }
            }
        }

        runCatching {
            TrustedContactsConfigGetEnabledFingerprint
                .match(classDefBy(TrustedContactsConfigGetEnabledFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }

        runCatching {
            PaywallHomescreenTriggerConfigGetEnabledFingerprint
                .match(classDefBy(PaywallHomescreenTriggerConfigGetEnabledFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                }
        }

        // Layer 23: Safety Network flow collectors (zp3)
        listOf(
            SafetyNetworkSingleInviteFlowCollectorFingerprint,
            SafetyNetworkPendingInvitesFlowCollectorFingerprint,
            FamilyPlanBenefitFlowCollectorFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, returnKotlinUnit)
                }
            }
        }

        runCatching {
            SafetyNetworkEducationFlowCollectorFingerprint
                .match(classDefBy(SafetyNetworkEducationFlowCollectorFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(
                        0,
                        "iget-object p0, p0, Lsp0n/citizen/social/safetynetwork/SafetyNetworkEducationActivity\$a\$a\$a;->d:Lsp0n/citizen/social/safetynetwork/SafetyNetworkEducationActivity;\n" +
                        "new-instance p1, Landroid/content/Intent;\n" +
                        "const-class p2, Lsp0n/citizen/social/safetynetwork/SafetyNetworkActivity;\n" +
                        "invoke-direct {p1, p0, p2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V\n" +
                        "invoke-virtual {p0, p1}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V\n" +
                        "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\n" +
                        "sget-object p0, Lkotlin/Unit;->a:Lkotlin/Unit;\n" +
                        "return-object p0"
                    )
                }
        }

        // Layer 24: MainActivity collectors (gone in v0.1298.0 — runCatching)
        // Layer 25: PremiumEducational internal collector
        listOf(
            MainActivityPaywallFlowCollectorAFingerprint,
            MainActivityPaywallFlowCollectorBFingerprint,
            MainActivityPaywallFlowCollectorCFingerprint,
            MainActivityPaywallFlowCollectorDFingerprint,
            MainActivityPaywallFlowCollectorEFingerprint,
            MainActivityPaywallFlowCollectorFFingerprint,
            MainActivityPaywallFlowCollectorGFingerprint,
            MainActivityPaywallFlowCollectorHFingerprint,
            MainActivityPaywallFlowCollectorAbaFingerprint,
            MainActivityPaywallFlowCollectorBbaFingerprint,
            MainActivityPaywallFlowCollectorCbaFingerprint,
            MainActivityPaywallFlowCollectorDbaFingerprint,
            PremiumEducationalPaywallInternalCollectorFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, returnKotlinUnit)
                }
            }
        }

        // Layers 26+27: Cross-package collectors
        // EXCLUDED: SafetyZonePaywallFlowCollector — multi-branch nav dispatcher (breaks alert zones)
        // EXCLUDED: SafetyNetworkActivityPaywallCollector — multi-branch nav dispatcher (breaks safety network)
        // EXCLUDED: SubscriptionCenterActivityCollector — multi-branch dispatcher
        listOf(
            MenuPaywallFlowCollectorFingerprint,         // empty in v0.1298.0 — runCatching safe
            OnboardingPaywallFlowCollectorFingerprint,   // empty in v0.1298.0 — runCatching safe
            ProfilePaywallFlowCollectorFingerprint,      // gone in v0.1298.0 — runCatching safe
            SafetyHomePaywallFlowCollectorFingerprint,   // older builds
            MyProfileFragmentPaywallCollectorFingerprint, // older builds (h$a$a)
            SafetyCenterPaywallActivityCollectorFingerprint,
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, returnKotlinUnit)
                }
            }
        }

        listOf(
            ObfuscatedW50F1PaywallCollectorFingerprint,
            ObfuscatedW70LPaywallCollectorFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, returnKotlinUnit)
                }
            }
        }

        // PurchasePremiumHelper: v8d (older) + c3d (v0.1298.0+)
        listOf(
            PurchasePremiumHelperCreateIntentFingerprint,
            PurchasePremiumHelperC3DFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
                }
            }
        }

        runCatching {
            PremiumEducationalPaywallActivityCreateIntentFingerprint
                .match(classDefBy(PremiumEducationalPaywallActivityCreateIntentFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(
                        0,
                        "if-nez p1, :goto_safety_network\n" +
                        "new-instance v0, Landroid/content/Intent;\n" +
                        "const-class v1, Lsp0n/citizen/paywall/superwall/PremiumEducationalPaywallActivity;\n" +
                        "invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V\n" +
                        "const-string p0, \"ORIGIN\"\n" +
                        "invoke-virtual {v0, p0, p2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;\n" +
                        "move-result-object p0\n" +
                        "const-string p2, \"LAUNCH_SAFETY_NETWORK\"\n" +
                        "invoke-virtual {p0, p2, p1}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Z)Landroid/content/Intent;\n" +
                        "move-result-object p0\n" +
                        "return-object p0\n" +
                        ":goto_safety_network\n" +
                        "new-instance v0, Landroid/content/Intent;\n" +
                        "const-class v1, Lsp0n/citizen/social/safetynetwork/SafetyNetworkActivity;\n" +
                        "invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V\n" +
                        "return-object v0"
                    )
                }
        }

        // NavigationType b1b$h0/i0/u0 (v0.1298.0+ — replaces MainActivity collectors)
        listOf(
            NavigationTypeH0PaywallFingerprint,
            NavigationTypeI0PaywallFingerprint,
            NavigationTypeU0PaywallFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, returnKotlinUnit)
                }
            }
        }

        // ProtectFabHelper zvc (v0.1298.0+)
        runCatching {
            ProtectFabHelperPaywallFingerprint
                .match(classDefBy(ProtectFabHelperPaywallFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "return-void")
                }
        }
    }
}
