package app.template.patches.citizen

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ─────────────────────────────────────────────────────────────────────────────
// Non-obfuscated approach (v0.1301.0+)
//
// Two parallel premium state paths — both need seeding:
//
// PATH A: PrivateUser (incident paywall, most feature gates)
//   CitizenPlusInfoDTO.getActive()Z         ← TARGET 1
//   CitizenProtectInfoDTO.getActive()Z      ← TARGET 2
//   → PrivateUserMapper.toModel() → PrivateUser.{isPlusActive,isProtectActive,isPaid}
//   → ShowPaywallUseCase.a/c/d/e/f()
//
// PATH B: SubscriptionDigest (SafetyCenter, SubscriptionRepository gates)
//   PremiumSubscriptionDTO.getSubscriptionState() ← TARGET 3
//   → SubscriptionDigestDTOKt.toModel() → SubscriptionDigest
//   → SubscriptionRepository._currentSubscription StateFlow
//   → SafetyCenterPaywallVMGate.n() / SafetyNetworkPaywallVMGate.n()
//   → SubscriptionDigest.getSubscriptionState() ← TARGET 4 (any direct reads)
//
// ROOT CAUSE of null initial state:
//   SubscriptionRepository._currentSubscription = MutableStateFlow(null) in constructor.
//   Until API responds, currentSubscription.getValue() == null.
//   SafetyCenterPaywallVMGate.n(): if null → return 1 (show paywall).
//   Fix: TARGET 9 — seed the StateFlow with ACTIVATED in the constructor.
//
// INDEPENDENT PATHS:
//   Superwall SDK (TARGET 5)
//   MonoSubscription feature flags (TARGETS 6+7)
//   Clarity entrypoint (TARGET 8)
// ─────────────────────────────────────────────────────────────────────────────

val CitizenPlusInfoDTOGetActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/CitizenPlusInfoDTO;",
    name = "getActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val CitizenProtectInfoDTOGetActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/CitizenProtectInfoDTO;",
    name = "getActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val PremiumSubscriptionDTOGetStateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/PremiumSubscriptionDTO;",
    name = "getSubscriptionState",
    returnType = "Lsp0n/citizen/data/user/dto/SubscriptionState;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val SubscriptionDigestGetStateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/SubscriptionDigest;",
    name = "getSubscriptionState",
    returnType = "Lsp0n/citizen/data/user/dto/SubscriptionState;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val SuperwallSetSubscriptionStatusFingerprint = Fingerprint(
    definingClass = "Lcom/superwall/sdk/Superwall;",
    name = "internallySetSubscriptionStatus\$superwall_release",
    parameters = listOf("Lcom/superwall/sdk/models/entitlements/SubscriptionStatus;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val MonoSubscriptionGetEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$MonoSubscription;",
    name = "getEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val MonoSubscriptionIsSafetyToolAvailableFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/variablesettings/FeatureConfigValue\$MonoSubscription;",
    name = "isSafetyToolAvailable",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val ClarityProfileEntrypointEnabledFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/clarity/ClarityEntrypointRepository;",
    name = "getProfileEntrypointEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
)

// TARGET 9: Seed SubscriptionRepository._currentSubscription with ACTIVATED on construction.
// Constructor sets _currentSubscription = MutableStateFlow(null) then returns.
// We addInstructions before return-void to call:
//   new SubscriptionDigest(ACTIVATED, null, null) → _currentSubscription.setValue(digest)
// This ensures currentSubscription.getValue() is never null → n() never shows paywall
// due to null state.
// .registers 15 in constructor; v0..v8 used; we use v8 (already used as temp) safely.
val SubscriptionRepositoryConstructorFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/SubscriptionRepository;",
    name = "<init>",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lsp0n/citizen/data/user/UserRetrofitApi;",
        "Lsp0n/citizen/data/user/PrivateUserRepository;",
        "Loc0;",
        "Lsp0n/citizen/data/variablesettings/VariableSettingsRepository;",
        "Ltv3;",
    ),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

// Paywall Activity dismissals — belt-and-suspenders
// All non-obfuscated Activity subclasses with stable sp0n.citizen.* package paths.

val ClarityPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/clarity/ClarityPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val ComparePlansActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/compare/ComparePlansActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val CarouselPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/inapp/CarouselPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val PromoOfferPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/promooffer/PromoOfferPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val PremiumEducationalPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/PremiumEducationalPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val SuperwallOnboardingWrapperActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/superwall/SuperwallOnboardingWrapperActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val SubscriptionCenterActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/paywall/center/SubscriptionCenterActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val SafetyCenterPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/safetycenter/paywall/SafetyCenterPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val SafetyNetworkEducationActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/SafetyNetworkEducationActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val FamilyPlanBenefitActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/social/safetynetwork/FamilyPlanBenefitActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
val SuperwallPaywallActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/superwall/sdk/paywall/view/SuperwallPaywallActivity;",
    name = "onCreate", parameters = listOf("Landroid/os/Bundle;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ── ShowPaywallUseCase gates ──────────────────────────────────────────────────
// Semantics (critical — easy to get backwards):
//   a(SubscriptionFeature)Z  true = user HAS ACCESS (no paywall), false = show paywall
//   c()Z                     true = user IS premium
//   d()Z                     true = user IS premium (Protect tier specifically)
//   e()Z                     true = SHOW safety network paywall
//   f(Boolean)Z              true = SHOW conditional paywall

val ShowPaywallUseCaseAFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "a",
    parameters = listOf("Lsp0n/citizen/incidentdetail/ShowPaywallUseCase\$SubscriptionFeature;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val ShowPaywallUseCaseCFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "c",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val ShowPaywallUseCaseDFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "d",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val ShowPaywallUseCaseEFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "e",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val ShowPaywallUseCaseFFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "f",
    parameters = listOf("Ljava/lang/Boolean;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── PrivateUser subscription state getters ────────────────────────────────────

val PrivateUserIsPaidFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isPaid",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
