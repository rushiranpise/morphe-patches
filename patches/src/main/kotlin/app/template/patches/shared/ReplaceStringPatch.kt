package app.template.patches.shared

import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import app.template.patches.shared.getReference
import app.template.patches.shared.matchAllMethodIndicesForEach
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference

/**
 * @param from Original string to replace.
 * @param to Replacement string or replacement substring when not using [StringComparisonType.EQUALS].
 * @param requireMatches If true and no matches exist, an exception is thrown.
 * @param comparison String comparison type. Defaults to [StringComparisonType.EQUALS].
 */
@Suppress("unused")
fun replaceStringPatch(
    from: String,
    to: String,
    requireMatches: Boolean = false,
    comparison: StringComparisonType = StringComparisonType.EQUALS,
) = bytecodePatch(
    description = "Replaces occurrences of '$from' with '$to' in String constants."
) {
    execute {
        string(from, comparison).matchAllMethodIndicesForEach(requireMatches = requireMatches) { index ->
            val replacement = when (comparison) {
                StringComparisonType.EQUALS -> to
                else -> {
                    getInstruction<ReferenceInstruction>(index)
                        .getReference<StringReference>()!!.string
                        .replace(from, to)
                }
            }

            replaceInstruction(
                index,
                BuilderInstruction21c(
                    Opcode.CONST_STRING,
                    getInstruction<OneRegisterInstruction>(index).registerA,
                    ImmutableStringReference(replacement),
                )
            )
        }
    }
}
