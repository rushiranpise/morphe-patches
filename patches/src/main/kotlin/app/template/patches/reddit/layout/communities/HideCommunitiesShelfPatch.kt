package app.template.patches.reddit.layout.communities

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.reddit.misc.version.is_2026_16_0_or_greater
import app.template.patches.reddit.misc.version.is_2026_18_0_or_greater
import app.template.patches.reddit.misc.version.versionCheckPatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY

// ─────────────────────────────────────────────────────────────────────────────
// Hide Communities Shelf
//
// Blanks the related/suggested communities composable.  Uses addInstructions
// (not addInstructionsWithLabels) — no labels in the injection means no risk
// of label-table displacement in complex composable methods.
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHideCommunitiesShelfPatch = bytecodePatch(
    name = "Hide Communities Shelf",
    description = "Hides the related or suggested communities shelf in subreddits.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    dependsOn(versionCheckPatch)

    execute {
        fun Fingerprint.blankComposable() {
            runCatching { method.addInstructions(0, "return-void") }
        }

        when {
            is_2026_18_0_or_greater -> CommunityRecommendationSection_2026_18_Fingerprint
            is_2026_16_0_or_greater -> CommunityRecommendationSection_2026_16_Fingerprint
            else -> CommunityRecommendationSectionLegacyFingerprint
        }.blankComposable()

        CommunityRecommendationsComposeMethodFingerprint.blankComposable()
    }
}
