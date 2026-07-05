package app.template.patches.camscanner

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CAMSCANNER_COMPATIBILITY

@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Disables CamScanner's custom telemetry/log-agent system.",
) {
    compatibleWith(CAMSCANNER_COMPATIBILITY)

    execute {
        IsSkipLoggingFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
