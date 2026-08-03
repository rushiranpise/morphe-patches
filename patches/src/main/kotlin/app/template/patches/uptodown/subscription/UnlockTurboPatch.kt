package app.template.patches.uptodown.subscription

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.UPTODOWN_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Unlock Uptodown Turbo — bypasses three integrity checks and the isTurbo gate.
 *
 * ── 1. isTurbo() bypass (Le6/w2;->d()Z) ─────────────────────────────────────
 *    Injects const/4 v0, 0x1 / return v0 at offset 0, causing the method to
 *    always return true regardless of turboToken or the x field value.
 *
 * ── 2. TrackingWorker.doWork() anti-tamper bypass ────────────────────────────
 *    doWork() runs four sequential integrity gates; any failure sends result
 *    code 0x25a to the global ResultReceiver (g6/g), which schedules
 *    Process.killProcess(myPid()) after 1 000 ms.
 *
 *    Gate A — SHA-256 cert-hash comparison
 *      invoke-static {v8, v9, v6}, Lx8/v;->t0(String, String, Z)Z
 *      move-result v8          ← replaced: const/4 v8, 0x1 (always "matched")
 *      if-nez v8, :cond_8      ← falls through to next gate
 *
 *    Gate B — ApplicationInfo.flags & FLAG_DEBUGGABLE (0x2)
 *      and-int/lit8 v8, v8, 0x2
 *      ^-- replaced: const/4 v8, 0x0 (always "not debuggable")
 *
 *    Gate C — Debug.isDebuggerConnected() [added in 7.37]
 *      invoke-static {}, Landroid/os/Debug;->isDebuggerConnected()Z
 *      move-result v8          ← replaced: const/4 v8, 0x0 (always "not connected")
 *      if-eqz v8, :cond_a
 *
 *    Gate D — Debug.waitingForDebugger() [added in 7.37]
 *      invoke-static {}, Landroid/os/Debug;->waitingForDebugger()Z
 *      move-result v8          ← replaced: const/4 v8, 0x0 (always "not waiting")
 *      if-eqz v8, :cond_b
 *
 *    All four replacements use a single forward scan; later-indexed gates are
 *    patched first (reverse-order rule) to keep earlier indices valid.
 *
 * Verified against smali: smali_classes4/com/uptodown/workers/TrackingWorker.smali
 * Verified against smali: smali_classes4/e6/w2.smali
 */
@Suppress("unused")
val unlockTurboPatch = bytecodePatch(
    name = "Unlock Turbo",
    description = "Unlocks Turbo subscription and bypasses cert/debug integrity checks.",
) {
    compatibleWith(UPTODOWN_COMPATIBILITY)

    execute {

        // ── 1. isTurbo() → always true ────────────────────────────────────────
        IsTurboFingerprint.method.returnEarly(true)

        // ── 2. Anti-tamper bypass (TrackingWorker.doWork) ─────────────────────
        val method = AntiTamperFingerprint.method
        val instructions = method.implementation!!.instructions.toList()

        // Gate A: find move-result immediately after Lx8/v;->t0() cert comparison.
        // Replacing the move-result forces the result to 1 (match) unconditionally.
        val t0Idx = instructions.indexOfFirst { instr ->
            instr.opcode == Opcode.INVOKE_STATIC &&
                instr is ReferenceInstruction &&
                (instr.reference as? MethodReference)?.name == "t0" &&
                (instr.reference as? MethodReference)?.definingClass == "Lx8/v;"
        }
        require(t0Idx != -1) { "Gate A: Lx8/v;->t0() invocation not found in AntiTamperFingerprint method" }
        val regA = (instructions[t0Idx + 1] as OneRegisterInstruction).registerA

        // Gate B: and-int/lit8 with literal 0x2 (FLAG_DEBUGGABLE).
        // Replacing the whole instruction forces the masked value to 0 (not debuggable).
        val andIdx = instructions.indexOfFirst { instr ->
            instr.opcode == Opcode.AND_INT_LIT8 &&
                instr is NarrowLiteralInstruction &&
                instr.narrowLiteral == 0x2
        }
        require(andIdx != -1) { "Gate B: and-int/lit8 0x2 not found in AntiTamperFingerprint method" }
        val regB = (instructions[andIdx] as OneRegisterInstruction).registerA

        // Gate C: move-result immediately after Debug.isDebuggerConnected().
        val isDebuggerIdx = instructions.indexOfFirst { instr ->
            instr.opcode == Opcode.INVOKE_STATIC &&
                instr is ReferenceInstruction &&
                (instr.reference as? MethodReference)?.name == "isDebuggerConnected" &&
                (instr.reference as? MethodReference)?.definingClass == "Landroid/os/Debug;"
        }
        require(isDebuggerIdx != -1) { "Gate C: Debug.isDebuggerConnected() not found in AntiTamperFingerprint method" }
        val regC = (instructions[isDebuggerIdx + 1] as OneRegisterInstruction).registerA

        // Gate D: move-result immediately after Debug.waitingForDebugger().
        val waitingIdx = instructions.indexOfFirst { instr ->
            instr.opcode == Opcode.INVOKE_STATIC &&
                instr is ReferenceInstruction &&
                (instr.reference as? MethodReference)?.name == "waitingForDebugger" &&
                (instr.reference as? MethodReference)?.definingClass == "Landroid/os/Debug;"
        }
        require(waitingIdx != -1) { "Gate D: Debug.waitingForDebugger() not found in AntiTamperFingerprint method" }
        val regD = (instructions[waitingIdx + 1] as OneRegisterInstruction).registerA

        // Apply patches in reverse index order to keep earlier indices valid.
        val patchTargets = listOf(
            waitingIdx + 1 to "const/4 v$regD, 0x0",  // Gate D — highest index first
            isDebuggerIdx + 1 to "const/4 v$regC, 0x0", // Gate C
            andIdx to "const/4 v$regB, 0x0",             // Gate B (replace whole and-int)
            t0Idx + 1 to "const/4 v$regA, 0x1",          // Gate A — lowest index last
        ).sortedByDescending { it.first }

        patchTargets.forEach { (idx, smali) ->
            method.replaceInstruction(idx, smali)
        }
    }
}
