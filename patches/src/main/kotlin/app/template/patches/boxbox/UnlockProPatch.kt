package app.template.patches.boxbox

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BOXBOX_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val RB8_PRO = "sget-object p1, Lrb8;->b:Lrb8;"

@Suppress("unused")
val boxBoxUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Box Box Pro",
    default = true,
) {
    compatibleWith(BOXBOX_COMPATIBILITY)

    execute {

        // 1. DataStore WRITE intercept — pd6.b(rb8, nd8)
        //    Force p1 = rb8.b (Pro) at method entry before any DataStore write logic.
        //    Covers jv6 emitting Free and any other write path.
        PlanDataStoreWriterFingerprint.method.addInstructions(
            0,
            RB8_PRO,
        )

        // 2. DataStore READ intercept — l0.invokeSuspend
        //    After blb.j() unwraps DataStore coroutine result into v0,
        //    check-cast v0, Lrb8; is the first rb8-typed instruction.
        //    Inject sget-object v0, Lrb8;->b:Lrb8; immediately after check-cast
        //    so the if-ne v0, rb8.b, :cond_62 comparison always takes the Pro branch.
        PlanDataStoreReaderFingerprint.method.apply {
            val instrs = implementation!!.instructions.toList()
            // Find the first check-cast targeting Lrb8;
            val checkCastIndex = instrs.indexOfFirst { instr ->
                instr.opcode == Opcode.CHECK_CAST &&
                    (instr as? ReferenceInstruction)?.reference
                        .let { it as? TypeReference }?.type == "Lrb8;"
            }
            addInstructions(
                checkCastIndex + 1,
                "sget-object v0, Lrb8;->b:Lrb8;",
            )
        }
    }
}
