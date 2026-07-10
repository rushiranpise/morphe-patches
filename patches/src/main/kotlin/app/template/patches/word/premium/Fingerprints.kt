package app.template.patches.word.premium

import app.morphe.patcher.Fingerprint

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
// obfuscated as j in Word (same as PowerPoint)
internal val licenseStatusIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/growth/upsellplugin/models/j;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
)

internal val subscriptionDataIsTrialFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/mobile/paywallsdk/publics/SubscriptionData;",
    name = "isTrial",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * licensing.f.g() — native licensing lookup via NativeProxy.Glifu.
 * Returns null when server has no entitlement, causing paywall to show despite
 * HasFamilyPlan/HasPersonalPlan patches. Returning empty LicenseInfo ensures
 * non-null, allowing the Has*Plan patches to return true.
 */
internal val licensingFGFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/f;",
    name = "g",
    returnType = "Lcom/microsoft/office/licensing/LicenseInfo;",
    parameters = listOf(
        "Ljava/lang/String;",
        "Lcom/microsoft/office/licensing/UserAccountType;",
        "Ljava/lang/String;",
        "Z",
    ),
)

internal val subscriptionStatusYFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/inapppurchase/a1\$a;",
    name = "y",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

/**
 * o7.b() — static method that checks if the current user is eligible for
 * subscription paywall (via p7$a.O() field d). Returns true → PaywallActivity launches.
 * Returning false blocks all LaunchSubscriptionPurchaseFlow paywall paths.
 */
internal val subscriptionPaywallGateFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/common/n7;",
    name = "b",
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
 * OHubUtil.GetLicensingState() — returns the LicensingState enum used throughout
 * the UI to determine subscription display. ConsumerPremium hides all "Buy" buttons,
 * shows subscribed status in Backstage, and suppresses all upsell UI.
 */
internal val getLicensingStateFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/officehub/util/OHubUtil;",
    name = "GetLicensingState",
    returnType = "Lcom/microsoft/office/licensing/LicensingState;",
    parameters = emptyList(),
)

/**
 * licensing.e.d() — returns LicensingState from the native OLS session (NativeProxy.Gs).
 * Called after server licensing check; overwrites local GetLicensingState with server result.
 * Returning ConsumerPremium prevents OLS_E_ENTITLEMENT_NOT_FOUND from downgrading local state.
 */
internal val licenseSessionStateFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/licensing/e;",
    name = "d",
    returnType = "Lcom/microsoft/office/licensing/LicensingState;",
    parameters = emptyList(),
)

/**
 * AccountProfileInfo.B() — returns field A (boolean hasProfile). False = doughboy/sign-in avatar.
 * Returning true makes MeControl show as signed-in even without a real account.
 */
internal val accountProfileInfoHasProfileFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/common/AccountProfileInfo;",
    name = "B",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * unifiedStorageQuota.f.b(Identity) — checks if storage quota UI should show for this identity.
 * Crashes with NPE when identity is null (no real account but AccountProfileInfo.B()=true).
 * Returning false safely skips quota display.
 */
internal val storageQuotaCheckFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/controls/unifiedStorageQuota/f;",
    name = "b",
    returnType = "Z",
    parameters = listOf("Lcom/microsoft/office/identity/Identity;"),
)

/**
 * b$n.run() — account switcher dialog runner. Crashes when GetActiveIdentity()=null.
 * Returning void safely skips account dialog when no real account exists.
 */
internal val accountSwitcherRunnableFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/controls/b\$n;",
    name = "run",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("layout_inflater"),
)
