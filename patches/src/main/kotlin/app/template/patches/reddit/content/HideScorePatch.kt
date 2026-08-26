package app.template.patches.reddit.content

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ─────────────────────────────────────────────────────────────────────────────
// Hide Post & Comment Score
//
// Inspired by: Reddit Enhancer extension (hidePostKarma + hideCommentKarma)
//              RES voteEnhancements (score visibility control).
//
// Two independent patches, each default=false (opt-in):
//
// 1. redditHidePostScorePatch
//    Forces Link.getHideScore()Z → true. The feed renderer checks this flag
//    before displaying the vote count; when true it renders "·" (hidden dot)
//    instead of the score number. This is the same flag Reddit itself uses for
//    contests and experimental score-hiding.
//
// 2. redditHideCommentScorePatch
//    Forces Comment.getScoreHidden()Z → true for every comment, hiding vote
//    counts on all comments in a thread.
//
// Smali verified (classes9):
//   Link.getHideScore()Z    — public final, iget-boolean hideScore:Z
//   Comment.getScoreHidden()Z — public final, iget-boolean scoreHidden:Z
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHidePostScorePatch = bytecodePatch(
    name = "Hide Post Score",
    description = "Hides vote counts on all posts in the feed.",
    default = false,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        runCatching {
            LinkGetHideScoreFingerprint.method.returnEarly(true)
        }
    }
}

@Suppress("unused")
val redditHideCommentScorePatch = bytecodePatch(
    name = "Hide Comment Score",
    description = "Hides vote counts on all comments.",
    default = false,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        runCatching {
            CommentGetScoreHiddenFingerprint.method.returnEarly(true)
        }
    }
}
