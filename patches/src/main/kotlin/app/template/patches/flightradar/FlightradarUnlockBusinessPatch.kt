package app.template.patches.flightradar

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FLIGHTRADAR_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.ensureRegisters

// ════════════════════════════════════════════════════════════════════════════════
// Strategy
// ════════════════════════════════════════════════════════════════════════════════
// Flightradar24 enforces subscription state through three independent layers:
//
//   Layer 1 — User object (p5f in 11.9.0, oye in 11.8.0): boolean methods like
//             isGold(), isBusiness(), isAdvertsEnabled() that gate in-memory state
//             shown in the UI.
//
//   Layer 2 — Subscription tier mapper (b7f in 11.9.0, e0f/ecf in prior versions):
//             static functions that derive the tier string ("Gold"/"Business") and
//             enum from UserData. Called on every login/refresh. Patched to
//             hard-return "Business" and the Business enum constant.
//
//   Layer 3 — UserFeatures (non-obfuscated): data class holding boolean/int/String
//             feature flags populated from the server response. All is*Enabled()
//             methods and numeric-limit getters are overridden at the call-site level.
//
// Additionally:
//   • FR24Application.onCreate() receives an init() call so the extension helper
//     can install its signature-spoof hook before the app registers anything.
//   • UserFeatures.<init>()V receives a hook to populate the extension's temp
//     reference, used by mockUserFeaturesFromTemp() to set premium field values
//     after every constructor call (including server-response deserialization).
//   • ClickhandlerExtendedFlightInfo getters for EMS/squawk/airspace/vspeed are
//     redirected through FlightradarHelper.getAvailability() which grants access
//     even when the server marks individual fields unavailable.
//
// Removed in 11.9.0:
//   • BillingPurchasesProviderIsValidFingerprint — The WorkManager billing
//     constraint gate (ContraintControllers.kt) was removed from the app. The
//     class name "Lip1;" was recycled by R8 for an unrelated synthetic lambda.
// ════════════════════════════════════════════════════════════════════════════════

private const val HELPER = "Lapp/template/extension/extension/FlightradarHelper;"

