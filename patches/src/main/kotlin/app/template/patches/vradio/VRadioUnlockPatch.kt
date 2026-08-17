package app.template.patches.vradio

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.VRADIO_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * VRadio — Premium Unlock
 *
 * App: VRadio (com.ilv.vradio) v2.9.2 (versionCode 90209002)
 * Billing: Google Play Billing (BillingClient)
 * No pairip, no obfuscated SDK, no server-side verification.
 *
 * The entire premium gate is a single static boolean method (o31.d(Context))
 * that reads four SharedPreferences flags set by individual purchase callbacks:
 *   "sleepTimerSecond" — sleep timer
 *   "appearanceP"      — appearance/themes
 *   "atvP"             — Android TV support
 *   "pfaC"             — all-access unlock
 *
 * Callers:
 *   MainActivity.onResume(): hides/shows the "action_premium" navigation menu item
 *   Various feature fragments: gate premium UI sections
 *   m2.smali layout fragment: controls visibility of upgrade prompt TextView
 *
 * Patch: returnEarly(true) makes o31.d() always return true → all gates report premium.
 */
@Suppress("unused")
val vRadioUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all VRadio premium features by bypassing the SharedPreferences purchase gate.",
    default = true
) {
    compatibleWith(VRADIO_COMPATIBILITY)

    execute {
        IsPremiumFingerprint.method.returnEarly(true)
    }
}
