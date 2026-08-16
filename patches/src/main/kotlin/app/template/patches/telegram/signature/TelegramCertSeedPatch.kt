package app.template.patches.telegram.signature

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.cert.seedCert
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

// ════════════════════════════════════════════════════════════════════════════════
// Telegram signing certificates (PKCS7 DER, Base64-encoded)
//
// org.telegram.messenger (regular + web) — signed by Nikolay Kudasov / VK
// org.telegram.plus                      — signed by android developer plus
// ════════════════════════════════════════════════════════════════════════════════

// // org.telegram.messenger + org.telegram.messenger.web — identical cert
// private const val TELEGRAM_CERT =
//     "MIIDWAYJKoZIhvcNAQcCoIIDSTCCA0UCAQExDzANBglghkgBZQMEAgEFADALBgkq" +
//     "hkiG9w0BBwGgggIbMIICFzCCAYCgAwIBAgIEUh+dSTANBgkqhkiG9w0BAQUFADBQ" +
//     "MRkwFwYDVQQHExBTYWludC1QZXRlcnNidXJnMQswCQYDVQQKEwJWSzELMAkGA1UE" +
//     "CxMCVksxGTAXBgNVBAMTEE5pa29sYXkgS3VkYXNob3cwHhcNMTMwODI5MTkxMzEz" +
//     "WhcNMzgwODIzMTkxMzEzWjBQMRkwFwYDVQQHExBTYWludC1QZXRlcnNidXJnMQsw" +
//     "CQYDVQQKEwJWSzELMAkGA1UECxMCVksxGTAXBgNVBAMTEE5pa29sYXkgS3VkYXNo" +
//     "b3YwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAN9emToN7Aq1tVff/3fgsiJx" +
//     "hsvxPR/R7Y6d61ZQxf1EZ7tRv6WFIo0IS9JwRfdBW3xOOPCL42Jjmi7rmwx0naRg" +
//     "8nBfan4UrKdqvjNgrwC3GcxfP/TU2gWVgyfpSLNnnmQXrXuqh3m51ol5m6NFg5oE" +
//     "n9RDYkmQVKCAOgF4x3N5AgMBAAEwDQYJKoZIhvcNAQEFBQADgYEA3aWM3ZAVnEMe" +
//     "zEoVkC6vsHpQ4Bup1PjmVewUsGvY6HcSOXEKKJkQOeAuNSdi61JK8HYCu9+0edNx" +
//     "hlilNNQR36swEiyNCl79FlpiBmnYCiIaBKx9aLOBEVDHac+X0ydL6bnyfExYd+q7" +
//     "z4mQQJ5ZQ9+N61CfqD1o6rx098WXZ0MxggEBMIH+AgEBMFgwUDEZMBcGA1UEBxMQ" +
//     "U2FpbnQtUGV0ZXJzYnVyZzELMAkGA1UEChMCVksxCzAJBgNVBAsTAlZLMRkwFwYD" +
//     "VQQDExBOaWtvbGF5IEt1ZGFzaG92AgRSH51JMA0GCWCGSAFlAwQCAQUAMA0GCSqG" +
//     "SIb3DQEBAQUABIGAstfEGVVVtxzYUGEoaFqzk1am7pizG4f9NBIuKjor+ejDq8Tj" +
//     "6+W8aNyaxfr5tOlOFCz4ZrmE821B487qqjlS1+cverVvxOaIxsKmWGmVwh+s4UHD" +
//     "6RJeJhb4Hz31jUjzZzTIU3Py35qkU03gPNs97jFonXAXLlBg9Z+PW3RHTk0="

