package app.template.patches.facebook.ads

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FACEBOOK_COMPATIBILITY
import app.template.patches.shared.returnEarly
import app.template.patches.facebook.misc.facebookSignaturePatch

// Blocks the Reels ads floating CTA overlay and Neko playable ad activity.
// Ported from github.com/Loukious/FacebookAppAdsRemover (fb571 → 569 mapping).
//
// Target 1: LX/ABK.A05(ABF, ABG, int)Z — IndicatorPillComponent ad eligibility gate (classes18).
//
// ABK is the IndicatorPillComponent class, containing both render() and A05().
// A05 is a PUBLIC STATIC method that decides whether the ReelsAdsFloatingCtaPlugin (the
// "Sponsored" CTA banner overlaid on Reels) should be rendered for a given plugin slot.
// Returning false unconditionally suppresses this CTA banner on all Reels.
//
// Target 2: NekoPlayableAdActivity — the full-screen playable ad activity.
//
// com.facebook.neko.playables.activity.NekoPlayableAdActivity is a non-obfuscated,
// stable class that shows interactive/playable ads inside Facebook. Returning void from
// its onCreate() prevents the activity from initializing — it launches then immediately
// completes with no ad content shown.
//
// Verified against com.facebook.katana 569.0.0.42.72:
//   classes18/X/ABK.smali → method A05(ABF, ABG, I)Z (line 1138)
//   classes7/com/facebook/neko/playables/activity/NekoPlayableAdActivity.smali
@Suppress("unused")
val facebookHideReelsAdsPatch = bytecodePatch(
    name = "Hide Reels ads",
    description = "Suppresses the Reels ads floating CTA overlay and blocks playable ad activities.",
) {
    compatibleWith(FACEBOOK_COMPATIBILITY)

    dependsOn(facebookSignaturePatch)

    execute {
        // ReelsAdsFloatingCtaPlugin eligibility → always ineligible
        ReelsAdIndicatorPillFingerprint.method.returnEarly(false)

        // NekoPlayableAdActivity (full-screen playable ads) → immediate noop on create
        NekoPlayableAdActivityFingerprint.method.returnEarly()
    }
}