@Suppress("unused")
val flightradarUnlockBusinessPatch = bytecodePatch(
    name = "Unlock Business Premium",
    description = "Unlocks Business-tier features in Flightradar24: ad-free experience, " +
        "weather layers, ATC routes, 3D view, flight history playback, and unlimited saved locations.",
    default = true,
) {
    compatibleWith(FLIGHTRADAR_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {

        // ── Layer 1: User-object tier checks ──────────────────────────────────
        // Gold and Business → true; Silver and Basic → false (Gold wins).
        // isAdvertsEnabled → false (disable ads for premium experience).
        // hasAlerts → true (alerts allowed).
        UserIsGoldFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        UserIsBusinessFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        UserIsSilverFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        UserIsBasicFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        UserHasAlertsFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        UserIsAdvertsEnabledFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ── Layer 2: Subscription tier mapper ────────────────────────────────
        // Force tier string to "Business" and tier enum to the Business constant.
        // Loye$a;->h is the Business enum field (d=UNKNOWN, e=Basic, f=Silver,
        // g=Gold, h=Business — verified in oye$a.<clinit> smali).
        EcfGetSubscriptionTierFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"Business\"\nreturn-object v0")
        }
        EcfGetSubscriptionTierEnumFingerprint.method.apply {
            clearBody()
            // Loye$a (11.7.0) → Lo0f$a (11.8.0); field 'h' = Business in both versions.
            val enumType = EcfGetSubscriptionTierEnumFingerprint.method.returnType
            addInstructions(0, "sget-object v0, $enumType->h:$enumType\nreturn-object v0")
        }

        // ── Layer 3a: UserFeatures boolean enablers ───────────────────────────
        listOf(
            UserFeaturesIsAirportFlightHistoryEnabledFingerprint,
            UserFeaturesIsAirportPanelLatestEventsEnabledFingerprint,
            UserFeaturesIsAirportPanelMovementsPerDayEnabledFingerprint,
            UserFeaturesIsAirportPanelRunwayDetailsEnabledFingerprint,
            UserFeaturesIsAirportPanelRunwayUsageEnabledFingerprint,
            UserFeaturesIsFiltersCategoriesEnabledFingerprint,
            UserFeaturesIsFull3dEnabledFingerprint,
            UserFeaturesIsMapLayerAtcEnabledFingerprint,
            UserFeaturesIsMapLayerNavdataEnabledFingerprint,
            UserFeaturesIsMapLayerTracksOceanicEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherAirmetEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherAustralianRadarEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherClearAirTurbulenceEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherHighLevelEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherIcingEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherInCloudTurbulenceEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherLightningEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherNorthAmericanRadarEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherRadarEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherSatelliteEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherVolcanoEnabledFingerprint,
            UserFeaturesIsMapLayerWeatherWindEnabledFingerprint,
        ).forEach { fp ->
            fp.method.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // Adverts disabled
        UserFeaturesIsAdvertsEnabledFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ── Layer 3b: UserFeatures string-typed feature gate ─────────────────
        // getMapInfoAircraft() — "full" enables squawk code + FIR airspace panel;
        // "limited" hides both. Hard-return "full".
        UserFeaturesGetMapInfoAircraftFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"full\"\nreturn-object v0")
        }

        // ── Layer 3c: UserFeatures numeric limits ─────────────────────────────
        // Premium values: 100 filters, 100 bookmarks, ~3 years of history.
        listOf(
            UserFeaturesGetMapFiltersMaxFingerprint         to 100,
            UserFeaturesGetUserBookmarksMaxFingerprint      to 100,
            UserFeaturesGetHistoryPlaybackDaysFingerprint   to 1095,
            UserFeaturesGetAirportFlightHistoryFingerprint  to 1095,
        ).forEach { (fp, value) ->
            fp.method.apply {
                clearBody()
                addInstructions(0, "const/16 v0, $value\nreturn v0")
            }
        }

        // ── Bootstrap: signature spoof init ──────────────────────────────────
        // Inject before any app code in FR24Application.onCreate() so the helper
        // can install its PackageManager intercept ahead of billing / signature checks.
        FR24ApplicationOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static {}, ${HELPER}->init()V",
        )

        // ── UserFeatures constructor hook ─────────────────────────────────────
        // Store `this` into a static temp slot then call mockUserFeaturesFromTemp().
        // This covers both app-start defaults and server-response deserialization
        // (both paths call the no-arg <init>()V).
        UserFeaturesConstructorFingerprint.method.apply {
            val lastIdx = instructions.indexOfLast {
                it.opcode.name.startsWith("return-void")
            }
            if (lastIdx >= 0) {
                addInstructions(
                    lastIdx,
                    """
                    sput-object p0, ${HELPER}->tempFeatures:Ljava/lang/Object;
                    invoke-static {}, ${HELPER}->mockUserFeaturesFromTemp()V
                    """.trimIndent(),
                )
            }
        }

        // ── UserData.getUserData() hook ───────────────────────────────────────
        // After returning the real UserSessionData, pass it through
        // mockUserSessionData() so the extension can patch login-state fields.
        UserDataGetUserDataFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                iget-object p0, p0, Lcom/flightradar24free/models/account/UserData;->userData:Lcom/flightradar24free/models/account/UserSessionData;
                invoke-static {p0}, ${HELPER}->mockUserSessionData(Ljava/lang/Object;)V
                return-object p0
                """.trimIndent(),
            )
        }

        // ── ClickhandlerExtendedFlightInfo getters ────────────────────────────
        // EMS data — clean up / grant access via extension
        ClickhandlerEmsFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                iget-object v0, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->ems:Lcom/flightradar24free/models/entity/EmsData;
                invoke-static {v0}, ${HELPER}->cleanupEmsData(Ljava/lang/Object;)V
                return-object v0
                """.trimIndent(),
            )
        }

        // Squawk availability — return true when squawk string is non-null/non-empty
        ClickhandlerSquawkFingerprint.method.apply {
            ensureRegisters(3)
            clearBody()
            addInstructions(
                0,
                """
                iget-object v0, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->squawkAvailability:Ljava/lang/Boolean;
                iget-object v1, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->squawk:Ljava/lang/String;
                invoke-static {v0, v1}, ${HELPER}->getAvailability(Ljava/lang/Boolean;Ljava/lang/Object;)Ljava/lang/Boolean;
                move-result-object v0
                return-object v0
                """.trimIndent(),
            )
        }

        // Vspeed availability
        ClickhandlerVspeedFingerprint.method.apply {
            ensureRegisters(3)
            clearBody()
            addInstructions(
                0,
                """
                iget-object v0, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->vspeedAvailability:Ljava/lang/Boolean;
                iget-object v1, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->vspeed:Ljava/lang/Integer;
                invoke-static {v0, v1}, ${HELPER}->getAvailability(Ljava/lang/Boolean;Ljava/lang/Object;)Ljava/lang/Boolean;
                move-result-object v0
                return-object v0
                """.trimIndent(),
            )
        }

        // Airspace availability
        ClickhandlerAirspaceFingerprint.method.apply {
            ensureRegisters(3)
            clearBody()
            addInstructions(
                0,
                """
                iget-object v0, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->airspaceAvailability:Ljava/lang/Boolean;
                iget-object v1, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->airspace:Ljava/lang/String;
                invoke-static {v0, v1}, ${HELPER}->getAvailability(Ljava/lang/Boolean;Ljava/lang/Object;)Ljava/lang/Boolean;
                move-result-object v0
                return-object v0
                """.trimIndent(),
            )
        }
    }
}
