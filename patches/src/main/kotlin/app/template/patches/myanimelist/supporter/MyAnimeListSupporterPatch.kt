package app.template.patches.myanimelist.supporter

import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MYANIMELIST_COMPATIBILITY
import app.template.patches.shared.returnEarly
import app.template.patches.shared.disablePairIPLicenseCheckPatch

@Suppress("unused")
val myAnimeListSupporterPatch = bytecodePatch(
    name = "Unlock Supporter",
    description = "Spoofs MAL supporter status to suppress all banner, list, and search ads."
) {
    compatibleWith(MYANIMELIST_COMPATIBILITY)
    dependsOn(disablePairIPLicenseCheckPatch)

    execute {
        // Adapter call sites (SeasonalPagingAdapter, SearchPagingAdapter,
        // WomPagingAdapter, SearchTopAdAsset): User is loaded by the time adapters
        // bind, so the getter patch works correctly here.
        UserIsSupporterFingerprint.method.returnEarly(true)

        // HomeActivity.onCreate: User is null at creation time, so the isSupporter()
        // check is bypassed and MobileAds.initialize() runs unconditionally.
        // Remove the initialize() call directly — the preceding sget-object is
        // harmless (loads a singleton reference onto the stack and it drops).
        val initIdx = MobileAdsInitFingerprint.instructionMatches[0].index
        MobileAdsInitFingerprint.method.removeInstruction(initIdx)
    }
}
