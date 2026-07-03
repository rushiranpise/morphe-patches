package app.template.patches.inmigreat

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import app.template.patches.shared.Constants.INMIGREAT_COMPATIBILITY

/**
 * LicenseClient.checkLicense(Context)V — PairIP license check entry point.
 *
 * Called from com.pairip.application.Application.attachBaseContext() before
 * super.attachBaseContext(), so it runs at every cold start.
 *
 * Flow on failure:
 *   checkLicense() → initializeLicenseCheck() → connectToLicensingService()
 *   → checkLicenseInternal() → processResponse(responseCode, bundle)
 *   → if responseCode != 0: handleError() → startErrorDialogActivity()
 *   → LicenseActivity blocks the UI with a "pirated app" / "buy license" screen.
 *
 * The check is async (connects to Google Play licensing service), so the app
 * does launch — but shortly after startup a blocking dialog appears if the
 * license is invalid or the patched APK is detected.
 *
 * Fix: replace the entire body of checkLicense() with return-void.
 * The method has no try-catch — clean replacement, no VerifyError risk.
 *
 * Fingerprinted by "Skipping license check in isolated process." which is unique
 * to this method and won't collide with other classes.
 */
private val CheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(string("Skipping license check in isolated process.")),
)

@Suppress("unused")
val inmigreatBypassLicensePatch = bytecodePatch(
    name = "Bypass PairIP License Check",
    description = "Makes LicenseClient.checkLicense() a no-op so the app never shows the license-denied blocking screen.",
    default = true,
) {
    compatibleWith(INMIGREAT_COMPATIBILITY)

    execute {
        CheckLicenseFingerprint
            .match(classDefBy(CheckLicenseFingerprint.definingClass!!))
            .method
            .apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }
    }
}
