package app.template.patches.spotangels

import app.morphe.patcher.Fingerprint

val EntitlementIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
    name = "isActive",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetAvailabilityFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getAvailability",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetSatelliteFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getSatellite",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetSpotRegulationsFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getSpotRegulations",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetMapFiltersFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getMapFilters",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetMultipleCarsFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getMultipleCars",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetCarSharingFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getCarSharing",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetDarkModeFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getDarkMode",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetCalendarFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getCalendar",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetCalendarSyncFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getCalendarSync",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetCalendarRecommendationsFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getCalendarRecommendations",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetOpenSpotAlertsFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getOpenSpotAlerts",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetAutomatedOpenSpotAlertsFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getAutomatedOpenSpotAlerts",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetParkingRemindersFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getParkingReminders",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetCustomNotificationsFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getCustomNotifications",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetSmsNotificationsFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getSmsNotifications",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetEmailNotificationsFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getEmailNotifications",
    returnType = "Z",
    parameters = emptyList(),
)

val EntitlementGetFreeSubscriptionFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Entitlement;",
    name = "getFreeSubscription",
    returnType = "Z",
    parameters = emptyList(),
)

val SubscriptionGetFallbackFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/util/PurchasesUtils\$Subscription\$Companion;",
    name = "getFALLBACK",
    returnType = "Lcom/spotangels/android/util/PurchasesUtils\$Subscription;",
    parameters = emptyList(),
    strings = listOf("entitlement "),
)

// MapFragment.b() — gb6 callback fired by MapboxHelper after style loads.
// Crashes when fragment is detached (view not yet created). Guard with isAdded().
val MapFragmentStyleLoadedCallbackFingerprint = Fingerprint(
    definingClass = "Lcom/spotangels/android/ui/MapFragment;",
    name = "b",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("mapboxHelper"),
)
