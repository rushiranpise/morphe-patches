package app.template.patches.hibernator

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * App.isPro()Z
 *
 * Central pro-status gate. Checks FORCE_PRO flag first, then falls back to
 * SettingsHelper.getIsAppUpgraded(). All pro-feature gates call this method.
 * Making it always return true unlocks all pro features.
 */
val IsProFingerprint = Fingerprint(
    definingClass = "Lcom/tafayor/hibernator/App;",
    name = "isPro",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * SettingsHelper.getIsAppUpgraded()Z
 *
 * Reads the KEY_PREF_APP_UPGRADED boolean from SharedPreferences.
 * Called by App.isPro() as the fallback when FORCE_PRO is false.
 * Always returning true ensures persistence across restarts.
 */
val GetIsAppUpgradedFingerprint = Fingerprint(
    definingClass = "Lcom/tafayor/hibernator/prefs/SettingsHelper;",
    name = "getIsAppUpgraded",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC)
)
