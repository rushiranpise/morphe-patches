package app.template.patches.facebook.ads

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FACEBOOK_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Blocks sponsored stories from entering the Facebook main feed pool.
// Ported from github.com/Loukious/FacebookAppAdsRemover (fb571 → 569 mapping).
//
// Target: LX/1zK.A03(GraphQLFeedUnitEdge)Z — the SponsoredPoolContainerAdapter add gate.
//
// LX/1zK is the SponsoredPoolContainerAdapter (classes13). A03 is the method that decides
// whether an incoming GraphQLFeedUnitEdge (a feed story item) is accepted into the sponsored
// pool. The pool is drained into the RecyclerView feed adapter. Returning false unconditionally
// means no sponsored stories ever enter the pool — they are silently dropped at source.
//
// Also targets A79(6HB, 3Xy)Z which is the secondary pool add path used by the network feed
// update pipeline (called when fresh stories arrive from a network response).
//
// Crash note (unrelated): the NoSuchElementException at X.hWL.<init>:124 in the logcat
// is a pre-existing Facebook bug in MibEbAutoRestoreHandler (E2EE cloud backup auto-restore
// flow). It occurs on first launch of a thread after the backup restore handshake fails.
// It is NOT caused by any of these patches.
//
// Verified against com.facebook.katana 569.0.0.42.72:
//   classes13/X/1zK.smali → methods A03 (line 818) and A79 (line 870)
@Suppress("unused")
val facebookHideFeedAdsPatch = bytecodePatch(
    name = "Hide feed ads",
    description = "Prevents sponsored stories from entering the Facebook main feed pool.",
) {
    compatibleWith(FACEBOOK_COMPATIBILITY)

    execute {
        // Primary pool gate: A03(GraphQLFeedUnitEdge)Z
        // returnEarly(false) → caller receives false → item not added to pool → not shown
        SponsoredPoolAddFingerprint.method.returnEarly(false)

        // Secondary pool gate: A79(6HB, 3Xy)Z — network feed update path
        SponsoredPoolNetworkAddFingerprint.method.returnEarly(false)
    }
}
