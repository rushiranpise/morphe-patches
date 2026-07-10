package app.template.patches.picturemushroom

import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.cert.seedCert

private const val PICTURE_MUSHROOM_CERT =
    "MIIDdTCCAl2gAwIBAgIEHXeXxDANBgkqhkiG9w0BAQsFADBqMQswCQYDVQQGEwJD" +
    "TjERMA8GA1UECBMIWmhlamlhbmcxETAPBgNVBAcTCEhhbmd6aG91MRAwDgYDVQQK" +
    "EwdHbG9yaXR5MRQwEgYDVQQLEwtOZXcgUHJvZHVjdDENMAsGA1UEAxMEV2laSDAg" +
    "Fw0xOTExMjAwNzQxMjZaGA8yMTE5MTAyNzA3NDEyNlowajELMAkGA1UEBhMCQ04x" +
    "ETAPBgNVBAgTCFpoZWppYW5nMREwDwYDVQQHEwhIYW5nemhvdTEQMA4GA1UEChMH" +
    "R2xvcml0eTEUMBIGA1UECxMLTmV3IFByb2R1Y3QxDTALBgNVBAMTBFdpWkgwggEi" +
    "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC2u1bYPgh5BXngiWLu2FO7U1XC" +
    "34j7BXqQ4r+IMwo5kVjZ30TIkJaSYVPanzQhMarNT2ZbRJ6d/JMSExScwMxso4e+" +
    "WljizIsw1f1Gw8UATlUONxRmzGzPjkYuejLyKE3aX5wyD6E0k2hN3KNmlqTrVcA8" +
    "aqRlDwdTXerMO8oOiUgKFQ4BJ1LaGCiiQiYUsSC0fMY44vSoU1sYhqVXjLHgxCfi" +
    "PBj7w0uoTbkf3Qia6x+qIxpwDPhv+kJeEd5CDC/M1lz8qL9pkMuEQ6DGGG9WkcAF" +
    "rWLWLVf/PZ/MtlPKELatvZjQ6QKTmxA9eYkLN5LJVYwLnlw0XPOw75smY7RdAgMB" +
    "AAGjITAfMB0GA1UdDgQWBBTX/+3YHWQ7/IjMrKjKgojUR/RnFjANBgkqhkiG9w0B" +
    "AQsFAAOCAQEApRe4ffEDQjOy3BQ/FsNctUSfjle295c5yRdbXdk3JZzvX/BLw7tZ" +
    "IqVsIs6LhLWj+bAAKHHDxai7mrnFRVstc0GYul7YjtWM3obHc9uHQimPxa22GH6W" +
    "Vs8dQk5FfIIpqONLq/xp6NoWAfbMpIqYE6G5ShPge6RgQ5iCunAK1+2cG/1QboFd" +
    "rtm1CugDqsNC/gcCVHKWppSDC75gF4BhB6Zv8dxEW2AGyxphQr3Rx2KLXNYZgITT" +
    "Y8+xhm0Vm6tgW2oDeXsPOI/Yn8HaJ46hQlUyMfsmreFwHCj9y87dBMsMlpdhNlbi" +
    "+cZCu3rOMqp1+BCkltSjPkBdLY+2v/rSBA=="

val pictureMushroomCertSeedPatch = rawResourcePatch(default = false) {
    execute { seedCert(PICTURE_MUSHROOM_CERT) }
}
