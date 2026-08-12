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

/**
 * Unlocks the Pro upgrade in Bluetooth Volume Manager (BlueMusic).
 *
 * ## Strategy — three-layer defence
 *
 * ### Layer 1 — InfoConstructorFingerprint: force isUpgraded=true at construction
 *
 * `UpgradeRepoGplay$Info.<init>` computes `isUpgraded` from the purchase list:
 *
 * ```smali
 * :L18  move v1, v2              ← v1=0 if no upgrades and no grace period
 * :L19  iput-boolean v1, p0, ->isUpgraded:Z
 * ```
 *
 * We read the value register (registerA of the matched iput-boolean) at patch time
 * and inject `const/4 vREG, 0x1` immediately before it. This forces `isUpgraded=true`
 * into every `Info` object, regardless of what billing returned.
 *
 * ### Layer 2 — IsProSuspendFingerprint: short-circuit the isPro coroutine
 *
 * `UStringsKt.isPro()` awaits the first emission of `UpgradeRepoGplay.upgradeInfo`
 * and then reads `Info.isUpgraded`. Returning `Boolean.TRUE` at entry skips the
 * entire coroutine state machine — no billing flow is ever consumed.
 *
 * ### Layer 3 — classDefForEach IGET scan: replace all cached reads
 *
 * Any compiled method that reads `UpgradeRepoGplay$Info.isUpgraded` via IGET_BOOLEAN
 * gets the field load replaced with `const/4 vREG, 0x1`. This covers UI lambdas and
 * other coroutines that hold a live `Info` reference and read the field directly.
 *
 * ## SKUs (orientation only)
 *   - IAP: `upgrade.premium.rewrite.v3` (current), `upgrade.premium` (legacy)
 *   - Sub: `upgrade.pro`
 */
@Suppress("unused")
val bluetoothVolumeManagerPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro upgrade in Bluetooth Volume Manager by forcing isUpgraded=true.",
) {
    compatibleWith(BLUETOOTH_VOLUME_MANAGER_COMPATIBILITY)

    execute {
        // Layer 1: force isUpgraded=true in the Info constructor.
        val iputIndex = InfoConstructorFingerprint.instructionMatches[0].index
        val valueReg = InfoConstructorFingerprint.instructionMatches[0]
            .getInstruction<TwoRegisterInstruction>().registerA
        InfoConstructorFingerprint.method.addInstructions(iputIndex, "const/4 v$valueReg, 0x1")

        // Layer 2: short-circuit the isPro() coroutine entry point.
        IsProSuspendFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        // Layer 3: replace every IGET_BOOLEAN of UpgradeRepoGplay$Info.isUpgraded with const true.
        var patchedReads = 0
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    if (instruction.opcode != Opcode.IGET_BOOLEAN) return@forEachIndexed
                    val ref = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                        ?: return@forEachIndexed
                    if (ref.definingClass != "Leu/darken/bluemusic/upgrade/core/UpgradeRepoGplay\$Info;" ||
                        ref.name != "isUpgraded" ||
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
            throw PatchException("No UpgradeRepoGplay\$Info.isUpgraded reads found — fingerprint may be stale.")
        }
    }
}
