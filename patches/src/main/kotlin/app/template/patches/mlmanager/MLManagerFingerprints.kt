package app.template.patches.mlmanager

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * j2/o.B(Context)Z — CRC32 package-name check.
 *
 * Returns true only if package CRC32 == 0x31e3c18b (pro package).
 * Used in AppsFragment to gate pro tabs AND in z1/a0 to trigger
 * LicenseChecker + server validation. Must return true for pro
 * features to unlock, but dontAllow/serverValidation callbacks
 * must be suppressed to prevent LicenseActivity.
 */
val ProPackageCheckFingerprint = Fingerprint(
    definingClass = "Lj2/o;",
    name = "B",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * z1/a0.e(I)V — LicenseCheckerCallback.dontAllow(int).
 *
 * Called when Google Play license check fails. Launches LicenseActivity
 * and finishes the current activity. Must be suppressed (return-void)
 * to prevent the license error screen.
 */
val LicenseDontAllowFingerprint = Fingerprint(
    definingClass = "Lz1/a0;",
    name = "e",
    parameters = listOf("I"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * z1/a0.c(ServerValidationsResponse)V — server validation callback.
 *
 * Called when server-side validation returns an error response.
 * Launches LicenseActivity and finishes. Must be suppressed.
 */
val ServerValidationCallbackFingerprint = Fingerprint(
    definingClass = "Lz1/a0;",
    name = "c",
    parameters = listOf("Lcom/javiersantos/servervalidation/objects/ServerValidationsResponse;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)
