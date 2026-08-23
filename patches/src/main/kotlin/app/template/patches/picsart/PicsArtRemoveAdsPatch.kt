package app.template.patches.picsart

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PICSART_COMPATIBILITY
import app.template.patches.shared.returnEarly
import app.template.patches.picsart.picsartDisableSignatureCheckPatch

/**
 * Removes PicsArt banner and interstitial ads.
 *
 * `com.picsart.studio.ads.b` (AdsManager) is the central ad gate. Its `a()Z`
 * method is the "ads disabled" master flag:
 *
 *   a() = !this.e || !e()          → true  (ads off)
 *       : AdsService.l.g() || !h() → true  (ads off via remote config)
 *
 * Every ad entry point consults it:
 *   g(touchPoint): if (a()) return false;            // banner not enabled
 *   i(context):    (!d().isConnected() || a() || …) ? false : true   // interstitial
 *   j(context):    if (a() || i(context) || …) return null           // banner load
 *
 * Forcing a() → true disables banners + interstitials app-wide with a single
 * fingerprint. Class name is stable (`com.picsart.studio.ads` is not obfuscated;
 * only method names are shortened to a/b/c/g/h/i/j, matched structurally here).
 *
 * Note: in-app "house ads" (PicsArt's own promotional interstitials) and native
 * feed ad cards are driven by other managers; the main banner/interstitial
 * surfaces (the ones free users see most) are covered by this gate.
 */
@Suppress("unused")
val picsartRemoveAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Disables PicsArt banner and interstitial ads by forcing the central AdsManager gate to ads-off.",
) {
    compatibleWith(PICSART_COMPATIBILITY)
    dependsOn(picsartDisableSignatureCheckPatch)

    execute {
        AdsManagerDisabledFingerprint.method.returnEarly(true)
    }
}
