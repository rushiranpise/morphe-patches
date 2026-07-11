package app.template.patches.uptodown.subscription

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.UPTODOWN_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Unlock Uptodown Turbo — two fixes:
 *
 * 1. isTurbo() bypass (Le6/w2;->d()Z)
 *    Forces the method to always return true so all UI turbo checks pass.
 *
 * 2. Anti-tamper bypass (TrackingWorker.doWork())
 *    Gate 1: Lx8/v;->m0() compares SHA256 cert hash to hardcoded value.
 *            Replace move-result after m0() with const/4 v, 0x1 → always "matches".
 *    Gate 2: ApplicationInfo.flags & 0x2 (FLAG_DEBUGGABLE) check.
 *            Replace and-int/lit8 with const/4 v, 0x0 → always "not debuggable".
 *    Both gates, on failure, send 0x25a to ResultReceiver g6/g which calls
 *    Process.killProcess(myPid()) after 1000ms.
 */
@Suppress("unused")
val unlockTurboPatch = bytecodePatch(
    name = "Unlock Turbo",
    description = "Unlocks Turbo subscription after login.",
) {
    compatibleWith(UPTODOWN_COMPATIBILITY)

    execute {
        // ── 1. isTurbo() → always true ────────────────────────────────────────
        IsTurboFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // ── 2. Anti-tamper bypass ──────────────────────────────────────────────
        val atMethod = AntiTamperFingerprint.method
        val atInstrs = atMethod.implementation!!.instructions.toList()

        // Gate 1: Lx8/v;->m0(String, String, Z)Z cert comparison → force true
        val m0Idx = atInstrs.indexOfFirst { instr ->
            instr.opcode == Opcode.INVOKE_STATIC &&
                instr is ReferenceInstruction &&
                (instr.reference as? MethodReference)?.name == "m0" &&
                (instr.reference as? MethodReference)?.definingClass == "Lx8/v;"
        }
        require(m0Idx != -1) { "m0 invoke not found in AntiTamperFingerprint method" }
        val reg1 = (atInstrs[m0Idx + 1] as OneRegisterInstruction).registerA
        atMethod.replaceInstruction(m0Idx + 1, "const/4 v$reg1, 0x1")

        // Gate 2: ApplicationInfo.flags & 0x2 (FLAG_DEBUGGABLE) → force 0
        val andIdx = atInstrs.indexOfFirst { instr ->
            instr.opcode == Opcode.AND_INT_LIT8 &&
                instr is NarrowLiteralInstruction &&
                instr.narrowLiteral == 0x2
        }
        if (andIdx != -1) {
            val reg2 = (atInstrs[andIdx] as OneRegisterInstruction).registerA
            atMethod.replaceInstruction(andIdx, "const/4 v$reg2, 0x0")
        }
    }
}
