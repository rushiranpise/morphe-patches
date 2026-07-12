package app.template.patches.shared.universal

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val universalEnableDebugBuildTargetPatch = bytecodePatch(
    name = "Enable debug build target",
    description = "Forces compatible BUILD_TARGET debug providers to debug=true.",
    default = false,
) {
    execute {
        var debugField: FieldReference? = null
        classDefForEach { classDef ->
            if (debugField != null) return@classDefForEach
            mutableClassDefBy(classDef).methods.forEach { method ->
                if (debugField != null) return@forEach
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                val strings = instructions.mapNotNull {
                    ((it as? ReferenceInstruction)?.reference as? StringReference)?.string
                }.toSet()
                if (!strings.containsAll(listOf("BUILD_TARGET", "debug", "release"))) return@forEach
                debugField = instructions
                    .filter { it.opcode == Opcode.IGET_BOOLEAN }
                    .mapNotNull { ((it as? ReferenceInstruction)?.reference as? FieldReference) }
                    .firstOrNull { it.type == "Z" }
            }
        }

        val field = debugField ?: throw PatchException("Could not find debug build target field")

        val targetClass = mutableClassDefBy(field.definingClass)
        val constructor = targetClass.methods.firstOrNull { it.name == "<init>" }
            ?: throw PatchException("Could not find debug provider constructor")
        val instructions = constructor.instructionsOrNull?.toList()
            ?: throw PatchException("Debug provider constructor has no instructions")
        val returnIndex = instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
        if (returnIndex < 0) throw PatchException("Could not find debug provider constructor return")

        constructor.addInstructions(
            returnIndex,
            """
                const/4 v0, 0x1
                iput-boolean v0, p0, ${field.definingClass}->${field.name}:Z
            """.trimIndent(),
        )
    }
}
