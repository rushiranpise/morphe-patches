package app.template.patches.citymapper

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CITYMAPPER_COMPATIBILITY

// Citymapper Club unlock — v11.56.1
//
// Club membership is gated behind FeatureFlag.USE_FAKE_SUBSCRIPTION. The flag is
// an enum constant whose class name is obfuscated and shifts every update:
//   v11.55: Lst3;->USE_FAKE_SUBSCRIPTION:Lst3;
//   v11.56: Lry3;->USE_FAKE_SUBSCRIPTION:Lry3;
//
// Rather than hardcoding the class descriptor, we resolve it at patch time from
// FeatureFlagIsEnabledFingerprint.classDef.type, which is resolved by the stable
// string filter "USE_FAKE_SUBSCRIPTION". This makes the smali inject body
// future-proof regardless of further class renames.
//
// Patch strategy:
//   1. Inject CitymapperHelper.init() at app startup (extension side-car).
//   2. Prepend isEnabled() so that when the caller is USE_FAKE_SUBSCRIPTION,
//      return true immediately — all other flag checks fall through normally.
//
// NOTE: App still requires a manual in-app purchase attempt to finalise the
// subscription flow (server-side validation is bypassed locally but not blocked
// by the patch itself).

@Suppress("unused")
val citymapperUnlockClubPatch = bytecodePatch(
    name = "Unlock Club",
    description = "Unlocks Citymapper Club Membership. Note: requires a manual purchase attempt inside the app.",
    default = true,
) {
    compatibleWith(CITYMAPPER_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {
        // Inject extension initialiser into Application.onCreate.
        CitymapperApplicationOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static {}, Lapp/template/extension/extension/CitymapperHelper;->init()V",
        )

        // Resolve the obfuscated FeatureFlag class descriptor at patch time so
        // the smali body is correct regardless of future class renames.
        val flagType = FeatureFlagIsEnabledFingerprint.classDef.type  // e.g. "Lry3;"

        // Prepend isEnabled() with an early-return for USE_FAKE_SUBSCRIPTION.
        // If p0 == USE_FAKE_SUBSCRIPTION → return true immediately.
        // All other enum constants fall through to the original method body.
        FeatureFlagIsEnabledFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, $flagType->USE_FAKE_SUBSCRIPTION:$flagType
                if-ne p0, v0, :ignore
                const/4 v0, 0x1
                return v0
                :ignore
            """.trimIndent(),
        )
    }
}
