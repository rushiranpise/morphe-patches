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

// UpgradeRepoGplay$Info.<init>(BillingData, Throwable, int)V   [classes.dex]
//
// The Info data class constructor computes isPro:Z from the purchase list:
//
//   this.isPro = (!upgrades.isEmpty() || gracePeriod)
//
// Smali verified (1.7.5-rc0, .registers 21, single constructor):
//   :L19  const/4 v3, 0            ← isEmpty=true AND gracePeriod=false
//   :L20  const/4 v3, 1
//   :L21  iput-boolean v3, v0, ->isPro:Z   ← smali line 443
//   ...rest of constructor (upgradedAt)...
//   return-void
//
// NOTE: constructor uses .registers 21 so `this` is moved to v0 early:
//   move-object/from16 v0, p0
// Injecting via const/4 + iput-boolean into v0 at end of method is safe.
//
// Stable anchor: non-obfuscated app-owned definingClass + name + full parameter list.
// Only one constructor on this class; no filter needed.
private val UpgradeInfoConstructorFingerprint = Fingerprint(
    definingClass = "Leu/darken/sdmse/common/upgrade/core/UpgradeRepoGplay\$Info;",
    name = "<init>",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Leu/darken/sdmse/common/upgrade/core/billing/BillingData;",
        "Ljava/lang/Throwable;",
        "I",
    ),
)

// RangesKt.isPro(UpgradeRepo, Continuation)Object   [classes.dex]
//
// Kotlin coroutine extension function, relocated to kotlin.ranges.RangesKt by R8.
// After awaiting the first emission from UpgradeRepoGplay.upgradeInfo, reads
// Info.isPro and boxes it as Boolean:
//
//   iget-boolean p0, p1, ...UpgradeRepoGplay$Info;->isPro:Z
//   invoke-static { p0 }, Ljava/lang/Boolean;->valueOf(Z)
//   return-object p0
//
// Fix: return Boolean.TRUE at method entry — short-circuits the coroutine state
// machine and bypasses the billing flow read entirely.
//
// Stable anchor: non-obfuscated app-owned UpgradeRepo type in the parameter list.
// SD Maid SE uses plain Continuation (unlike BlueMusic which uses ContinuationImpl).
private val IsProSuspendFingerprint = Fingerprint(
    definingClass = "Lkotlin/ranges/RangesKt;",
    name = "isPro",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = listOf(
        "Leu/darken/sdmse/common/upgrade/UpgradeRepo;",
        "Lkotlin/coroutines/Continuation;",
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
 * We locate the RETURN_VOID, verify the `isPro` write exists, then inject
 * `const/4 v0, 0x1 / iput-boolean v0, p0, ->isPro:Z` immediately before return,
 * overriding whatever the billing logic decided.
 *
 * Note: `this` is in v0 (moved early via `move-object/from16 v0, p0`) so `v0`
 * is safe to reuse for the boolean value here at end-of-method.
 *
 * ### Layer 2 — IsProSuspendFingerprint: short-circuit the isPro() coroutine
 *
 * `RangesKt.isPro()` awaits `upgradeInfo` and then reads `Info.isPro`. Returning
 * `Boolean.TRUE` at entry skips the coroutine entirely.
 *
 * ### Layer 3 — classDefForEach IGET scan: replace all cached field reads
 *
 * Every compiled method that reads `UpgradeRepoGplay$Info.isPro` via IGET_BOOLEAN
 * gets the load replaced with `const/4 vREG, 0x1`. This covers the 28 call-sites
 * across MainViewModel, Swiper, FlowShell, IPCFunnel, SAFSetupCardVH, etc.
 * At least one such site must exist or the patch throws to surface stale fingerprints.
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
