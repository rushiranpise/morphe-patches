package app.template.patches.inure.fullversion

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.INURE_COMPATIBILITY
import app.template.patches.shared.Constants.INURE_GITHUB_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlocks Inure App Manager full version for both Play and GitHub variants.
 *
 * SplashScreen.unlockStateChecker() flow:
 *   if isTrialWithoutFull():
 *       if isFullVersion(): gone(daysLeft); return      <- trial UI hidden
 *       else: setText(daysLeft, "X days remaining")     <- shows days-left text
 *   if isFullVersion():
 *       if hasLicenceKey() && !isUnlockerVerificationRequired(): gone(daysLeft); return
 *       elif isPackageInstalled(unlocker): gone(daysLeft); return
 *       else: showWarning("full_version_deactivated") + setFullVersion(false)
 *   else: setText(daysLeft, "X days remaining")
 *
 * Six methods patched:
 *   1. isAppFullVersionEnabled() -> true    primary gate for every premium screen
 *   2. isFullVersion() -> true              Trial screen + unlockStateChecker entry
 *   3. isWithinTrialPeriod() -> true        suppress trial-expired upsell UI elsewhere
 *   4. isTrialWithoutFull() -> false        skip the first branch entirely (no days-left text)
 *   5. hasLicenceKey() -> true              trigger licence key mode exit in unlockStateChecker
 *   6. isUnlockerVerificationRequired() -> false   complete licence key mode condition
 *
 * With 4: unlockStateChecker skips straight to the isFullVersion() branch.
 * With 2+5+6: hits gone(daysLeft) in licence key mode and returns cleanly.
 *
 * Play variant additionally:
 *   7. isPlayFlavor() -> false   skips forced unlocker re-verification on every launch
 *
 * Verified smali: TrialPreferences in classes.dex, AppUtils in classes4.dex.
 */
@Suppress("unused")
val inureUnlockPremiumPatch = bytecodePatch(
    name = "Inure — Unlock Full Version",
    description = "Bypasses the 15-day trial gate and unlocks all premium features in Inure App Manager.",
    default = true,
) {
    compatibleWith(INURE_COMPATIBILITY)

    execute {
        // 1. Primary gate for every premium screen
        IsAppFullVersionEnabledFingerprint.method.returnEarly(true)

        // 2. Trial screen listener and unlockStateChecker entry condition
        IsFullVersionFingerprint.method.returnEarly(true)

        // 3. Suppress trial upsell UI elsewhere in the app
        IsWithinTrialPeriodFingerprint.method.returnEarly(true)

        // 4. Skip the first branch of unlockStateChecker entirely so the
        //    days-left text ("X days left in trial period") is never shown
        IsTrialWithoutFullFingerprint.method.returnEarly(false)

        // 5+6. Force licence key mode: gone(daysLeft); return — no package check
        HasLicenceKeyFingerprint.method.returnEarly(true)
        IsUnlockerVerificationRequiredFingerprint.method.returnEarly(false)

        // 7. Play only: skip forced unlocker re-verification on every launch
        IsPlayFlavorFingerprint.methodOrNull?.returnEarly(false)

        println("[InureUnlockPremiumPatch] Play variant patched")
    }
}

@Suppress("unused")
val inureGithubUnlockPremiumPatch = bytecodePatch(
    name = "Inure — Unlock Full Version (GitHub)",
    description = "Bypasses the 15-day trial gate and unlocks all premium features in Inure App Manager (GitHub).",
    default = true,
) {
    compatibleWith(INURE_GITHUB_COMPATIBILITY)

    execute {
        // 1. Primary gate
        IsAppFullVersionEnabledFingerprint.method.returnEarly(true)

        // 2. Trial screen listener and unlockStateChecker entry
        IsFullVersionFingerprint.method.returnEarly(true)

        // 3. Suppress trial upsell UI elsewhere
        IsWithinTrialPeriodFingerprint.method.returnEarly(true)

        // 4. Skip first unlockStateChecker branch — no days-left text
        IsTrialWithoutFullFingerprint.method.returnEarly(false)

        // 5+6. Force licence key mode exit
        HasLicenceKeyFingerprint.method.returnEarly(true)
        IsUnlockerVerificationRequiredFingerprint.method.returnEarly(false)

        println("[InureUnlockPremiumPatch] GitHub variant patched")
    }
}
