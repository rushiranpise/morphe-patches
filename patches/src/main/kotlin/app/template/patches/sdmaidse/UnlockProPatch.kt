package app.template.patches.sdmaidse

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SD_MAID_SE_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private val UpgradeInfoConstructorFingerprint = Fingerprint(
    definingClass = "Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay\$Info;",
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "Leu/darken/sdmse/common/upgrade/core/billing/BillingData;",
        "Ljava/lang/Throwable;",
        "I",
    ),
)

private val IsProSuspendFingerprint = Fingerprint(
    definingClass = "Lkotlin/ranges/RangesKt;",
    name = "isPro",
    returnType = "Ljava/lang/Object;",
    parameters = listOf(
        "Leu/darken/sdmse/common/upgrade/UpgradeRepo;",
        "Lkotlin/coroutines/Continuation;",
    ),
)

@Suppress("unused")
val sdMaidSeUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks SD Maid SE Pro features.",
    default = true,
) {
    compatibleWith(SD_MAID_SE_COMPATIBILITY)

    execute {
        val infoConstructor = UpgradeInfoConstructorFingerprint.method
        val returnIndex = infoConstructor.instructionsOrNull
            ?.indexOfLast { it.opcode == Opcode.RETURN_VOID }
            ?.takeIf { it >= 0 }
            ?: throw PatchException("Could not find UpgradeRepoGplay.Info constructor return.")

        if (!infoConstructor.writesIsProField()) {
            throw PatchException("Could not verify UpgradeRepoGplay.Info.isPro field write.")
        }

        infoConstructor.addInstructions(
            returnIndex,
            """
                const/4 v1, 0x1
                iput-boolean v1, p0, Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay${'$'}Info;->isPro:Z
            """.trimIndent(),
        )

        IsProSuspendFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        var isProReadsPatched = 0
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    if (instruction.opcode != Opcode.IGET_BOOLEAN) return@forEachIndexed
                    val reference = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                        ?: return@forEachIndexed
                    if (reference.definingClass != "Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay\$Info;" ||
                        reference.name != "isPro" ||
                        reference.type != "Z"
                    ) return@forEachIndexed

                    val targetRegister = (instruction as? TwoRegisterInstruction)?.registerA ?: return@forEachIndexed
                    method.replaceInstruction(index, "const/4 v$targetRegister, 0x1")
                    isProReadsPatched++
                }
            }
        }

        if (isProReadsPatched == 0) {
            throw PatchException("Could not find UpgradeRepoGplay.Info.isPro reads.")
        }
    }
}

private fun app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.writesIsProField(): Boolean =
    instructionsOrNull?.any { instruction ->
        val reference = (instruction as? ReferenceInstruction)?.reference as? FieldReference
        reference?.definingClass == "Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay\$Info;" &&
            reference.name == "isPro" &&
            reference.type == "Z"
    } == true
