package app.template.patches.aviate.license

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

private const val LICENSE_CLIENT = "Lcom/pairip/licensecheck/LicenseClient;"
private const val LICENSE_RESPONSE_HELPER = "Lcom/pairip/licensecheck/LicenseResponseHelper;"

/**
 * Targets processResponse(I, Bundle) in LicenseClient.
 * Called by the license server callback with responseCode:
 *   0 = LICENSED, 1 = NOT_LICENSED, 2 = RETRY, 3 = ERROR
 * Inject const/4 p1, 0x0 at index 0 to always force responseCode=LICENSED.
 * Filter: unique combination of validateResponse + Log.i calls, only in processResponse.
 */
object ProcessLicenseResponseFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("I", "Landroid/os/Bundle;"),
    filters = listOf(
        methodCall(
            definingClass = LICENSE_RESPONSE_HELPER,
            name = "validateResponse",
        ),
        methodCall(
            definingClass = "Landroid/util/Log;",
            name = "i",
        ),
    ),
    custom = { _, classDef -> classDef.type == LICENSE_CLIENT }
)

/**
 * Targets validateResponse(Bundle, String) in LicenseResponseHelper.
 * Performs cryptographic signature verification of the license response.
 * Always throws LicenseCheckException on re-signed APKs.
 * returnEarly() skips the entire body so no exception is thrown.
 */
object ValidateLicenseResponseFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
    custom = { _, classDef -> classDef.type == LICENSE_RESPONSE_HELPER }
)

/**
 * Targets checkLicense(Context) — the public entry point that starts the whole flow.
 * returnEarly() stops the license check before it even connects to Google Play.
 */
object CheckLicenseFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(
            definingClass = LICENSE_CLIENT,
            name = "isIsolatedProcess",
        ),
    ),
    custom = { _, classDef -> classDef.type == LICENSE_CLIENT }
)
