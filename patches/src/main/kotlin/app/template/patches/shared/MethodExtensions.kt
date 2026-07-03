package app.template.patches.shared

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.builder.BuilderTryBlock
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

/**
 * Wipe the method body in place: drops every instruction AND every try/catch
 * range. Required before replacing a method body via addInstructions() when the
 * original method has try-blocks — leaving the try table pointing at removed
 * offsets causes ART VerifyError ("bad exception entry").
 *
 * NOTE: this does NOT change registerCount — that field is private final on
 * MutableMethodImplementation with no public setter, and MutableMethod.implementation
 * is read-only (val), so registerCount cannot be grown via this codebase's current
 * API surface. Do not attempt to introduce a brand-new local register (e.g. a fresh
 * v0 for a const/4) after clearBody() — it is NOT guaranteed safe; some methods
 * verify fine, others throw VerifyError ("register vN has type Undefined but
 * expected Integer"). Reusing a parameter register (p0/p1/p2...) for a new value
 * is also illegal — ART enforces parameter registers keep their declared type for
 * the entire method (confirmed via VerifyError: "register vN has type Reference:
 * ... but expected Integer"). Only reuse an existing parameter register for a
 * value of a type compatible with normal control flow (e.g. reassigning p0 to a
 * boolean/enum return value immediately before a return, the pattern used by the
 * isPremium-style gates in this codebase), and only on methods that don't already
 * use that register for an object reference elsewhere.
 */
fun MutableMethod.clearBody() {
    val impl = implementation ?: return
    val field = tryBlocksField
        ?: throw PatchException(
            "MutableMethodImplementation has no List<BuilderTryBlock> field. dexlib2 layout changed?",
        )
    @Suppress("UNCHECKED_CAST")
    (field.get(impl) as MutableList<BuilderTryBlock>).clear()
    val n = impl.instructions.toList().size
    repeat(n) { impl.removeInstruction(0) }
}

private val tryBlocksField: Field? = run {
    MutableMethodImplementation::class.java.declaredFields
        .firstOrNull { f ->
            if (!MutableList::class.java.isAssignableFrom(f.type) &&
                !List::class.java.isAssignableFrom(f.type)
            ) return@firstOrNull false
            val generic = f.genericType as? ParameterizedType ?: return@firstOrNull false
            val arg = generic.actualTypeArguments.firstOrNull() ?: return@firstOrNull false
            arg.typeName == BuilderTryBlock::class.java.name ||
                arg.typeName.startsWith("${BuilderTryBlock::class.java.name}<")
        }
        ?.apply { isAccessible = true }
}

fun MutableMethod.returnEarly() = addInstructions(
    0,
    when (returnType) {
        "V" -> "return-void"
        "Z", "B", "C", "S", "I", "F" -> "const/4 v0, 0x0\nreturn v0"
        "J", "D" -> "const-wide/16 v0, 0x0\nreturn-wide v0"
        else -> "const/4 v0, 0x0\nreturn-object v0"
    },
)