// // org.telegram.plus — signed by android developer plus / Rafael
// private const val TELEGRAM_PLUS_CERT =
//     "MIIEQwYJKoZIhvcNAQcCoIIENDCCBDACAQExDzANBglghkgBZQMEAgEFADALBgkq" +
//     "hkiG9w0BBwGgggKzMIICrzCCAhgCCQDH4XYjX14GtDANBgkqhkiG9w0BAQUFADCB" +
//     "mzELMAkGA1UEBhMCRVMxETAPBgNVBAgTCEFsaWNhbnRlMQ4wDAYDVQQHEwVSYWZh" +
//     "bDEfMB0GA1UEChMWYW5kcm9pZCBkZXZlbG9wZXIgcGx1czEQMA4GA1UECxMHc2Vj" +
//     "dGlvbjESMBAGA1UEAxMJcmFmYWxlbnNlMSIwIAYJKoZIhvcNAQkBFhNyYWZhbGVu" +
//     "c2VAZ21haWwuY29tMB4XDTEzMDQyMjE4MzEyNloXDTQwMDkwNjE4MzEyNlowgZsx" +
//     "CzAJBgNVBAYTAkVTMREwDwYDVQQIEwhBbGljYW50ZTEOMAwGA1UEBxMFUmFmYWwx" +
//     "HzAdBgNVBAoTFmFuZHJvaWQgZGV2ZWxvcGVyIHBsdXMxEDAOBgNVBAsTB3NlY3Rp" +
//     "b24xEjAQBgNVBAMTCXJhZmFsZW5zZTEiMCAGCSqGSIb3DQEJARYTcmFmYWxlbnNl" +
//     "QGdtYWlsLmNvbTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAqQ15N+7XjRR8" +
//     "UGR40CwK9wE2+7R3tWPjq2qSBxfpKy3YxxNm8bOzXiyR3JECZRSNUE1OyAVrb1jv" +
//     "nIPT2SzfZdauAAzpfYiuhdEseSrqOQtlup9hHeol+fZCKHQOidgZotuvUW8j3v3o" +
//     "4fs+8HKN4/twHpmJBGlgW+QrOP7fTScCAwEAATANBgkqhkiG9w0BAQUFAAOBgQBz" +
//     "CLfFb/xUD61z/HM+EmTpXoE44uXTuovTd8iBhVm4G6WAaw3txLKIlnKbgsqLVlyB" +
//     "2QCCUj/kFCklyT0ng4aVFsUDZQYetxKckGFZGAlyj8nAvN//dOCiRwGotx9cyiGW" +
//     "9JgzcmkvCMtygd2HzD8QNQk9FL7V0YgakERbpd+xFTGCAVQwggFQAgEBMIGpMIGb" +
//     "MQswCQYDVQQGEwJFUzERMA8GA1UECBMIQWxpY2FudGUxDjAMBgNVBAcTBVJhZmFs" +
//     "MR8wHQYDVQQKExZhbmRyb2lkIGRldmVsb3BlciBwbHVzMRAwDgYDVQQLEwdzZWN0" +
//     "aW9uMRIwEAYDVQQDEwlyYWZhbGVuc2UxIjAgBgkqhkiG9w0BCQEWE3JhZmFsZW5z" +
//     "ZUBnbWFpbC5jb20CCQDH4XYjX14GtDANBglghkgBZQMEAgEFADANBgkqhkiG9w0B" +
//     "AQEFAASBgIELoJ/+UfQFws9TR6BWTUI7ZUPzk2EZZsuqiL2IS4GVBQYE6Ivq2ggV" +
//     "BYE9uBnxZjI68FOR0GHiG/FG1ZiBQUtLYimntW0I4HtUaHg4RX/iNpyXqWUs5lI0" +
//     "WFoIIQjrRcFJQJMlPfsokos9K8eZoEhsLF+8W7oz2KJwi9MiKYWS"

// // Seed both certs so auto-extraction works for all three package variants
// internal val telegramCertSeedPatch = rawResourcePatch(default = false) {
//     execute {
//         seedCert(TELEGRAM_CERT)       // org.telegram.messenger + web
//         seedCert(TELEGRAM_PLUS_CERT)  // org.telegram.plus
//     }
// }

// ════════════════════════════════════════════════════════════════════════════════
// Telegram base patch — auto-applied as dependsOn by every Telegram patch.
// Ensures spoof install source, spoof signature, and Firebase cert fix are
// always active whenever any Telegram patch is enabled, without requiring the
// user to manually select these support patches.
// ════════════════════════════════════════════════════════════════════════════════

val telegramBasePatch = bytecodePatch(
    default = false,
) {
    dependsOn(
        // telegramCertSeedPatch,
        spoofSignatureVerificationPatch,
        spoofFirebaseCertHashPatch,
        spoofInstallSourcePatch,
    )
}

/** Wire this into every Telegram patch via dependsOn(telegramSpoofDependency()). */
@JvmSynthetic
internal fun telegramSpoofDependency() = telegramBasePatch
