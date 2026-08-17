package app.template.patches.flightradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ════════════════════════════════════════════════════════════════════════════════
// Flightradar24 Fingerprints — verified against 11.9.0 (versionCode 110900000)
//
// Stability guide:
//   STABLE    — uses only non-obfuscated SDK/app class names → survives R8 reruns
//   OBFUSCATED — references a name that changes on every R8 run; review on update
//
// Update history:
//   11.8.0 → 11.9.0:
//     - BillingPurchasesProviderIsValidFingerprint REMOVED. "Lip1;" was recycled
//       by R8 into an unrelated synthetic lambda (R8$$SyntheticClass) — the
//       b(WorkSpec)Z method no longer exists on it. The WorkManager billing
//       constraint gate was removed from the app in this version.
//     - Tier mapper class: e0f/ecf → b7f (classes2). Enum type: Lo0f$a; → Lp5f$a;
//       Business enum field: h = Business in BOTH versions — no smali change needed.
//     - Tier enum analytics class: now Ldwd; (fields: b=Free, c=Silver, d=Gold, e=Business)
//     - User class: oye → p5f. Tier methods: isGold=p()Z, isBusiness=n()Z,
//       isSilver=r()Z, isBasic=m()Z. All string-based fingerprints are STABLE.
// ════════════════════════════════════════════════════════════════════════════════

// ─── Subscription tier checks (User class) ───────────────────────────────────
//
// [OBFUSCATED class, STABLE anchors]
// The User class (p5f in 11.9.0, oye in 11.8.0) exposes one no-arg PUBLIC FINAL
// instance method per tier that compares the tier-string field against a literal:
//
//   const-string v0, "Gold"
//   invoke-virtual {v1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
//   return v0
//
// Fingerprint anchors: strings=[TierName] + returnType=Z + PUBLIC FINAL + no params.
// Unique across all DEX files in 11.9.0 — no collision with enum <clinit> or JSON
// adapters (those are STATIC or have parameters).

// p()Z — tier string == "Gold"
val UserIsGoldFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("Gold"),
)

// n()Z — tier string == "Business"
val UserIsBusinessFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("Business"),
)

// r()Z — tier string == "Silver" (patched false → Gold check takes precedence)
val UserIsSilverFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("Silver"),
)

// m()Z — tier string == "Basic" (patched false — Basic = unauthenticated/free)
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
// b7f (classes2, 11.9.0) hosts two static mappers:
//   b(UserData)String  — returns the tier name string
//   a(UserData)Lp5f$a; — maps the string to the tier enum
//
// Update note: The tier enum type changed Lo0f$a; → Lp5f$a; in 11.9.0, but the
// Business field is still named "h" in both. EcfGetSubscriptionTierEnumFingerprint
// reads the returnType at patch-time and uses it directly — no smali edit needed.

// b7f.b(UserData)String
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

// b7f.a(UserData)Lp5f$a;
// [STABLE] Only one PUBLIC STATIC FINAL method takes UserData and returns a type ending in "$a;"
// The returnType is read at patch time and used directly in the sget-object instruction.
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
