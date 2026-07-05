package app.template.patches.kinestop.pro

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants

// KineStop uses a simple pref-based trial check:
//   KineService.Companion.isTrial(Context):
//     reads pref "u"; returns false (full) only when value == 42 (0x2a).
//     Patching it to always return false (0) unlocks full/pro features.
@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
    description = "Unlocks all pro features.",
    default = true,
) {
    compatibleWith(Constants.KINESTOP_COMPATIBILITY)

    execute {
        IsTrialFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x0
                return p0
            """,
        )
    }
}
