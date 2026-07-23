package app.template.patches.chargemeter.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// App.onCreate()V — the Application subclass lifecycle method, runs before any Activity.
// Inject SharedPrefs write here: persists premium_user=true on first launch, covering
// ALL read sites app-wide (x7/j onClick PiP, x7/o SeekBar, widget activities, etc.).
// .registers 5 → p0=this, v0..v3 locals, all non-wide, safe to clobber at index 1
// (after invoke-super). Uses v0/v1/v2 which are overwritten immediately after.
// Fingerprinted by the unique NotificationChannel static calls inside the method.
internal object AppOnCreateFingerprint : Fingerprint(
    definingClass = "Ldev/km/android/chargemeter/Utilities/App;",
    name = "onCreate",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// SettingsActivity.onResume()V — reads premium_user on every Settings open.
// Controls GET PREMIUM card visibility and premium badge icons on SeekBar rows.
// String order: "premium_user" THEN "PremiumPreference" (reversed vs other methods).
// Overrides move-result after getBoolean → const/4 reg, 0x1.
internal object SettingsActivityOnResumePremiumFingerprint : Fingerprint(
    definingClass = "Ldev/km/android/chargemeter/Activities/SettingsActivity;",
    name = "onResume",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("premium_user"),
        string("PremiumPreference"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)

// ProfileActivity.u(Z)V — sole write point for premium_user SharedPreference.
// Inject const/4 p1, 0x1 at index 0 → always writes true.
internal object SetPremiumFingerprint : Fingerprint(
    definingClass = "Ldev/km/android/chargemeter/Activities/ProfileActivity;",
    name = "u",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Z"),
    filters = listOf(
        string("PremiumPreference"),
        string("premium_user"),
    ),
)

// LicenseClient.processResponse(I, Bundle)V — Pairip license check callback.
// returnEarly() skips NOT_LICENSED paywall and RSA sig validation.
internal object ProcessLicenseResponseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "processResponse",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("I", "Landroid/os/Bundle;"),
    filters = listOf(
        string("LicenseClient"),
        string("License check succeeded."),
    ),
)
