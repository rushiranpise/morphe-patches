package app.template.patches.canva

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CANVA_COMPATIBILITY

/**
 * Forces VideoProto.Video / MediaProto.Media getRestrictedAccess()Z
 * to always return false, removing locked/paywalled UI on premium media.
 */
@Suppress("unused")
val canvaRemoveRestrictedAccessPatch = bytecodePatch(
    name = "Disable Restricted Access",
    description = "Removes restricted/locked indicators on premium media in Canva.",
    default = true
) {
    compatibleWith(CANVA_COMPATIBILITY)

    execute {
        listOf(
            VideoRestrictedAccessFingerprint,
            MediaRestrictedAccessFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(
                        0,
                        """
                        const/4 v0, 0x0
                        return v0
                        """.trimIndent()
                    )
                }
        }
    }
}