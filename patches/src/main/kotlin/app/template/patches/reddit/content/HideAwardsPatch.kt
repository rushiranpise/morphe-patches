package app.template.patches.reddit.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ─────────────────────────────────────────────────────────────────────────────
// Hide Awards
//
// Inspired by: Reddit Enhancer extension (hideAwards feature).
//
// Two targets cover awards on both posts and comments:
//
// 1. Link.getAwards()Ljava/util/List;
//    Returns the awards list for a post. Returning an empty list causes the
//    post renderer to skip rendering award icons and count badges entirely.
//
// 2. Comment.isAwardedRedditGold()Z
//    Gate for the gold award badge on comments. Forcing false hides the badge.
//
// Smali verified (classes9):
//   Link.getAwards()Ljava/util/List;         — public final, iget-object awards:List
//   Comment.isAwardedRedditGold()Z           — public final, iget-boolean isAwardedRedditGold:Z
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHideAwardsPatch = bytecodePatch(
    name = "Hide Awards",
    description = "Removes award icons and badges from posts and comments.",
    default = false,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        // Post awards: return empty list
        runCatching {
            LinkGetAwardsFingerprint.method.addInstructions(
                0,
                """
                    invoke-static { }, Ljava/util/Collections;->emptyList()Ljava/util/List;
                    move-result-object p0
                    return-object p0
                """.trimIndent(),
            )
        }

        // Comment gold badge: return false
        runCatching {
            CommentIsAwardedRedditGoldFingerprint.method.returnEarly(false)
        }
    }
}
