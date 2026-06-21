package app.template.patches.inscodeautoclicker

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.INSCODE_AUTOCLICKER_COMPATIBILITY

@Suppress("unused")
val inscodeAutoclickerForcePremiumPatch = bytecodePatch(
    name = "Unlock Premium ",
    description = "Unlocks premium in Clickmate",
    default = true
) {
    compatibleWith(INSCODE_AUTOCLICKER_COMPATIBILITY)

    execute {
        val method = HasActivePurchaseFingerprint.method
        val lastIndex = method.implementation!!.instructions.size - 1
        method.replaceInstruction(lastIndex - 1, "const/4 v0, 0x1")
    }
}