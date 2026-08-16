package app.template.patches.camscanner

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CAMSCANNER_COMPATIBILITY
import app.template.patches.shared.clearBody

// LogAgent (com.intsig.logagent.LogAgent) is CamScanner's custom telemetry SDK.
// Killing all three Init() overloads prevents the agent from ever being created,
// which suppresses all telemetry, event tracking, and remote logging.
//
// LogAgent.Init is non-obfuscated (stable across versions).
// Smali: classes4/com/intsig/logagent/LogAgent.smali
//   .method public static Init(Application, int, String, String, String)I
//   .method public static Init(Application, SocketInterface)I
//   .method public static Init(Application, String, String, String, String)I
// All return int (0 = already initialised / success). Clearing body + returning 0 is safe.

@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Disables CamScanner's custom telemetry/log-agent system.",
) {
    compatibleWith(CAMSCANNER_COMPATIBILITY)

    execute {
        val logAgent = mutableClassDefBy("Lcom/intsig/logagent/LogAgent;")

        logAgent.methods
            .filter { it.name == "Init" && it.returnType == "I" }
            .forEach { method ->
                method.clearBody()
                method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
    }
}
