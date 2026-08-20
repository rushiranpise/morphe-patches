package app.template.patches.tiktok_lite.feedfilter

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TIKTOK_LITE_COMPATIBILITY

@Suppress("unused")
val tiktokLiteFeedFilterPatch = bytecodePatch(
    name = "Feed Filter",
    description = "Removes ads, sponsored content, and commerce posts from the TikTok Lite home feed.",
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // isAd() -- reads isAd:Z field then awemeRawAd!=null. Return false always.
        // Cascade: isAdTraffic() calls isAd(); isPseudoAd() reads commerceInfo.
        AwemeIsAdFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // isAdTraffic() -- delegates to isAd() then isSoftAd().
        // Patch directly so isSoftAd() path is also short-circuited.
        AwemeIsAdTrafficFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // isPseudoAd() -- reads commerceInfo. Covers commerce-disguised ad posts.
        // Source: tiktokkk FeedFilter.isAdvert -> extBool("isPseudoAd").
        AwemeIsPseudoAdFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // isMonetizationTraffic() -- delegates to isAdTraffic(). Belt-and-suspenders.
        // Source: tiktokkk FeedFilter.isAdvert -> extBool("isMonetizationTraffic").
        AwemeIsMonetizationTrafficFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}

@Suppress("unused")
val tiktokLiteHideLivePatch = bytecodePatch(
    name = "Hide Live Cards",
    description = "Removes live stream cards from the TikTok Lite home feed.",
    default = true,
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // isLive() -- checks awemeType==101 (0x65). Return false to suppress live cards.
        // Source: Toki installFeedFilters / tiktokkk FeedFilter.isLiveCard.
        AwemeIsLiveFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
