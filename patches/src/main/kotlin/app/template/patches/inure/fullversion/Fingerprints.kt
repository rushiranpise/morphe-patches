package app.template.patches.inure.fullversion

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * TrialPreferences.isAppFullVersionEnabled()Z
 *
 * The primary full-version gate used throughout the app (ScopedFragment,
 * BaseActivity, Trial screen). Returns true if IS_APP_FULL_VERSION_ENABLED
 * SharedPreference is set, OR if fewer than 15 days have passed since first launch.
 *
 * Stable: public final, returns Z, no params, reads "is_full_version_" string.
 * Defined in: app/simple/inure/preferences/TrialPreferences (classes.dex).
 */
internal object IsAppFullVersionEnabledFingerprint : Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isAppFullVersionEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

/**
 * TrialPreferences.isFullVersion()Z
 *
 * Reads IS_APP_FULL_VERSION_ENABLED from SharedPreferences directly (no trial fallback).
 * Used by Trial screen SharedPreference listener and license key checks.
 *
 * Stable: public final, returns Z, no params, reads "is_full_version_" string.
 */
internal object IsFullVersionFingerprint : Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isFullVersion",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

/**
 * TrialPreferences.isWithinTrialPeriod()Z
 *
 * Returns true if fewer than 15 days since first launch (independent of purchase flag).
 * Used in isTrialWithoutFull() and trial UI. Patching this ensures trial period
 * is always reported as active, suppressing upsell UI even without full version flag.
 *
 * Stable: public final, returns Z, no params, calls CalendarUtils.getDaysBetweenTwoDates.
 */
internal object IsWithinTrialPeriodFingerprint : Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isWithinTrialPeriod",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lapp/simple/inure/util/CalendarUtils;",
            name = "getDaysBetweenTwoDates",
        ),
    ),
)

/**
 * TrialPreferences.hasLicenceKey()Z
 *
 * Returns true if "has_license_key" is set in EncryptedSharedPreferences.
 * Used by unlockStateChecker() in SplashScreen: if hasLicenceKey() && !isUnlockerVerificationRequired()
 * the app enters "licence key mode" and skips the unlocker package presence check.
 * Patching to return true triggers this safe exit path.
 *
 * Stable: public final, returns Z, no params, reads "has_license_key" string.
 */
internal object HasLicenceKeyFingerprint : Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "hasLicenceKey",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    strings = listOf("has_license_key"),
)

/**
 * TrialPreferences.isUnlockerVerificationRequired()Z
 *
 * Returns true if "is_unlocker_verification_required_" is set OR if isPlayFlavor() is true.
 * Used by unlockStateChecker(): if isFullVersion && hasLicenceKey && !isUnlockerVerificationRequired
 * -> licence key mode (no unlocker package check). Patching to false enables this path.
 *
 * Stable: public final, returns Z, no params, reads "is_unlocker_verification_required_" string.
 */
internal object IsUnlockerVerificationRequiredFingerprint : Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isUnlockerVerificationRequired",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    strings = listOf("is_unlocker_verification_required_"),
)

/**
 * TrialPreferences.isTrialWithoutFull()Z
 *
 * Returns true when trial period has expired AND full version is not purchased.
 * Used as the first branch in SplashScreen.unlockStateChecker():
 *   if isTrialWithoutFull(): if isFullVersion(): gone(daysLeft) else setText(daysLeft)
 *
 * Note: JADX shows this condition is logically impossible (daysBetween <= 15 && > 15),
 * but patching to false guarantees we never enter this branch and avoids any edge case
 * where stale SharedPreferences state causes the days-left text to appear.
 *
 * Stable: public final, returns Z, no params, calls CalendarUtils.getDaysBetweenTwoDates.
 * Defined in: app/simple/inure/preferences/TrialPreferences (classes.dex).
 */
internal object IsTrialWithoutFullFingerprint : Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isTrialWithoutFull",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lapp/simple/inure/util/CalendarUtils;",
            name = "getDaysBetweenTwoDates",
        ),
    ),
)

/**
 * AppUtils.isPlayFlavor()Z  (Play variant only)
 *
 * Returns hardcoded true in the Play build. Inure uses this to require
 * unlocker verification (isUnlockerVerificationRequired checks isPlayFlavor()).
 * Patching to return false puts the Play variant into the same trust path
 * as the GitHub variant, skipping mandatory unlocker re-verification.
 *
 * Stable: public final, returns Z, no params, body is const/4 v0, 0x1 + return v0.
 * Defined in: app/simple/inure/util/AppUtils (classes4.dex).
 */
internal object IsPlayFlavorFingerprint : Fingerprint(
    definingClass = "Lapp/simple/inure/util/AppUtils;",
    name = "isPlayFlavor",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)
