package app.template.patches.reddit.layout.trending

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.reddit.misc.version.is_2026_11_0_or_greater
import app.template.patches.reddit.misc.version.is_2026_21_0_or_greater
import app.template.patches.reddit.misc.version.versionCheckPatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY

// ─────────────────────────────────────────────────────────────────────────────
// Hide Trending Shelves
//
// Blanks the Trending Today shelf in the feed and trending search rows in
// the search zero-state by return-voiding their composable render methods.
//
// Uses addInstructions (NOT addInstructionsWithLabels) to inject return-void.
// addInstructionsWithLabels at index 0 in large Compose methods (52+ registers,
// existing label table) causes ExceptionWithContext: label not placed — the
// label table gets displaced.  addInstructions inserts a raw opcode with no
// label table interaction.
//
// SearchSectionHeaderFingerprint removed: f.a() is the generic section header
// for ALL search sections (trending, communities, posts, etc.).  Blanking it
// would break all search results.  The trending rows themselves are suppressed
// by TrendingItemFingerprint and TrendingFeedUnitSectionFingerprint.
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHideTrendingShelvesPatch = bytecodePatch(
    name = "Hide Trending Shelves",
    description = "Removes Trending Today from the feed and trending searches from the search screen.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    dependsOn(versionCheckPatch)

    execute {

        fun Fingerprint.blankComposable() {
            runCatching {
                // Use addInstructions not addInstructionsWithLabels — the latter
                // corrupts label tables in large composable methods.
                method.addInstructions(0, "return-void")
            }
        }

        // ── Trending search item rows ─────────────────────────────────────
        TrendingItemFingerprint.blankComposable()

        if (!is_2026_11_0_or_greater) {
            TrendingItemLegacyFingerprint.blankComposable()
        }

        // ── Typeahead community suggestions ───────────────────────────────
        TypeaheadSuggestionItemFingerprint.blankComposable()

        // ── Trending feed unit (2026.21.0+) ───────────────────────────────
        if (is_2026_21_0_or_greater) {
            TrendingFeedUnitSectionFingerprint.blankComposable()
            TrendingFeedUnitDismissedSectionFingerprint.blankComposable()
        }
    }
}
