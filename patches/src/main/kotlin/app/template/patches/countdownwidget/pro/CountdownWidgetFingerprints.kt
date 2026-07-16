package app.template.patches.countdownwidget.pro

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * PhUtils.a()Z — app-side premium gate wrapper.
 *
 * Calls PremiumHelper.getInstance().preferences.h() which reads
 * "has_active_purchase" from SharedPreferences.  Used throughout
 * the app (SettingsActivity, BackupsActivity, widget providers).
 *
 * Patch: replace body → always return true.
 */
object PhUtilsIsPremiumFingerprint : Fingerprint(
    definingClass = "Lme/gira/widget/countdown/utils/PhUtils;",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    custom = { method, _ -> method.name == "a" }
)

/**
 * Preferences.h()Z — PremiumHelper library premium check.
 *
 * Reads "has_active_purchase" from the library's own SharedPreferences.
 * Patching this covers any direct calls from PremiumHelper internals.
 */
object PreferencesHasActivePurchaseFingerprint : Fingerprint(
    definingClass = "Lcom/zipoapps/premiumhelper/Preferences;",
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("has_active_purchase"),
    custom = { method, _ -> method.name == "h" }
)
