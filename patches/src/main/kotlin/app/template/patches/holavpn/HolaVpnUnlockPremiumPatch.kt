package app.template.patches.holavpn

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import app.template.patches.shared.Constants.HOLAVPN_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val holaVpnUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium"
) {
    compatibleWith(HOLAVPN_COMPATIBILITY)

    execute {
        // 1. Find the field in Lorg/hola/H; that is initialized with "premium_user"
        val hClass = classDefBy("Lorg/hola/H;")
        val clinit = hClass.methods.first { it.name == "<clinit>" }
        var premiumUserField: String? = null
        
        val instructions = clinit.implementation?.instructions?.toList() ?: emptyList()
        instructions.forEachIndexed { index, instruction ->
            if (instruction.opcode == Opcode.CONST_STRING) {
                val refStr = (instruction as ReferenceInstruction).reference.toString()
                if (refStr.contains("premium_user")) {
                    // Find the next SPUT_OBJECT
                    for (i in index + 1 until instructions.size) {
                        val nextInst = instructions[i]
                        if (nextInst.opcode == Opcode.SPUT_OBJECT) {
                            val sputRef = (nextInst as ReferenceInstruction).reference.toString()
                            premiumUserField = sputRef.substringAfter("->").substringBefore(":")
                            break
                        }
                    }
                }
            }
        }
        
        if (premiumUserField == null) {
            error("Could not find premium_user field in org.hola.H")
        }

        // 2. Find the method in Lorg/hola/util; that accesses this field
        val utilClass = mutableClassDefBy("Lorg/hola/util;")
        val k3Method = utilClass.methods.firstOrNull { method ->
            method.returnType == "Z" && method.parameterTypes == listOf("Lorg/hola/H;") &&
            method.implementation?.instructions?.any { inst ->
                if (inst.opcode == Opcode.SGET_OBJECT) {
                    val refStr = (inst as ReferenceInstruction).reference.toString()
                    refStr.contains("->$premiumUserField:")
                } else false
            } == true
        }

        if (k3Method == null) {
            error("Could not find premium_user check method in org.hola.util")
        }

        k3Method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // 3. Patch JSON is_premium parsing in Lorg/hola/H0;
        val h0Class = mutableClassDefByOrNull("Lorg/hola/H0;")
        val isPremiumParserMethod = h0Class?.methods?.firstOrNull { method ->
            method.parameterTypes == listOf("Lorg/json/JSONObject;") && method.returnType == "Z" &&
            method.implementation?.instructions?.any { inst ->
                inst.opcode == Opcode.CONST_STRING && 
                (inst as ReferenceInstruction).reference.toString().contains("is_premium")
            } == true
        }
        
        isPremiumParserMethod?.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
    }
}
