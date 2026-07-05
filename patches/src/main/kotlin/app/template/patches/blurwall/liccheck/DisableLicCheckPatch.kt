package app.template.patches.blurwall.liccheck

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLURWALL_COMPATIBILITY

// BlurWall v2.9.2 — SharedPreferences license gate bypass.
//
// Premium state is stored in SharedPreferences file "flip_clock", key "start_blur".
// The obfuscated getter LͿ/Ϳ;->މ(String, Object) is called throughout the UI
// (composables, paywall gate) with key="start_blur" and default=Boolean.FALSE.
//
// If the RevenueCat callback hasn't fired yet (cold start, no network, re-signed APK),
// the key is absent and the getter returns FALSE → paywall is shown.
//
// Patch: prepend a short-circuit at the top of the getter — when key == "start_blur",
// return Boolean.TRUE immediately, bypassing the SharedPreferences read entirely.

@Suppress("unused")
val disableLicCheckPatch = bytecodePatch(
    name = "Disable License Check",
    description = "Disables License Check",
) {
    compatibleWith(BLURWALL_COMPATIBILITY)

    execute {
        SharedPrefsGetterFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "start_blur"
                invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :skip_start_blur
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
                :skip_start_blur
                nop
            """,
        )
    }
}
