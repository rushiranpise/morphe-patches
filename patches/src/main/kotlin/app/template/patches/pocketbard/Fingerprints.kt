package app.template.patches.pocketbard

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Pocket Bard (com.MojoFilterMediaLLC.RPGSoundSystem) v3.1.15
//
// Anti-tamper: PairIP wraps Application.attachBaseContext()
//   → LicenseClient.checkLicense() → initializeLicenseCheck()
//   → Google Play LVL network check when licenseCheckState ordinal = 0 (CHECK_REQUIRED)
//   Fix: set licenseCheckState = LOCAL_CHECK_OK (ordinal 2) at method entry → return-void
//
// Subscription architecture:
//   RevCat → toUserInfo(CustomerInfo) → updateUserInfo(expirationDate, ...) → DataStore
//   userInfoFlow() = DataStore.getCachedSubscription() → map lambda:
//     hasValidSubscription = expirationDate != null && expirationDate > Clock.now()
//     → UserInfo { hasValidSubscription, ... }
//   NOTE: hasValidSubscription is NOT persisted - it's recomputed from expirationDate each time.
//
//   All UI reads hasValidSubscription exclusively via UserInfo.getHasValidSubscription().
//   Callers: ProfileViewModel, LibraryViewModel, SoundEffectDetailViewModel,
//            SceneDetailViewModel, DiscoverViewModel, GetDownloadMetadataForSceneUseCase,
//            ObserveSearchStateDataUseCase, ObserveDiscoverDataUseCase, etc.
//
// Patch strategy:
//   1. PairIP: InitializeLicenseCheckFingerprint → early return by setting LOCAL_CHECK_OK
//   2. Subscription: UserInfo.getHasValidSubscription() → always return true
//      Single getter used by all callers; covers online, offline, and any future paths.

/** Targets LicenseClient.initializeLicenseCheck() — PairIP anti-tamper bypass. */
internal object InitializeLicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.SGET_OBJECT),
        opcode(Opcode.INVOKE_VIRTUAL),
    ),
)

/**
 * Targets UserInfo.getHasValidSubscription()Z.
 * The single getter used by all ViewModels and UseCases to check subscription status.
 * Forcing true here makes the app behave as if the user has a valid subscription
 * regardless of DataStore cache or RevenueCat response.
 */
internal object HasValidSubscriptionFingerprint : Fingerprint(
    definingClass = "Lcom/pocketbard/pocketbard/subscription/domain/models/UserInfo;",
    name = "getHasValidSubscription",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.IGET_BOOLEAN),
    ),
)
