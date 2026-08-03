package app.template.patches.wristweb.pairip

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets LicenseClient.checkLicense(Context)V — the Pairip license check entry point.
 *
 * PUBLIC STATIC, single Context parameter, no try-catch blocks.
 * returnEarly() inserts return-void at index 0 — the check never runs at all.
 *
 * Anchors:
 *   1. string("LicenseClient") — TAG constant used in the isolated-process log
 *   2. string("Skipping license check in isolated process.") — unique to this method
 *
 * VERIFIED v1.1.8 (versionCode 19), classes.dex line 250:
 *   .method public static checkLicense(Landroid/content/Context;)V
 *   .registers 2
 */
object PairipCheckLicenseFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("LicenseClient"),
        string("Skipping license check in isolated process."),
    ),
)
