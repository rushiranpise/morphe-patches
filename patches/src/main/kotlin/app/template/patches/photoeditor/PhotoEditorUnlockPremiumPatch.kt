package app.template.patches.photoeditor

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PHOTOEDITOR_COMPATIBILITY

@Suppress("unused")
val photoeditorUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Photo Editor premium features by bypassing license checks.",
    default = true
) {
    compatibleWith(PHOTOEDITOR_COMPATIBILITY)

    execute {
        // Patch c81.a(Context)Z — unconditionally return true (licensed)
        // Strategy: Insert const/4 + return at method start (before monitor-enter)
        // This preserves original try-catch structure and exception handlers
        PhotoEditorLicenseCacheCheckFingerprint
            .match(classDefBy(PhotoEditorLicenseCacheCheckFingerprint.definingClass!!))
            .method.apply {
                // Insert at position 0 (very start, before any locks)
                addInstructions(0, "const/4 v0, 0x1")
                addInstructions(1, "return v0")
            }
    }
}
