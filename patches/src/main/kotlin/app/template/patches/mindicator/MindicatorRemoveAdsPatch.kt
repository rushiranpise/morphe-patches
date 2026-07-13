package app.template.patches.mindicator

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MINDICATOR_COMPATIBILITY

/**
 * Removes ads from m-Indicator — Mumbai Local Train (com.mobond.mindicator).
 *
 * ## Monetization model
 * Purely ad-supported (no IAP/subscription). Ads are served via:
 * - Admob interstitial (shown on list item click / screen transitions)
 * - DFP/AdX native exit banner (shown on app exit)
 * - Web-based banner ads loaded via WebView
 *
 * ## Patch strategy
 *
 * ### Layer 1 — c.f0(Activity)Z — suppress interstitial display
 * The sole interstitial show entry point. Called from ui/e.R(), MsrtcBusRouteUI,
 * and Multicity_home on item selection. Returns false immediately — no ad shown,
 * no InterstitialAd.show() call, no FullScreenContentCallback side effects.
 *
 * ### Layer 2 — c.A(Context, I)V — suppress interstitial load
 * Fetches and caches the interstitial ad. Blocking the load ensures f0() always
 * finds c==null and exits early even without Layer 1, providing defence-in-depth.
 *
 * ### Layer 3 — c.W(Activity, I, b, k)View — suppress exit native ad
 * Loads the DFP native ad shown on the exit dialog. Returning null means the
 * AdUI exit screen renders with an empty view instead of an ad.
 */
@Suppress("unused")
val mindicatorRemoveAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes interstitial and exit native ads from m-Indicator.",
    default = true
) {
    compatibleWith(MINDICATOR_COMPATIBILITY)

    execute {
        // ── Layer 1: c.f0(Activity)Z — always return false (no show) ─────────
        ShowInterstitialFingerprint
            .match(classDefBy(ShowInterstitialFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 p0, 0x0
                    return p0
                    """.trimIndent()
                )
            }

        // ── Layer 2: c.A(Context, I)V — skip interstitial load ───────────────
        LoadInterstitialFingerprint
            .match(classDefBy(LoadInterstitialFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }

        // ── Layer 3: c.W(Activity,I,b,k)View — skip exit native ad load ──────
        LoadExitNativeAdFingerprint
            .match(classDefBy(LoadExitNativeAdFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x0
                    return-object v0
                    """.trimIndent()
                )
            }
    }
}
