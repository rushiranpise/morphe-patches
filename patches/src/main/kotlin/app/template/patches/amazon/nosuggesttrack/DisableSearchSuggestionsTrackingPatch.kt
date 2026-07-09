package app.template.patches.amazon.nosuggesttrack

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AMAZON_IN_COMPATIBILITY
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY

@Suppress("unused")
val amazonDisableSearchSuggestionsTrackingPatch = bytecodePatch(
    name = "Disable search suggestions tracking",
    description = "Prevents search keypress and focus events from being sent with suggestion requests.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY, AMAZON_IN_COMPATIBILITY)

    execute {
        // return p0 (builder) without setting event - .locals 0
        SearchSuggestionsSetEventFingerprint.method.addInstructions(
            0,
            "return-object p0",
        )
    }
}
