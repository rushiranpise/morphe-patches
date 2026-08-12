package app.template.patches.facebook.ads

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FACEBOOK_COMPATIBILITY
import app.template.patches.shared.returnEarly
import app.template.patches.facebook.misc.facebookSignaturePatch

// Targets the ad-unit visibility dispatcher in 2UY.A06 (classes13).
//
// 2UY.A06 is a PUBLIC STATIC dispatcher that reads the cached visibility string
// for Facebook's ad feed unit types (HoldoutAd, QuickPromotion, QuickPromotionNativeTemplate,
// and several obfuscated type-ID-based ad units identified by literals 0x2b0083ed / -0x91415ea).
// Returning null from this method suppresses the corresponding RecyclerView items.
//
// Non-ad story types (GraphQLStory, GraphQLStorySet, regular posts) are handled by
// the sibling A07 dispatcher and are NOT affected by this patch.
//
// Verified against com.facebook.katana 569.0.0.42.72 (classes13/X/2UY.smali).
@Suppress("unused")
val facebookHideSponsoredStoriesPatch = bytecodePatch(
    name = "Hide sponsored stories",
    description = "Hides sponsored and promoted ad units in the Facebook main feed by suppressing their visibility dispatch.",
) {
    compatibleWith(FACEBOOK_COMPATIBILITY)

    dependsOn(facebookSignaturePatch)

    execute {
        // A06 returns Ljava/lang/String; — must use returnEarly(null), not returnEarly().
        // returnEarly() (no arg) inserts return-void which mismatches the Object return type.
        // returnEarly(null) injects const/4 v0, 0x0 + return-object v0.
        // Returning null → adapter reads null visibility string → item suppressed.
        GetAdVisibilityDispatcherFingerprint.method.returnEarly(null)
    }
}
