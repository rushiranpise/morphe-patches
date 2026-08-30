package app.template.patches.zee5.ads

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ZEE5_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val zee5AdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Disables the client-side ZEE5 mobile ad configuration path.",
) {
    compatibleWith(ZEE5_COMPATIBILITY)

    execute {
        VideoAdDtoAdsUrlFingerprint.method.returnEarly(null)
        VideoAdDtoIntervalsFingerprint.method.returnEarly(null)
        AdsConfigInputAdsUrlFingerprint.method.returnEarly(null)
        AdsConfigInputImaAdsFingerprint.method.returnEarly(null)
        NoPrerollEnabledFingerprint.method.returnEarly(false)
    }
}