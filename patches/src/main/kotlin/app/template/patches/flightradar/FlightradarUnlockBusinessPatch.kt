package app.template.patches.flightradar

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.blockerhero.ensureRegisters
import app.template.patches.shared.Constants.FLIGHTRADAR_COMPATIBILITY

@Suppress("unused")
val flightradarUnlockBusinessPatch = bytecodePatch(
    name = "Unlock Business Premium",
    description = "Unlocks Business/Gold premium features in Flightradar24: ad-free map, weather layers, ATC, 3D view, flight history, and unlimited saved locations.",
    default = true
) {
    compatibleWith(FLIGHTRADAR_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {
        // 1. Mock local purchases provider
        BillingPurchasesProviderIsValidFingerprint.match(mutableClassDefBy(BillingPurchasesProviderIsValidFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // 2. Mock User status checks (Gold/Silver/Alerts)
        UserIsGoldFingerprint.match(mutableClassDefBy(UserIsGoldFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        UserIsBusinessFingerprint.match(mutableClassDefBy(UserIsBusinessFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        UserIsSilverFingerprint.match(mutableClassDefBy(UserIsSilverFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            // Return false so Gold check (r()) takes precedence
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        UserHasAlertsFingerprint.match(mutableClassDefBy(UserHasAlertsFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        UserIsBasicFingerprint.match(mutableClassDefBy(UserIsBasicFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        UserIsAdvertsEnabledFingerprint.match(mutableClassDefBy(UserIsAdvertsEnabledFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // 3. Mock UserFeatures enablers (true for premium layers, false for adverts)
        val trueEnablers = listOf(
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
            UserFeaturesIsMapLayerWeatherWindEnabledFingerprint
        )

        trueEnablers.forEach { fp ->
            fp.match(mutableClassDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        UserFeaturesIsAdvertsEnabledFingerprint.match(mutableClassDefBy(UserFeaturesIsAdvertsEnabledFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // 4a. getMapInfoAircraft() returns "full" or "limited" - gates squawk/FIR display
        UserFeaturesGetMapInfoAircraftFingerprint.match(mutableClassDefBy(UserFeaturesGetMapInfoAircraftFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const-string v0, \"full\"\nreturn-object v0")
        }

        // 4b. Override UserFeatures numeric limits to high/premium values
        val highLimits = listOf(
            UserFeaturesGetMapFiltersMaxFingerprint to 100,
            UserFeaturesGetUserBookmarksMaxFingerprint to 100,
            UserFeaturesGetHistoryPlaybackDaysFingerprint to 1095,
            UserFeaturesGetAirportFlightHistoryFingerprint to 1095
        )

        highLimits.forEach { (fp, limitValue) ->
            fp.match(mutableClassDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                // limitValue is < 32768, const/16 v0, limitValue works perfectly
                addInstructions(0, "const/16 v0, $limitValue\nreturn v0")
            }
        }

        // 5. Bypass Map signature checks (PackageManager spoofing) via FR24Application.onCreate
        FR24ApplicationOnCreateFingerprint.match(mutableClassDefBy(FR24ApplicationOnCreateFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "invoke-static {}, Lapp/template/extension/extension/FlightradarHelper;->init()V")
        }

        // 6. Force subscription tier to Gold when query occurs (e.g. after login)
        EcfGetSubscriptionTierFingerprint.match(mutableClassDefBy(EcfGetSubscriptionTierFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const-string v0, \"Business\"\nreturn-object v0")
        }

        EcfGetSubscriptionTierEnumFingerprint.match(mutableClassDefBy(EcfGetSubscriptionTierEnumFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "sget-object v0, Lzse\$a;->h:Lzse\$a;\nreturn-object v0")
        }



        // 8. Inject mockUserFeatures at the end of all UserFeatures constructors (before return-void)
        //    NOTE: InlineSmaliCompiler crashes when passing registers (e.g. {p0}) via mutableClassDefBy().methods loop.
        //    Workaround: use sput-object to store p0 into a static field, then call parameterless method.
        UserFeaturesConstructorFingerprint.match(mutableClassDefBy(UserFeaturesConstructorFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            val lastIdx = instructions.indexOfLast { it.opcode.name.startsWith("return-void") }
            if (lastIdx >= 0) {
                addInstructions(
                    lastIdx,
                    """
                    sput-object p0, Lapp/template/extension/extension/FlightradarHelper;->tempFeatures:Ljava/lang/Object;
                    invoke-static {}, Lapp/template/extension/extension/FlightradarHelper;->mockUserFeaturesFromTemp()V
                    """.trimIndent()
                )
            }
        }

        UserDataGetUserDataFingerprint.match(mutableClassDefBy(UserDataGetUserDataFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, """
                iget-object p0, p0, Lcom/flightradar24free/models/account/UserData;->userData:Lcom/flightradar24free/models/account/UserSessionData;
                invoke-static {p0}, Lapp/template/extension/extension/FlightradarHelper;->mockUserSessionData(Ljava/lang/Object;)V
                return-object p0
            """.trimIndent())
        }

        ClickhandlerEmsFingerprint.match(mutableClassDefBy(ClickhandlerEmsFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, """
                iget-object v0, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->ems:Lcom/flightradar24free/models/entity/EmsData;
                invoke-static {v0}, Lapp/template/extension/extension/FlightradarHelper;->cleanupEmsData(Ljava/lang/Object;)V
                return-object v0
            """.trimIndent())
        }

        ClickhandlerSquawkFingerprint.match(mutableClassDefBy(ClickhandlerSquawkFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            ensureRegisters(3)
            removeInstructions(0, instructions.count())
            addInstructions(0, """
                iget-object v0, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->squawkAvailability:Ljava/lang/Boolean;
                iget-object v1, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->squawk:Ljava/lang/String;
                invoke-static {v0, v1}, Lapp/template/extension/extension/FlightradarHelper;->getAvailability(Ljava/lang/Boolean;Ljava/lang/Object;)Ljava/lang/Boolean;
                move-result-object v0
                return-object v0
            """.trimIndent())
        }

        ClickhandlerVspeedFingerprint.match(mutableClassDefBy(ClickhandlerVspeedFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            ensureRegisters(3)
            removeInstructions(0, instructions.count())
            addInstructions(0, """
                iget-object v0, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->vspeedAvailability:Ljava/lang/Boolean;
                iget-object v1, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->vspeed:Ljava/lang/Integer;
                invoke-static {v0, v1}, Lapp/template/extension/extension/FlightradarHelper;->getAvailability(Ljava/lang/Boolean;Ljava/lang/Object;)Ljava/lang/Boolean;
                move-result-object v0
                return-object v0
            """.trimIndent())
        }

        ClickhandlerAirspaceFingerprint.match(mutableClassDefBy(ClickhandlerAirspaceFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            ensureRegisters(3)
            removeInstructions(0, instructions.count())
            addInstructions(0, """
                iget-object v0, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->airspaceAvailability:Ljava/lang/Boolean;
                iget-object v1, p0, Lcom/flightradar24free/models/clickhandler/ClickhandlerExtendedFlightInfo;->airspace:Ljava/lang/String;
                invoke-static {v0, v1}, Lapp/template/extension/extension/FlightradarHelper;->getAvailability(Ljava/lang/Boolean;Ljava/lang/Object;)Ljava/lang/Boolean;
                move-result-object v0
                return-object v0
            """.trimIndent())
        }
    }
}
