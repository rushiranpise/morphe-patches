package app.template.patches.qbitconnect.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// LicenseClient.checkLicense(Context)V
// Static entry point called from Application.attachBaseContext().
// Smali verified: contains "Skipping license check in isolated process." at instructions[5].
val CheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(string("Skipping license check in isolated process.")),
)

// LicenseResponseHelper.validateResponse(Bundle, String)V
// RSA/RS256 JWS signature check. Smali verified: contains "Response must be signed
// with RS256 algorithm." as a const-string in the method body.
val ValidateResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
    filters = listOf(string("Response must be signed with RS256 algorithm.")),
)

// LicenseClient.handleError(LicenseCheckException)V
// Called on any LVL failure. Smali verified: contains "Error while checking license: "
// as first const-string argument to StringBuilder.<init>.
val HandleErrorFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "handleError",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("Lcom/pairip/licensecheck/LicenseCheckException;"),
    filters = listOf(string("Error while checking license: ")),
)

// LicenseClient.startPaywallActivity(PendingIntent)V
// Launches LicenseActivity with PAYWALL type then schedules app shutdown.
// Smali verified: method body uses "paywallintent" (lowercase) as the Intent extra key
// for the PendingIntent and "activitytype" for the PAYWALL enum extra. The string
// "PAYWALL_INTENT" appears in a different method (processResponse) — NOT this one.
val StartPaywallActivityFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "startPaywallActivity",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("Landroid/app/PendingIntent;"),
    filters = listOf(string("paywallintent")),
)


// com.pairip.application.Application.attachBaseContext(Context)V
//
// The earliest Java lifecycle hook in this app — called before any Flutter engine
// initialisation. This is the optimal injection point for writing SharedPreferences
// before PurchaseService (Dart/libapp.so) reads them on first frame.
//
// Smali (full body, smali verified — .registers 2, p0=this, p1=context):
//   invoke-static  {p1}, Lcom/pairip/licensecheck/LicenseClient;->checkLicense(Context)V   [0]
//   invoke-super   {p0, p1}, Landroid/app/Application;->attachBaseContext(Context)V          [1]
//   return-void                                                                               [2]
//
// We inject at index 2 (before return-void) so p1 (Context) is still the valid
// base context — stable and guaranteed to be non-null by Android lifecycle contract.
//
// Anchor: the unique invoke-static to LicenseClient.checkLicense() makes this method
// trivially identifiable even after obfuscation of surrounding classes.
val ApplicationAttachBaseContextFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/application/Application;",
    name = "attachBaseContext",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PROTECTED),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
            name = "checkLicense",
        ),
    ),
)
