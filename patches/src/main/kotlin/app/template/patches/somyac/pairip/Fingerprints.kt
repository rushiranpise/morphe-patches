package app.template.patches.somyac.pairip

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.Opcode

/**
 * Somyac companion apps (Mini Web Browser, Translator) use Pairip v2 —
 * the LicenseActivity / LicenseClient variant (no VMRunner / StartupLauncher).
 *
 * Entry point: LicenseContentProvider.onCreate() instantiates LicenseClient
 * and calls initializeLicenseCheck(), which connects to the Play licensing
 * service and eventually calls processResponse(). Both apps share identical
 * bytecode; only LicenseClient.licensePubKey and LicenseClient.packageName
 * static fields differ.
 *
 * Targets (verified in classes.dex, both 1.0.4/1.5.0 versionCode 303):
 *
 *   LicenseContentProvider.onCreate()          → returnEarly(true)
 *     — prevents initializeLicenseCheck() from ever running.
 *       Belt-and-suspenders: even if the provider is called, the next two
 *       patches handle the response and validation stages independently.
 *
 *   LicenseClient.checkLicense(Context)        → returnEarly()
 *     — public static entry point, returnEarly() as extra guard.
 *
 *   LicenseClient.processResponse(int, Bundle) → const/4 p1, 0x0 at index 0
 *     — forces responseCode to 0 (LICENSED) before any branch.
 *       Smali: p1==0 → licensed path, p1==2 → paywall, p1==3 → exit.
 *
 *   repeatedCheckEnabled (SGET_BOOLEAN inside processResponse)
 *     — zeroed so no repeated licence check is ever scheduled.
 *
 *   LicenseResponseHelper.validateResponse(Bundle, String) → returnEarly()
 *     — short-circuits JWS signature verification.
 */

// LicenseContentProvider.onCreate(): Z — returnEarly(true)
internal val LicenseContentProviderOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseContentProvider;",
    name = "onCreate",
    returnType = "Z",
    parameters = listOf()
)

// LicenseClient.checkLicense(Context): V — returnEarly()
internal val CheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;")
)

// LicenseClient.processResponse(int responseCode, Bundle): V — const/4 p1, 0x0
// Private method, fingerprinted via the bridge synthetic accessor.
// Bridge smali: -$$Nest$mprocessResponse(LicenseClient, int, Bundle): V
internal val ProcessLicenseResponseFingerprint = Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/pairip/licensecheck/LicenseClient;" &&
                method.name == "processResponse"
    }
)

// sget-boolean repeatedCheckEnabled inside processResponse
// Zeroed so the licensed state is not re-checked on a timer.
// Smali line ~780: sget-boolean p1, Lcom/pairip/licensecheck/LicenseClient;->repeatedCheckEnabled:Z
internal val RepeatedCheckFingerprint = Fingerprint(
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_BOOLEAN,
            name = "repeatedCheckEnabled"
        )
    )
)

// LicenseResponseHelper.validateResponse(Bundle, String): V — returnEarly()
internal val ValidateLicenseResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;")
)
