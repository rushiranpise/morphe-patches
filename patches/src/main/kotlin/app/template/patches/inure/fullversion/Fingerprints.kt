package app.template.patches.inure.fullversion

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ─────────────────────────────────────────────────────────────────────────────
// Targets: app.simple.inure.play (Play) and app.simple.inure (GitHub).
// Class paths are identical across both flavours.
// Smali verified against: build107.2.0 (versionCode 10720).
// ─────────────────────────────────────────────────────────────────────────────

/**
 * TrialPreferences.isFullVersion()Z — classes.dex
 *
 * Deepest single-point gate. Reads "is_full_version_" from EncryptedSharedPreferences.
 * Returning true cascades through the entire premium system:
 *   - initCheck() skips verifyCertificate()
 *   - unlockStateChecker() enters the "licence key" or "unlocker installed" branch
 *   - all feature guards in Activities/Fragments open unconditionally
 */
val IsFullVersionFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isFullVersion",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * TrialPreferences.isAppFullVersionEnabled()Z — classes.dex
 *
 * Combined gate: "is_full_version_" OR (daysBetween <= 15).
 * Primary feature-access predicate used throughout Activities and Fragments.
 */
val IsAppFullVersionEnabledFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isAppFullVersionEnabled",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * TrialPreferences.isWithinTrialPeriod()Z — classes.dex
 *
 * Returns true while fewer than 15 days have elapsed since first launch.
 * Returning true prevents expiry-based lockout paths.
 */
val IsWithinTrialPeriodFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isWithinTrialPeriod",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * TrialPreferences.isTrialWithoutFull()Z — classes.dex
 *
 * Returns true when trial expired AND full version not purchased.
 * Used in unlockStateChecker() to decide whether to show the purchase nag.
 * Returning false suppresses all countdown UI and purchase dialogs.
 */
val IsTrialWithoutFullFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isTrialWithoutFull",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * TrialPreferences.hasLicenceKey()Z — classes.dex
 *
 * Returns true if a Gumroad licence key is stored. Introduced in build107.2.0.
 * unlockStateChecker() uses this to decide whether to skip unlocker-APK verification.
 * Returning true routes the checker into the "licence key mode" fast-path which
 * calls ViewUtils.gone(daysLeft) and returns — skipping the dangerous
 * setFullVersion(false) call that would overwrite our isFullVersion() patch.
 */
val HasLicenceKeyFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "hasLicenceKey",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * TrialPreferences.isUnlockerVerificationRequired()Z — classes.dex
 *
 * Returns true when the unlocker APK signature must be re-verified.
 * Returning false keeps the "no verification needed" fast-path active
 * and prevents the companion-APK check that calls setFullVersion(false).
 */
val IsUnlockerVerificationRequiredFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "isUnlockerVerificationRequired",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * BaseActivity.fullVersionCheck()Z — classes.dex
 *
 * Inline gate in the Activity base class (no-arg overload).
 * Reads "is_full_version_" directly; shows FullVersion dialog if false.
 * Returning true prevents the dialog at every Activity entry point.
 */
val BaseActivityFullVersionCheckFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/extensions/activities/BaseActivity;",
    name = "fullVersionCheck",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * BaseActivity.fullVersionCheck(Function0)Z — classes.dex
 *
 * Lambda overload of the Activity gate. Same logic as the no-arg variant.
 * Returning true skips the dialog and lets the onClose lambda proceed.
 */
val BaseActivityFullVersionCheckWithCallbackFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/extensions/activities/BaseActivity;",
    name = "fullVersionCheck",
    returnType = "Z",
    parameters = listOf("Lkotlin/jvm/functions/Function0;"),
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * SplashScreen.unlockStateChecker()V — classes4.dex
 *
 * Called from onViewCreated on every launch. In build107.2.0 it has 5 branches:
 *   1. isTrialWithoutFull() && isFullVersion()       → gone(daysLeft), return    (ok)
 *   2. isTrialWithoutFull() && !isFullVersion()      → show countdown             (nag)
 *   3. !isTrialWithoutFull() && isFullVersion()
 *        && hasLicenceKey() && !verificationRequired → gone(daysLeft), return    (ok)
 *        && isPackageInstalled(unlocker)             → gone(daysLeft), return    (ok)
 *        else                                        → showWarning + setFullVersion(false) ← DANGEROUS
 *   4. !isTrialWithoutFull() && !isFullVersion()     → show countdown             (nag)
 *
 * With Layers 1–6 all returning true/false appropriately, branch 3 "licence key
 * mode" is always taken and returns early. This fingerprint no-ops the whole method
 * as a belt-and-suspenders defence so setFullVersion(false) can never be reached.
 */
val UnlockStateCheckerFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/ui/launcher/SplashScreen;",
    name = "unlockStateChecker",
    returnType = "V",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)

/**
 * TrialPreferences.getFirstLaunchDate()J — classes.dex
 *
 * Reads "first_launch_" epoch-ms from EncryptedSharedPreferences. This is the
 * anchor for ALL trial computations across the entire app.
 *
 * SAFE TO PATCH — verified across all 50+ callers (NotesEditor, Notes, Music,
 * Statistics, Trackers, UsageStatisticsGraph, every settings screen, etc.):
 * every caller uses this value exclusively in the pattern
 *   daysBetween(firstLaunch, today) > 15
 * as an inline trial gate. It is NEVER used for note timestamps, file creation
 * dates, or any other real-world date displayed to the user.
 * CalendarUtils.getToday() callers are identical — same pattern only.
 *
 * Returning System.currentTimeMillis() makes daysBetween(today, today) = 0
 * everywhere — trial always reads as "started today", never expires.
 */
val GetFirstLaunchDateFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/preferences/TrialPreferences;",
    name = "getFirstLaunchDate",
    returnType = "J",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * CalendarUtils.getToday()Date — classes4.dex
 *
 * Returns new Date() (now) cleared to midnight. Used as the "end date" in
 * every daysBetween(firstLaunch, today) trial check.
 *
 * SAFE TO PATCH — all 50+ callers use it solely in the daysBetween > 15 trial
 * pattern. Never used for displaying real timestamps to the user.
 *
 * Returning new Date(0) (1970-01-01 epoch) makes daysBetween(firstLaunch, epoch)
 * always <= 0 regardless of what firstLaunchDate is on device — trial gate
 * permanently open. Belt-and-suspenders alongside Layer 10.
 */
val GetTodayFingerprint = Fingerprint(
    definingClass = "Lapp/simple/inure/util/CalendarUtils;",
    name = "getToday",
    returnType = "Ljava/util/Date;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)
