package app.template.patches.excel.premium

import app.morphe.patcher.Fingerprint

// ── Stable non-obfuscated fingerprints ───────────────────────────────────────

internal val isPremiumPlanUpsellEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/plat/PlatFeatureGateHelper;",
    name = "isPremiumPlanUpsellEnabled",
    returnType = "Z",
    parameters = emptyList(),
)

internal val isEnterpriseViewOLSCheckEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/plat/PlatFeatureGateHelper;",
    name = "IsEnterpriseViewOLSCheckEnabled",
    returnType = "Z",
    parameters = emptyList(),
)

internal val hasFamilyPlanFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/LicenseInfo;",
    name = "HasFamilyPlan",
    returnType = "Z",
    parameters = emptyList(),
)

internal val hasPersonalPlanFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/LicenseInfo;",
    name = "HasPersonalPlan",
    returnType = "Z",
    parameters = emptyList(),
)

internal val hasPremiumPlanFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/LicenseInfo;",
    name = "HasPremiumPlan",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * OHubUtil.GetLicensingState() — returns the LicensingState enum controlling all
 * subscription UI. ConsumerPremium suppresses all upsell/buy surfaces.
 * Stable: non-obfuscated public static API.
 */
internal val getLicensingStateFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/officehub/util/OHubUtil;",
    name = "GetLicensingState",
    returnType = "Lcom/microsoft/office/licensing/LicensingState;",
    parameters = emptyList(),
)

/**
 * SubscriptionData.isTrial() — paywallsdk trial flag.
 * Stable: non-obfuscated public API in paywallsdk.
 */
internal val subscriptionDataIsTrialFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/mobile/paywallsdk/publics/SubscriptionData;",
    name = "isTrial",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * LicenseStatus.isPremium() — non-obfuscated enum method in growth/upsellplugin.
 * Excel retains the non-obfuscated class name (unlike Word which uses 'j').
 */
internal val licenseStatusIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/growth/upsellplugin/models/LicenseStatus;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
)

// ── Obfuscated fingerprints — pinned by return type + params ──────────────────

/**
 * licensing.e.d() — returns LicensingState from the native OLS session.
 * Returning ConsumerPremium prevents OLS_E_ENTITLEMENT_NOT_FOUND from
 * downgrading the local license state after a server check.
 */
internal val licenseSessionStateFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/e;",
    name = "d",
    returnType = "Lcom/microsoft/office/licensing/LicensingState;",
    parameters = emptyList(),
)

/**
 * licensing.f.h(String, UserAccountType, String, Z) → LicenseInfo
 * Native licensing lookup. Returning an empty (non-null) LicenseInfo ensures
 * Has*Plan methods are reached and return true.
 * Method renamed g→h in 16.0.20228; params and return type unchanged (used as pin).
 */
internal val licensingFGFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/f;",
    name = "h",
    returnType = "Lcom/microsoft/office/licensing/LicenseInfo;",
    parameters = listOf(
        "Ljava/lang/String;",
        "Lcom/microsoft/office/licensing/UserAccountType;",
        "Ljava/lang/String;",
        "Z",
    ),
)

/**
 * a1$a.y(Context) — subscription status check called at boot via postAppActivate.
 * Returning true prevents PaywallActivity launch at startup.
 */
internal val subscriptionStatusYFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/inapppurchase/a1\$a;",
    name = "y",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

/**
 * AccountProfileInfo.B() — boolean hasProfile field getter.
 * Returning true makes MeControl render as signed-in without a real account.
 */
internal val accountProfileInfoHasProfileFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/common/AccountProfileInfo;",
    name = "B",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * unifiedStorageQuota.f.b(Identity) — storage quota UI check.
 * NPE guard when identity is null. Returning false skips quota display.
 */
internal val storageQuotaCheckFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/controls/unifiedStorageQuota/f;",
    name = "b",
    returnType = "Z",
    parameters = listOf("Lcom/microsoft/office/identity/Identity;"),
)

/**
 * b$n.run() — account-switcher dialog runner.
 * Pinned by "layout_inflater" string in body. NPE guard when GetActiveIdentity()=null.
 */
internal val accountSwitcherRunnableFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/controls/b\$n;",
    name = "run",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("layout_inflater"),
)
