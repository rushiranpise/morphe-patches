package app.template.patches.esexplorer

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ES_EXPLORER_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ES File Explorer (com.estrongs.android.pop) v4.4.3.7
//
// CHANGED FROM v4.4.3.7:
//   Old: two-step noop — FexApplication.M()V (UMeng) + x07.a(String, x07$d)V (telemetry thread).
//   The x07$b thread crash (NPE in tg7.<clinit> via ni7.b(Context=null)) was the reason
//   for nooping x07.a() separately from nw.c().
//
//   In 4.4.3.5: x07 was restructured into a clean interface (abstract a(tb7)r87).
//   The inner class x07$b no longer exists. The only implementor is w57, which uses
//   the Alipay TSCenter SDK for structured RPC reporting — no Thread spawning.
//   The NPE crash path (x07$b → tg7.<clinit> → ni7.b(null)) is gone.
//   TelemetryThreadFingerprint is removed entirely.
//
// REMAINING TRACKING:
//   UMeng (UMConfigure.preInit + UMConfigure.init + UMCrash) — still in FexApplication.M()V.
//   Nooping M()V kills all three calls at once.
//   AnalyticsInitFingerprint: custom = FexApplication class + method name "M" + string("China").
//   FexApplication is non-obfuscated; method name "M" stable across versions.
@Suppress("unused")
val esExplorerDisableTrackingPatch = bytecodePatch(
    name = "Disable Tracking",
    description = "Disables UMeng analytics and crash reporting in ES File Explorer.",
    default = true,
) {
    compatibleWith(ES_EXPLORER_COMPATIBILITY)

    execute {
        // Noop UMeng init — kills UMConfigure.preInit/init and UMCrash in one call
        UMConfigurePreInitFingerprint.method.returnEarly()
        UMConfigureInitFingerprint.method.returnEarly()
        UMCrashRegisterCallbackFingerprint.method.returnEarly()
    }
}
