package app.template.patches.parallelspace

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PARALLELSPACE_COMPATIBILITY

@Suppress("unused")
val parallelSpaceForceProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Pro Features in app.",
    default = true
) {
    compatibleWith(PARALLELSPACE_COMPATIBILITY)

    execute {
        val method = IsProActiveFingerprint.method
        val lastIndex = method.implementation!!.instructions.size - 1

        // return v0 -> const/4 v0, 0x1 ; return v0
        method.replaceInstruction(lastIndex - 1, "const/4 v0, 0x1")
    }
}
