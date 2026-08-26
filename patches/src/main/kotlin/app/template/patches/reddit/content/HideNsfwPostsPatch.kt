package app.template.patches.reddit.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ─────────────────────────────────────────────────────────────────────────────
// Hide NSFW Posts
//
// Inspired by: Reddit Enhancer extension (hideNSFW feature) and
//              filteReddit (RES) NSFW content filtering.
//
// The Android feed reads Link.getOver18()Z to decide whether to apply the NSFW
// blur/gate. Forcing this to always return false causes the app to treat all
// posts as SFW, suppressing the blur and the "NSFW" label rendering.
//
// NOTE: This is a client-side content rendering gate only. The underlying
// post content URL is unchanged. Server-enforced NSFW gating (e.g. age
// verification dialogs on certain subreddits) may still appear.
//
// Smali verified (classes9, Lcom/reddit/domain/model/Link;):
//   getOver18()Z — public final, .registers 1
//   iget-boolean p0, p0, Lcom/reddit/domain/model/Link;->over18:Z
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHideNsfwPostsPatch = bytecodePatch(
    name = "Hide NSFW Posts",
    description = "Hides NSFW posts from the feed by treating all posts as SFW.",
    default = false,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        runCatching {
            LinkGetOver18Fingerprint.method.returnEarly(false)
        }
    }
}
