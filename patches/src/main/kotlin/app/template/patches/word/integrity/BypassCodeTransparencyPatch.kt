package app.template.patches.word.integrity

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.clearBody

private val codeTransparencyCheckFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/apphost/e0;",
    name = "c",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/microsoft/office/apphost/CodeTransparencyCheckCallback;",
    ),
)

private val wordBypassCodeTransparencyPatch = bytecodePatch(
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
internal fun wordBypassCodeTransparencyDependency() = wordBypassCodeTransparencyPatch
