package app.template.patches.bluetoothvolumemanager

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLUETOOTH_VOLUME_MANAGER_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val INFO = "Leu/darken/bluemusic/upgrade/core/UpgradeRepoGplay\$Info;"

/**
 * Unlocks Pro in Bluetooth Volume Manager (BlueMusic).
 *
 * ## 3.5.0 changes vs 3.4.x
 *
 * - `isUpgraded:Z` field renamed to `isPro:Z` — old InfoConstructorFingerprint and
 *   isUpgraded classDefForEach scan both dead.
 * - `UStringsKt.isPro()` coroutine shim (IsProSuspendFingerprint) is gone — R8 inlined it.
 * - Constructor signature changed from `(BillingData, Throwable, int)` to
 *   `(Z, BillingData, Throwable, Z)` — primary constructor now receives gracePeriod as p1.
 *
 * ## Strategy — two-layer, both anchored on stable non-obfuscated names
 *
 * ### Layer 1 — InfoConstructorFingerprint: force isPro=true at construction
 *
 * The primary constructor computes isPro from the purchase list and writes it:
 *   iput-boolean v6, v0, ...Info;->isPro:Z
 *
 * We read v6 (registerA of matched iput-boolean) and inject `const/4 v6, 0x1`
 * immediately before the write. Every Info object is born with isPro=true.
 *
 * ### Layer 2 — classDefForEach IGET_BOOLEAN scan: cover all cached reads
 *
 * Any method that reads Info->isPro:Z via IGET_BOOLEAN gets the field load
 * replaced with `const/4 vREG, 0x1`. This covers UI lambdas and coroutines
 * that hold a live Info reference and read the field directly (21 sites in 3.5.0).
 *
 * Both layers use only non-obfuscated, app-owned class/field names — stable across
 * R8 rebuilds as long as the billing architecture stays the same.
 */
@Suppress("unused")
val bluetoothVolumeManagerPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro upgrade in Bluetooth Volume Manager by forcing isPro=true.",
) {
    compatibleWith(BLUETOOTH_VOLUME_MANAGER_COMPATIBILITY)

    execute {
        // Layer 1: force isPro=true in the Info constructor.
        val iputIndex = InfoConstructorFingerprint.instructionMatches[0].index
        val valueReg = InfoConstructorFingerprint.instructionMatches[0]
            .getInstruction<TwoRegisterInstruction>().registerA
        InfoConstructorFingerprint.method.addInstructions(iputIndex, "const/4 v$valueReg, 0x1")

        // Layer 2: replace every IGET_BOOLEAN of UpgradeRepoGplay$Info.isPro with const true.
        var patchedReads = 0
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    if (instruction.opcode != Opcode.IGET_BOOLEAN) return@forEachIndexed
                    val ref = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                        ?: return@forEachIndexed
                    if (ref.definingClass != INFO ||
                        ref.name != "isPro" ||
                        ref.type != "Z"
                    ) return@forEachIndexed

                    val destReg = (instruction as? TwoRegisterInstruction)?.registerA
                        ?: return@forEachIndexed
                    method.replaceInstruction(index, "const/4 v$destReg, 0x1")
                    patchedReads++
                }
            }
        }

        if (patchedReads == 0) {
            throw PatchException("No UpgradeRepoGplay\$Info.isPro reads found — field may have been renamed again.")
        }
    }
}
