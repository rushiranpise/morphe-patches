package app.template.patches.tasker

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── Licence.w0(SharedPreferences)Z ───────────────────────────────────────────
// Reads the "lcl" (license key) pref and returns true if it has content.
// Forcing true prevents the "no key" branch from showing an invalid-state dialog.
val LicenceHasKeyFingerprint = Fingerprint(
    definingClass = "Lnet/dinglisch/android/taskerm/Licence;",
    name = "w0",
    parameters = listOf("Landroid/content/SharedPreferences;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

// ── MonitorService.P3(Context)String ─────────────────────────────────────────
// Returns the notification icon name. Calls Kid.b() and if true tries tn.v(0)
// which crashes when kid/data.xml is absent. The else-branch returns the safe
// hardcoded string "cust_notification". We short-circuit to always return that.
val MonitorServiceP3Fingerprint = Fingerprint(
    definingClass = "Lnet/dinglisch/android/taskerm/MonitorService;",
    name = "P3",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

// ── rf.w.z(ZZZ)Single ────────────────────────────────────────────────────────
// The main license check method. Normally calls Kid.b() → if true → S() →
// Licensed. We patch z() to always invoke S() directly, bypassing Kid.b()
// entirely so we never activate kid-mode code paths that require kid/data.xml.
val LicenseCheckZFingerprint = Fingerprint(
    definingClass = "Lrf/w;",
    name = "z",
    parameters = listOf("Z", "Z", "Z"),
    returnType = "Lio/reactivex/Single;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)