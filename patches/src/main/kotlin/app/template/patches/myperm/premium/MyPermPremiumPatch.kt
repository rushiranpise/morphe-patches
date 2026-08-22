package app.template.patches.myperm.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_MYPERM
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * Unlocks Pro in Permission Pilot (eu.darken.myperm).
 *
 * ## Architecture
 *
 * Permission Pilot uses the same darken/UpgradeRepoGplay pattern as Bluetooth Volume Manager.
 * `UpgradeRepoGplay$Info` is an immutable data class constructed once per billing event with:
 *
 *   isPro = !upgrades.isEmpty() || gracePeriod
 *
 * `UpgradeRepoExtensionsKt.isProForUi` is a suspend extension that awaits the upgradeInfo
 * StateFlow and reads Info.isPro. All UI and feature gates consume this flow.
 *
 * ## Strategy — three-layer defence
 *
 * ### Layer 1 — InfoConstructorFingerprint: force isPro=true at construction
 *
 * Find the iput-boolean isPro instruction via the fingerprint match, read its value register,
 * and inject `const/4 vREG, 0x1` immediately before it. Every Info object is born with isPro=true.
 *
 * ### Layer 2 — IsProForUiFingerprint: short-circuit the suspend coroutine
 *
 * `isProForUi` awaits the upgradeInfo flow then reads Info.isPro. Returning Boolean.TRUE
 * at entry skips the entire coroutine state machine — no billing flow is consumed.
 * (The function already has a catch-all that fails open, but this is cleaner.)
 *
 * ### Layer 3 — classDefForEach IGET scan: replace all cached field reads
 *
 * Any method holding a live Info reference and reading isPro via IGET_BOOLEAN gets
 * the field load replaced with `const/4 vREG, 0x1`. This covers Compose lambdas,
 * AppRepo coroutines, ExportViewModel, and other observers.
 */
@Suppress("unused")
val myPermPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Permission Pilot Pro by forcing isPro=true in the billing Info object.",
) {
    compatibleWith(COMPATIBILITY_MYPERM)

    execute {
        // Layer 1: force isPro=true in the Info constructor.
        // The iput-boolean isPro is the second filter match (index 1).
        // Its registerA holds the computed boolean value (v6 in this version).
        val iputIndex = InfoConstructorFingerprint.instructionMatches[1].index
        val valueReg = InfoConstructorFingerprint.instructionMatches[1]
            .getInstruction<TwoRegisterInstruction>().registerA
        InfoConstructorFingerprint.method.addInstructions(iputIndex, "const/4 v$valueReg, 0x1")

        // Layer 2: short-circuit the isProForUi suspend coroutine.
        // Return Boolean.TRUE immediately — no billing flow consumed.
        IsProForUiFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        // Layer 3: replace every IGET_BOOLEAN of UpgradeRepoGplay$Info.isPro with const true.
        // Covers all observers holding a live Info reference: ExportViewModel,
        // AppRepo coroutines, Compose lambdas, and kotlinx.coroutines internals.
        var patchedReads = 0
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    if (instruction.opcode != Opcode.IGET_BOOLEAN) return@forEachIndexed
                    val ref = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                        ?: return@forEachIndexed
                    if (ref.definingClass != "Leu/darken/myperm/common/upgrade/core/UpgradeRepoGplay\$Info;" ||
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
            throw PatchException("No UpgradeRepoGplay\$Info.isPro reads found — fingerprint may be stale.")
        }
    }
}
