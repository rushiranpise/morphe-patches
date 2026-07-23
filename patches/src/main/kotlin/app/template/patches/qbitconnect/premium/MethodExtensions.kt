package app.template.patches.qbitconnect.premium

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import java.lang.reflect.Field

/**
 * Bumps MutableMethodImplementation.registerCount via reflection so that
 * addInstructions() can use scratch registers beyond the original method locals.
 *
 * Copied from blockerhero/BlockerHeroMethodExtensions.kt — the shared/MethodExtensions.kt
 * only provides clearBody(), not ensureRegisters().
 */
fun MutableMethod.ensureRegisters(needed: Int) {
    val impl = implementation ?: return
    if (impl.registerCount >= needed) return
    val field = registerCountField
        ?: throw PatchException(
            "MutableMethodImplementation has no int field for registerCount. dexlib2 layout changed?",
        )
    field.setInt(impl, needed)
}

private val registerCountField: Field? = run {
    MutableMethodImplementation::class.java.declaredFields
        .firstOrNull { it.type == Int::class.javaPrimitiveType }
        ?.apply { isAccessible = true }
}
