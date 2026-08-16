package app.template.patches.protonvpn.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ProtonVPN v5.19.78.0 (versionCode 605197800)
// Strategy: spoof Plus UI tier but route ALL actual connections through free servers only.
// Free servers (tier 0) are accessible server-side without subscription — connection works.
// Plus UI features are unlocked client-side for a seamless experience.
// Adapted from Paresh-Maheshwari's patch for this version.

// === VpnUser tier getters ===

object VpnUserGetUserTierFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "getUserTier",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

object VpnUserGetMaxTierFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "getMaxTier",
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

object VpnUserIsFreeUserFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "isFreeUser",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

object VpnUserIsUserPlusOrAboveFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "isUserPlusOrAbove",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// VpnUser.getUserTierName() — plan name string used for display, defaults to "free"
// "vpn2022" signals a current paid plan to feature flag checks.
object VpnUserGetUserTierNameFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "getUserTierName",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// === Server access gates ===

// VpnUserKt.hasAccessToServer(VpnUser, Server) — server.tier <= user.tier
object HasAccessToServerFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf(
        "Lcom/protonvpn/android/auth/data/VpnUser;",
        "Lcom/protonvpn/android/servers/Server;",
    ),
    filters = listOf(
        methodCall(definingClass = "Lcom/protonvpn/android/servers/Server;", name = "getTier"),
        methodCall(definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;", name = "getUserTier"),
    ),
)

object HaveAccessWithFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUserKt;",
    name = "haveAccessWith",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf(
        "Lcom/protonvpn/android/servers/Server;",
        "Ljava/lang/Integer;",
    ),
)

// Countries tab server list filter — only show free servers (inverted from Paresh)
// asFilteredSequence_fnZhiP4$lambda$0: p0=isFreeUser, p6=Server
// We short-circuit: if server is NOT free → return false (hide it)
object ServerListFilterFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf(
        "Z",
        "Lcom/protonvpn/android/redesign/countries/ui/ServerFilterType;",
        "Ljava/lang/String;",
        "Lcom/protonvpn/android/redesign/CityStateId;",
        "Z",
        "Ljava/lang/String;",
        "Lcom/protonvpn/android/servers/Server;",
    ),
    filters = listOf(
        methodCall(definingClass = "Lcom/protonvpn/android/servers/Server;", name = "isFreeServer"),
    ),
)

// ServerGroupUiItem$ServerGroup.getAvailable() — controls connect vs upsell in Countries tab
object ServerGroupGetAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/redesign/countries/ui/ServerGroupUiItem\$ServerGroup;",
    name = "getAvailable",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// ServerManager.getBestScoreServer — picks actual server for connection
// Pre-filters the input list to free servers only before scoring
object GetBestScoreServerFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/utils/ServerManager;",
    name = "getBestScoreServer",
    returnType = "Lcom/protonvpn/android/servers/Server;",
    parameters = listOf(
        "Ljava/lang/Iterable;",
        "Lcom/protonvpn/android/auth/data/VpnUser;",
        "Lcom/protonvpn/android/vpn/ProtocolSelection;",
        "Ljava/util/List;",
    ),
)

// === Feature flags and UI ===

// IsFeatureFlagEnabledImpl.invoke(UserId) — base class for all feature flags
// Force all feature flags to enabled
object IsFeatureFlagEnabledFingerprint : Fingerprint(
    definingClass = "Lme/proton/core/featureflag/data/IsFeatureFlagEnabledImpl;",
    name = "invoke",
    returnType = "Z",
    parameters = listOf("Lme/proton/core/domain/entity/UserId;"),
    filters = listOf(
        methodCall(definingClass = "Lme/proton/core/featureflag/data/IsFeatureFlagEnabledImpl;", name = "isLocalEnabled"),
        methodCall(definingClass = "Lme/proton/core/featureflag/data/IsFeatureFlagEnabledImpl;", name = "isRemoteEnabled"),
    ),
)

// NetShieldAvailabilityKt.getNetShieldAvailability(VpnUser) → AVAILABLE
object GetNetShieldAvailabilityFingerprint : Fingerprint(
    returnType = "Lcom/protonvpn/android/netshield/NetShieldAvailability;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/protonvpn/android/auth/data/VpnUser;"),
    filters = listOf(
        methodCall(definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;", name = "isFreeUser"),
        fieldAccess(name = "AVAILABLE"),
    ),
)

// ServerGroupsViewModel.getFilterButtons — hides SecureCore/P2P/Tor filter tabs
object GetFilterButtonsFingerprint : Fingerprint(
    returnType = "Ljava/util/List;",
    accessFlags = listOf(AccessFlags.PROTECTED, AccessFlags.FINAL),
    parameters = listOf(
        "Ljava/util/Set;",
        "Lcom/protonvpn/android/redesign/countries/ui/ServerFilterType;",
        "I",
        "Ljava/util/Set;",
        "Lkotlin/jvm/functions/Function1;",
    ),
    filters = listOf(
        string("availableTypes"),
        methodCall(definingClass = "Lcom/protonvpn/android/redesign/countries/ui/ServerFilterType;", name = "getEntries"),
    ),
)

// ProfilesServerDataAdapter.countries — redirect getVpnCountries → getFreeCountries
object ProfileCountriesFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/profiles/ui/ProfilesServerDataAdapter;",
    name = "countries",
    filters = listOf(
        methodCall(definingClass = "Lcom/protonvpn/android/servers/ServerManager2;", name = "getVpnCountries"),
    ),
)

// TypeAndLocationScreenState$Standard.getAvailableTypes — only Standard profile type
object ProfileAvailableTypesFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/profiles/ui/TypeAndLocationScreenState\$Standard;",
    name = "getAvailableTypes",
    returnType = "Ljava/util/List;",
    parameters = emptyList(),
)

// VpnUser.<init> — inject here to force maxTier=Integer(3) and subscribed=1 at construction.
// This is the ProtonPass approach: override fields at construction time so all derived
// getters (getUserTier, getMaxTier, isFreeUser, isUserPlusOrAbove, getSubscribed) derive
// the correct values naturally — no need to patch each getter individually.
//
// Constructor params (0-indexed instruction view, index 10 = first iput after super.<init>()):
//   p0 = this
//   p2 = subscribed:I     → force 1
//   p12 = maxTier:Integer → force Integer.valueOf(3) via v3
//
// .locals 4 → v0..v3 available. v3 is free after the null-check preamble.
//
// Fingerprint: unique by definingClass + name=<init> + exact parameter list.
object VpnUserConstructorFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "<init>",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Lme/proton/core/domain/entity/UserId;",
        "I", "I", "I", "I", "Z", "I", "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Z",
        "Ljava/lang/Integer;",
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "J",
        "Lme/proton/core/network/domain/session/SessionId;",
        "Ljava/lang/String;",
        "Lcom/protonvpn/android/models/login/NetShieldConfig;",
    ),
)
