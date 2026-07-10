package app.template.patches.duolingo

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.DUOLINGO_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val duolingoEnableDebugMenuPatch = bytecodePatch(
    name = "Enable Debug Menu",
    description = "Enables Duolingo's hidden debug menu in settings.",
    default = false,
) {
    compatibleWith(DUOLINGO_COMPATIBILITY)

    execute {
        val isDebugField = BuildTargetFieldFingerprint.method.instructions
            .elementAt(BuildTargetFieldFingerprint.instructionMatches.first().index + 1)
            .let { instruction ->
                (instruction as? ReferenceInstruction)?.reference as? FieldReference
            } ?: throw PatchException("Could not find debug field")

        mutableClassDefBy(isDebugField.definingClass).methods
            .first { method -> method.name == "<init>" }
            .apply {
                val returnIndex = instructions.indexOfLast { instruction -> instruction.opcode == Opcode.RETURN_VOID }
                if (returnIndex < 0) throw PatchException("Could not find debug provider constructor return")

                addInstructions(
                    returnIndex,
                    """
                    const/4 v0, 0x1
                    iput-boolean v0, p0, ${isDebugField.definingClass}->${isDebugField.name}:Z
                    """.trimIndent(),
                )
            }
    }
}
