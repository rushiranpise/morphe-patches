package app.template.patches.cloudflare.warp

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WARP_COMPATIBILITY

/**
 * Disable Analytics / Telemetry — two-layer analytics suppression.
 *
 * ── Architecture (v6.38.8) ────────────────────────────────────────────────────
 * Cloudflare's analytics pipeline is built on RxJava chains wired in the
 * AnalyticsService constructor. The full event lifecycle is:
 *
 *   [Observable] → $6 (Function1<Boolean, Bundle>: builds event bundle)
 *               → AnalyticsService.c() (static: enriches bundle with network metadata)
 *               → $3 / $7 (Function1<Bundle, Unit>: dispatches to Firebase)
 *                     → zzdq.n(String, String, Bundle, Z)  (GMS measurement SDK)
 *
 * Events fired: "enable" (VPN on, via $3), "disable" (VPN off, via $7), plus any
 * screen-tracking or A/B events routed through the same chain.
 *
 * ── Layer 1: AnalyticsServiceDispatchFingerprint → returnEarly() ──────────────
 * Targets AnalyticsService.c(AnalyticsService, Bundle)V — the static Kotlin
 * extension-function dispatcher that enriches the event bundle with network
 * metadata (MNC, MCC, connectivity type). No-oping it:
 *   - Prevents bundle enrichment (no meaningful event data).
 *   - Is safe: the method is void with no observable side effects.
 *   - Is stable: AnalyticsService is a Hilt @Singleton — class and method
 *     signature are R8-kept permanently.
 *
 * ── Layer 2: classDefForEach scan → no-op Firebase dispatchers ───────────────
 * The $3 and $7 anonymous lambda classes still fire even with Layer 1 applied
 * (they receive an unenriched bundle but still call zzdq.n()). Because these
 * inner classes have no stable names — they are numbered by the Kotlin compiler
 * and may be renumbered across recompilations — they are found at patch time by:
 *
 *   1. Enclosing class prefix: "Lcom/cloudflare/app/domain/analytics/AnalyticsService$"
 *      (stable: tied to the Hilt-kept outer class)
 *   2. Implementing kotlin.jvm.functions.Function1
 *      (stable: Kotlin standard library interface, never renamed)
 *   3. Having an invoke(Object)Object method whose instructions contain a call
 *      to com.google.android.gms.internal.measurement.* → method "n"
 *      (narrowed to measurement package; not dependent on GMS class name)
 *
 * The early return injects "sget-object p1, kotlin.Unit;->a" + "return-object p1"
 * which is the canonical Kotlin Unit return — avoids NPE in callers that check
 * the return value via Kotlin's non-null contract.
 *
 * ── Migration from v6.38.7 ────────────────────────────────────────────────────
 * v6.38.7 Layer 2 targeted FirebaseAnalytics.a(Bundle, String)V — a public
 * wrapper for logEvent(). That method was removed in v6.38.8; the GMS measurement
 * SDK now exposes logEvent only via zzdq (field `a` of FirebaseAnalytics).
 * The classDefForEach approach replaces it without any obfuscated class reference.
 */
@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable Analytics / Telemetry",
    description = "Disables all Cloudflare telemetry by no-oping the analytics bundle builder and the Firebase event dispatchers.",
    default = true,
) {
    compatibleWith(WARP_COMPATIBILITY)

    execute {
        // Layer 1 — no-op the static analytics bundle builder.
        // AnalyticsService is Hilt-kept; method 'c' is unique by its (AnalyticsService, Bundle) → V signature.
        AnalyticsServiceDispatchFingerprint.method.addInstructions(0, "return-void")

        // Layer 2 — no-op the Firebase event dispatcher lambdas.
        // Scans AnalyticsService anonymous inner classes without relying on
        // their compiler-assigned numbers ($3, $7) or obfuscated GMS class names.
        classDefForEach { classDef ->
            // Only anonymous inner classes of AnalyticsService
            if (!classDef.type.startsWith(
                    "Lcom/cloudflare/app/domain/analytics/AnalyticsService\$"
                )
            ) return@classDefForEach

            // Must implement kotlin.jvm.functions.Function1
            if (classDef.interfaces.none { it == "Lkotlin/jvm/functions/Function1;" }) {
                return@classDefForEach
            }

            // Find invoke(Object)Object methods that dispatch to the GMS measurement SDK
            val targets = classDef.methods.filter { method ->
                method.name == "invoke" &&
                    method.returnType == "Ljava/lang/Object;" &&
                    method.parameterTypes.size == 1 &&
                    method.implementation?.instructions?.any { instruction ->
                        val s = instruction.toString()
                        s.contains("gms/internal/measurement") && s.contains("->n(")
                    } == true
            }

            if (targets.isEmpty()) return@classDefForEach

            val mutableClass = mutableClassDefByOrNull(classDef.type) ?: return@classDefForEach
            targets.forEach { target ->
                mutableClass.methods.firstOrNull { candidate ->
                    candidate.name == target.name &&
                        candidate.returnType == target.returnType &&
                        candidate.parameterTypes.map { it.toString() } ==
                        target.parameterTypes.map { it.toString() }
                }?.addInstructions(
                    0,
                    """
                        sget-object p1, Lkotlin/Unit;->a:Lkotlin/Unit;
                        return-object p1
                    """,
                )
            }
        }
    }
}
