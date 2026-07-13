package app.template.patches.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.longOption
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.blockerhero.ensureRegisters
import app.template.patches.shared.Constants.WAZE_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun resourceStream(path: String) =
    object {}.javaClass.classLoader.getResourceAsStream(path)

/**
 * Write or update a single key in the preferences file.
 * If the file does not yet exist, creates it (with parent dirs).
 * If the key already exists, replaces its line in-place.
 * Otherwise appends it.
 */
private fun java.io.File.writePref(key: String, value: Any) {
    val line = "$key: $value"
    if (exists()) {
        val text = readText()
        val pattern = Regex("^${Regex.escape(key)}:.*", RegexOption.MULTILINE)
        writeText(if (pattern.containsMatchIn(text)) text.replace(pattern, line) else "$text\n$line")
    } else {
        parentFile?.mkdirs()
        writeText(line)
    }
}

private fun java.io.File.writePrefs(pairs: List<Pair<String, Any>>) =
    pairs.forEach { (k, v) -> writePref(k, v) }

/**
 * Ensure the bundled base preferences file is present.
 * Called by every patch that touches prefs so patches can work independently.
 */
private fun app.morphe.patcher.patch.ResourcePatchContext.ensureBasePrefs() {
    val prefFile = get("assets/res/preferences")
    if (!prefFile.exists()) {
        prefFile.parentFile?.mkdirs()
        resourceStream("waze/assets/res/preferences")?.use { prefFile.writeBytes(it.readBytes()) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Disable Ads
//
// Credits:
//   • Waze CGE Mod  — ExternalPOI suppression keys
//   • Waze Chuppito — ExternalPO_ / Extern__POI dual-key coverage
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeDisableAdsPatch = rawResourcePatch(
    name = "Disable Ads",
    description = "Suppresses all Waze ad systems via bundled preferences file:\n" +
        "• AdMob SDK (Ad_.*)\n" +
        "• Google Ads (Google_Ads.*)\n" +
        "• Ads Inventory Prediction\n" +
        "• ExternalPOI pins, coupons, popups (ExternalPO_ + Extern__POI both key variants)\n" +
        "• Search autocomplete server ads\n" +
        "Credits: Waze CGE Mod (ExternalPOI keys), Waze Chuppito (dual-key coverage).",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)
    execute {
        val prefFile = get("assets/res/preferences")
        prefFile.parentFile?.mkdirs()
        resourceStream("waze/assets/res/preferences")?.use { prefFile.writeBytes(it.readBytes()) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Disable Advil Ad Requests
//
// Fingerprint verified: Lcom/waze/jni/protos/AdvilRequest;->getPageUrl()
//   Classes6.dex — 1 register, 2 instructions
//   ORIG: iget-object v0, v0, →pageUrl_ Ljava/lang/String;  |  return-object v0
//   MOD:  const-string v0, ""                                |  return-object v0
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeAdvilStubPatch = bytecodePatch(
    name = "Disable Advil Ad Requests",
    description = "Stubs AdvilRequest.getPageUrl() → \"\" so the Advil ad server receives " +
        "no page URL and returns no ad content. ",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)
    execute {
        runCatching { AdvilRequestGetPageUrlFingerprint.method }.getOrNull()?.apply {
            replaceInstruction(0, "const-string v0, \"\"")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Uncensored Radar / Camera Display
//
// Credits: Waze CGE Mod — enforcement alert enable keys
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeUncensoredRadarPatch = rawResourcePatch(
    name = "Uncensored Radar / Camera Display",
    description = "Shows exact fixed and mobile speed camera locations, including those not yet " +
        "in the official Waze radar zone. Enables enforcement alerts via preferences keys:\n" +
        "Credits: Waze CGE Mod.",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)
    execute {
        ensureBasePrefs()
        get("assets/res/preferences").writePrefs(listOf(
            "Alerts.Enable Enforcement Alert_" to "yes",
            "Alerts.Enable Enforcement Alert"  to "yes",
            "Alerts.Enable Enforcement Polic_" to "yes",
            "Alerts.Enable_Enforcement_Alerts" to "yes",
            "Alerts.Enable_Enforcement_Police" to "yes",
        ))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Radar Sound (Any Speed)
//
// Fingerprint verified: Lcom/waze/ConfigManager;->onConfigSyncedFromServer()V
//   Classes5.dex — 1 register, 5 instructions
//   Ordinal 632 = CONFIG_VALUE_ALERTS_PLAY_SPEED_CAMERA_SOUND_BELOW_SPEED_LIMIT
//   Injection: setConfigValueBoolNTV(632, true) before return-void at index 4
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeRadarSoundAnySpeedPatch = bytecodePatch(
    name = "Radar Sound (Any Speed)",
    description = "Plays radar/speed camera sound alerts regardless of current speed. " +
        "Official Waze only alerts when over the speed limit.\n",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)
    execute {
        runCatching { ConfigManagerOnConfigSyncedFingerprint.method }.getOrNull()?.apply {
            ensureRegisters(3)
            addInstructions(
                4,
                """
                invoke-static {}, Lcom/waze/ConfigManager;->getInstance()Lcom/waze/ConfigManager;
                move-result-object v0
                const/16 v1, 632
                const/4 v2, 0x1
                invoke-virtual {v0, v1, v2}, Lcom/waze/ConfigManager;->setConfigValueBoolNTV(IZ)V
                """.trimIndent()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Report Speed Limit
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeReportSpeedLimitPatch = rawResourcePatch(
    name = "Report Speed Limit",
    description = "Adds a Report option when tapping the speedometer to report wrong or missing " +
        "speed limits. Not available in the official version.\n",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)
    execute {
        ensureBasePrefs()
        get("assets/res/preferences").writePref("Map.Speedometer report speed enable_", 1)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. Speed Limit Sign
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeSpeedLimitSignPatch = rawResourcePatch(
    name = "Speed Limit Sign",
    description = "Sets the speed limit sign style shown on the map.",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)

    val signStyle by stringOption(
        key = "signStyle",
        default = "us",
        title = "Sign style",
        description = "us = large circular US-style (recommended). metric = small local-style.",
        required = false,
        values = mapOf(
            "US circular — large, more readable" to "us",
            "Local metric — small"               to "metric"
        )
    )

    execute {
        ensureBasePrefs()
        get("assets/res/preferences").writePref("Map.Speedometer sign style", signStyle ?: "us")
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. Enlarged Speedometer
//
// Fingerprint verified: Lcom/waze/main_screen/h/b/d;->f(Lcom/waze/bq/h;)V
//   Classes6.dex — 43 registers
//   [131] const/16 v7, 20  → 28sp   (speed < 100)
//   [132] goto              (gap — scan must be non-consecutive)
//   [133] const/16 v7, 13  → 21sp   (speed ≥ 100)
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeSpeedometerTextSizePatch = bytecodePatch(
    name = "Enlarged Speedometer",
    description = "Increases speedometer digit size for better readability.",
    default = false
) {
    compatibleWith(WAZE_COMPATIBILITY)

    val smallTextSize by longOption(
        key = "smallTextSize",
        default = 28L,
        title = "Text size — speed below 100",
        description = "Speedometer font size when speed < 100. Default: 28sp (original: 20sp).",
        required = false
    )
    val largeTextSize by longOption(
        key = "largeTextSize",
        default = 21L,
        title = "Text size — speed 100+",
        description = "Speedometer font size when speed ≥ 100. Default: 21sp (original: 13sp).",
        required = false
    )

    execute {
        runCatching { SpeedometerUpdateFingerprint.method }.getOrNull()?.apply {
            val instrs = implementation!!.instructions.toList()
            var idx20 = -1; var idx13 = -1
            for (i in instrs.indices) {
                val instr = instrs[i]
                if (instr is NarrowLiteralInstruction && instr is OneRegisterInstruction) {
                    when (instr.narrowLiteral) {
                        20 -> if (idx20 < 0) idx20 = i
                        13 -> if (idx20 >= 0 && idx13 < 0) idx13 = i
                    }
                }
            }
            if (idx20 >= 0 && idx13 >= 0) {
                replaceInstruction(idx20, "const/16 v${(instrs[idx20] as OneRegisterInstruction).registerA}, ${(smallTextSize ?: 28L).toInt()}")
                replaceInstruction(idx13, "const/16 v${(instrs[idx13] as OneRegisterInstruction).registerA}, ${(largeTextSize ?: 21L).toInt()}")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 8. Alert Distances
//
// Credits: Waze CGE Mod — all distance values
//   Official → CGE defaults:
//   Accident 600→2000, Alert 600→1000, Between 300→200,
//   Freeways 2000→1200, Highways 1000→900, Streets 500→700,
//   Hazard 600→500, Heavy Traffic 600→3000, Police 600→1000
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeAlertDistancesPatch = rawResourcePatch(
    name = "Alert Distances",
    description = "Configures radar/camera and hazard alert announcement distances.\n" +
        "Credits: Waze CGE Mod.\n",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)

    val accidentDist  by longOption(key = "accidentDist",  default = 2000L, title = "Accident alert (m)",           description = "Official: 600m.",  required = false)
    val alertDist     by longOption(key = "alertDist",     default = 1000L, title = "General alert (m)",            description = "Official: 600m.",  required = false)
    val policeDist    by longOption(key = "policeDist",    default = 1000L, title = "Police / camera alert (m)",    description = "Official: 600m.",  required = false)
    val freewayDist   by longOption(key = "freewayDist",   default = 1200L, title = "Enforcement — freeways (m)",   description = "Official: 2000m.", required = false)
    val highwayDist   by longOption(key = "highwayDist",   default = 900L,  title = "Enforcement — highways (m)",   description = "Official: 1000m.", required = false)
    val streetDist    by longOption(key = "streetDist",    default = 700L,  title = "Enforcement — streets (m)",    description = "Official: 500m.",  required = false)
    val hazardDist    by longOption(key = "hazardDist",    default = 500L,  title = "Hazard alert (m)",             description = "Official: 600m.",  required = false)
    val heavyTrafDist by longOption(key = "heavyTrafDist", default = 3000L, title = "Heavy traffic alert (m)",      description = "Official: 600m.",  required = false)
    val betweenDist   by longOption(key = "betweenDist",   default = 200L,  title = "Min between alerts (m)",       description = "Official: 300m.",  required = false)

    execute {
        val d  = accidentDist  ?: 2000L; val al = alertDist     ?: 1000L; val po = policeDist    ?: 1000L
        val fr = freewayDist   ?: 1200L; val hi = highwayDist   ?: 900L;  val st = streetDist    ?: 700L
        val ha = hazardDist    ?: 500L;  val ht = heavyTrafDist ?: 3000L; val be = betweenDist   ?: 200L

        ensureBasePrefs()
        get("assets/res/preferences").writePrefs(listOf(
            "Alerts.Accident Alert Distace"                to d,
            "Alerts.Accident_Alert_Distace"                to d,
            "Alerts.Alert Distance"                        to al,
            "Alerts.Alert Distanc_"                        to al,
            "Alerts.Alert_Distance"                        to al,
            "Alerts.Blocked_lane_alert_distance"           to al,
            "Alerts.Distance Between Alerts"               to be,
            "Alerts.Distance_Between_Alerts"               to be,
            "Alerts.Enforcement Alerts Distanc_ Freeways"  to fr,
            "Alerts.Enforcement Alerts Distanc_ Highways"  to hi,
            "Alerts.Enforcement Alerts Distanc_ Streets"   to st,
            "Alerts.Enforcement_Alerts_Distance_Freeways"  to fr,
            "Alerts.Enforcement_Alerts_Distance_Highways"  to hi,
            "Alerts.Enforcement_Alerts_Distance_Streets"   to st,
            "Alerts.Hazard Alert Distace"                  to ha,
            "Alerts.Hazard_Alert_Distace"                  to ha,
            "Alerts.Heavy Traffic Alert Distace"           to ht,
            "Alerts.Heavy_Traffic_Alert_Distace"           to ht,
            "Alerts.Police Alert Distac_"                  to po,
            "Alerts.Police Alert Distace"                  to po,
            "Alerts.Police_Alert_Distace"                  to po,
        ))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 9. Popup Suppression
//
// Credits: Waze Chuppito Mod — all popup threshold values and Popu__ key variant
//   Official → Chuppito defaults:
//   Min speed: 0 → 999999 MMSec (never triggers at any real speed)
//   Fully stopped: varies → -1 (disable stopped trigger)
//   Min Distance: varies → 1m
//   Min reset scroll speed: varies → 7000 MMSec
//   Delay after interaction: 0 → 1 second
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazePopupSuppressionPatch = rawResourcePatch(
    name = "Popup Suppression",
    description = "Prevents promotional and ad popups from appearing while driving.\n" +
        "Raises the minimum trigger speed to a near-impossible value so popups never appear.\n" +
        "Credits: Waze Chuppito Mod.",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)

    val minSpeed             by longOption(key = "minSpeed",             default = 999999L, title = "Min speed to show popups (MMSec)",      description = "Default 999999 = never. Official: 0 (always shows).",     required = false)
    val fullyStoppedSpeed    by longOption(key = "fullyStoppedSpeed",    default = -1L,     title = "Fully stopped speed (MMSec)",            description = "-1 = disable stopped trigger. Official: 0.",               required = false)
    val minDistance          by longOption(key = "minDistance",          default = 1L,      title = "Min distance to show popup (m)",         description = "Official: 100m.",                                           required = false)
    val minResetScrollSpeed  by longOption(key = "minResetScrollSpeed",  default = 7000L,   title = "Min reset scroll speed (MMSec)",         description = "Speed at which scroll suppression resets. Official: 1400.", required = false)
    val delayAfterInteraction by longOption(key = "delayAfterInteraction", default = 1L,    title = "Delay after user interaction (seconds)", description = "Seconds before popup can appear post-interaction. Official: 0.", required = false)

    execute {
        val ms = minSpeed ?: 999999L; val fs = fullyStoppedSpeed ?: -1L
        val md = minDistance ?: 1L;   val mr = minResetScrollSpeed ?: 7000L
        val dl = delayAfterInteraction ?: 1L

        ensureBasePrefs()
        get("assets/res/preferences").writePrefs(listOf(
            "Popup_.Delay seconds after user interaction" to dl,
            "Popup_.Fully stopped speed MMSec"            to fs,
            "Popup_.Min Distance"                         to md,
            "Popup_.Min reset scroll speed MMSec"         to mr,
            "Popup_.Min speed MMSec"                      to ms,
            "Popu__.Delay seconds after user interaction" to dl,
            "Popu__.Fully stopped speed MMSec"            to fs,
            "Popu__.Min Distance"                         to md,
            "Popu__.Min reset scroll speed MMSec"         to mr,
            "Popu__.Min speed MMSec"                      to ms,
        ))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 10. Navigation & Map
//
// Credits:
//   • Waze CGE Mod  — Navigation.Nearing destination distance: 0
//   • Waze Chuppito — Android Auto distances, GPS icon, Map Turn Mode, Traffic Bar
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeNavigationPatch = rawResourcePatch(
    name = "Navigation & Map",
    description = "Configures navigation and map behaviour:\n" +
        "• Nearing destination distance (Credits: CGE Mod)\n" +
        "• Android Auto head-up alert distances\n" +
        "• Map turn mode (auto-zoom to upcoming turn)\n" +
        "• Traffic bar minimum time threshold\n" +
        "• GPS icon visibility\n" +
        "• Route notifications (hazard, school zone) both disabled by default\n" +
        "Credits: Waze CGE Mod (nearing destination), Waze Chuppito (remaining keys).",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)

    val nearingDestDist  by longOption(key = "nearingDestDist",  default = 0L,    title = "Nearing destination distance (m)",           description = "0 = silent arrival. Official: ~500m.",          required = false)
    val aaHeadUpNormal   by longOption(key = "aaHeadUpNormal",   default = 1200L, title = "Android Auto head-up — normal roads (m)",    description = "Official: 800m.",                               required = false)
    val aaHeadUpFreeway  by longOption(key = "aaHeadUpFreeway",  default = 1300L, title = "Android Auto head-up — freeways (m)",        description = "Official: 1000m.",                              required = false)
    val trafficBarMinTime by longOption(key = "trafficBarMinTime", default = 180L, title = "Traffic bar min time in traffic (seconds)",  description = "Official: 300s.",                               required = false)
    val showGpsIcon      by stringOption(key = "showGpsIcon",    default = "no",  title = "Show GPS icon on map",
        description = "Official: yes.", required = false,
        values = mapOf("Hidden (less clutter)" to "no", "Visible" to "yes"))
    val mapTurnMode      by stringOption(key = "mapTurnMode",    default = "1",   title = "Map turn mode (auto-zoom to turn)",
        description = "Default: enabled.", required = false,
        values = mapOf("Enabled" to "1", "Disabled" to "0"))
    val hazardNotification by stringOption(key = "hazardNotif",  default = "0",   title = "Permanent hazard route notification",
        description = "Default: disabled (0).", required = false,
        values = mapOf("Disabled" to "0", "Enabled" to "1"))
    val schoolNotification by stringOption(key = "schoolNotif",  default = "0",   title = "School zone route notification",
        description = "Default: disabled (0).", required = false,
        values = mapOf("Disabled" to "0", "Enabled" to "1"))

    execute {
        ensureBasePrefs()
        get("assets/res/preferences").writePrefs(listOf(
            "Navigation.Nearing destination distance"   to (nearingDestDist ?: 0L),
            "Android Auto.Heads Up Distance Normal"     to (aaHeadUpNormal  ?: 1200L),
            "Android Auto.Heads Up Distance Freeway"    to (aaHeadUpFreeway ?: 1300L),
            "Android Auto.Num Venue Pins"               to 2,
            "Traffic Bar.Min time in traffic seconds"   to (trafficBarMinTime ?: 180L),
            "GPS.Show GPS"                              to (showGpsIcon ?: "no"),
            "Map Turn Mode.Feature Enabled"             to (mapTurnMode ?: "1"),
            "Map Turn Mode.Min distance to next turn"   to 150,
            "Map Turn Mode.Max distance to turn"        to 200,
            "Map Turn Mode.Force 2D distance"           to 80,
            "Map Turn Mode.Continuous zoom"             to 0,
            "Notifications on route.Permanent hazard"   to (hazardNotification ?: "0"),
            "Notifications on route.School zone"        to (schoolNotification ?: "0"),
        ))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 11. AutoZoom
//
// Credits: Waze Chuppito — Speed Factor: 20, Gradient Speed Threshold: 60
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val wazeAutoZoomPatch = rawResourcePatch(
    name = "AutoZoom",
    description = "Controls how aggressively the map zooms in/out based on driving speed.\n" +
        "Credits: Waze Chuppito Mod",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)

    val speedFactor   by longOption(key = "speedFactor",   default = 20L,   title = "Speed factor",                   description = "Higher = more zoom-out at speed. Official: 10.",         required = false)
    val scaleFactor   by longOption(key = "scaleFactor",   default = 25L,   title = "Scale factor",                   description = "Overall zoom scale multiplier. Official: 25.",            required = false)
    val maxScale      by longOption(key = "maxScale",      default = 8000L, title = "Max scale",                      description = "Maximum zoom-out scale. Official: 8000.",                 required = false)
    val gradientSpeed by longOption(key = "gradientSpeed", default = 60L,   title = "Gradient speed threshold (km/h)", description = "Zoom gradient activates above this speed. Official: 30.", required = false)

    execute {
        ensureBasePrefs()
        get("assets/res/preferences").writePrefs(listOf(
            "AutoZoom.Speed Factor"             to (speedFactor   ?: 20L),
            "AutoZoom.Scale Factor"             to (scaleFactor   ?: 25L),
            "AutoZoom.Max Scale"                to (maxScale      ?: 8000L),
            "AutoZoom.Min Scale"                to 0,
            "AutoZoom.Threshold"                to 25,
            "AutoZoom.Gradient Factor"          to 150,
            "AutoZoom.Gradient Speed Threshold" to (gradientSpeed ?: 60L),
        ))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 12. Map Skin (Vitamin C)
//
// Credits: ALEX02-GTT + Waze Chuppito Mod
//   Skin files sourced from Chuppito APK, original design by ALEX02-GTT.
//   • Night: true black (0x000000) for AMOLED
//   • Day: warm beige (0xebe7dd)
//   • FontSizes: huge 14→18, big 12→14, medium 10→12, small 8→10
//   • nav_arrow_head_width_factor: 3.0 → 10.0 (wider = more visible chevron)
//   • Custom car 3D models: Batmobile, Riddler, race car, 3D arrow
// ─────────────────────────────────────────────────────────────────────────────

private val SKIN_FILES = listOf(
    "skins/default/skin_structure.main.lua",
    "skins/default/skin_values.day.lua",
    "skins/default/skin_values.night.lua",
    "skins/default/skin_values.editor.day.lua",
    "skins/default/skin_values.editor.night.lua",
    "skins/default/skin_values.editor.lua",
    "skins/default/skin_values.low_contrasts.lua",
    "skins/default/aa_skin_values.day.lua",
    "skins/default/aa_skin_values.night.lua",
    "skins/default/experiment/skin_structure.main.lua",
    "skins/default/experiment/skin_values.day.lua",
    "skins/default/experiment/skin_values.night.lua",
    "skins/default/experiment/skin_values.editor.day.lua",
    "skins/default/experiment/skin_values.editor.night.lua",
    "skins/default/experiment/skin_values.editor.lua",
    "skins/default/experiment/skin_values.low_contrasts.lua",
)

private val CAR_MODELS = listOf(
    "skins/default/cars/3d_arrow_nice.obj",
    "skins/default/cars/ShadowSquare.obj",
    "skins/default/cars/batmobile_model.obj",
    "skins/default/cars/riddler_model.obj",
    "skins/default/cars/arrow_model.obj",
    "skins/default/cars/car_race.png",
    "skins/default/cars/car_race@2x.png",
)

@Suppress("unused")
val wazeBlackSkinPatch = rawResourcePatch(
    name = "Map Skin (Vitamin C)",
    description = "Applies Chuppito's 'Vitamin C' map skin. All visual values configurable.\n" +
        "• Night: true black AMOLED background (saves battery, prevents burn-in)\n" +
        "• Day: warm beige background\n" +
        "• Larger font labels across the board\n" +
        "• Wider navigation arrow head for better visibility\n" +
        "• Custom car 3D models: Batmobile, Riddler, race car, 3D arrow\n" +
        "Credits: ALEX02-GTT (skin design), Waze Chuppito Mod (integration).",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)

    val nightBg      by stringOption(key = "nightBg",      default = "000000", title = "Night background color (hex)",    description = "Default: 000000 (true black, AMOLED). Original Waze: 1a1a2e.", required = false)
    val dayBg        by stringOption(key = "dayBg",        default = "ebe7dd", title = "Day background color (hex)",      description = "Default: ebe7dd (warm beige). Original Waze: f7f3eb.",        required = false)
    val fontHuge     by longOption(  key = "fontHuge",     default = 18L,      title = "Font size — huge labels",         description = "Default: 18 (original: 14).",   required = false)
    val fontBig      by longOption(  key = "fontBig",      default = 14L,      title = "Font size — big labels",          description = "Default: 14 (original: 12).",   required = false)
    val fontMedium   by longOption(  key = "fontMedium",   default = 12L,      title = "Font size — medium labels",       description = "Default: 12 (original: 10).",   required = false)
    val fontSmall    by longOption(  key = "fontSmall",    default = 10L,      title = "Font size — small labels",        description = "Default: 10 (original: 8).",    required = false)
    val navArrow     by stringOption(key = "navArrow",     default = "10.0",   title = "Nav arrow head width factor",     description = "Default: 10.0 (original: 3.0). Wider = more visible chevron.", required = false)

    execute {
        // Inject base skin Lua files and car models
        for (relPath in SKIN_FILES) {
            val f = get("assets/res/$relPath"); f.parentFile?.mkdirs()
            resourceStream("waze/assets/res/$relPath")?.use { f.writeBytes(it.readBytes()) }
        }
        for (relPath in CAR_MODELS) {
            val f = get("assets/res/$relPath"); f.parentFile?.mkdirs()
            resourceStream("waze/assets/res/$relPath")?.use { f.writeBytes(it.readBytes()) }
        }

        // Night skin overrides
        get("assets/res/skins/default/skin_values.night.lua").let { f ->
            if (f.exists()) f.writeText(f.readText()
                .replace(Regex("map_background = rgb\\(0x[0-9a-fA-F]+\\)"), "map_background = rgb(0x${nightBg ?: "000000"})")
                .replace(Regex("map_missing = rgb\\(0x[0-9a-fA-F]+\\)"),    "map_missing = rgb(0x${nightBg ?: "000000"})"))
        }

        // Day skin overrides
        get("assets/res/skins/default/skin_values.day.lua").let { f ->
            if (f.exists()) f.writeText(f.readText()
                .replace(Regex("map_background = rgb\\(0x[0-9a-fA-F]+\\)"), "map_background = rgb(0x${dayBg ?: "ebe7dd"})")
                .replace(Regex("map_missing = rgb\\(0x[0-9a-fA-F]+\\)"),    "map_missing = rgb(0x${dayBg ?: "ebe7dd"})"))
        }

        // Font sizes and nav arrow overrides to skin_structure
        get("assets/res/skins/default/skin_structure.main.lua").let { f ->
            if (f.exists()) f.writeText(f.readText()
                .replace(Regex("\\bhuge\\s*=\\s*[0-9]+"),                        "huge = ${fontHuge   ?: 18L}")
                .replace(Regex("\\bbig\\s*=\\s*[0-9]+,"),                         "big = ${fontBig     ?: 14L},")
                .replace(Regex("\\bmedium\\s*=\\s*[0-9]+,"),                      "medium = ${fontMedium ?: 12L},")
                .replace(Regex("\\bsmall\\s*=\\s*[0-9]+,"),                       "small = ${fontSmall  ?: 10L},")
                .replace(Regex("nav_arrow_head_width_factor\\s*=\\s*[0-9.]+"),    "nav_arrow_head_width_factor = ${navArrow ?: "10.0"}"))
        }
    }
}
