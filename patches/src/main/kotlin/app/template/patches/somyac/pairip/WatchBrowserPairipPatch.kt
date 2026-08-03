package app.template.patches.somyac.pairip

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SOMYAC_WATCH_BROWSER_COMPATIBILITY

/**
 * Mini Web Browser for Wear OS — Pairip bypass.
 *
 * Package: com.somyac.watch.browser  (companion phone app)
 * Version: 1.0.4 (versionCode 303)
 *
 * The companion app validates the Pairip licence on behalf of the watch-side
 * browser app (com.somyac.watch.browser). After bypass, the companion will
 * send a valid licence token to the watch without contacting Pairip servers.
 *
 * All bypass logic lives in [somyacPairipBytecodePatch] (shared with Translator).
 */
@Suppress("unused")
val watchBrowserPairipPatch = bytecodePatch(
    name = "Mini Web Browser Pairip Bypass",
    description = "Strips the Pairip v2 licence check from the Mini Web Browser companion app.",
    default = true
) {
    compatibleWith(SOMYAC_WATCH_BROWSER_COMPATIBILITY)
    dependsOn(somyacPairipBytecodePatch)
}
