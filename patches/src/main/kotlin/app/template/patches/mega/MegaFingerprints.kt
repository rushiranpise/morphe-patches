package app.template.patches.mega

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─── Pro entitlement ────────────────────────────────────────────────────────

/**
 * AccountTypeMapper.a(I) — static method mapping raw proLevel int → AccountType enum.
 * Called by DefaultAccountRepository at every account detail fetch.
 * All entitlement gates (upgrade prompt, hidden nodes, transfer quota banner,
 * current plan display) chain through the AccountType stored in AccountLevelDetail.a,
 * which is always populated via this mapper.
 *
 * Mapping: 0=FREE, 1=PRO_I, 2=PRO_II, 3=PRO_III, 4=PRO_LITE, 100=BUSINESS, 101=PRO_FLEXI
 * Forcing PRO_I makes every downstream check treat the account as paid.
 *
 * No const-string in method body — matched via returnType + opcodes + custom class.
 */
object AccountTypeMapperFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Lmega/privacy/android/domain/entity/AccountType;",
    parameters = listOf("I"),
    filters = listOf(
        opcode(Opcode.IF_EQZ),
        opcode(Opcode.CONST_4),
        opcode(Opcode.IF_EQ),
        opcode(Opcode.SGET_OBJECT),
        opcode(Opcode.RETURN_OBJECT),
    ),
    custom = { _, classDef ->
        classDef.type == "Lmega/privacy/android/data/mapper/AccountTypeMapper;"
    }
)

/**
 * AccountType.isPaid() — returns false only for FREE and UNKNOWN enum values.
 * Used as a feature gate throughout the app for Pro-only features
 * (hidden nodes, hidden nodes onboarding, node toolbar/bottom-sheet items, etc.).
 *
 * No const-string in method body — matched via accessFlags + custom class+method.
 */
object AccountTypeIsPaidFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lmega/privacy/android/domain/entity/AccountType;" &&
            method.name == "isPaid"
    }
)

// ─── Ads removal ────────────────────────────────────────────────────────────

/**
 * GoogleAdsManager.c() — creates BannerAdRequest with MEGA's ad unit and pushes
 * it to the AdsUiState MutableStateFlow. NewAdsContainer checks BannerAdRequest != null
 * before rendering; nooping this method keeps it null → no banner shown.
 *
 * Two strings present in this method and nowhere else in the APK.
 */
object GoogleAdsManagerCreateAdRequestFingerprint : Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    strings = listOf(
        "ca-app-pub-2135147798858967/9835644604",
        "Creating new AdRequest",
    ),
    custom = { _, classDef ->
        classDef.type == "Lmega/privacy/android/shared/ads/advertisements/GoogleAdsManager;"
    }
)

/**
 * AdsViewModel.c() — checkForAdsAvailability.
 * Calls GetFeatureFlagValueUseCase(GoogleAdsFeatureFlag) and writes the boolean
 * result to AdsUiState.isAdsFeatureEnabled (MutableStateFlow). When the cached
 * server flag is true, this triggers GoogleAdsManager.c() to create an AdRequest.
 *
 * Returning Unit early prevents isAdsFeatureEnabled from ever being set true,
 * blocking the upstream trigger for ad loading.
 *
 * Both strings present only in this method body.
 */
object AdsViewModelCheckAvailabilityFingerprint : Fingerprint(
    strings = listOf(
        "Ads feature enabled: ",
        "Error getting feature flag value",
    ),
    custom = { _, classDef ->
        classDef.type == "Lmega/privacy/android/shared/ads/advertisements/AdsViewModel;"
    }
)
