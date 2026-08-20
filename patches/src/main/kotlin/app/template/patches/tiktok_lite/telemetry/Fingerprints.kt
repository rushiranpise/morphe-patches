package app.template.patches.tiktok_lite.telemetry

/*
 * TikTok Lite analytics targets -- smali verified in classes3/classes4.
 *
 * The previous patch targeted com.bytedance.applog.AppLog and com.appsflyer.AppsFlyerLib
 * which live in deleted classes1/classes2. These are the CORRECT targets present in Lite.
 *
 * Note: com.bytedance.applog.AppLog and com.ss.android.common.applog.AppLog are
 * both referenced but their class bodies are absent from the surviving DEX.
 * LiteApplogServiceImpl is the in-app wrapper that IS present and patchable.
 */

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

private const val LITE_APPLOG = "Lcom/ss/android/ugc/aweme/lancet/applog/LiteApplogServiceImpl;"
private const val NET_CLIENT  = "Lcom/ss/android/ugc/aweme/statistic/AppLogNetworkClient;"

// ── LiteApplogServiceImpl (classes3) ─────────────────────────────────────────
// The in-app analytics wrapper. All methods confirmed by smali.

// onEvent(Context, String x6, Long x2, Boolean, JSONObject)V -- fires every analytics event.
internal object ApplogOnEventFingerprint : Fingerprint(
    definingClass = LITE_APPLOG,
    name = "onEvent",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/Long;",
        "Ljava/lang/Long;",
        "Ljava/lang/Boolean;",
        "Lorg/json/JSONObject;",
    ),
)

// initStatisticLogger(Context)V -- initialises the analytics SDK on startup.
internal object ApplogInitStatisticLoggerFingerprint : Fingerprint(
    definingClass = LITE_APPLOG,
    name = "initStatisticLogger",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

// statisticLoggerInit()V -- secondary init called after SDK is ready.
internal object ApplogStatisticLoggerInitFingerprint : Fingerprint(
    definingClass = LITE_APPLOG,
    name = "statisticLoggerInit",
    returnType = "V",
    parameters = emptyList(),
)

// reportPending()V -- flushes queued events to server.
internal object ApplogReportPendingFingerprint : Fingerprint(
    definingClass = LITE_APPLOG,
    name = "reportPending",
    returnType = "V",
    parameters = emptyList(),
)

// config()V -- configures SDK properties.
internal object ApplogConfigFingerprint : Fingerprint(
    definingClass = LITE_APPLOG,
    name = "config",
    returnType = "V",
    parameters = emptyList(),
)

// beforeInit()V -- pre-init setup. Calls com.ss.android.common.applog.AppLog methods.
internal object ApplogBeforeInitFingerprint : Fingerprint(
    definingClass = LITE_APPLOG,
    name = "beforeInit",
    returnType = "V",
    parameters = emptyList(),
)

// ── AppLogNetworkClient (classes4) ────────────────────────────────────────────
// HTTP client that physically sends analytics data to ByteDance servers.
// Blocking these stops all outbound analytics traffic regardless of the wrapper layer.

// LBL(String, List)String -- sends a batch of events via POST.
internal object NetClientSendBatchFingerprint : Fingerprint(
    definingClass = NET_CLIENT,
    name = "LBL",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/List;"),
)

// LCC(String, byte[], Map)String -- sends binary event payload.
internal object NetClientSendBytesFingerprint : Fingerprint(
    definingClass = NET_CLIENT,
    name = "LCC",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;", "[B", "Ljava/util/Map;"),
)

// LCCII(String, byte[], Map)byte[] -- sends binary payload, returns response bytes.
internal object NetClientSendBytesRawFingerprint : Fingerprint(
    definingClass = NET_CLIENT,
    name = "LCCII",
    returnType = "[B",
    parameters = listOf("Ljava/lang/String;", "[B", "Ljava/util/Map;"),
)

// ── Startup init tasks ────────────────────────────────────────────────────────

// StatisticLoggerInitTask.run(Context)V (classes4) -- lego startup task.
internal object StatisticLoggerInitTaskFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/statistic/StatisticLoggerInitTask;",
    name = "run",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

// SwitchToBDTrackerTask.run(Context)V (classes3) -- switches to BD tracker on startup.
internal object SwitchToBDTrackerTaskFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/launcher/task/SwitchToBDTrackerTask;",
    name = "run",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)
