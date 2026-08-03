package app.template.patches.wallverse.pairip

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

// Pairip's non-obfuscated startup entry point. The ordered filter verifies
// that it creates a LicenseClient and starts the full license flow.
internal object PairipCheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
            name = "initializeLicenseCheck",
            returnType = "V",
        ),
    ),
)
