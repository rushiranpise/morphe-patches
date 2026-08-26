package app.template.patches.reddit.content

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ─────────────────────────────────────────────────────────────────────────────
// Hide Repost Button
//
// Inspired by: Reddit Enhancer extension (hideRepostButton).
//
// The post action bar checks Link.isCrosspostable$domain_model()Z before
// rendering the repost button. Returning false removes the button from all
// posts, keeping the action bar cleaner.
//
// Smali verified (classes9, Lcom/reddit/domain/model/Link;):
//   isCrosspostable$domain_model()Z — public final, .registers 1
//   iget-boolean p0, p0, Lcom/reddit/domain/model/Link;->isCrosspostable:Z
//
// Note: The method name contains a Kotlin-mangled $ suffix. The Fingerprint
// definingClass + name match is exact, so obfuscation of the enclosing class
// doesn't affect matching stability. The field isCrosspostable:Z as the filter
// anchor ensures uniqueness.
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHideRepostButtonPatch = bytecodePatch(
    name = "Hide Repost Button",
    description = "Removes the repost (crosspost) button from all posts.",
    default = false,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        runCatching {
            LinkIsCrosspostableFingerprint.method.returnEarly(false)
        }
    }
}
