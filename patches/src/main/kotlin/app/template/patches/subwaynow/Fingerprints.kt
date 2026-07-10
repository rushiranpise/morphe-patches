package app.template.patches.subwaynow

import app.morphe.patcher.Fingerprint

internal val PurchaseManagerConstructorFingerprint = Fingerprint(
    definingClass = "Lp5/i;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;"),
)

internal val LicenseProviderOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseContentProvider;",
    name = "onCreate",
    returnType = "Z",
    parameters = emptyList(),
)

internal val PurchaseQueryCallbackFingerprint = Fingerprint(
    definingClass = "Lp5/f;",
    name = "b",
    returnType = "V",
    parameters = listOf("Lf2/c;", "Ljava/util/List;"),
)

internal val PurchaseUpdateCallbackFingerprint = Fingerprint(
    definingClass = "Lp5/g;",
    name = "b",
    returnType = "V",
    parameters = listOf("Lf2/c;", "Ljava/util/List;"),
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
