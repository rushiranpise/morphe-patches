package app.template.patches.citizen

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── Layer 1: SubscriptionDigestDTOKt ─────────────────────────────────────────────

val SubscriptionDigestToModelFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/SubscriptionDigestDTOKt;",
    name = "toModel",
    parameters = listOf("Lsp0n/citizen/data/user/dto/SubscriptionDigestDTO;"),
    returnType = "Lsp0n/citizen/data/user/dto/SubscriptionDigest;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

// ── Layer 2: DTO active getters ──────────────────────────────────────────────

val CitizenPlusInfoGetActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/CitizenPlusInfoDTO;",
    name = "getActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val CitizenProtectInfoGetActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/CitizenProtectInfoDTO;",
    name = "getActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 3: Superwall ───────────────────────────────────────────────────────────

val SuperwallSetSubscriptionStatusFingerprint = Fingerprint(
    definingClass = "Lcom/superwall/sdk/Superwall;",
    name = "internallySetSubscriptionStatus\$superwall_release",
    parameters = listOf("Lcom/superwall/sdk/models/entitlements/SubscriptionStatus;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 4: PrivateUser + CitizenProtectInfo domain getters ─────────────────────

val CitizenProtectInfoDomainGetActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/CitizenProtectInfo;",
    name = "getActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val PrivateUserIsPlusActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isPlusActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val PrivateUserIsProtectActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isProtectActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val PrivateUserIsProtectActiveOrInSetupFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isProtectActiveOrInSetup",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 5: ShowPaywallUseCase + PrivateUser.isPaid ─────────────────────────────

val ShowPaywallUseCaseAFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "a",
    parameters = listOf("Lsp0n/citizen/incidentdetail/ShowPaywallUseCase\$SubscriptionFeature;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ShowPaywallUseCaseCFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "c",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ShowPaywallUseCaseDFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "d",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val PrivateUserIsPaidFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isPaid",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 6: SafetyCenterPaywallViewModel ────────────────────────────────────────

val SafetyCenterPaywallVMGateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/safetycenter/paywall/b;",
    name = "n",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 7: SafetyNetwork removeNetworkIfExpired (merged 7+8+9) ─────────────────

/**
 * SafetyNetworkRepository$removeNetworkIfExpired$1.invokeSuspend()
 *
 * R8 compiles the full state-machine (isPaid + isProtectActive checks) into
 * this inner lambda. Outer class has no standalone smali. Replacing invokeSuspend
 * with kotlin.Unit early return skips the entire expiry check.
 * kotlin.Unit singleton field confirmed: Lkotlin/Unit;->a:Lkotlin/Unit;
 */
val SafetyNetworkRemoveExpiredFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/social/SafetyNetworkRepository\$removeNetworkIfExpired\$1;",
    name = "invokeSuspend",
    parameters = listOf("Ljava/lang/Object;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 10: Clarity entrypoint visible lambda ──────────────────────────────────

val ClarityEntrypointVisibleFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/clarity/ClarityEntrypointRepository\$profileEntrypointVisible\$1;",
    name = "invokeSuspend",
    parameters = listOf("Ljava/lang/Object;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layers 11/13/14/15: MonoSubscription boolean getters ─────────────────────────

val MonoSubscriptionGetEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$MonoSubscription;",
    name = "getEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MonoSubscriptionIsSafetyToolAvailableFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$MonoSubscription;",
    name = "isSafetyToolAvailable",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MonoSubscriptionGetHidePremiumOnboardingFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$MonoSubscription;",
    name = "getHidePremiumOnboarding",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MonoSubscriptionGetShowPlusToPremiumEducationFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$MonoSubscription;",
    name = "getShowPlusToPremiumEducation",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MonoSubscriptionGetShowPlusToPremiumProfileBannerFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$MonoSubscription;",
    name = "getShowPlusToPremiumProfileBanner",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 12: Campaign paywall Activities ────────────────────────────────────────
// AccessFlags.FINAL intentionally omitted — R8 may or may not emit FINAL on
// Activity subclass onCreate(); omitting it makes the fingerprint robust to
// both variants without affecting match precision (definingClass+name+params
// already uniquely identifies these methods).

val OnboardingOverridePaywallOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/campaign/OnboardingOverridePaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val InAppOverridePaywallOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/campaign/InAppOverridePaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── Layer 16: SubscriptionDigest getter ──────────────────────────────────────────

val SubscriptionDigestGetStateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/SubscriptionDigest;",
    name = "getSubscriptionState",
    returnType = "Lsp0n/citizen/data/user/dto/SubscriptionState;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 17: ClarityMasterSwitch boolean getters ────────────────────────────────
//
// Patch each iget-boolean getter directly instead of replacing the Companion.get()
// factory (which used new-instance smali that crashed InlineSmaliCompiler).
// Fields confirmed from smali: mapTooltipPlusUpsellEnabled, radioClipsPlusUpsellEnabled,
// settingsPlusUpsellEnabled (all ZZZ constructor).

val ClarityMapTooltipUpsellEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$ClarityMasterSwitch;",
    name = "getMapTooltipPlusUpsellEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ClarityRadioClipsUpsellEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$ClarityMasterSwitch;",
    name = "getRadioClipsPlusUpsellEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ClaritySettingsUpsellEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$ClarityMasterSwitch;",
    name = "getSettingsPlusUpsellEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 18: CitizenPlusV1MasterSwitch boolean getters ──────────────────────────
//
// Fields confirmed from smali: neighborhoodTrendsEnabled, radioClipsEnabled (ZZ constructor).

val PlusV1NeighborhoodTrendsEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$CitizenPlusV1MasterSwitch;",
    name = "getNeighborhoodTrendsEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val PlusV1RadioClipsEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$CitizenPlusV1MasterSwitch;",
    name = "getRadioClipsEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 19: Superwall backstop ─────────────────────────────────────────────────
// AccessFlags.FINAL intentionally omitted — same rationale as Layer 12/22.

val SuperwallPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/superwall/sdk/paywall/view/SuperwallPaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── Layer 20: Safety Network UI paywall gates ────────────────────────────────────
//
// Safety Tools unlocked (Layer 13 ✓) but Safety Network still paywalled.
// Layer 7 only stops expiry cleanup - the UI entry point has its own gates.
//
// Gate map (confirmed from APK smali, June 2026):
//   a) PrivateUser.isProtectEligible()       — eligibility check distinct from isProtectActive
//   b) PrivateUser.isProtectSubscriber()     — subscriber flag used in safetynetwork flows
//   c) MonoSubscription.isSafetyNetworkAvailable() — FeatureConfig gate
//   d) SafetyNetworkEducationViewModel.o()  — obfuscated gate in h.smali at
//        Lsp0n/citizen/social/safetynetwork/h;
//        Calls ShowPaywallUseCase.d()Z — returns true (show paywall) when user is
//        not a paid subscriber OR safety network member list has ≤1 entry.
//        ShowPaywallUseCase.d() is already patched to return false in Layer 5
//        (ShowPaywallUseCaseDFingerprint → return false/0x0), so this gate will
//        already be blocked. The Activity dismiss in Layer 22 is the final backstop.
//
// NOTE: The previous definingClass for SafetyNetworkPaywallVMGateFingerprint
//   was Lsp0n/citizen/social/paywall/b; — that package does NOT exist in this APK.
//   Corrected to Lsp0n/citizen/social/safetynetwork/h; (confirmed June 2026).

val PrivateUserIsProtectEligibleFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isProtectEligible",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val PrivateUserIsProtectSubscriberFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isProtectSubscriber",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MonoSubscriptionIsSafetyNetworkAvailableFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$MonoSubscription;",
    name = "isSafetyNetworkAvailable",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SafetyNetworkPaywallVMGateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/h;",
    name = "n",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 21: Clarity entrypoint enabled getter ──────────────────────────────────

val ClarityProfileEntrypointEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/clarity/ClarityEntrypointRepository;",
    name = "getProfileEntrypointEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)

// ── Layer 22: Additional Paywall Activity gates ──────────────────────────────────

val ClarityPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/clarity/ClarityPaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val ComparePlansActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/compare/ComparePlansActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val CarouselPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/inapp/CarouselPaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val PromoOfferPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/promooffer/PromoOfferPaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val PremiumEducationalPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/PremiumEducationalPaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val SuperwallOnboardingWrapperActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/SuperwallOnboardingWrapperActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val SubscriptionCenterActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/center/SubscriptionCenterActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val SafetyCenterPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/safetycenter/paywall/SafetyCenterPaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val SafetyNetworkEducationActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/SafetyNetworkEducationActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val FamilyPlanBenefitActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/FamilyPlanBenefitActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val TrustedContactsConfigGetEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$TrustedContactsConfig;",
    name = "getEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val PaywallHomescreenTriggerConfigGetEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$PaywallHomescreenTriggerConfig;",
    name = "getEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 23: Safety Network Flow collector paywall suppression ──────────────────

val SafetyNetworkEducationFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/SafetyNetworkEducationActivity\$a\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SafetyNetworkSingleInviteFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/SafetyNetworkSingleInviteActivity\$a\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SafetyNetworkPendingInvitesFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/SafetyNetworkPendingInvitesActivity\$a\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val FamilyPlanBenefitFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/FamilyPlanBenefitActivity\$a\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 24: MainActivity paywall flow collector suppression ────────────────────

val MainActivityPaywallFlowCollectorAFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$a\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorBFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$b\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorCFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$c\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorDFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$d\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorEFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$e\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorFFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$f\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorGFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$g\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorHFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$h\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorAbaFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$a\$b\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorBbaFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$b\$b\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorCbaFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$c\$b\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MainActivityPaywallFlowCollectorDbaFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$d\$b\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 25: PremiumEducationalPaywallActivity internal collector suppression ───
// PremiumEducationalPaywallActivity$b$a$a.emit() collects C17951c.a product-load state.
// Even with Layer 22 dismissing the Activity in onCreate, this collector can still
// fire before finish() completes and continue the paywall flow. No-op it directly.
// Smali confirmed from smali_classes5 (v0.1293.0):
//   emit(Ljava/lang/Object;Lux/b;)Ljava/lang/Object;
// Descriptor is Lux/b; — same continuation type as Layers 23/24, NOT Lvx/b;.
val PremiumEducationalPaywallInternalCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/PremiumEducationalPaywallActivity\$b\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 26: Cross-package PremiumEducational flow collector suppression ─────────
//
// These collectors were found calling PremiumEducationalPaywallActivity$a (static
// Intent factory) from outside the main/safetynetwork packages. All confirmed
// emit(Ljava/lang/Object;Lux/b;)Ljava/lang/Object; via smali grep (June 2026).
//
// Confirmed citizen-package collectors (full smali path verified):
//   smali_classes5/sp0n/citizen/zones/SafetyZoneActivity$c$a$a  — zones flow
//   smali_classes5/sp0n/citizen/menu/h$a$a                      — menu/nav flow
//   smali_classes5/sp0n/citizen/onboarding/h$a$a                — onboarding flow
//   smali_classes5/sp0n/citizen/profile/h$a$a                   — profile flow
//   smali_classes5/sp0n/citizen/safetyhome/l$a$a                — safety home flow
//
// Layer 27: MyProfileFragment direct startActivity call site
//   smali_classes5/sp0n/citizen/profile/myprofile/ui/h$a$a
//     — MyProfileFragment Effects$1$1$1 (source "MyProfileFragment.kt")
//     — DISTINCT from sp0n/citizen/profile/h$a$a (shallow package, different Fragment)
//     — emit(Object, ux/b)Object — same flow collector pattern as all other layers
//     — NOT invokeSuspend: targeting invokeSuspend wipes the full coroutine state
//       machine including Theme.AppCompat UI init, breaking Safety Tools / Safety
//       Network / Alert Zones / Friends screens (ThemeUtils crash confirmed in logcat)
//
// Obfuscated collectors (runCatching-protected; package may shift across builds):
//   smali_classes5/w50/f1$a$a   — confirmed PremiumEducational launcher in paste.txt
//   smali_classes5/w70/l$a$a    — confirmed present alongside safetyhome/l$a$a

val SafetyZonePaywallFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/zones/SafetyZoneActivity\$c\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MenuPaywallFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/menu/h\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val OnboardingPaywallFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/onboarding/h\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ProfilePaywallFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/profile/h\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SafetyHomePaywallFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/safetyhome/l\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// Layer 27: MyProfileFragment sub-package — emit() only, NOT invokeSuspend()
// Targeting invokeSuspend wipes full coroutine body including UI init → broken features.
// emit() on the $a$a inner lambda is the safe surgical target (paywall dispatch only).
val MyProfileFragmentPaywallCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/profile/myprofile/ui/h\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// Obfuscated — runCatching in patch; class name may not exist in future builds
val ObfuscatedW50F1PaywallCollectorFingerprint = Fingerprint(
    definingClass = "Lw50/f1\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ObfuscatedW70LPaywallCollectorFingerprint = Fingerprint(
    definingClass = "Lw70/l\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Liq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 28 REVERTED ─────────────────────────────────────────────────────────────
//
// SafetyNetworkRepository.isFeatureEnabled() is a GENERIC suspend function reused
// for ALL FeatureConfigValue subtypes (TrustedContactsConfig, PremiumEducationalPaywall,
// and others). The $isFeatureEnabled$1 continuation is the SHARED coroutine body for
// every call site. Patching invokeSuspend() on it breaks all callers — confirmed by
// ClassCastException: kotlin.Unit cannot be cast to FeatureConfigValue$TrustedContactsConfig
// at SafetyNetworkRepository.kt:68 on every launch.
//
// The correct intercept point is the specific downstream collector that acts on the
// PremiumEducationalPaywall result, not the upstream shared coroutine body.
// Layers 23–27 already cover all known downstream collectors. No replacement added.

val PurchasePremiumHelperCreateIntentFingerprint = Fingerprint(
    definingClass = "Lv8d;",
    name = "a",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lsp0n/citizen/data/variablesettings/VariableSettingsRepository;",
        "Lsp0n/citizen/analytics/premium/purchase/PurchaseAnalyticsApi\$ProtectPaywallOrigin;"
    ),
    returnType = "Landroid/content/Intent;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

val PremiumEducationalPaywallActivityCreateIntentFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/PremiumEducationalPaywallActivity\$a;",
    name = "a",
    parameters = listOf("Landroid/content/Context;", "Z", "Ljava/lang/String;"),
    returnType = "Landroid/content/Intent;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)



