package app.template.patches.topwallpapers.pairip

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import app.template.patches.shared.Constants.TOPWALLPAPERS_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ─────────────────────────────────────────────────────────────────────────────
// Pairip variant: bytecode-only LVL (no VMRunner, no SignatureCheck,
// no libpairipcore.so). The arm64 split contains only libdatastore and
// libunitycoherencenative — no native pairip component.
//
// attachBaseContext (classes2.dex):
//   invoke-static {p1}, LicenseClient;->checkLicense(Context)V
//   invoke-super   {p0, p1}, super->attachBaseContext(Context)V
//
// super class: hd.uhd.live.wallpapers.topwallpapers.application.AppLoader
// ─────────────────────────────────────────────────────────────────────────────

/**
 * LicenseClient.checkLicense(Context)V — classes2.dex
 *
 * Static entry point called from Application.attachBaseContext on every launch.
 * Instantiates LicenseClient and calls initializeLicenseCheck() which connects
 * to the Play Store licensing service, then processes the response via
 * processResponse() → validateResponse(). On a failed check it shows
 * LicenseActivity (a blocking fullscreen "not licensed" screen).
 *
 * No-oping this at the entry point prevents the entire check from starting.
 */
private val CheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;")
)

/**
 * LicenseResponseHelper.validateResponse(Bundle, String)V — classes2.dex
 *
 * Called from LicenseClient.processResponse() with the raw Play Store LVL
 * response. Verifies the JWS signature against the app's public RSA key.
 * Throws LicenseCheckException on any mismatch → triggers blocking LicenseActivity.
 *
 * No-oping ensures that even if checkLicense somehow runs (e.g. from a cached
 * pending task), signature verification always passes silently.
 */
private val ValidateResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;")
)

@Suppress("unused")
val topWallpapersPairipPatch = bytecodePatch(
    name = "TopWallpapers Disable License Check",
    description = "Removes the Pairip Play Store license verification check invoked at startup.",
) {
    compatibleWith(TOPWALLPAPERS_COMPATIBILITY)

    execute {
        // Stop the license check at the entry point — no connection to Play Store LVL
        CheckLicenseFingerprint.method.returnEarly()

        // Belt-and-suspenders: no-op signature verification so any pending
        // or background check always passes without throwing LicenseCheckException
        ValidateResponseFingerprint.method.returnEarly()
    }
}
