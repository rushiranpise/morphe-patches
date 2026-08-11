package app.template.patches.aviate.license

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AVIATE_COMPATIBILITY
import app.template.patches.shared.returnEarly

private const val LICENSE_CLIENT = "Lcom/pairip/licensecheck/LicenseClient;"
private const val LICENSE_CHECK_STATE = "Lcom/pairip/licensecheck/LicenseClient\$LicenseCheckState;"

@Suppress("unused")
val aviateLicensePatch = bytecodePatch(
    name = "Aviate License",
    description = "Bypasses the Pairip license check to prevent non-licensed app termination."
) {
    compatibleWith(AVIATE_COMPATIBILITY)

    execute {
        // 1. Force responseCode=LICENSED (0x0) before processResponse branches on it.
        //    Without this, re-signed APKs receive NOT_LICENSED and are killed.
        ProcessLicenseResponseFingerprint.method.addInstruction(
            0,
            "const/4 p1, 0x0"
        )

        // 2. Skip cryptographic signature validation.
        //    validateResponse() always throws LicenseCheckException on re-signed APKs
        //    because the APK signature no longer matches the original certificate.
        ValidateLicenseResponseFingerprint.method.returnEarly()

        // 3. Block the license check at the entry point.
        //    checkLicense(Context) is the public API called at app startup.
        //    Returning early prevents any connection to Google Play licensing service.
        CheckLicenseFingerprint.method.returnEarly()
    }
}
