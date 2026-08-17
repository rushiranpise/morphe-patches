package app.template.patches.bubbleupnp.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BUBBLEUPNP_COMPATIBILITY
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

/**
 * Unlocks BubbleUPnP licence by forcing the licence setter to always
 * write `true` into the master boolean Ljn;->q0:Z.
 *
 * ## Architecture
 *
 * BubbleUPnP uses Google Play Billing (via obfuscated class `oc0`) to
 * verify a one-time purchase.  On success or failure the billing callback
 * (mc0.run) calls:
 *
 *   AndroidUpnpService.n1(int responseCode, String licenceValue)
 *
 * Inside n1 the app calls `Boolean.parseBoolean(licenceValue)` and stores
 * the result into:
 *   - `Ljn;->q0:Z`   — AbstractApplicationC1142jn.f16941q0 (Application singleton)
 *   - `Lhs6;->q0:Z`  — renderer licence flag (set from the same parsed string)
 *
 * All feature gates in the UI read `Ljn;->q0:Z` directly; patching n1 is
 * the single point of truth.
 *
 * ## Patch
 *
 * At index 0 of n1 we inject:
 *   const-string p2, "true"
 *
 * `p2` is the `String licenceValue` parameter.  Overwriting it before any
 * other instruction causes every subsequent `Boolean.parseBoolean(p2)` call
 * inside n1 to return `true`, so both q0 fields are set to `true` regardless
 * of what the billing server returned.
 *
 * The guard at the top of n1 (`sget-boolean h2:Z`) only skips the logging
 * path when the service is not yet started; once h2 is true the injected
 * string takes effect normally.
 */
@Suppress("unused")
val bubbleUpnpPremiumPatch = bytecodePatch(
    name = "Unlock Licence",
    description = "Unlocks BubbleUPnP licence by forcing the licence setter to always write true.",
) {
    compatibleWith(BUBBLEUPNP_COMPATIBILITY)

    dependsOn(
        spoofInstallSourcePatch,
        spoofSignatureVerificationPatch,
    )

    execute {
        // Force licenceValue parameter to "true" before any downstream parseBoolean call.
        LicenseSetterFingerprint.method.addInstructions(
            0,
            """const-string p2, "true"""",
        )
    }
}
