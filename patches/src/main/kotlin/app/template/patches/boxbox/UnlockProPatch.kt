package app.template.patches.boxbox

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BOXBOX_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

/**
 * Unlocks Box Box Pro by forcing both halves of its RevenueCat + DataStore
 * persisted-subscription-state pipeline (see Fingerprints.kt for the full
 * architecture writeup):
 *   1. The DataStore plan writer always persists Pro, regardless of what
 *      RevenueCat/the caller actually passed in.
 *   2. The DataStore plan reader (used by the FirebaseMessagingService
 *      coroutine to gate Pro features) always sees Pro, regardless of what
 *      was previously persisted — necessary because a stale Free value may
 *      already be on disk from before the device was patched.
 *
 * Neither the plan enum's type name nor its Pro constant's field name is
 * hardcoded anywhere here: both are obfuscated and have already been
 * observed to change between versions. Instead, the enum type is read off
 * the writer method's own first parameter, and the Pro constant is read off
 * the reader method's own "compare the unwrapped DataStore value against
 * Pro" instruction — both discovered fresh every run.
 */
@Suppress("unused")
val boxBoxUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Box Box Pro",
    default = true,
) {
    compatibleWith(BOXBOX_COMPATIBILITY)

    execute {
        // The Free/Pro plan enum's type, read off the writer's own first
        // parameter rather than hardcoded.
        val planEnumType = PlanDataStoreWriterFingerprint.originalMethod.parameterTypes[0].toString()

        // Within the reader coroutine, find the check-cast that unwraps the
        // DataStore read result to the plan enum type, then the sget-object
        // immediately following it — the app's own "is this Pro?" comparison
        // value. Reusing that field reference verbatim means the Pro
        // constant's (obfuscated) field name never needs to be hardcoded.
        val readerMethod = PlanDataStoreReaderFingerprint.method
        val readerInstructions = readerMethod.implementation!!.instructions.toList()

        val checkCastIndex = readerInstructions.indexOfFirst { instruction ->
            instruction.opcode == Opcode.CHECK_CAST &&
                (instruction as? ReferenceInstruction)?.reference
                    ?.let { it as? TypeReference }?.type == planEnumType
        }

        val proField = readerInstructions
            .subList(checkCastIndex + 1, readerInstructions.size)
            .first { it.opcode == Opcode.SGET_OBJECT }
            .let { (it as ReferenceInstruction).reference as FieldReference }

        // 1. DataStore WRITE intercept — force every persisted plan write to
        //    Pro by overwriting the first parameter (p1) at method entry.
        PlanDataStoreWriterFingerprint.method.addInstructions(
            0,
            "sget-object p1, ${proField.definingClass}->${proField.name}:${proField.type}",
        )

        // 2. DataStore READ intercept — force the unwrapped read result to
        //    Pro immediately after the check-cast, before the Pro comparison
        //    that follows it runs.
        readerMethod.addInstructions(
            checkCastIndex + 1,
            "sget-object v0, ${proField.definingClass}->${proField.name}:${proField.type}",
        )
    }
}
