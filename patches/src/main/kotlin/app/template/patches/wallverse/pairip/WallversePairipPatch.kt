package app.template.patches.wallverse.pairip

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WALLVERSE_COMPATIBILITY

@Suppress("unused")
val wallverseStripPairipPatch = bytecodePatch(
    name = "Strip Pairip",
    description = "Removes the Pairip startup license check.",
) {
    compatibleWith(WALLVERSE_COMPATIBILITY)

    execute {
        // Application.attachBaseContext calls this before WallverseApp starts.
        // Returning immediately preserves the original application initialization.
        PairipCheckLicenseFingerprint.method.addInstructions(0, "return-void")
    }
}
