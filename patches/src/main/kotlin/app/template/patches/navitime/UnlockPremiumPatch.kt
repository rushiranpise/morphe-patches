package app.template.patches.navitime

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NAVITIME_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val navitimeUnlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks NAVITIME Japan Travel Premium features in app",
    default = true,
) {
    compatibleWith(NAVITIME_COMPATIBILITY)

    execute {
        // DataManager.updateBillingStatus(OffsetDateTime, String)V
        //
        // Logic before patch:
        //   cmp-long p1, p1, v2      // p1 = between(currentTime, periodTime) > 0 ? 0 : 1
        //   if-gtz p1, :cond_0       // if seconds remaining > 0 → isFree=0 (paid)
        //   goto :goto_0             // else → isFree=1 (free, falls to v1=0x1)
        //   :cond_0 const/4 p1, 0x0  // isFree = 0 (paid path)
        //   :goto_1                  // merge
        //   setBoolean("PREF_BILLING_STATUS_IS_FREE", p1)
        //
        // After patch: inject const/4 p1, 0x0 right before setBoolean() call.
        // This forces isFree=false (premium) regardless of the date comparison above.
        // p1 is the register written by the branch and used by setBoolean().

        UpdateBillingStatusFingerprint.method.apply {
            val instructions = implementation!!.instructions.toList()

            // Find the setBoolean() call index
            val setBooleanIndex = instructions.indexOfFirst { instr ->
                instr.opcode == Opcode.INVOKE_VIRTUAL &&
                    (instr as? ReferenceInstruction)?.reference
                        .let { it as? MethodReference }
                        ?.name == "setBoolean"
            }

            // Inject const/4 p1, 0x0 right before setBoolean
            // p1 is the isFree register (parameter 1 of this method reused as the boolean result)
            addInstructions(
                setBooleanIndex,
                "const/4 p1, 0x0",
            )
        }
    }
}
