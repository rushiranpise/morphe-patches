package app.template.patches.tiktok_lite.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TIKTOK_LITE_COMPATIBILITY

@Suppress("unused")
val tiktokLiteRemoveAdsPatch = bytecodePatch(
    name = "TikTok Lite Remove Ads",
    description = "Removes feed ads, splash ads, and soft ads from TikTok Lite.",
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // ── Feed ad gates ─────────────────────────────────────────────────────
        // Each method returns a boolean used by the rendering pipeline to decide
        // whether to show an ad slot. Returning false clears all ad slots.

        // is3rdAd() -- third-party / MSDK ad slot (iget-boolean of is3rdAd:Z field).
        AwemeIs3rdAdFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // isAppAd() -- in-app ad format (banner / interstitial inside feed).
        AwemeIsAppAdFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // isMsdkAdAweme() -- ByteDance MSDK ad type.
        AwemeIsMsdkAdFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // isSoftAd() -- native commerce-disguised ads; secondary gate in isAdTraffic().
        AwemeIsSoftAdFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // isMarketplace() -- shopping/marketplace ad posts.
        AwemeIsMarketplaceFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // ── Splash ad init ────────────────────────────────────────────────────
        // SplashAdInitTask.run(Context)V is executed once at app startup.
        // return-void at index 0 prevents the SDK from initialising; no splash
        // ad can load or render. The task's keyString() == "SplashAdInitTask"
        // confirms the match is correct.
        SplashAdInitTaskFingerprint.method.addInstructions(
            0,
            "return-void",
        )
    }
}
