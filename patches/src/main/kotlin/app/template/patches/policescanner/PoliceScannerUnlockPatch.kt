package app.template.patches.policescanner

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.POLICESCANNER_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

/**
 * Unlocks Police Scanner — Fire & Radio (police.scanner.radio.broadcastify.citizen).
 *
 * ## Entitlement model
 *
 * Premium state is persisted in Room DB "premium_status" via I9/l (PremiumStatus):
 *   b:Z = entitled, a:I = id
 *
 * The UI observes a LiveData backed by I9/k$a.call() (the Room Callable).
 * On startup: iap/b creates an initial in-memory I9/l then checks billing.
 * After billing: iap/a creates a new I9/l and writes it to the DB.
 * The LiveData emits from the DB, so both paths must be patched.
 *
 * ## Layers
 *
 * **Layer 1 — H9/p.b(Purchase)Z**
 * Forces every purchase to appear unexpired → iap/a writes PremiumStatus(true).
 *
 * **Layer 2 — I9/l.<init>(Z)V**
 * Swaps `iput-boolean p1` → `iput-boolean v0` (v0=1 from id-field above).
 * Covers in-memory creation in iap/a and iap/b.
 * No try-catch involvement → no VerifyError risk.
 *
 * **Layer 3 — I9/k$a.call()Object (Room DB reader)**
 * Replaces instruction 11 "move-result v1" with "const/4 v1, 0x1".
 * This forces the cursor.getInt(entitled) result to 1 (true) while keeping
 * all surrounding try-catch, cursor.close(), and return logic intact.
 * The exception table is untouched → no VerifyError.
 * This is the primary LiveData source the UI observes.
 */
@Suppress("unused")
val policeScannerUnlockPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks premium features in app.",
    default = true,
) {
    compatibleWith(POLICESCANNER_COMPATIBILITY)

    execute {
        // ── Layer 1: purchase expiry ──────────────────────────────────────────
        PurchaseValidityFingerprint
            .match(classDefBy(PurchaseValidityFingerprint.definingClass!!))
            .method
            .apply {
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 2: PremiumStatus constructor ────────────────────────────────
        // Swap iput-boolean p1 (register 1) → iput-boolean v0 (register 0 = 0x1).
        PremiumStatusConstructorFingerprint
            .match(classDefBy(PremiumStatusConstructorFingerprint.definingClass!!))
            .method
            .apply {
                val idx = instructions.indexOfFirst { instr ->
                    instr.opcode == Opcode.IPUT_BOOLEAN &&
                        (instr as? TwoRegisterInstruction)?.registerA == 1 // p1
                }
                if (idx != -1) replaceInstruction(idx, "iput-boolean v0, p0, LI9/l;->b:Z")
            }

        // ── Layer 3: Room DB reader — force entitled=true from cursor ─────────
        // Replace instruction 11 "move-result v1" with "const/4 v1, 0x1".
        // v1 is the raw cursor.getInt(0) value; forcing 1 makes entitled=true
        // without altering try-catch ranges or cursor lifecycle.
        DbReaderCallFingerprint
            .match(classDefBy(DbReaderCallFingerprint.definingClass!!))
            .method
            .apply {
                // Instruction 11 = "move-result v1" after second cursor.getInt call
                replaceInstruction(11, "const/4 v1, 0x1")
            }
    }
}
