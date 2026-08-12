package app.template.patches.facebook.misc

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FACEBOOK_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Suppresses the "Get Meta Verified" (Meta subscription) bottom-sheet dialog
// that Facebook shows after users publish posts.
//
// Target: MetaVerifiedFbAfterPostUpsellBottomSheetHandlerImpl.A00
// — the public coroutine entry point in a non-obfuscated class.
//
// A00 is a Kotlin suspend function (state-machine; label dispatch).
// Returning null at index 0 bypasses the state machine entirely:
//   const/4 v0, 0x0
//   return-object v0   → caller receives null = coroutine completed with no result
//
// The try/catch range in A00 starts at bytecode offset ~0x7a (well past index 0),
// so prepending at 0 is safe — no dangling try-table references.
//
// Verified against com.facebook.katana 569.0.0.42.72
// (classes13/com/facebook/nme/fbafterpostupsell/impl/
//  MetaVerifiedFbAfterPostUpsellBottomSheetHandlerImpl.smali).
@Suppress("unused")
val facebookSuppressMetaVerifiedUpsellPatch = bytecodePatch(
    name = "Suppress Meta Verified upsell",
    description = "Suppresses the Meta Verified subscription prompt that appears after publishing posts.",
) {
    compatibleWith(FACEBOOK_COMPATIBILITY)

    dependsOn(facebookSignaturePatch)

    execute {
        // A00 returns Ljava/lang/Object; (Kotlin suspend function result).
        // returnEarly(null) injects const/4 v0, 0x0 + return-object v0.
        // Caller receives null = coroutine completed with no result → no bottom sheet shown.
        MetaVerifiedUpsellFingerprint.method.returnEarly(null)
    }
}
