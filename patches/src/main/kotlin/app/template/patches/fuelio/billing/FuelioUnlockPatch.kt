package app.template.patches.fuelio.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FUELIO_COMPATIBILITY
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

// ── Architecture ─────────────────────────────────────────────────────────────
//
// Six independent premium/maps/promo signal paths:
//
// Path A — synchronous feature gate
//   ProFeatureManager.b()Z
//
// Path B — dashboard promo banner flow
//   SubscriptionDataRepository.map$2$2  → BuyState.b (hasPremium)
//   SubscriptionDataRepository.map$1$2  → BuyState.a (hasRenewablePremium)
//   → BuyViewModel.j → DashboardViewModel.r() → DashboardViewModel.n
//   NOTE: n=true shows the promo banner — Paths B+F handle this correctly:
//   B makes n=true (PRO user recognised), F suppresses the promo cards.
//
// Path C — BuyFragment paywall screen navigation
//   BuyViewModel$1$1.invokeSuspend() → posts DestinationScreen to g LiveData
//
// Path D — async suspend gate
//   ProFeatureManager.a(ContinuationImpl)Object — awaitAccess()
//
// Path E — Google Maps cert + API key spoof (extension)
//   FuelioHelper.init() → IPackageManager proxy in FuelioApplication.onCreate()
//
// Path F — "Limited Promo" / "30% OFF" banner suppression
//   FirebaseRemoteConfigRepository.c()  reads "promo30_enabled" from Firebase
//   FirebaseRemoteConfigRepository.d()  reads "promo30_enabled_home"
//   Dashboard composable shows promo banner only when either returns true.
//   PRO users should not see the paywall promo — patch both to return false.
//
@Suppress("unused")
val fuelioUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Fuelio Premium, restores Google Maps, and suppresses " +
        "the Limited Promo paywall banner for unlocked users.",
    default = true,
) {
    compatibleWith(FUELIO_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {
        // ── Path A: ProFeatureManager.b()Z → always true ──────────────────────
        IsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Path B1: hasPremium flow emitter → always emits true ──────────────
        val hasPremiumValueOfIndex = HasPremiumEmitFingerprint.instructionMatches[1].index
        val hasPremiumReg = HasPremiumEmitFingerprint.method
            .getInstruction<FiveRegisterInstruction>(hasPremiumValueOfIndex).registerC
        HasPremiumEmitFingerprint.method.addInstructions(
            hasPremiumValueOfIndex,
            "const/4 v$hasPremiumReg, 0x1",
        )

        // ── Path B2: hasRenewablePremium flow emitter → always emits true ─────
        val hasRenewableValueOfIndex = HasRenewablePremiumEmitFingerprint.instructionMatches[2].index
        val hasRenewableReg = HasRenewablePremiumEmitFingerprint.method
            .getInstruction<FiveRegisterInstruction>(hasRenewableValueOfIndex).registerC
        HasRenewablePremiumEmitFingerprint.method.addInstructions(
            hasRenewableValueOfIndex,
            "const/4 v$hasRenewableReg, 0x1",
        )

        // ── Path C: BuyFragment paywall → always navigate to PRO profile ──────
        DestinationScreenFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                iget-object v0, p0, Lcom/kajda/fuelio/ui/paywall/BuyViewModel${'$'}1${'$'}1;->d:Lcom/kajda/fuelio/ui/paywall/BuyViewModel;
                iget-object v0, v0, Lcom/kajda/fuelio/ui/paywall/BuyViewModel;->g:Landroidx/lifecycle/MutableLiveData;
                sget-object v1, Lcom/kajda/fuelio/ui/paywall/BuyViewModel${'$'}DestinationScreen;->PREMIUM_RENEWABLE_PROFILE:Lcom/kajda/fuelio/ui/paywall/BuyViewModel${'$'}DestinationScreen;
                invoke-virtual { v0, v1 }, Landroidx/lifecycle/LiveData;->j(Ljava/lang/Object;)V
                sget-object v0, Lkotlin/Unit;->a:Lkotlin/Unit;
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Path D: ProFeatureManager.a() → always return Boolean.TRUE ────────
        AwaitAccessFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Path E: Google Maps cert + API key spoof ──────────────────────────
        FuelioApplicationOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static { }, Lapp/template/extension/extension/FuelioHelper;->init()V",
        )

        // ── Path F: Suppress "Limited Promo" / "30% OFF" banner ──────────────
        // c() → false: promo30_enabled check returns false → banner hidden
        PromoEnabledFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        // d() → false: promo30_enabled_home check returns false → banner hidden
        PromoHomeEnabledFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
    }
}
