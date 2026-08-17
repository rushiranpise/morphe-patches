package app.template.patches.sdmaidse

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SD_MAID_SE_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// UpgradeRepoGplay$Info.<init>(Z, BillingData, Throwable, Z)V   [classes.dex]
//
// The Info data class constructor computes isPro:Z from the purchase/grace-period check:
//
//   this.isPro = (!upgrades.isEmpty() || gracePeriod)
//
// Smali verified (2.0.2-rc0, .registers 22, real constructor):
//   :L15   const/4 v2, 0
//   :L16   const/4 v2, 1
//   :L17
//           iput-boolean v2, v0, ...UpgradeRepoGplay$Info;->isPro:Z   ← line 425
//   ...
//           iput-object v4, v0, ...->upgradedAt:Ljava/time/Instant;
//           return-void                                                  ← line 555
//
// v0 = this (moved from p0 early via move-object/from16 v0, p0).
// v1 is a free scratch register at end-of-method.
// Injection: const/4 v1, 0x1 / iput-boolean v1, v0, ->isPro:Z immediately before return-void.
//
// NOTE: constructor parameter order changed between 1.7.5-rc0 and 2.0.2-rc0:
//   1.7.5-rc0:  (BillingData, Throwable, int)
//   2.0.2-rc0:  (Z, BillingData, Throwable, Z)   ← gracePeriod moved to p1, isSettled added as p4
//
// Anchor: non-obfuscated app-owned definingClass + name + full non-obfuscated parameter list.
// Only one real (non-synthetic) constructor on this class; no filter needed.
private val UpgradeInfoConstructorFingerprint = Fingerprint(
    definingClass = "Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay\$Info;",
    name = "<init>",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Z",
        "Leu/darken/sdmse/common/upgrade/core/billing/BillingData;",
        "Ljava/lang/Throwable;",
        "Z",
    ),
)

// UpgradeRepoExtensionsKt.isPro(UpgradeRepoGplay, ContinuationImpl)Object   [classes.dex]
//
// Kotlin coroutine extension function for isPro, compiled into the non-obfuscated
// UpgradeRepoExtensionsKt class (moved from RangesKt in 1.7.5-rc0).
//
// After awaiting the first emission from UpgradeRepoGplay.upgradeInfo, reads
// Info.isPro and boxes it as Boolean:
//
//   iget-boolean p0, p1, ...UpgradeRepoGplay$Info;->isPro:Z
//   invoke-static { p0 }, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
//   return-object p0
//
// Fix: return Boolean.TRUE at method entry — short-circuits the coroutine state
// machine and bypasses the billing flow read entirely.
//
// Anchor: FULLY non-obfuscated — app-owned class (eu.darken.*), app-owned UpgradeRepoGplay
// receiver, and kotlin.coroutines.jvm.internal.ContinuationImpl (stable SDK class).
// This fingerprint will NOT break on R8 re-obfuscation because none of these names
// are short R8-generated identifiers.
private val IsProSuspendFingerprint = Fingerprint(
    definingClass = "Leu/darken/sdmse/common/upgrade/UpgradeRepoExtensionsKt;",
    name = "isPro",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = listOf(
        "Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay;",
        "Lkotlin/coroutines/jvm/internal/ContinuationImpl;",
    ),
)

/**
 * Unlocks SD Maid SE Pro features.
 *
 * ## Strategy — three-layer defence
 *
 * ### Layer 1 — UpgradeInfoConstructorFingerprint: force isPro=true at construction
 *
 * `UpgradeRepoGplay$Info.<init>` writes `isPro` near the end of the constructor.
 * We verify the `isPro` write exists, then inject
 * `const/4 v1, 0x1 / iput-boolean v1, v0, ->isPro:Z` immediately before RETURN_VOID,
 * overriding whatever the billing logic decided.
 *
 * Note: `this` is in v0 (moved early via `move-object/from16 v0, p0`); v1 is free
 * scratch at end-of-method and safe to clobber.
 *
 * ### Layer 2 — IsProSuspendFingerprint: short-circuit the isPro() coroutine
 *
 * `UpgradeRepoExtensionsKt.isPro()` awaits `upgradeInfo` and reads `Info.isPro`.
 * Returning `Boolean.TRUE` at entry skips the coroutine entirely.
 * Both the class and both parameter types are non-obfuscated app/SDK names —
 * this fingerprint is stable across R8 rebuilds.
 *
 * ### Layer 3 — classDefForEach IGET scan: replace all cached field reads
 *
 * Every compiled method that reads `UpgradeRepoGplay$Info.isPro` via IGET_BOOLEAN
 * gets the load replaced with `const/4 vREG, 0x1`. This covers all call-sites
 * across ViewModels, Swipers, Shells, etc.
 * At least one such site must exist or the patch throws to surface stale state.
 */
@Suppress("unused")
val sdMaidSeUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks SD Maid SE Pro features.",
    default = true,
) {
    compatibleWith(SD_MAID_SE_COMPATIBILITY)

    execute {
        // Layer 1: force isPro=true in the Info constructor, just before return-void.
        val infoConstructor = UpgradeInfoConstructorFingerprint.method

        if (!infoConstructor.writesIsProField()) {
            throw PatchException("UpgradeRepoGplay.Info.<init> does not write isPro field — app may have restructured.")
        }

        val returnIndex = infoConstructor.instructionsOrNull
            ?.indexOfLast { it.opcode == Opcode.RETURN_VOID }
            ?.takeIf { it >= 0 }
            ?: throw PatchException("Could not find RETURN_VOID in UpgradeRepoGplay.Info constructor.")

        // v0 = this (moved from p0 at method entry); v1 = free scratch at end of method.
        infoConstructor.addInstructions(
            returnIndex,
            """
                const/4 v1, 0x1
                iput-boolean v1, v0, Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay${'$'}Info;->isPro:Z
            """.trimIndent(),
        )

        // Layer 2: short-circuit the isPro() coroutine entry point.
        IsProSuspendFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        // Layer 3: replace every IGET_BOOLEAN of UpgradeRepoGplay$Info.isPro with const true.
        var patchedReads = 0
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    if (instruction.opcode != Opcode.IGET_BOOLEAN) return@forEachIndexed
                    val ref = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                        ?: return@forEachIndexed
                    if (ref.definingClass != "Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay\$Info;" ||
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

private fun app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.writesIsProField(): Boolean =
    instructionsOrNull?.any { instruction ->
        val ref = (instruction as? ReferenceInstruction)?.reference as? FieldReference
        ref?.definingClass == "Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay\$Info;" &&
            ref.name == "isPro" &&
            ref.type == "Z"
    } == true
