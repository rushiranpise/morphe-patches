package app.template.patches.toxly

import app.morphe.patcher.Fingerprint

val BillingRepositoryConstructorFingerprint = Fingerprint(
    definingClass = "Lg60;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Lct1;"),
    strings = listOf("Please provide a valid listener for purchases updates."),
)

val PairIpCheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

val BillingRepositoryRefreshPurchasesFingerprint = Fingerprint(
    definingClass = "Lg60;",
    name = "d",
    returnType = "V",
    parameters = listOf("Ljava/util/List;"),
    strings = listOf("toxly_premium_sub", "purchaseState", "acknowledged"),
)
