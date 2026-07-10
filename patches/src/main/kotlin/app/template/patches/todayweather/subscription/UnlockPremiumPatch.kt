package app.template.patches.todayweather.subscription

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TODAYWEATHER_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Today Weather — Unlock Premium (v2.5.0-5 / APKS).
 *
 * Two gates patched in the obfuscated billing manager class Bd:
 *
 *   1. k()Z  — isPurchased. Iterates static ArrayList f (in-app SKUs:
 *              "mobi.lockdown.weather", "mobi.lockdown.weather.1year.donate.new",
 *              "mobi.lockdown.weather.3months", "mobi.lockdown.weather.6months",
 *              "mobi.lockdown.weather.1year", "mobi.lockdown.weather.1year.donate",
 *              "mobi.lockdown.weather.monthly", "mobi.lockdown.weather.quarterly",
 *              "mobi.lockdown.weather.quarterly.new", "mobi.lockdown.weather.yearly")
 *              and ArrayList g (subscription SKUs: "mobi.lockdown.weather.premium",
 *              "mobi.lockdown.weather.premium.saleoff"), reading SharedPrefs keys
 *              "purchased_file_google_inapp" / "purchased_file_google_sub".
 *              Patched to always return true.
 *
 *   2. j()Z  — isTrialRestricted / isSaleOff. Returns true if days-since-install > 5
 *              AND SharedPref "prefIsOffSale" == true. Controls interstitial ads and
 *              feature restrictions after the free trial window.
 *              Patched to always return false.
 */
@Suppress("unused")
val todayWeatherUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features.",
    default = true,
) {
    compatibleWith(TODAYWEATHER_COMPATIBILITY)

    execute {
        // Gate 1: isPurchased — always return true
        BdIsPurchasedFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }

        // Gate 2: isTrialRestricted — always return false
        BdIsTrialRestrictedFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
        }
    }
}
