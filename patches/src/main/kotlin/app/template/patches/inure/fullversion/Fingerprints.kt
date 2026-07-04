package app.template.patches.inure.fullversion

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * TrialPreferences.isFullVersion()Z
 *
 * Reads "is_full_version_" from EncryptedSharedPreferences.
 * Always returning true bypasses the purchase gate.
 */
val IsFullVersionFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isFullVersion",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * TrialPreferences.isAppFullVersionEnabled()Z
 *
 * Combined check: reads "is_full_version_" flag AND trial period.
 * Always returning true ensures all feature gates treat the app as purchased.
 */
val IsAppFullVersionEnabledFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isAppFullVersionEnabled",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * TrialPreferences.isWithinTrialPeriod()Z
 *
 * Returns true if fewer than 15 days since first launch.
 * Always returning true prevents expiry-based lockout.
 */
val IsWithinTrialPeriodFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isWithinTrialPeriod",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * TrialPreferences.isTrialWithoutFull()Z
 *
 * Returns true when trial expired and not purchased.
 * Always returning false prevents showing purchase nags.
 */
val IsTrialWithoutFullFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isTrialWithoutFull",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * BaseActivity.fullVersionCheck()Z
 *
 * Inline duplicate of the trial/purchase gate in the Activity base class.
 * Reads "is_full_version_" directly; shows FullVersion dialog if expired.
 * Always returning true prevents the dialog from appearing at activity level.
 */
val BaseActivityFullVersionCheckFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/extensions/activities/BaseActivity;",
    name = "fullVersionCheck",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * BaseActivity.fullVersionCheck(Function0)Z
 *
 * Overload that accepts an onClose lambda; same inline gate as the no-arg variant.
 * Always returning true skips the dialog and invokes the action directly.
 */
val BaseActivityFullVersionCheckWithCallbackFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/extensions/activities/BaseActivity;",
    name = "fullVersionCheck",
    returnType = "Z",
    parameters = listOf("Lkotlin/jvm/functions/Function0;"),
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * SplashScreen.unlockStateChecker()V
 *
 * Called on every onViewCreated. Checks for the unlocker companion app
 * (app.simple.inureunlocker); if absent, calls setFullVersion(false) and shows
 * "Full version deactivated" warning — even if our patches already set it true.
 * Returning immediately prevents the false-deactivation write.
 */
val UnlockStateCheckerFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/ui/launcher/SplashScreen;",
    name = "unlockStateChecker",
    returnType = "V",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)
