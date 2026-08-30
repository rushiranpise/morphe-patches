package app.template.patches.zee5.ads

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ZEE5_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * This intentionally targets the DTO visibility predicate first rather than
 * deleting the whole ad repository, which can break normal home/content data.
 */
@Suppress("unused")
val zee5DisplayAdsPatch = bytecodePatch(
    name = "Hide ZEE5 display ads",
    description = "Disables selected client-side display-ad visibility flags.",
) {
    compatibleWith(ZEE5_COMPATIBILITY)

    execute {
        RegisteredUserAdsVisibilityFingerprint.methodOrNull?.returnEarly(false)
        GuestAdsVisibilityFingerprint.methodOrNull?.returnEarly(false)
        PremiumUserAdsVisibilityFingerprint.methodOrNull?.returnEarly(false)
    }
}