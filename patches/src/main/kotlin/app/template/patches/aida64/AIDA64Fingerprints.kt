package app.template.patches.aida64

import app.morphe.patcher.Fingerprint

// loadBannerAd — private, stubs banner ad loading called from showHideAds()
val AIDA64LoadBannerAdFingerprint = Fingerprint(
    definingClass = "Lcom/finalwire/aida64/HHMainActivity;",
    name = "loadBannerAd",
    returnType = "V",
)

// loadInterstitialAd — private, stubs interstitial ad loading called from showHideAds()
val AIDA64LoadInterstitialAdFingerprint = Fingerprint(
    definingClass = "Lcom/finalwire/aida64/HHMainActivity;",
    name = "loadInterstitialAd",
    returnType = "V",
)

// onPurchasesUpdated — PurchasesUpdatedListener callback, stub to prevent async billing resets
val AIDA64OnPurchasesUpdatedFingerprint = Fingerprint(
    definingClass = "Lcom/finalwire/aida64/HHMainActivity;",
    name = "onPurchasesUpdated",
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/BillingResult;", "Ljava/util/List;"),
)

// onQueryPurchasesResponse — sets adMode=true when purchases.size==0 (no purchase found).
// Stubbing prevents adMode from ever being set to true via the billing query path.
val AIDA64OnQueryPurchasesResponseFingerprint = Fingerprint(
    definingClass = "Lcom/finalwire/aida64/HHMainActivity\$GetIAPSKUsThread_New\$1\$1;",
    name = "onQueryPurchasesResponse",
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/BillingResult;", "Ljava/util/List;"),
)

// MobileAdsInitProvider.attachInfo — prevents ad SDK auto-init via ContentProvider
val AIDA64MobileAdsInitProviderFingerprint = Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAdsInitProvider;",
    name = "attachInfo",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Landroid/content/pm/ProviderInfo;"),
)
