package app.template.patches.word.premium

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
 * OHubUtil.GetLicensingState() — returns the LicensingState enum used throughout the
 * UI to determine subscription display. ConsumerPremium hides all upsell/buy UI.
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
 * Returning false removes the trial badge from paywall UI.
 * Stable: non-obfuscated public API in paywallsdk.
 */
internal val subscriptionDataIsTrialFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/mobile/paywallsdk/publics/SubscriptionData;",
    name = "isTrial",
    returnType = "Z",
    parameters = emptyList(),
)

// ── Obfuscated fingerprints — pinned by return type + params (stable across renames) ──

/**
 * licensing.e.d() — returns LicensingState from the native OLS session.
 * Called after server licensing check; overwrites local GetLicensingState with the
 * server result. Returning ConsumerPremium prevents OLS_E_ENTITLEMENT_NOT_FOUND
 * from downgrading the local state.
 * Note: definingClass 'e' and method name 'd' are obfuscated but pinned by unique
 * return type + empty params within the licensing package.
 */
internal val licenseSessionStateFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/e;",
    name = "d",
    returnType = "Lcom/microsoft/office/licensing/LicensingState;",
    parameters = emptyList(),
)

/**
 * licensing.f.h(String, UserAccountType, String, Z) → LicenseInfo
 * Native licensing lookup via NativeProxy.Glifu. Returns null when the server has
 * no entitlement, causing paywall to show despite HasFamilyPlan patches.
 * Returning an empty LicenseInfo object ensures non-null, so Has*Plan patches apply.
 * Renamed g→h in 16.0.20228 — params and return type are unchanged and used as pin.
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
 * growth/upsellplugin/models/j.isPremium() — enum method, returns true only for
 * MANAGED_PREMIUM/UNMANAGED_PREMIUM instances.
 * Class name 'j' is obfuscated; pinned by definingClass + name + unique return type.
 */
internal val licenseStatusIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/growth/upsellplugin/models/j;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * a1$a.y(Context) — subscription status check called at boot via postAppActivate.
 * Returns true if user has active subscription (skips paywall), false otherwise.
 * Patching to always return true prevents PaywallActivity launch at startup.
 */
internal val subscriptionStatusYFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/inapppurchase/a1\$a;",
    name = "y",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

/**
 * AccountProfileInfo.B() — returns boolean hasProfile field.
 * False = doughboy/sign-in avatar shown in MeControl.
 * Returning true makes the header render as if a real account is present.
 */
internal val accountProfileInfoHasProfileFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/common/AccountProfileInfo;",
    name = "B",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * unifiedStorageQuota.f.b(Identity) — checks if the storage quota UI should show.
 * Crashes with NPE when identity is null (no real account but hasProfile=true).
 * Returning false safely skips quota display.
 */
internal val storageQuotaCheckFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/controls/unifiedStorageQuota/f;",
    name = "b",
    returnType = "Z",
    parameters = listOf("Lcom/microsoft/office/identity/Identity;"),
)

/**
 * b$n.run() — account-switcher dialog runner. Crashes when GetActiveIdentity()=null.
 * Pinned by the stable "layout_inflater" string in the body. Returning void safely
 * skips the dialog when no real account exists.
 */
internal val accountSwitcherRunnableFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/controls/b\$n;",
    name = "run",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("layout_inflater"),
)
