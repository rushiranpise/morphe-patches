package app.template.patches.wallverse.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WALLVERSE_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c

@Suppress("unused")
val wallverseUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks lifetime Premium in Wallverse.",
    default = true,
) {
    compatibleWith(WALLVERSE_COMPATIBILITY)

    execute {

        // Application.attachBaseContext calls this before WallverseApp starts.
        // Returning immediately preserves the original application initialization.
        PairipCheckLicenseFingerprint.method.addInstructions(0, "return-void")
        
        // Force the isPremium boolean to always be true right before it is
        // boxed by Boolean.valueOf(Z). This is the single choke point
        // consumed by every premium check in the app (see Fingerprints.kt).
        //
        // The call site is a goto/if-eqz join point (":cond_x"/":goto_x" both
        // land directly on the invoke-static Boolean.valueOf(Z) instruction).
        // A plain addInstructions() before that index leaves the original
        // control-flow label attached to the now-shifted invoke-static
        // instruction, so incoming jumps skip straight past our injected
        // code and the override never takes effect for unpurchased users
        // (confirmed by decompiling the previously built/installed APK).
        //
        // Fix: duplicate the invoke-static instruction one slot later, insert
        // our override at the ORIGINAL index (which keeps the original
        // control-flow label), then delete the now-duplicated instruction.
        // This moves the label onto our injected code so every incoming
        // branch executes the override first, matching the technique used
        // by Morphe's addInstructionsAtControlFlowLabel() helper.
        val method = WallverseIsPremiumFingerprint.method
        val callIndex = WallverseIsPremiumFingerprint.instructionMatches[1].index
        val reg = method.getInstruction<Instruction35c>(callIndex).registerC

        method.addInstruction(callIndex + 1, method.getInstruction(callIndex))
        method.addInstructionsWithLabels(callIndex + 1, "const/4 v$reg, 0x1")
        method.removeInstruction(callIndex)
    }
}
