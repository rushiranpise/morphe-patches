package app.template.patches.life360

import app.morphe.patcher.Fingerprint

internal val CircleFeaturesIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/life360/model_store/base/localstore/CircleFeatures;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
)

internal val FeatureSetIsAvailableFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/FeatureSetEntitlementManager;",
    name = "isAvailable",
    returnType = "Z",
    parameters = listOf(
        "Lcom/life360/android/core/models/FeatureKey;",
        "Lcom/life360/inapppurchase/Premium;",
    ),
)

internal val FeatureSetIsEnabledForAnyCircleFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/FeatureSetEntitlementManager;",
    name = "isEnabledForAnyCircle",
    returnType = "Z",
    parameters = listOf(
        "Lcom/life360/android/core/models/FeatureKey;",
        "Lcom/life360/inapppurchase/Premium;",
    ),
)

internal val FeatureSetIsEnabledForCircleFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/FeatureSetEntitlementManager;",
    name = "isEnabledForCircle",
    returnType = "Z",
    parameters = listOf(
        "Lcom/life360/android/core/models/FeatureKey;",
        "Ljava/lang/String;",
        "Lcom/life360/inapppurchase/Premium;",
    ),
)

internal val DefaultActiveMappedSkuOrFreeFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/DefaultMembershipUtil;",
    name = "getActiveMappedSkuOrFree\$lambda\$0",
    returnType = "Lcom/life360/android/core/models/Sku;",
    parameters = listOf("Ljava/util/Optional;"),
)

internal val DefaultActiveSkuOrFreeFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/DefaultMembershipUtil;",
    name = "getActiveSkuOrFree\$lambda\$0",
    returnType = "Lcom/life360/android/core/models/Sku;",
    parameters = listOf("Ljava/util/Optional;"),
)

internal val DefaultIsActiveCircleFreeFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/DefaultMembershipUtil;",
    name = "isActiveCircleFree\$lambda\$0",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("Ljava/util/Optional;"),
)

internal val PremiumCircleActiveMappedSkuOrFreeFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/PremiumCircleUtilImpl;",
    name = "getActiveMappedSkuOrFree\$lambda\$0",
    returnType = "Lcom/life360/android/core/models/Sku;",
    parameters = listOf("Ljava/util/Optional;"),
)

internal val PurchasedSkuInfoGetSkuFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/PurchasedSkuInfo;",
    name = "getSku",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

internal val PurchasedSkuInfoGetProductIdFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/PurchasedSkuInfo;",
    name = "getProductId",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

internal val PurchasedSkuInfoGetPeriodFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/PurchasedSkuInfo;",
    name = "getPeriod",
    returnType = "Lcom/life360/model_store/base/localstore/room/premium/PurchasePeriod;",
    parameters = emptyList(),
)

internal val PurchasedSkuInfoGetPaymentStateFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/PurchasedSkuInfo;",
    name = "getPaymentState",
    returnType = "Lcom/life360/inapppurchase/PaymentState;",
    parameters = emptyList(),
)

internal val PurchasedSkuInfoEntityGetProductIdFingerprint = Fingerprint(
    definingClass = "Lcom/life360/model_store/base/localstore/premium/PurchasedSkuInfoEntity;",
    name = "getProductId",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

internal val PremiumSkuInfoForCircleFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/Premium;",
    name = "skuInfoForCircle",
    returnType = "Lcom/life360/inapppurchase/PurchasedSkuInfo;",
    parameters = listOf("Ljava/lang/String;"),
)

internal val PremiumSkuForCircleFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/Premium;",
    name = "skuForCircle",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;"),
)

internal val PremiumPaymentStateForCircleFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/Premium;",
    name = "paymentStateForCircle",
    returnType = "Lcom/life360/inapppurchase/PaymentState;",
    parameters = listOf("Ljava/lang/String;"),
)

internal val PremiumAvailableProductsForSkuFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/Premium;",
    name = "availableProductsForSku",
    returnType = "Lcom/life360/inapppurchase/AvailableProductIds;",
    parameters = listOf("Ljava/lang/String;"),
)

internal val PremiumGetSkuForProductIdFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/Premium;",
    name = "getSkuForProductId",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;"),
)

internal val PremiumPricesForSkuFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/Premium;",
    name = "pricesForSku",
    returnType = "Lcom/life360/inapppurchase/Prices;",
    parameters = listOf("Ljava/lang/String;"),
)

internal val PremiumTrialForSkuFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/Premium;",
    name = "trialForSku",
    returnType = "Ljava/lang/Integer;",
    parameters = listOf("Ljava/lang/String;"),
)

internal val PremiumGetAvailableSkusFingerprint = Fingerprint(
    definingClass = "Lcom/life360/inapppurchase/Premium;",
    name = "getAvailableSkus\$inapppurchase_release",
    returnType = "Ljava/util/Set;",
    parameters = emptyList(),
)
