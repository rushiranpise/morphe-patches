package app.template.patches.reddit.layout.subredditdialog

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY

// ─────────────────────────────────────────────────────────────────────────────
// Remove Subreddit Dialogs
//
// Suppresses the notification re-enable bottom sheet that appears when visiting
// a subreddit where you're subscribed but have notifications off.
//
// Strategy: return-void at the top of invokeSuspend.
// The coroutine state machine exits cleanly via the standard coroutine sentinel
// value (kotlin.Unit) — injecting return-void at index 0 means the state
// machine body is never reached but the coroutine framework still resumes
// cleanly because the outer invoke() returns null/Unit which is expected.
//
// Note: NSFW dialog — verified in smali, that flow is in a separate class
// and requires server-confirmed NSFW status to trigger. Handled separately
// if user reports it.
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditRemoveSubRedditDialogPatch = bytecodePatch(
    name = "Remove Subreddit Dialogs",
    description = "Suppresses the notification re-enable prompt that appears on subreddit visits.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        runCatching {
            // Simply blank the entire coroutine body — it only shows a bottom sheet,
            // no functional side-effects that would break navigation.
            FrequentUpdatesHandlerFingerprint.method.addInstructions(
                0,
                "return-object p1",  // return the continuation result — safe coroutine exit
            )
        }
    }
}
