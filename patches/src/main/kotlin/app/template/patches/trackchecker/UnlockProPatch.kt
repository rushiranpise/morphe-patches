package app.template.patches.trackchecker

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TRACKCHECKER_COMPATIBILITY

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro / No Ads",
    description = "Unlocks Pro/No Ads feature in app",
) {
    compatibleWith(TRACKCHECKER_COMPATIBILITY)

    execute {
        // r43.e() — force subscribed = true
        NoAdsSubFingerprint.method.addInstruction(0, "const/4 v0, 0x1")
        NoAdsSubFingerprint.method.addInstruction(1, "return v0")

        // TC_Application.j() — force shouldShowAds = false
        ShouldShowAdsFingerprint.method.addInstruction(0, "const/4 v0, 0x0")
        ShouldShowAdsFingerprint.method.addInstruction(1, "return v0")
    }
}
