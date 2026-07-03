package app.template.patches.skinsort

import app.morphe.patcher.Fingerprint

object SkinSortEntitlementInfosGetActiveFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
    name = "getActive",
    returnType = "Ljava/util/Map;"
)

object SkinSortEntitlementInfoIsActiveFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
    name = "isActive",
    returnType = "Z"
)

object SkinSortCustomerInfoGetActiveSubscriptionsFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
    name = "getActiveSubscriptions",
    returnType = "Ljava/util/Set;"
)

object SkinSortCustomerInfoGetAllPurchasedProductIdsFingerprint : Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
    name = "getAllPurchasedProductIds",
    returnType = "Ljava/util/Set;"
)

object SkinSortPremiumStatusReplyFingerprint : Fingerprint(
    definingClass = "Lod/k0;",
    name = "run",
    returnType = "V",
    strings = listOf("Premium status: ", "check_premium")
)

object SkinSortPremiumStatusBroadcastFingerprint : Fingerprint(
    definingClass = "Lsd/l0;",
    name = "onReceived",
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;", "Z"),
    returnType = "V",
    strings = listOf("com.skinsort.PREMIUM_STATUS_DID_CHANGE", "is_premium")
)

object SkinSortPaywallGateCallbackFingerprint : Fingerprint(
    definingClass = "Lnd/k;",
    name = "invoke",
    parameters = listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
    returnType = "Ljava/lang/Object;",
    strings = listOf("User is already premium, blocking paywall", "Fetching offerings from RevenueCat")
)

object SkinSortPairIpApplicationAttachFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/application/Application;",
    name = "attachBaseContext",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "V"
)

object SkinSortPairIpLicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "V"
)

object SkinSortPairIpLicenseContentProviderOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseContentProvider;",
    name = "onCreate",
    returnType = "Z"
)
