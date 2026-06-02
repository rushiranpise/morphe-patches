package app.template.patches.cloudflare.warp

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WARP_COMPATIBILITY

/**
 * Silences all Firebase analytics and Cloudflare telemetry via a two-layer strategy.
 *
 * --- Layer 1: AnalyticsService.c(AnalyticsService, Bundle)V (no-op) ---
 * This is the Kotlin-compiler-generated static dispatcher method for the
 * logAnalyticsEvent() extension function.  Every Cloudflare-specific telemetry
 * call site (tunnel_mode, encryption, feature, connection_type, ip_version events)
 * routes through this static method — no-oping it at position 0 silences all
 * Cloudflare analytics at the dispatch layer without touching the constructor.
 *
 * WHY NOT the constructor:
 *   Injecting return-void before super.<init>() in an <init> method is illegal
 *   under the Dex verifier — it produces a VerifyError and the APK crashes on
 *   first launch.  Constructors must always complete their super/this call chain.
 *
 * --- Layer 2: FirebaseAnalytics.logEvent / method `a` (no-op) ---
 * Belt-and-suspenders guard: the obfuscated logEvent method `a(Bundle, String)V`
 * inside the Firebase SDK wrapper is also no-oped.  This catches any residual
 * caller that bypasses AnalyticsService and invokes the Firebase SDK directly
 * (screen-tracking, A/B testing SDKs, third-party libraries bundled in the APK).
 *
 * Events silenced (observed in decompiled AnalyticsService Smali):
 *   - tunnel_mode (WARP / DOT / DOH / WARP_DOH)
 *   - encryption  (e.g. DoH, DoT)
 *   - feature     ("onedotone")
 *   - connection_type (wi-fi / cellular + mnc/mcc)
 *   - ip_version  (v4 / v6 / dual-stack / unknown)
 */
@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable Analytics / Telemetry",
    description = "Disables App Analytics / Telemetry.",
    default = false
) {
    compatibleWith(WARP_COMPATIBILITY)

    execute {
        // Layer 1 — no-op the static Cloudflare analytics dispatcher.
        AnalyticsServiceDispatchFingerprint.method.addInstructions(
            0,
            """
                return-void
            """
        )

        // Layer 2 — no-op the Firebase SDK logEvent wrapper.
        FirebaseAnalyticsLogEventFingerprint.method.addInstructions(
            0,
            """
                return-void
            """
        )
    }
}