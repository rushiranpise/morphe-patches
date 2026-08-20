package app.template.patches.excel.integrity

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.clearBody

// Stable anchor: (Context, CodeTransparencyCheckCallback)V is globally unique to
// exactly one method in the app — no definingClass or name needed.
// Class "e0" is obfuscated and will shift; CodeTransparencyCheckCallback is stable.
private val codeTransparencyCheckFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/microsoft/office/apphost/CodeTransparencyCheckCallback;",
    ),
)

private val excelBypassCodeTransparencyPatch = bytecodePatch(
) {
    execute {
        codeTransparencyCheckFingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                    invoke-interface {p2}, Lcom/microsoft/office/apphost/CodeTransparencyCheckCallback;->transparencyVerificationSucceeded()V
                    return-void
                """)
        }
    }
}

@JvmSynthetic
internal fun excelBypassCodeTransparencyDependency() = excelBypassCodeTransparencyPatch
