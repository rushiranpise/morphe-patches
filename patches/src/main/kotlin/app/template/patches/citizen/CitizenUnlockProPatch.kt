package app.template.patches.citizen

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CITIZEN_COMPATIBILITY

/**
 * Unlocks Citizen Pro/Protect in sp0n.citizen.
 *
 * Five independent gate layers are patched, working from the outermost
 * API/SDK boundary down to the innermost domain-model + use-case layer.
 *
 * ## Root cause (confirmed from APK decompilation)
 *
 * The main Safety Network paywall is triggered by SafetyNetworkViewModel
 * coroutine lambda db0/f2 and db0/g2, which emit q$b$i (ShowPaywallRequired)
 * by calling ShowPaywallUseCase.d() via SafetyNetworkEducationViewModel.o().
 *
 * ShowPaywallUseCase.d() calls:
 *   PrivateUserRepository.getUser().isPlusActive()   → reads stored field
 *   PrivateUserRepository.getUser().isProtectActive() → reads stored field
 *
 * Both fields are set ONCE at PrivateUser construction time inside
 * PrivateUserMapper.toModel(), which reads CitizenPlusInfoDTO.getActive() and
 * CitizenProtectInfoDTO.getActive() via invoke-virtual/range — NOT the
 * standard virtual dispatch our Layer 2 getter patches cover.
 *
 * Result: even with Layers 1-4 applied, d() can still return false because
 * the PrivateUser object was constructed before patches fire, with fields=false.
 *
 * Layer 5 is the definitive fix: patch ShowPaywallUseCase.a/c/d directly to
 * return true, bypassing the entire remote-config + subscription-field chain.
 *
 * ## Layer 1 — SubscriptionDigestDTOKt.toModel()
 * Forces every subscription API response to ACTIVATED.
 *
 * ## Layer 2 — CitizenPlusInfoDTO / CitizenProtectInfoDTO.getActive()
 * DTO getters forced to true at source.
 *
 * ## Layer 3 — Superwall SDK
 * Forces SubscriptionStatus.Active so no Superwall paywall sheet is shown.
 *
 * ## Layer 4 — PrivateUser domain model getters
 * isPlusActive(), isProtectActive(), isProtectActiveOrInSetup() replaced with
 * unconditional true. Covers all UI code that calls these getters post-construction.
 *
 * ## Layer 5 — ShowPaywallUseCase.a/c/d + PrivateUser.isPaid()
 * The true master gate. a(SubscriptionFeature) is the entry point for every
 * feature paywall check in the app. c() and d() are the sub-checks. All three
 * are replaced with unconditional true, eliminating the remote-config dependency.
 * isPaid() is a stored field not reachable by getter patches — also forced true.
 *
 * Features unlocked:
 *   Safety Network      — isProtectActiveOrInSetup + ShowPaywallUseCase.d
 *   Safety Network edu  — ShowPaywallUseCase.d (via h.smali.o())
 *   Offender alerts     — ShowPaywallUseCase.c
 *   Safety home feed    — ShowPaywallUseCase.a/d
 *   Incident detail     — ShowPaywallUseCase.a/f
 *   Zones follow-loc    — ShowPaywallUseCase.d
 *   Profile features    — ShowPaywallUseCase.a
 *   Community feed      — ShowPaywallUseCase.a + isPaid
 *   Menu/nav items      — isPlusActive + isProtectActive + isPaid
 */
@Suppress("unused")
val citizenUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all Citizen Pro/Protect features including Safety Network.",
    default = true
) {
    compatibleWith(CITIZEN_COMPATIBILITY)

    execute {

        // ── Layer 1: Force ACTIVATED in SubscriptionDigestDTOKt.toModel() ────
        SubscriptionDigestToModelFingerprint
            .match(classDefBy(SubscriptionDigestToModelFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    13,
                    "sget-object v1, Lsp0n/citizen/data/user/dto/SubscriptionState;" +
                    "->ACTIVATED:Lsp0n/citizen/data/user/dto/SubscriptionState;"
                )
            }

        // ── Layer 2: Force active=true on Plus and Protect DTO getters ───────
        listOf(
            CitizenPlusInfoGetActiveFingerprint,
            CitizenProtectInfoGetActiveFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }

        // ── Layer 3: Keep Superwall SDK in Active state ───────────────────────
        val activeClass =
            "Lcom/superwall/sdk/models/entitlements/SubscriptionStatus\$Active;"
        SuperwallSetSubscriptionStatusFingerprint
            .match(classDefBy(SuperwallSetSubscriptionStatusFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "sget-object p1, $activeClass->INSTANCE:$activeClass")
            }

        // ── Layer 4: Patch PrivateUser domain model getters ──────────────────
        // Covers all UI code that calls these getters post-construction.
        // Note: these are stored boolean fields (iget-boolean), so patching
        // the getter returns true for every call even though the field itself
        // remains false from the mapper. isPaid() is handled in Layer 5.
        listOf(
            PrivateUserIsPlusActiveFingerprint,
            PrivateUserIsProtectActiveFingerprint,
            PrivateUserIsProtectActiveOrInSetupFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }

        // ── Layer 5: Patch ShowPaywallUseCase.a/c/d + PrivateUser.isPaid() ───
        //
        // ShowPaywallUseCase.a(SubscriptionFeature) is the single entry point
        // called by every feature paywall check in the app. It consults a
        // server-controlled remote config (FeatureConfigValue$SubscriptionBasedFeatures)
        // BEFORE checking subscription status — meaning it can return false even
        // when domain getters are patched.
        //
        // ShowPaywallUseCase.d() is the direct trigger for the Safety Network
        // paywall: SafetyNetworkViewModel lambda db0/g2.invoke() calls
        // q.n.j(q$b$i) (emits ShowPaywallRequired state) when d() returns false.
        // SafetyNetworkEducationViewModel.o() also calls d() directly at lines
        // 400 and 415 of social/safetynetwork/h.smali.
        //
        // Note: ShowPaywallUseCase.a() uses p1 register (first parameter register)
        // not v0, because it's a non-static method with a parameter we reuse.
        ShowPaywallUseCaseAFingerprint
            .match(classDefBy(ShowPaywallUseCaseAFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 p1, 0x1\nreturn p1")
            }

        listOf(
            ShowPaywallUseCaseCFingerprint,
            ShowPaywallUseCaseDFingerprint,
            PrivateUserIsPaidFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }
    }
}