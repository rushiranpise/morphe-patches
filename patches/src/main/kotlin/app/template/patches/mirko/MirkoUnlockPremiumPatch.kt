package app.template.patches.mirko

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MIRKO_COMPATIBILITY

/**
 * Unlocks premium in Beta by Mirko (it.mirko.beta).
 *
 * ## Layer 1 — Ads/premium state gate
 *
 * `z3/c.E()Z` reads SharedPrefs key "key_ads_2" (default true = has ads).
 * Returning false signals premium to all callers (ad display, paywall, subscribe button).
 *
 * ## Layer 2 — PairIP ContentProvider startup bypass
 *
 * `LicenseContentProvider.onCreate()Z` runs before any Activity via ContentProvider
 * init. It creates LicenseClient and calls initializeLicenseCheck() which connects
 * to Play Store licensing, fails on unowned copies, and launches LicenseActivity →
 * process killed. Returning true immediately skips all license initialization.
 *
 * ## Layer 3 — PairIP RSA response validation bypass
 *
 * `ResponseValidator.validateResponse(Bundle, String)V` performs SHA256withRSA
 * signature verification. return-void makes every response silently pass.
 * Defensive fallback in case initializeLicenseCheck() is called from elsewhere.
 */
@Suppress("unused")
val mirkoUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium in Beta by Mirko.",
    default = true
) {
    compatibleWith(MIRKO_COMPATIBILITY)

    execute {
        // Layer 1: force no-ads / premium state
        AdsStateFingerprint
            .match(classDefBy(AdsStateFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x0
                    return v0
                    """.trimIndent()
                )
            }

        // Layer 2: skip PairIP license check at ContentProvider startup
        PairIpContentProviderFingerprint
            .match(classDefBy(PairIpContentProviderFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // Layer 3: bypass PairIP RSA license validation (defensive)
        PairIpValidateResponseFingerprint
            .match(classDefBy(PairIpValidateResponseFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }
    }
}
