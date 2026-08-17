package app.template.patches.reddit.layout.subredditdialog

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Subreddit dialog fingerprints — verified against 2026.32.0 smali
//
// FrequentUpdatesHandler$handleFrequentUpdates$1 (classes3)
// Full invokeSuspend flow:
//   1. iget-object $subredditState:Lnlw;
//   2. invoke-static flow collect
//   3. check-cast → Subreddit
//   4. if-nez (null check on subreddit)
//   5. invoke-static getSubredditId()
//   6. invoke-virtual f1f0.k()Z         ← "is logged in" check
//   7. if-nez → :L5 (skip to subscriber check if logged in)
//   8. :L5 invoke-virtual Subreddit.getUserIsSubscriber()
//   9. sget-object Boolean.TRUE
//  10. invoke-static Intrinsics.areEqual(Object,Object)Z
//  11. move-result
//  12. if-nez → :L7 (if already subscribed, return)
//  13. ... show notification bottom sheet
//
// Fix strategy: return-void at start — simplest, safest, no false-positive risk.
// The method's only side-effect is showing the notification re-enable prompt;
// blanking it entirely is safe.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * FrequentUpdatesHandler coroutine lambda — shows notification re-enable
 * bottom sheet when user is subscribed but has notifications off.
 * We blank it to suppress the dialog.
 */
internal object FrequentUpdatesHandlerFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/screens/pager/FrequentUpdatesHandler\$handleFrequentUpdates\$1;",
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        // Stable: getUserIsSubscriber() is never obfuscated
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            smali = "Lcom/reddit/domain/model/Subreddit;->getUserIsSubscriber()Ljava/lang/Boolean;",
        ),
        // Stable: Kotlin Intrinsics.areEqual() is never obfuscated
        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            smali = "Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z",
        ),
    ),
)

/**
 * Link.getPromoted()Z — returns the promoted:Z field.
 * Used as a filter to remove promoted posts from the listing.
 * Smali: iget-boolean p0, p0, Lcom/reddit/domain/model/Link;->promoted:Z
 */
internal object LinkGetPromotedFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Link;",
    name = "getPromoted",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            smali = "Lcom/reddit/domain/model/Link;->promoted:Z",
        ),
    ),
)
