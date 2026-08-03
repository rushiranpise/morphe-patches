package app.template.patches.wristweb.pairip

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WRISTWEB_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val wristWebPairipPatch = bytecodePatch(
    name = "WristBrowser Pairip License Bypass",
    description = "Prevents the Pairip Play Integrity license check from running.",
    default = true,
) {
    compatibleWith(WRISTWEB_COMPATIBILITY)

    execute {
        PairipCheckLicenseFingerprint.method.returnEarly()
    }
}
