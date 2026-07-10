package app.template.patches.amazon.hiderufus

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMAZON_IN_COMPATIBILITY
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY

/**
 * Hides the Rufus AI shopping assistant tab from Amazon's bottom navigation bar.
 * SavXTabController.isEnabled() is replaced with a direct false return.
 */
@Suppress("unused")
val amazonHideRufusPatch = bytecodePatch(
    name = "Hide Rufus tab",
    description = "Removes the Rufus AI assistant tab from Amazon's bottom navigation bar.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY, AMAZON_IN_COMPATIBILITY)

    execute {
        SavXTabControllerIsEnabledFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )
    }
}
