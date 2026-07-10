package app.template.patches.amoledpix

import app.morphe.patcher.Fingerprint

internal val AdGatekeeperIsPremiumUserFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/AdGatekeeper;",
    name = "isPremiumUser",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

internal val AdGatekeeperShouldShowBannerFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/AdGatekeeper;",
    name = "shouldShowBanner",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

internal val AdGatekeeperShouldShowInterstitialFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/AdGatekeeper;",
    name = "shouldShowInterstitial",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

internal val AdGatekeeperShouldShowNativeFeedFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/AdGatekeeper;",
    name = "shouldShowNativeFeed",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

internal val AdGatekeeperShouldShowPagerNativeFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/AdGatekeeper;",
    name = "shouldShowPagerNative",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

internal val AdGatekeeperShouldShowRewardedFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/AdGatekeeper;",
    name = "shouldShowRewarded",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

internal val PrefManagerIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/PrefManager;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
)

internal val PrefManagerIsPremiumLinkedFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/PrefManager;",
    name = "isPremiumLinked",
    returnType = "Z",
    parameters = emptyList(),
)

internal val PrefManagerIsPremiumMigrationPendingFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/PrefManager;",
    name = "isPremiumMigrationPending",
    returnType = "Z",
    parameters = emptyList(),
)

internal val PrefManagerIsPremiumRestoreAvailableFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/PrefManager;",
    name = "isPremiumRestoreAvailable",
    returnType = "Z",
    parameters = emptyList(),
)

internal val PrefManagerShouldPreserveLegacyPremiumAccessFingerprint = Fingerprint(
    definingClass = "Lcom/androholic/amoledpix/manager/PrefManager;",
    name = "shouldPreserveLegacyPremiumAccess",
    returnType = "Z",
    parameters = emptyList(),
)

internal val LicenseProviderOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseContentProvider;",
    name = "onCreate",
    returnType = "Z",
    parameters = emptyList(),
)

internal val InitializeLicenseCheckFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    parameters = emptyList(),
)

internal val CheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)
