package app.template.patches.flightradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ════════════════════════════════════════════════════════════════════════════════
// Flightradar24 Fingerprints — verified against 11.8.0 (versionCode 110807310)
//
// Stability guide:
//   STABLE    — uses only non-obfuscated SDK/app class names → survives R8 reruns
//   OBFUSCATED — references a name that changes on every R8 run; review on update
// ════════════════════════════════════════════════════════════════════════════════

// ─── Billing network-constraint gate ─────────────────────────────────────────
//
// [OBFUSCATED] ip1 = ContraintControllers.kt (WorkManager network constraint).
// b(WorkSpec)Z reads WorkSpec→constraints→requiresNetwork and returns it.
// Patched to always return true so the billing work-request proceeds offline.
//
// To find the new name after an update:
//   grep -r "ContraintControllers" smali/ --include="*.smali" -l
// Then pick the class whose c()I returns const/4 5 (NETWORK_TYPE_CONNECTED).
// DEX: classes
val BillingPurchasesProviderIsValidFingerprint = Fingerprint(
    definingClass = "Lip1;",
    name = "b",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("L"),
)

// ─── Subscription tier checks (User class) ───────────────────────────────────
//
// [OBFUSCATED class, STABLE anchors]
// The User class exposes one no-arg instance method per tier that:
//   1. iget-objects the UserData field directly (no getUserData() call)
//   2. invoke-statics the tier-string mapper with that UserData
//   3. const-strings the tier literal and calls String.equals()
//
// Fingerprint anchors: strings=[TierName] + returnType=Z + PUBLIC FINAL + no params.
// Confirmed unique across all DEX files in 11.8.0 (only collision candidates are
// enum <clinit>s and JSON adapters — none have PUBLIC FINAL ()Z signature).
//
// NOTE: earlier versions of this fingerprint used a methodCall(UserData.getUserData)
// filter. That was wrong — these methods never call getUserData(); they iget-object
// the field directly. The filter was removed; strings alone are unique.

// isGold — tier string == "Gold"
val UserIsGoldFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("Gold"),
)

// isBusiness — tier string == "Business"
val UserIsBusinessFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("Business"),
)

// isSilver — tier string == "Silver" (patched false → Gold check takes precedence)
val UserIsSilverFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("Silver"),
)

// isBasic — tier string == "Basic" (patched false — Basic = unauthenticated/free)
val UserIsBasicFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("Basic"),
)

// isAdvertsEnabled — gates ads via isLoggedIn check then UserFeatures.isAdvertsEnabled().
// [STABLE] anchor: the non-obfuscated isAdvertsEnabled call site on UserFeatures.
val UserIsAdvertsEnabledFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
            name = "isAdvertsEnabled",
        ),
    ),
)

// hasAlerts — reads UserFeatures.userAlertsMax:I > 0.
// [STABLE] anchor: the non-obfuscated field access on UserFeatures.
val UserHasAlertsFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET,
            definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
            name = "userAlertsMax",
        ),
    ),
)

// ─── Subscription tier mapper ─────────────────────────────────────────────────
//
// [OBFUSCATED class, STABLE anchors]
// e2f (classes2) hosts two static mappers:
//   b(UserData)String  — returns the tier name string
//   a(UserData)Loye$a; — maps the string to the tier enum
// Both are patched to hard-return "Business" / the Business enum constant.

// e2f.b(UserData)String
// [STABLE] anchors: parameters + methodCall(UserDataSubscriptionsItem.getName) + strings=["Basic"]
val EcfGetSubscriptionTierFingerprint = Fingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/flightradar24free/models/account/UserData;"),
    strings = listOf("Basic"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/flightradar24free/models/account/UserDataSubscriptionsItem;",
            name = "getName",
        ),
    ),
)

// e2f.a(UserData)Loye$a; / Lo0f$a;
// [STABLE] Only one PUBLIC STATIC FINAL method takes UserData and returns a type ending in "$a;"
val EcfGetSubscriptionTierEnumFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/flightradar24free/models/account/UserData;"),
    custom = { method, _ -> method.returnType.endsWith("\$a;") },
)

// ─── Application bootstrap ────────────────────────────────────────────────────

// [STABLE] Non-obfuscated class and method name.
val FR24ApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/FR24Application;",
    name = "onCreate",
    returnType = "V",
)

// ─── UserFeatures — all stable (non-obfuscated class) ────────────────────────

val UserFeaturesIsAdvertsEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAdvertsEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsAirportFlightHistoryEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportFlightHistoryEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsAirportPanelLatestEventsEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportPanelLatestEventsEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsAirportPanelMovementsPerDayEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportPanelMovementsPerDayEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsAirportPanelRunwayDetailsEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportPanelRunwayDetailsEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsAirportPanelRunwayUsageEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportPanelRunwayUsageEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsFiltersCategoriesEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isFiltersCategoriesEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsFull3dEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isFull3dEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerAtcEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerAtcEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerNavdataEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerNavdataEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerTracksOceanicEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerTracksOceanicEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherAirmetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherAirmetEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherAustralianRadarEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherAustralianRadarEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherClearAirTurbulenceEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherClearAirTurbulenceEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherHighLevelEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherHighLevelEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherIcingEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherIcingEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherInCloudTurbulenceEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherInCloudTurbulenceEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherLightningEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherLightningEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherNorthAmericanRadarEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherNorthAmericanRadarEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherRadarEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherRadarEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherSatelliteEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherSatelliteEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherVolcanoEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherVolcanoEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesIsMapLayerWeatherWindEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherWindEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesGetMapFiltersMaxFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getMapFiltersMax",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesGetUserBookmarksMaxFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getUserBookmarksMax",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesGetHistoryPlaybackDaysFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getHistoryPlaybackDays",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val UserFeaturesGetAirportFlightHistoryFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getAirportFlightHistory",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// getMapInfoAircraft()String — "full" enables squawk/FIR panel; "limited" hides them.
val UserFeaturesGetMapInfoAircraftFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getMapInfoAircraft",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// No-arg default constructor — delegates to the full constructor with all fields zeroed.
// parameters = listOf() ensures we match ONLY the zero-param variant.
val UserFeaturesConstructorFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "<init>",
    returnType = "V",
    parameters = listOf(),
)

// ─── UserData / session ────────────────────────────────────────────────────────

val UserDataGetUserDataFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserData;",
    name = "getUserData",
    returnType = "Lcom/flightradar24free/models/account/UserSessionData;",
)

// ─── ClickhandlerExtendedFlightInfo — all stable ──────────────────────────────

val ClickhandlerEmsFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;",
    name = "getEms",
    returnType = "Lcom/flightradar24free/models/entity/EmsData;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val ClickhandlerSquawkFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;",
    name = "getSquawkAvailability",
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val ClickhandlerAirspaceFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;",
    name = "getAirspaceAvailability",
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

val ClickhandlerVspeedFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;",
    name = "getVspeedAvailability",
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
