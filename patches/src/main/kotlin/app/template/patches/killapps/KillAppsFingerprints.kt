package app.template.patches.killapps

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * App.isPro()Z
 *
 * Central pro-status gate. Checks FORCE_PRO flag first, then falls back to
 * SettingsHelper.getIsAppUpgraded(). Returning true unlocks all pro features.
 */
val IsProFingerprint = Fingerprint(
    definingClass = "Lcom/tafayor/killall/App;",
    name = "isPro",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * SettingsHelper.getIsAppUpgraded()Z
 *
 * SharedPreferences-backed fallback called by App.isPro().
 * Always returning true ensures pro state survives restarts.
 */
val GetIsAppUpgradedFingerprint = Fingerprint(
    definingClass = "Lcom/tafayor/killall/prefs/SettingsHelper;",
    name = "getIsAppUpgraded",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC)
)
