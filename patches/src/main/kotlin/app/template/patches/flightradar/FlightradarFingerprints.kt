package app.template.patches.flightradar

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val BillingPurchasesProviderIsValidFingerprint = Fingerprint(
    definingClass = "Lep1;",
    name = "b",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserIsGoldFingerprint = Fingerprint(
    definingClass = "Lzse;",
    name = "r",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserIsBasicFingerprint = Fingerprint(
    definingClass = "Lzse;",
    name = "q",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserIsAdvertsEnabledFingerprint = Fingerprint(
    definingClass = "Lzse;",
    name = "a",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val EcfGetSubscriptionTierFingerprint = Fingerprint(
    definingClass = "Llue;",
    name = "b",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Lcom/flightradar24free/models/account/UserData;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

val EcfGetSubscriptionTierEnumFingerprint = Fingerprint(
    definingClass = "Llue;",
    name = "a",
    returnType = "Lzse\$a;",
    parameters = listOf("Lcom/flightradar24free/models/account/UserData;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

val FR24ApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/FR24Application;",
    name = "onCreate",
    returnType = "V"
)

// UserFeatures methods

val UserFeaturesIsAdvertsEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAdvertsEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsAirportFlightHistoryEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportFlightHistoryEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsAirportPanelLatestEventsEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportPanelLatestEventsEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsAirportPanelMovementsPerDayEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportPanelMovementsPerDayEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsAirportPanelRunwayDetailsEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportPanelRunwayDetailsEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsAirportPanelRunwayUsageEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isAirportPanelRunwayUsageEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsFiltersCategoriesEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isFiltersCategoriesEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsFull3dEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isFull3dEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerAtcEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerAtcEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerNavdataEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerNavdataEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerTracksOceanicEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerTracksOceanicEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherAirmetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherAirmetEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherAustralianRadarEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherAustralianRadarEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherClearAirTurbulenceEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherClearAirTurbulenceEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherHighLevelEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherHighLevelEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherIcingEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherIcingEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherInCloudTurbulenceEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherInCloudTurbulenceEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherLightningEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherLightningEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherNorthAmericanRadarEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherNorthAmericanRadarEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherRadarEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherRadarEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherSatelliteEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherSatelliteEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherVolcanoEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherVolcanoEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesIsMapLayerWeatherWindEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "isMapLayerWeatherWindEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesGetMapFiltersMaxFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getMapFiltersMax",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesGetUserBookmarksMaxFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getUserBookmarksMax",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesGetHistoryPlaybackDaysFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getHistoryPlaybackDays",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesGetAirportFlightHistoryFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getAirportFlightHistory",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserFeaturesConstructorFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "<init>",
    returnType = "V"
)

// getMapInfoAircraft returns "full" or "limited" - gates squawk display
val UserFeaturesGetMapInfoAircraftFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserFeatures;",
    name = "getMapInfoAircraft",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// Loaf.e() is the central UserFeatures provider
val LoafGetUserFeaturesFingerprint = Fingerprint(
    definingClass = "Lzse;",
    name = "e",
    returnType = "Lcom/flightradar24free/models/account/UserFeatures;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// Loaf.u() = isSilver
val UserIsSilverFingerprint = Fingerprint(
    definingClass = "Lzse;",
    name = "u",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// Loaf.b() = hasAlerts
val UserHasAlertsFingerprint = Fingerprint(
    definingClass = "Lzse;",
    name = "b",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val UserDataGetUserDataFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/account/UserData;",
    name = "getUserData",
    returnType = "Lcom/flightradar24free/models/account/UserSessionData;"
)

val UserIsBusinessFingerprint = Fingerprint(
    definingClass = "Lzse;",
    name = "o",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ClickhandlerEmsFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;",
    name = "getEms",
    returnType = "Lcom/flightradar24free/models/entity/EmsData;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ClickhandlerSquawkFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;",
    name = "getSquawkAvailability",
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ClickhandlerAirspaceFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;",
    name = "getAirspaceAvailability",
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ClickhandlerVspeedFingerprint = Fingerprint(
    definingClass = "Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;",
    name = "getVspeedAvailability",
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

