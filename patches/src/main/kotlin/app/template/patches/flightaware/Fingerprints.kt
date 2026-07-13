package app.template.patches.flightaware

import app.morphe.patcher.Fingerprint

internal val BillingInitFingerprint = Fingerprint(
    definingClass = "Lcom/flightaware/android/liveFlightTracker/billing/MyBillingClient;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("pref_ad_free_state"),
)

internal val ProcessPurchasesFingerprint = Fingerprint(
    definingClass = "Lcom/flightaware/android/liveFlightTracker/billing/MyBillingClient;",
    name = "processPurchases",
    returnType = "V",
    parameters = listOf("Ljava/util/Set;"),
)

internal val SettingsPreferenceClickFingerprint = Fingerprint(
    definingClass = "Lcom/flightaware/android/liveFlightTracker/fragments/SettingsFragment;",
    name = "onPreferenceClick",
    returnType = "Z",
    parameters = listOf("Landroidx/preference/Preference;"),
    strings = listOf("pref_manage_subscription"),
)
