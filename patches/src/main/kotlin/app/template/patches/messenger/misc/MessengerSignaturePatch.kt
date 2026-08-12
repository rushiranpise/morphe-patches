package app.template.patches.messenger.misc

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import app.template.patches.shared.cert.seedCert
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

// SHA-256: E3F9E1E0CF99D0E56A055BA65E241B3399F7CEA524326B0CDD6EC1327ED0FDC1
// Subject: Facebook Corporation / Facebook Mobile / Palo Alto CA US
// Valid:   2009-08-31 → 2050-09-25
// Extracted from: com.facebook.orca 573.0.0.44.88 (APK Signature Scheme v2)
private const val MESSENGER_B64 =
    "MIICaDCCAdECBEqcRhAwDQYJKoZIhvcNAQEEBQAwejELMAkGA1UEBhMCVVMxCzAJ" +
    "BgNVBAgTAkNBMRIwEAYDVQQHEwlQYWxvIEFsdG8xGDAWBgNVBAoTD0ZhY2Vib29r" +
    "IE1vYmlsZTERMA8GA1UECxMIRmFjZWJvb2sxHTAbBgNVBAMTFEZhY2Vib29rIENv" +
    "cnBvcmF0aW9uMCAXDTA5MDgzMTIxNTIxNloYDzIwNTAwOTI1MjE1MjE2WjB6MQsw" +
    "CQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExEjAQBgNVBAcTCVBhbG8gQWx0bzEYMBYG" +
    "A1UEChMPRmFjZWJvb2sgTW9iaWxlMREwDwYDVQQLEwhGYWNlYm9vazEdMBsGA1UE" +
    "AxMURmFjZWJvb2sgQ29ycG9yYXRpb24wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGB" +
    "AMIHlR3464yX2TugyMEALJKPqwDcG0L8peZumcwwI+0tIU2CK8WejjXdz19Ex66" +
    "K3lDX4MQ09QDmwTH0ooNPmH/EZAYRXeIBjruw1aPCYb2XWBzP73avxxNabVnohV7" +
    "NfqzI+HN+eUxgp2HFNrcrEfrI5gP12hotVKoQO4oTwNvBAgMBAAEwDQYJKoZIhvcN" +
    "AQEEBQADgYEAXum+i8uyUGSNO3QSkKgqHJ3C52oK8vIijx2fnEAHUpxEanAXXFqQ" +
    "DVFBgShm20a+ZVniFBYWSDmYIR9KZzFJ+yIyoQ0kdmOyapAx4V+EvBx00UH/mKAt" +
    "dvhbLIqyVxtkabIy2Odop/fKBPer5Kd1YVkWwHlAZWtYcXRXtCvZKKI="

private val messengerCertSeedPatch = rawResourcePatch(default = false) {
    execute {
        seedCert(MESSENGER_B64)
    }
}

// Applied automatically as a dependency of every user-facing Messenger patch.
// Wires cert seed + signature spoof + install-source spoof in one step.
internal val messengerSignaturePatch = bytecodePatch(
    name = "Messenger signature + install source",
    description = "Pre-seeds the Facebook signing certificate and enables signature and install-source spoofing.",
    default = false,
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(
        messengerCertSeedPatch,
        spoofSignatureVerificationPatch,
        spoofInstallSourcePatch,
    )
}
