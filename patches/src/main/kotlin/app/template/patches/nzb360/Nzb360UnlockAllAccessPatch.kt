package app.template.patches.nzb360

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NZB360_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c

/**
 * Unlock nzb360 All Access (v23.4)
 *
 * Forces [isAASubscriptionActive] and [isUnlocked] to return true,
 * and both [isLocked] overloads to return false, bypassing all
 * per-service module paywalls (SABnzbd, Torrents, Radarr, Sonarr, etc.).
 *
 * Also pre-selects the Yearly plan in Settings → Upgrade Center.
 */
@Suppress("unused")
val nzb360UnlockAllAccessPatch = bytecodePatch(
    name = "Unlock All Access",
    description = "Unlocks All access in Nzb360.",
    default = true
) {
    compatibleWith(NZB360_COMPATIBILITY)

    execute {

        fun forceBoolean(value: Boolean, vararg fps: Fingerprint) {
            val body = if (value) "const/4 v0, 0x1\nreturn v0" else "const/4 v0, 0x0\nreturn v0"
            fps.forEach { fp ->
                runCatching { fp.match(classDefBy(fp.definingClass!!)).method }.getOrNull()?.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, body)
                }
            }
        }

        forceBoolean(true, IsAASubscriptionActiveFingerprint, IsUnlockedFingerprint, IsSubscribedFingerprint)
        forceBoolean(false, IsLockedTwoArgFingerprint, IsLockedOneArgFingerprint)

        // Default Upgrade Center to Yearly plan
        runCatching {
            val method = SubscriptionSectionDefaultPlanFingerprint
                .match(classDefBy(SubscriptionSectionDefaultPlanFingerprint.definingClass!!))
                .method

            val idx = method.instructions.indexOfFirst {
                it.opcode == Opcode.CONST_STRING &&
                    (it as Instruction21c).reference.toString() == "Monthly"
            }

            if (idx >= 0) {
                val reg = (method.instructions[idx] as Instruction21c).registerA
                method.replaceInstruction(idx, "const-string v$reg, \"Yearly\"")
            }
        }
    }
}