package app.template.patches.torrentsearch.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TORRENTSEARCH_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Torrent Search Revolution V2 — Premium Bypass
 *
 * Mechanism: The app uses a companion app (ProKey) to verify purchase.
 * `w1/c0` is the license ViewModel. It checks ProKey presence and installer
 * source, then dispatches the result via `c(String, Z)`.
 *
 * Patches applied:
 * 1. Force `f()` (isProKeyVisible) → true, so ProKey is always seen as present.
 * 2. Force `b()` (isInstalledFromPlayStore) → true, bypassing installer check.
 * 3. Force `c(reason, isPro)` to inject `isPro=true` before proceeding,
 *    ensuring the EventBus event `w1/b0(true)` is always posted, which:
 *    - Writes `ppk=true` to `tsr_security_prefs` in SearchActivity
 *    - Calls `v1/d.e()` to hide and remove the ad container view
 *    - Unlocks pro search provider sources
 */
@Suppress("unused")
val torrentSearchPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all pro features and removes ads ."
) {
    compatibleWith(TORRENTSEARCH_COMPATIBILITY)

    execute {
        // 1. Pretend ProKey is installed (skips "prokey_not_visible" rejection path)
        IsProKeyVisibleFingerprint.method.returnEarly(true)

        // 2. Pretend app was installed from Play Store (skips "legacy_installer_rejected" path)
        InstallerCheckFingerprint.method.returnEarly(true)

        // 3. Force isPro=true in the result dispatcher.
        //    p0 = this, p1 = String reason, p2 = Z isPro
        //    Inject const/4 p2, 0x1 at the top so isPro is always true.
        ProVerificationResultFingerprint.method.addInstructions(
            0,
            "const/4 p2, 0x1"
        )
    }
}
