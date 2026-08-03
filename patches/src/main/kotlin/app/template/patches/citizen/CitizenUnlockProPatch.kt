package app.template.patches.citizen

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CITIZEN_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

// Citizen v0.1301.0 — sp0n.citizen
//
// ShowPaywallUseCase method semantics (CRITICAL — easy to get backwards):
//   a(SubscriptionFeature)Z  true = user HAS ACCESS → callers show content
//                            false = no access     → callers show paywall
//   c()Z                     true = user IS premium (Plus/Protect active)
//   d()Z                     true = user IS premium (Protect active specifically)
//   e()Z                     true = SHOW safety network paywall
//   f(Boolean)Z              true = SHOW conditional paywall
//   isPaid()Z on PrivateUser true = user is paid
//   SafetyCenterPaywallVMGate.s()Z / SafetyNetworkPaywallVMGate.n()Z
//                            true = user HAS premium access (no paywall)
//
// Patch directions:
//   a() → true  (user always has access)
//   c() → true  (user always is premium)
//   d() → true  (user always is premium)
//   e() → false (never show safety network paywall)
//   f() → false (never show conditional paywall)
//   isPaid() → true
//
// 9 stable targets (all non-obfuscated):
//   1. CitizenPlusInfoDTO.getActive()Z → true
//   2. CitizenProtectInfoDTO.getActive()Z → true
//   3. PremiumSubscriptionDTO.getSubscriptionState() → TRIAL_ACTIVATED
//   4. SubscriptionDigest.getSubscriptionState() → TRIAL_ACTIVATED
//   5. Superwall.internallySetSubscriptionStatus() → return-void
//   6. MonoSubscription.getEnabled() → true
//   7. MonoSubscription.isSafetyToolAvailable() → true
//   8. ClarityEntrypointRepository.getProfileEntrypointEnabled() → true
//   9. SubscriptionRepository.<init> — seed _currentSubscription with TRIAL_ACTIVATED

private const val SUBSCRIPTION_STATE =
    "Lsp0n/citizen/data/user/dto/SubscriptionState;"
private const val SUBSCRIPTION_DIGEST =
    "Lsp0n/citizen/data/user/dto/SubscriptionDigest;"
private const val SUBSCRIPTION_REPO =
    "Lsp0n/citizen/data/user/SubscriptionRepository;"

@Suppress("unused")
val citizenUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all Citizen Plus and Protect features.",
    default = true,
) {
    compatibleWith(CITIZEN_COMPATIBILITY)

    execute {
        // ── Targets 1+2: Plus/Protect active DTO getters → true ──────────────
        CitizenPlusInfoDTOGetActiveFingerprint.method.returnEarly(true)
        CitizenProtectInfoDTOGetActiveFingerprint.method.returnEarly(true)

        // ── Targets 3+4: SubscriptionState → TRIAL_ACTIVATED ─────────────────
        // TRIAL_ACTIVATED (case 5) = premium in all packed-switch tables.
        // ACTIVATED (case 4) = base tier = FREE — do not use ACTIVATED.
        // .registers 1 in both: p0 = this. Overwrite p0 with enum, return. Safe.
        val returnTrialActivated =
            "sget-object p0, $SUBSCRIPTION_STATE->TRIAL_ACTIVATED:$SUBSCRIPTION_STATE\n" +
            "return-object p0"

        PremiumSubscriptionDTOGetStateFingerprint.method.addInstructions(0, returnTrialActivated)
        SubscriptionDigestGetStateFingerprint.method.addInstructions(0, returnTrialActivated)

        // ── Target 5: Superwall → block server status override ────────────────
        SuperwallSetSubscriptionStatusFingerprint.method.addInstructions(0, "return-void")

        // ── Targets 6+7: MonoSubscription feature flags → true ────────────────
        MonoSubscriptionGetEnabledFingerprint.method.returnEarly(true)
        MonoSubscriptionIsSafetyToolAvailableFingerprint.method.returnEarly(true)

        // ── Target 8: Clarity entrypoint → visible ────────────────────────────
        runCatching {
            ClarityProfileEntrypointEnabledFingerprint.method.returnEarly(true)
        }

        // ── Target 9: Seed SubscriptionRepository._currentSubscription ───────
        // Insert BEFORE "const/4 p0, 3" (which overwrites this-ref with int 3).
        // p0 = register 8 in .registers 15 with 7 params.
        // Scan for NarrowLiteralInstruction where registerA==8, literal==3.
        // Save p0→v4 before it is clobbered; use v4 for iget-object.
        val constructorMethod = SubscriptionRepositoryConstructorFingerprint.method
        val constP0Index = constructorMethod.implementation!!.instructions.indexOfFirst { instr ->
            instr is NarrowLiteralInstruction &&
                instr is OneRegisterInstruction &&
                (instr as OneRegisterInstruction).registerA == 8 &&
                (instr as NarrowLiteralInstruction).narrowLiteral == 3
        }
        require(constP0Index != -1) { "SubscriptionRepository.<init>: const/4 p0, 3 not found" }

        constructorMethod.addInstructions(
            constP0Index,
            "move-object v4, p0\n" +
            "new-instance v0, $SUBSCRIPTION_DIGEST\n" +
            "sget-object v1, $SUBSCRIPTION_STATE->TRIAL_ACTIVATED:$SUBSCRIPTION_STATE\n" +
            "const/4 v2, 0\n" +
            "invoke-direct { v0, v1, v2, v2 }, ${SUBSCRIPTION_DIGEST}-><init>(${SUBSCRIPTION_STATE}Ljava/time/Instant;Ljava/time/Instant;)V\n" +
            "iget-object v3, v4, ${SUBSCRIPTION_REPO}->_currentSubscription:Lgwa;\n" +
            "invoke-interface { v3, v0 }, Lgwa;->setValue(Ljava/lang/Object;)V",
        )

        // ── ShowPaywallUseCase gates ──────────────────────────────────────────
        // a(SubscriptionFeature)Z: true = user HAS ACCESS → callers show content.
        // With c() and d() returning true, a() would naturally return true too,
        // but we patch directly for belt-and-suspenders.
        ShowPaywallUseCaseAFingerprint.method.returnEarly(true)

        // c()Z, d()Z: true = user IS premium. Patch to true.
        ShowPaywallUseCaseCFingerprint.method.returnEarly(true)
        ShowPaywallUseCaseDFingerprint.method.returnEarly(true)

        // e()Z: true = SHOW safety network paywall. Patch to false.
        ShowPaywallUseCaseEFingerprint.method.returnEarly(false)

        // f(Boolean)Z: true = SHOW conditional paywall. Patch to false.
        ShowPaywallUseCaseFFingerprint.method.returnEarly(false)

        // isPaid()Z on PrivateUser: true = user is paid. Patch to true.
        PrivateUserIsPaidFingerprint.method.returnEarly(true)

        // ── Paywall Activity dismissals (belt-and-suspenders) ─────────────────
        // super.onCreate(bundle) + finish() — uses only p0 (this) and p1 (Bundle),
        // both param registers valid in any Activity.onCreate(Bundle) method.
        val dismissOnCreate =
            "invoke-super { p0, p1 }, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n" +
            "invoke-virtual { p0 }, Landroid/app/Activity;->finish()V\n" +
            "return-void"

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
            SuperwallPaywallActivityOnCreateFingerprint,
        ).forEach { fp ->
            runCatching {
                fp.method.addInstructions(0, dismissOnCreate)
            }
        }
    }
}
