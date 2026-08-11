package app.template.patches.citizen

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CITIZEN_COMPATIBILITY

// move-object/from16 v0, p0 brings p0 into v0 regardless of .registers count,
// avoiding v23 overflow on high-register-count methods like SuperwallPaywallActivity.
private val dismissOnCreate =
    "move-object/from16 v0, p0\n" +
    "invoke-virtual { v0 }, Landroid/app/Activity;->finish()V\n" +
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
        // Layer 2: Plus/Protect DTO getters
        listOf(
            CitizenPlusInfoGetActiveFingerprint,
            CitizenProtectInfoGetActiveFingerprint
        ).forEach { fp ->
            fp.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // Layer 3: Kill Superwall at SuperwallInitializer.create() — the androidx.startup
        // entry point called from SplashActivity.onCreate(). It calls configure$default
        // then getInstance(). Returning null skips both; Superwall never initializes.
        // Belt-and-suspenders: also block status override + disable enable gate.
        SuperwallInitializerCreateFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 p0, 0x0\nreturn-object p0")
        }
        SuperwallSetSubscriptionStatusFingerprint.method.addInstructions(0, "return-void")
        AndroidSuperwallGetEnabledFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // Layer 4: Domain model getters
        listOf(
            PrivateUserIsPlusActiveFingerprint,
            PrivateUserIsProtectActiveFingerprint,
            PrivateUserIsProtectActiveOrInSetupFingerprint,
            CitizenProtectInfoDomainGetActiveFingerprint
        ).forEach { fp ->
            fp.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // Layer 5: ShowPaywallUseCase gates
        ShowPaywallUseCaseAFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 p1, 0x1\nreturn p1")
        }
        listOf(
            ShowPaywallUseCaseCFingerprint,
            ShowPaywallUseCaseDFingerprint,
            PrivateUserIsPaidFingerprint
        ).forEach { fp ->
            fp.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }
        // e() = true means SHOW safety network paywall; f() = true means SHOW conditional paywall
        listOf(
            ShowPaywallUseCaseEFingerprint,
            ShowPaywallUseCaseFFingerprint
        ).forEach { fp ->
            fp.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }

        // Layer 6: SafetyCenter paywall VM gate (may be absent in v0.1303.2)
        runCatching {
            SafetyCenterPaywallVMGateFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }

        // Layer 7: Safety Network expiry check
        SafetyNetworkRemoveExpiredFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
        }

        // Layer 10: Clarity entrypoint visibility
        ClarityEntrypointVisibleFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;\nreturn-object p1")
        }

        // Layers 11/13/14/15: MonoSubscription feature flags
        // getEnabled removed in v0.1303.2 — runCatching protects
        runCatching {
            MonoSubscriptionGetEnabledFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }
        MonoSubscriptionIsSafetyToolAvailableFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        MonoSubscriptionGetHidePremiumOnboardingFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        listOf(
            MonoSubscriptionGetShowPlusToPremiumEducationFingerprint,
            MonoSubscriptionGetShowPlusToPremiumProfileBannerFingerprint
        ).forEach { fp ->
            fp.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }

        // Layer 12: Override paywall Activities
        runCatching {
            OnboardingOverridePaywallOnCreateFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, dismissOnCreate)
            }
        }
        runCatching {
            InAppOverridePaywallOnCreateFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, dismissOnCreate)
            }
        }

        // Layer 17: Clarity upsell flags
        listOf(
            ClarityMapTooltipUpsellEnabledFingerprint,
            ClarityRadioClipsUpsellEnabledFingerprint,
            ClaritySettingsUpsellEnabledFingerprint
        ).forEach { fp ->
            runCatching {
                fp.method.apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
            }
        }

        // Layer 18: PlusV1 feature flags
        listOf(
            PlusV1NeighborhoodTrendsEnabledFingerprint,
            PlusV1RadioClipsEnabledFingerprint
        ).forEach { fp ->
            runCatching {
                fp.method.apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
            }
        }

        // Layer 19: SuperwallPaywallActivity dismiss
        // .registers 25 in this method — move-object/from16 avoids v23 overflow
        runCatching {
            SuperwallPaywallActivityOnCreateFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, dismissOnCreate)
            }
        }

        // Layer 20: Protect eligibility getters
        listOf(
            PrivateUserIsProtectEligibleFingerprint,
            PrivateUserIsProtectSubscriberFingerprint
        ).forEach { fp ->
            runCatching {
                fp.method.apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
            }
        }

        // Older builds only
        runCatching {
            MonoSubscriptionIsSafetyNetworkAvailableFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }
        runCatching {
            SafetyNetworkPaywallVMGateFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }

        // Layer 21: Clarity profile entrypoint
        runCatching {
            ClarityProfileEntrypointEnabledFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // Layer 22: Paywall Activity dismissals
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
            FamilyPlanBenefitActivityOnCreateFingerprint,
            MultimonthPaywallActivityOnCreateFingerprint,
            ProtectOnBoardingUpsellActivityOnCreateFingerprint,
            PostDemoRealCallUpsellActivityOnCreateFingerprint
        ).forEach { fp ->
            runCatching {
                fp.method.apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, dismissOnCreate)
                }
            }
        }

        runCatching {
            TrustedContactsConfigGetEnabledFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }
        runCatching {
            PaywallHomescreenTriggerConfigGetEnabledFingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }

        // Layer 23: Safety Network flow collectors (continuation type nq3 in v0.1303.2)
        listOf(
            SafetyNetworkSingleInviteFlowCollectorFingerprint,
            SafetyNetworkPendingInvitesFlowCollectorFingerprint,
            FamilyPlanBenefitFlowCollectorFingerprint
        ).forEach { fp ->
            runCatching {
                fp.method.apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, returnKotlinUnit)
                }
            }
        }

        runCatching {
            SafetyNetworkEducationFlowCollectorFingerprint.method.apply {
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

        // Layers 24+25: MainActivity + PremiumEducational collectors
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
                fp.method.apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, returnKotlinUnit)
                }
            }
        }

        // Layers 26+27: Cross-package collectors
        listOf(
            MenuPaywallFlowCollectorFingerprint,
            OnboardingPaywallFlowCollectorFingerprint,
            ProfilePaywallFlowCollectorFingerprint,
            SafetyHomePaywallFlowCollectorFingerprint,
            MyProfileFragmentPaywallCollectorFingerprint,
            SafetyCenterPaywallActivityCollectorFingerprint,
            ObfuscatedW50F1PaywallCollectorFingerprint,
            ObfuscatedW70LPaywallCollectorFingerprint
        ).forEach { fp ->
            runCatching {
                fp.method.apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, returnKotlinUnit)
                }
            }
        }

        // PurchasePremiumHelper: v8d (older) + c3d (current)
        listOf(
            PurchasePremiumHelperCreateIntentFingerprint,
            PurchasePremiumHelperC3DFingerprint
        ).forEach { fp ->
            runCatching {
                fp.method.apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
                }
            }
        }

        runCatching {
            PremiumEducationalPaywallActivityCreateIntentFingerprint.method.apply {
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

        // NavigationType b1b$h0/i0/u0 — gone in v0.1303.2, runCatching protects
        listOf(
            NavigationTypeH0PaywallFingerprint,
            NavigationTypeI0PaywallFingerprint,
            NavigationTypeU0PaywallFingerprint
        ).forEach { fp ->
            runCatching {
                fp.method.apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, returnKotlinUnit)
                }
            }
        }

        // ProtectFabHelper
        runCatching {
            ProtectFabHelperPaywallFingerprint.method.addInstructions(0, "return-void")
        }
    }
}
