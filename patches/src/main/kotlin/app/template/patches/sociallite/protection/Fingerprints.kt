package app.template.patches.sociallite.protection

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── LicenseCheckFingerprint ───────────────────────────────────────────────────
//
// Targets: com.pairip.licensecheck.LicenseClient.checkLicense(Context)V
//          [classes.dex]
//
// Called from com.pairip.application.Application.attachBaseContext(). When the
// APK signature doesn't match the original Play Store certificate (which it
// never will after Morphe re-signs), LicenseClient.checkLicense() detects the
// mismatch and eventually starts LicenseActivity which blocks the UI or calls
// System.exit(). No VMRunner or libpairipcore.so — this is a lighter Pairip
// integration that only uses the Play LVL license check path.
//
// Smali evidence: com/pairip/licensecheck/LicenseClient.smali
//   .method public static checkLicense(Landroid/content/Context;)V
//   ...
//   invoke-virtual {...}, Landroid/content/Context;->getPackageManager()...
//   invoke-static  {...}, Lcom/pairip/licensecheck/LicenseResponseHelper;->...
//
// The manifest fix (android:name swap) already prevents Application.attachBaseContext
// from being called at all — this fingerprint is a belt-and-suspenders no-op
// on the method itself in case it is called through any other path.
//
// Access flags: PUBLIC STATIC (class method, no instance)
// Return type: V
// Parameter: Landroid/content/Context;
//
object LicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)
