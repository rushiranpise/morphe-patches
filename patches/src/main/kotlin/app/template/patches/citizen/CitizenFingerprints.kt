package app.template.patches.citizen

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * SubscriptionDigestDTOKt.toModel(SubscriptionDigestDTO) -> SubscriptionDigest
 *
 * Converts the server JSON DTO into the in-memory SubscriptionDigest model.
 * Strategy: inject ACTIVATED into register v1 at index 13 before the
 * SubscriptionDigest constructor consumes it.
 */
val SubscriptionDigestToModelFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/SubscriptionDigestDTOKt;",
    name = "toModel",
    parameters = listOf("Lsp0n/citizen/data/user/dto/SubscriptionDigestDTO;"),
    returnType = "Lsp0n/citizen/data/user/dto/SubscriptionDigest;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

/**
 * CitizenPlusInfoDTO.getActive()Z
 *
 * Returns whether the Plus subscription is active from the API response.
 * Strategy: returnEarly(true) - forces true for every caller.
 */
val CitizenPlusInfoGetActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/CitizenPlusInfoDTO;",
    name = "getActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * CitizenProtectInfoDTO.getActive()Z
 *
 * Returns whether the Protect subscription is active from the API response.
 * Strategy: returnEarly(true) - forces true for every caller.
 */
val CitizenProtectInfoGetActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/data/user/dto/CitizenProtectInfoDTO;",
    name = "getActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * Superwall.internallySetSubscriptionStatus(SubscriptionStatus) -> V
 *
 * Controls paywall screen display in the Superwall SDK.
 * Strategy: inject SubscriptionStatus.Active into p1 at index 0.
 */
val SuperwallSetSubscriptionStatusFingerprint = Fingerprint(
    definingClass = "Lcom/superwall/sdk/Superwall;",
    name = "internallySetSubscriptionStatus\$superwall_release",
    parameters = listOf("Lcom/superwall/sdk/models/entitlements/SubscriptionStatus;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * PrivateUser.isPlusActive()Z
 *
 * Domain model getter for Plus subscription status (reads stored boolean field).
 * Called by ShowPaywallUseCase.c(), menu, feed, social features.
 * Strategy: replace body with const/4 v0, 0x1 / return v0.
 */
val PrivateUserIsPlusActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isPlusActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * PrivateUser.isProtectActive()Z
 *
 * Domain model getter for Protect subscription status (reads stored boolean field).
 * Called by ShowPaywallUseCase.d(), Shield/Protect UI, safety network.
 * Strategy: replace body with const/4 v0, 0x1 / return v0.
 */
val PrivateUserIsProtectActiveFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isProtectActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * PrivateUser.isProtectActiveOrInSetup()Z
 *
 * Domain model getter gating Safety Network access (reads stored boolean field).
 * This is the PRIMARY gate for Safety Network — checked by SafetyNetworkRepository,
 * SafetyCenterMember, CommunityHomeViewModel, SafetyHomeFeedActionHandler.
 * Strategy: replace body with const/4 v0, 0x1 / return v0.
 */
val PrivateUserIsProtectActiveOrInSetupFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isProtectActiveOrInSetup",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * ShowPaywallUseCase.a(SubscriptionFeature)Z
 *
 * Master paywall gate for ALL premium features.
 * Consults server-controlled FeatureConfigValue\$SubscriptionBasedFeatures
 * BEFORE checking subscription — triggers paywall even when domain getters
 * are patched if remote config is absent/stale.
 * Strategy: replace body with const/4 p1, 0x1 / return p1 (p1 = first param reg).
 */
val ShowPaywallUseCaseAFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "a",
    parameters = listOf("Lsp0n/citizen/incidentdetail/ShowPaywallUseCase\$SubscriptionFeature;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * ShowPaywallUseCase.c()Z
 *
 * Plus subscription sub-check: calls PrivateUserRepository.getUser().isPlusActive().
 * Called by a() for the plusFeatures config path and directly by safetyhome
 * feed delegates.
 * Strategy: replace body with const/4 v0, 0x1 / return v0.
 */
val ShowPaywallUseCaseCFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "c",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * ShowPaywallUseCase.d()Z
 *
 * Protect/Premium subscription sub-check: calls PrivateUser.isPlusActive() +
 * isProtectActive() with MonoSubscription flag logic.
 * Called by a() for the premiumFeatures path, SafetyZoneActivity (FOLLOW_LOCATION),
 * safetyhome/s.smali (lines 6548/6610), SafetyNetworkEducationActivity, and
 * social/safetynetwork/h.smali (lines 400/415). This is the direct cause of the
 * Safety Network paywall — q$b$i (ShowPaywallRequired) state is emitted when d()
 * returns false.
 * Strategy: replace body with const/4 v0, 0x1 / return v0.
 */
val ShowPaywallUseCaseDFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/incidentdetail/ShowPaywallUseCase;",
    name = "d",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * PrivateUser.isPaid()Z
 *
 * Stored boolean field on PrivateUser — derived at construction time from
 * isProtectActiveOrInSetup || isPlusActive || isProtectActive.
 * Because it is a stored FIELD (iget-boolean), NOT a computed getter,
 * the Layer 4 domain getter patches do NOT propagate to it.
 * Called by SafetyNetworkRepository.removeNetworkIfExpired(), menu/f.smali,
 * main/b.smali, CommunityHomeFeedViewModel, ClarityEntrypointRepository.
 * Strategy: replace body with const/4 v0, 0x1 / return v0.
 */
val PrivateUserIsPaidFingerprint = Fingerprint(
    definingClass = "Lsp0n/citizen/domain/models/user/PrivateUser;",
    name = "isPaid",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)