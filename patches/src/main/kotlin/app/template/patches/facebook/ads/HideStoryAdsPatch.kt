package app.template.patches.facebook.ads

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FACEBOOK_COMPATIBILITY
import app.template.patches.shared.returnEarly
import app.template.patches.facebook.misc.facebookSignaturePatch

// Ported from meridianfresco/morphe-meta-patches (forked from ReVanced).
// Targets the five Redex-obfuscated Runnable inner classes of AdBucketDataSourceUtil.
// All five are anchored via the stable __redex_internal_original_name field that Redex
// preserves even after obfuscated class rename.  Verified against classes6/ and classes18/
// in com.facebook.katana 569.0.0.42.72 (versionCode 472947931).
@Suppress("unused")
val facebookHideStoryAdsPatch = bytecodePatch(
    name = "Hide story ads",
    description = "Blocks ad insertion, deferred ad fetch, fetch-more-ads, and CTA/dwell tail-loads in Facebook stories.",
) {
    compatibleWith(FACEBOOK_COMPATIBILITY)

    dependsOn(facebookSignaturePatch)

    execute {
        AdsInsertionMethodFingerprint.method.returnEarly()
        FetchDeferredAdsMethodFingerprint.method.returnEarly()
        FetchMoreAdsMethodFingerprint.method.returnEarly()
        TriggerCtaTailloadFingerprint.method.returnEarly()
        TriggerDwellTailloadFingerprint.method.returnEarly()
    }
}
