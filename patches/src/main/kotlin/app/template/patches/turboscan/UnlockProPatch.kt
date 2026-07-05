package app.template.patches.turboscan

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TURBOSCAN_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode

@Suppress("unused")
val turboScanUnlockProPatch = bytecodePatch(
    name = "Unlock Pro features",
    description = "Unlock Pro features in app.",
    default = true,
) {
    compatibleWith(TURBOSCAN_COMPATIBILITY)

    execute {
        DocLimitCheckFingerprint.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        PageLimitCheckFingerprint.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        UpgradeGateFingerprint.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // Nop both CRC if-ne integrity checks in onCreate, fix div-by-zero
        OnCreateIntegrityCheckFingerprint.method.let { method ->
            val instrs = method.implementation!!.instructions.toList()

            // Find all IF_NE (not IF_NEZ) — only 2 exist, both are integrity checks
            instrs.indices
                .filter { instrs[it].opcode == Opcode.IF_NE }
                .forEach { idx -> method.replaceInstruction(idx, "nop") }

            // Fix div-by-zero in crash path
            val divIdx = instrs.indexOfFirst { it.opcode == Opcode.DIV_INT_2ADDR }
            if (divIdx != -1) method.replaceInstruction(divIdx, "const/4 v7, 0x1")
        }
    }
}
