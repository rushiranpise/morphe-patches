package app.template.patches.citizen

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── Layer 2 ───────────────────────────────────────────────────────────────────────

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

// ── Layer 3 ───────────────────────────────────────────────────────────────────────

val SuperwallSetSubscriptionStatusFingerprint = Fingerprint(
    definingClass = "Lcom/superwall/sdk/Superwall;",
    name = "internallySetSubscriptionStatus\$superwall_release",
    parameters = listOf("Lcom/superwall/sdk/models/entitlements/SubscriptionStatus;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// AndroidSuperwall.getEnabled() — gate in LaunchSuperwallWrapper.b().
// If true → Superwall.register() called → paywall shown.
val AndroidSuperwallGetEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/AndroidSuperwall;",
    name = "getEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 4 ───────────────────────────────────────────────────────────────────────

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

// ── Layer 5 ───────────────────────────────────────────────────────────────────────

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

val ShowPaywallUseCaseEFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "e",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ShowPaywallUseCaseFFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "f",
    parameters = listOf("Ljava/lang/Boolean;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val PrivateUserIsPaidFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isPaid",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 6 ───────────────────────────────────────────────────────────────────────
// SafetyCenterPaywallVMGate — was Lsp0n/citizen/safetycenter/paywall/b; in older builds.
// safetycenter/paywall package absent in v0.1303.2; wrapped in runCatching for compat.

val SafetyCenterPaywallVMGateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/safetycenter/paywall/b;",
    name = "n",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 7 ───────────────────────────────────────────────────────────────────────

val SafetyNetworkRemoveExpiredFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/social/SafetyNetworkRepository\$removeNetworkIfExpired\$1;",
    name = "invokeSuspend",
    parameters = listOf("Ljava/lang/Object;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 10 ──────────────────────────────────────────────────────────────────────

val ClarityEntrypointVisibleFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/clarity/ClarityEntrypointRepository\$profileEntrypointVisible\$1;",
    name = "invokeSuspend",
    parameters = listOf("Ljava/lang/Object;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layers 11/13/14/15 ────────────────────────────────────────────────────────────
// MonoSubscription.getEnabled() removed in v0.1303.2 — runCatching protects.

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

// ── Layer 12 ──────────────────────────────────────────────────────────────────────

val OnboardingOverridePaywallOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/campaign/OnboardingOverridePaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// InAppOverridePaywallActivity — may not exist in all builds
val InAppOverridePaywallOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/campaign/InAppOverridePaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── Layer 17 ──────────────────────────────────────────────────────────────────────

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

// ── Layer 18 ──────────────────────────────────────────────────────────────────────

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

// ── Layer 19 ──────────────────────────────────────────────────────────────────────

val SuperwallPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/superwall/sdk/paywall/view/SuperwallPaywallActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── Layer 20 ──────────────────────────────────────────────────────────────────────

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

// ── Layer 21 ──────────────────────────────────────────────────────────────────────

val ClarityProfileEntrypointEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/clarity/ClarityEntrypointRepository;",
    name = "getProfileEntrypointEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)

// ── Layer 22 ──────────────────────────────────────────────────────────────────────

val ClarityPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/clarity/ClarityPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val ComparePlansActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/compare/ComparePlansActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val CarouselPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/inapp/CarouselPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val PromoOfferPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/promooffer/PromoOfferPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val PremiumEducationalPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/PremiumEducationalPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val SuperwallOnboardingWrapperActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/SuperwallOnboardingWrapperActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val SubscriptionCenterActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/center/SubscriptionCenterActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val SafetyCenterPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/safetycenter/paywall/SafetyCenterPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val SafetyNetworkEducationActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/SafetyNetworkEducationActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val FamilyPlanBenefitActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/FamilyPlanBenefitActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
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

// ── Layer 23: Safety Network flow collectors ──────────────────────────────────────
// Continuation type: zp3 (older) → nq3 (v0.1303.2)

val SafetyNetworkEducationFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/SafetyNetworkEducationActivity\$a\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SafetyNetworkSingleInviteFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/SafetyNetworkSingleInviteActivity\$a\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SafetyNetworkPendingInvitesFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/SafetyNetworkPendingInvitesActivity\$a\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val FamilyPlanBenefitFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/FamilyPlanBenefitActivity\$a\$a\$a;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 24: MainActivity paywall flow collectors ────────────────────────────────
// R8 merged into NavigationType in v0.1298.0. Kept with runCatching. Updated to nq3.

val MainActivityPaywallFlowCollectorAFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$a\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorBFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$b\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorCFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$c\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorDFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$d\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorEFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$e\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorFFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$f\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorGFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$g\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorHFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$h\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorAbaFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$a\$b\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorBbaFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$b\$b\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorCbaFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$c\$b\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MainActivityPaywallFlowCollectorDbaFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/main/MainActivity\$d\$b\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 25: PremiumEducational internal collector ───────────────────────────────

val PremiumEducationalPaywallInternalCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/PremiumEducationalPaywallActivity\$b\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Layer 26+27: Cross-package collectors ─────────────────────────────────────────

val MenuPaywallFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/menu/h\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val OnboardingPaywallFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/onboarding/h\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val ProfilePaywallFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/profile/h\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val SafetyHomePaywallFlowCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/safetyhome/l\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val MyProfileFragmentPaywallCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/profile/myprofile/ui/h\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val SafetyCenterPaywallActivityCollectorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/safetycenter/paywall/SafetyCenterPaywallActivity\$b\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ObfuscatedW50F1PaywallCollectorFingerprint = Fingerprint(
    definingClass = "Lw50/f1\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
val ObfuscatedW70LPaywallCollectorFingerprint = Fingerprint(
    definingClass = "Lw70/l\$a\$a;",
    name = "emit", parameters = listOf("Ljava/lang/Object;", "Lnq3;"),
    returnType = "Ljava/lang/Object;", accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── PurchasePremiumHelper ─────────────────────────────────────────────────────────

// v8d (older) → nyc (v0.1303.2): extra Z param added; c3d is now an interface, skip.
val PurchasePremiumHelperCreateIntentFingerprint = Fingerprint(
    definingClass = "Lnyc;",
    name = "a",
    parameters = listOf(
        "Landroid/content/Context;",
        "Z",
        "Lsp0n/citizen/data/variablesettings/VariableSettingsRepository;",
        "Lsp0n/citizen/analytics/premium/purchase/PurchaseAnalyticsApi\$ProtectPaywallOrigin;"
    ),
    returnType = "Landroid/content/Intent;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

// c3d is now an abstract interface in v0.1303.2 — fingerprint retained for older builds.
val PurchasePremiumHelperC3DFingerprint = Fingerprint(
    definingClass = "Lc3d;",
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

// ── NavigationType paywall launchers (v0.1298.0) — gone in v0.1303.2 ─────────────
// b1b$h0/i0/u0 subclasses absent in v0.1303.2. runCatching protects.

val NavigationTypeH0PaywallFingerprint = Fingerprint(
    definingClass = "Lb1b\$h0;",
    name = "b",
    parameters = listOf("Landroid/content/Context;", "Ljqd;", "Lzp3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val NavigationTypeI0PaywallFingerprint = Fingerprint(
    definingClass = "Lb1b\$i0;",
    name = "b",
    parameters = listOf("Landroid/content/Context;", "Ljqd;", "Lzp3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val NavigationTypeU0PaywallFingerprint = Fingerprint(
    definingClass = "Lb1b\$u0;",
    name = "b",
    parameters = listOf("Landroid/content/Context;", "Ljqd;", "Lzp3;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── ProtectFabHelper ──────────────────────────────────────────────────────────────

// zvc (older) → myc (v0.1303.2): ao→Lun;, tr6→Lzs6;
val ProtectFabHelperPaywallFingerprint = Fingerprint(
    definingClass = "Lmyc;",
    name = "a",
    parameters = listOf(
        "Lsp0n/citizen/data/variablesettings/VariableSettingsRepository;",
        "Lsp0n/citizen/data/store/PreferenceStorage;",
        "Lun;",
        "Z",
        "Lzs6;",
        "Lsp0n/citizen/premium/purchase/ui/ProtectFabOrigin;"
    ),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

// ── _currentSubscription iput locator ────────────────────────────────────────────

val SubscriptionRepositoryCurrentSubIputFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/SubscriptionRepository;",
    name = "<init>",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            definingClass = "Lsp0n/citizen/data/user/SubscriptionRepository;",
            name = "_currentSubscription",
        ),
    ),
)

val SubscriptionRepositoryConstructorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/SubscriptionRepository;",
    name = "<init>",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lsp0n/citizen/data/user/UserRetrofitApi;",
        "Lsp0n/citizen/data/user/PrivateUserRepository;",
        "L",
        "Lsp0n/citizen/data/variablesettings/VariableSettingsRepository;",
        "L",
    ),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

// ── SuperwallInitializer killswitch ──────────────────────────────────────────────
// SuperwallInitializer.create() is the androidx.startup entry point called on
// SplashActivity.onCreate(). It calls configure$default then getInstance().
// Patching create() → return null skips both — Superwall never initializes,
// no pre-caching, no paywall. Returning null is valid for androidx.startup.Initializer.
// DEX: classes5 — smali verified against versionCode 1138.

val SuperwallInitializerCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/init/SuperwallInitializer;",
    name = "create",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Additional manifest-verified paywall Activities ───────────────────────────────
// Confirmed in AndroidManifest.xml but missing from Layer 22.

val MultimonthPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/MultimonthPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val ProtectOnBoardingUpsellActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/onboarding/modules/protect/ProtectOnBoardingUpsellActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val PostDemoRealCallUpsellActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/premium/protect/ui/demo/PostDemoRealCallUpsellActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)
