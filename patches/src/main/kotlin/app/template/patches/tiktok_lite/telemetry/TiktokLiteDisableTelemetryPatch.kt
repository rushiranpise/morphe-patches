package app.template.patches.tiktok_lite.telemetry

/*
 * Disables TikTok Lite analytics using classes present in the surviving DEX (classes3/4).
 *
 * Previous patch was wrong: com.bytedance.applog.AppLog and com.appsflyer.AppsFlyerLib
 * live in classes1/classes2 (deleted). These are the correct targets verified in smali.
 */

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TIKTOK_LITE_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val tiktokLiteDisableTelemetryPatch = bytecodePatch(
    name = "Disable Telemetry",
    description = "Disables ByteDance analytics by blocking LiteApplogServiceImpl, AppLogNetworkClient, and startup tracker init tasks.",
    default = true,
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // ── LiteApplogServiceImpl wrapper (classes3) ──────────────────────────
        // Block event firing at the wrapper level so no events reach the network layer.
        ApplogOnEventFingerprint.method.returnEarly()
        ApplogInitStatisticLoggerFingerprint.method.returnEarly()
        ApplogStatisticLoggerInitFingerprint.method.returnEarly()
        ApplogReportPendingFingerprint.method.returnEarly()
        ApplogConfigFingerprint.method.returnEarly()
        ApplogBeforeInitFingerprint.method.returnEarly()

        // ── AppLogNetworkClient HTTP sender (classes4) ────────────────────────
        // Block at the network layer too -- belt and suspenders.
        // Return empty string / empty byte array so callers don't crash on null.
        NetClientSendBatchFingerprint.method.returnEarly("")
        NetClientSendBytesFingerprint.method.returnEarly("")
        NetClientSendBytesRawFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                new-array v0, v0, [B
                return-object v0
            """,
        )

        // ── Startup init tasks ────────────────────────────────────────────────
        // Kill analytics SDK init at the lego task level so init never runs.
        StatisticLoggerInitTaskFingerprint.method.returnEarly()
        SwitchToBDTrackerTaskFingerprint.method.returnEarly()
    }
}
