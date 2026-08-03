package app.template.patches.somyac.pairip

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SOMYAC_WATCH_TRANSLATOR_COMPATIBILITY

/**
 * Translator for Wear OS — Pairip bypass.
 *
 * Package: com.somyac.watch.translator  (companion phone app)
 * Version: 1.5.0 (versionCode 303)
 *
 * Identical codebase to Mini Web Browser — only LicenseClient.licensePubKey
 * and LicenseClient.packageName static fields differ. The same
 * [somyacPairipBytecodePatch] covers both apps.
 */
@Suppress("unused")
val watchTranslatorPairipPatch = bytecodePatch(
    name = "Translator Pairip Bypass",
    description = "Strips the Pairip v2 licence check from the Translator companion app.",
    default = true
) {
    compatibleWith(SOMYAC_WATCH_TRANSLATOR_COMPATIBILITY)
    dependsOn(somyacPairipBytecodePatch)
}
