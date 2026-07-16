package app.template.patches.esexplorer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ES_EXPLORER_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Disables analytics and telemetry in ES File Explorer.
 *
 * Tracking SDKs:
 *   1. UMeng — UMConfigure.preInit() + UMConfigure.init() + UMCrash — init in FexApplication.M()
 *   2. ES custom tracker — x07 reporting system (stat.doglobal.net / nrcapi.ssl2.duapps.com)
 *
 * CRASH FIX (NPE in es.tg7.<clinit>):
 *   Previous version nooped nw.c() → left dgb/e.a (Context) null.
 *   x07.a() (called from es/o90, independent of nw.c()) still spawned x07$b thread.
 *   x07$b → tg7.<clinit> → ni7.b(Context=null) → NPE → FATAL EXCEPTION.
 *
 *   Fix: noop FexApplication.M() (kills UMeng) + noop x07.a() (prevents x07$b thread).
 *   nw.c() runs normally → dgb/e.a Context is set → no NPE.
 *   x07$b thread never starts → no telemetry sent.
 */
@Suppress("unused")
val esExplorerDisableTrackingPatch = bytecodePatch(
    name = "Disable Tracking",
    description = "Disables analytics and telemetry in ES File Explorer",
    default = true,
) {
    compatibleWith(ES_EXPLORER_COMPATIBILITY)

    execute {
        // Noop UMeng init — kills UMConfigure.preInit/init and UMCrash
        AnalyticsInitFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // Noop x07.a() — prevents x07$b background thread that caused NPE crash
        // nw.c() still runs (sets dgb/e.a Context), but the thread is never started
        TelemetryThreadFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }
    }
}
