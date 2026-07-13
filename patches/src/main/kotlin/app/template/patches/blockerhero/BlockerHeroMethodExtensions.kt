package app.template.patches.blockerhero

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.builder.BuilderTryBlock
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

/**
 * `MutableMethodImplementation.registerCount` is a `final int`. Growing it is
 * required when injecting smali code that needs scratch registers beyond the
 * method's existing locals.
 *
 * This helper bumps the field via reflection so subsequent `addInstructions`
 * calls compile against the new register count (the inline-smali compiler reads
 * `method.implementation.registerCount` to set `.registers N` in its template).
 *
 * Note: smali parameter notation `p0..pN` is RELATIVE to register count — it
 * always refers to the LAST `parameterCount + 1` registers. Growing register
 * count automatically remaps `p0` to a higher v-register, so existing parameter
 * references stay correct.
 */
fun MutableMethod.ensureRegisters(needed: Int) {
    val impl = implementation ?: return
    if (impl.registerCount >= needed) return

    val field = registerCountField
        ?: throw PatchException(
            "MutableMethodImplementation has no `int` field named 'registerCount' " +
                "(scanned all declared fields). dexlib2 internal layout changed?",
        )
    field.setInt(impl, needed)
}

/**
 * Wipe the method body in place: drop every instruction AND every try/catch
 * range. Morphe-patcher exposes `MutableMethod.implementation` as `val`, so we
 * cannot swap a fresh `MutableMethodImplementation(N)` like ReVanced did.
 * Calling `removeInstructions(0, count)` on its own leaves the original
 * try-block table around — once the body collapses to a single instruction,
 * those tries point at offsets that no longer exist, ART rejects the class
 * with VerifyError ("bad exception entry: startAddr=0 endAddr=0"), and
 * baksmali throws InstructionOffsetMap.InvalidInstructionIndex(-1) on the
 * resulting DEX. The public `getTryBlocks()` returns
 * `Collections.unmodifiableList(...)`, so `.clear()` on it throws
 * UnsupportedOperationException; this helper grabs the backing List via
 * reflection instead — matching the effect of constructing a brand-new
 * `MutableMethodImplementation(registerCount)`.
 */
fun MutableMethod.clearBody() {
    val impl = implementation ?: return
    val field = tryBlocksField
        ?: throw PatchException(
            "MutableMethodImplementation has no List<BuilderTryBlock> field " +
                "(scanned all declared fields). dexlib2 internal layout changed?",
        )
    @Suppress("UNCHECKED_CAST")
    (field.get(impl) as MutableList<BuilderTryBlock>).clear()
    val n = impl.instructions.toList().size
    repeat(n) { impl.removeInstruction(0) }
}

// Resolve the private fields once at class-load time by scanning structure
// rather than hard-coding names. Survives dexlib2 / morphe-smali renames as
// long as the field types remain `int` (registerCount) and
// `List<BuilderTryBlock>` (tryBlocks). Each declaredFields scan yields stable
// ordering so caching the result is safe.

private val registerCountField: Field? = run {
    MutableMethodImplementation::class.java.declaredFields
        .firstOrNull { it.type == Int::class.javaPrimitiveType }
        ?.apply { isAccessible = true }
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
