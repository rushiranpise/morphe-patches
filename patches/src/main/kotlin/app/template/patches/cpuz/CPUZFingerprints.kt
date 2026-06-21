package app.template.patches.cpuz

import app.morphe.patcher.Fingerprint

// Purchase update callback in q23 — sets N=true (premium) when remove_ads is purchased.
// Stub to prevent billing from ever verifying/denying purchase.
val CPUZPurchaseCallbackFingerprint = Fingerprint(
    definingClass = "Lq23;",
    name = "w",
    returnType = "V",
    parameters = listOf("Lxa;", "Ljava/util/List;"),
)

// Stubs MobileAdsInitProvider.attachInfo to prevent ad SDK auto-init via ContentProvider.
val CPUZMobileAdsInitProviderAttachInfoFingerprint = Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAdsInitProvider;",
    name = "attachInfo",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Landroid/content/pm/ProviderInfo;"),
)
