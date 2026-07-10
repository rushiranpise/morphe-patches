package app.template.patches.life360

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import java.lang.reflect.Field

internal fun MutableMethod.ensureRegisters(needed: Int) {
    val impl = implementation ?: return
    if (impl.registerCount >= needed) return
    val field = registerCountField
        ?: throw PatchException("MutableMethodImplementation: no int field for registerCount")
    field.setInt(impl, needed)
}

private val registerCountField: Field? =
    MutableMethodImplementation::class.java.declaredFields
        .firstOrNull { it.type == Int::class.javaPrimitiveType }
        ?.apply { isAccessible = true }
