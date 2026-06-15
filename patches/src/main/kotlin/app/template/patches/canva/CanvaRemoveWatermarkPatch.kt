package app.template.patches.canva

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CANVA_COMPATIBILITY

/**
 * Forces all proto DTO getWatermarked()Z getters to return false,
 * removing watermarks from exported/previewed media.
 */
@Suppress("unused")
val canvaRemoveWatermarkPatch = bytecodePatch(
    name = "Remove Watermark",
    description = "Removes watermarks from Canva exports and previews.",
    default = true
) {
    compatibleWith(CANVA_COMPATIBILITY)

    execute {
        listOf(
            VideoFile2WatermarkedFingerprint,
            DashVideoFileWatermarkedFingerprint,
            DashVideoFileReferenceWatermarkedFingerprint,
            ImageFileReferenceWatermarkedFingerprint,
            VideoFileReferenceWatermarkedFingerprint,
            MediaFileWatermarkedFingerprint
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