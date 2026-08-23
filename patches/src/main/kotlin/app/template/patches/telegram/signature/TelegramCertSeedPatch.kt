package app.template.patches.telegram.signature

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.cert.seedCert
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

// ════════════════════════════════════════════════════════════════════════════════
// Telegram signing certificates — DER-encoded X.509 (NOT PKCS7)
//
// seedCert() requires the raw DER X.509 certificate extracted from inside the
// PKCS7 .RSA file. Using the full PKCS7 bytes causes Firebase 403 + SSL failures
// because the cert hash doesn't match what getCertificateSHA256Fingerprint() expects.
//
// org.telegram.messenger + org.telegram.messenger.web — same VK / Nikolay Kudasov key
// org.telegram.plus                                  — android developer plus / Rafael
// ════════════════════════════════════════════════════════════════════════════════

// org.telegram.messenger + org.telegram.messenger.web (539 bytes, same key)
private const val TELEGRAM_CERT =
    "MIICFzCCAYCgAwIBAgIEUh+dSTANBgkqhkiG9w0BAQUFADBQMRkwFwYDVQQHExBT" +
    "YWludC1QZXRlcnNidXJnMQswCQYDVQQKEwJWSzELMAkGA1UECxMCVksxGTAXBgNV" +
    "BAMTEE5pa29sYXkgS3VkYXNob3YwHhcNMTMwODI5MTkxMzEzWhcNMzgwODIzMTkx" +
    "MzEzWjBQMRkwFwYDVQQHExBTYWludC1QZXRlcnNidXJnMQswCQYDVQQKEwJWSzEL" +
    "MAkGA1UECxMCVksxGTAXBgNVBAMTEE5pa29sYXkgS3VkYXNob3YwgZ8wDQYJKoZI" +
    "hvcNAQEBBQADgY0AMIGJAoGBAN9emToN7Aq1tVff/3fgsiJxhsvxPR/R7Y6d61ZQ" +
    "xf1EZ7tRv6WFIo0IS9JwRfdBW3xOOPCL42Jjmi7rmwx0naRg8nBfan4UrKdqvjNg" +
    "rwC3GcxfP/TU2gWVgyfpSLNnnmQXrXuqh3m51ol5m6NFg5oEn9RDYkmQVKCAOgF4" +
    "x3N5AgMBAAEwDQYJKoZIhvcNAQEFBQADgYEA3aWM3ZAVnEMezEoVkC6vsHpQ4Bup" +
    "1PjmVewUsGvY6HcSOXEKKJkQOeAuNSdi61JK8HYCu9+0edNxhlilNNQR36swEiyN" +
    "Cl79FlpiBmnYCiIaBKx9aLOBEVDHac+X0ydL6bnyfExYd+q7z4mQQJ5ZQ9+N61Cf" +
    "qD1o6rx098WXZ0M="

// org.telegram.plus (691 bytes, android developer plus / Rafael)
private const val TELEGRAM_PLUS_CERT =
    "MIICrzCCAhgCCQDH4XYjX14GtDANBgkqhkiG9w0BAQUFADCBmzELMAkGA1UEBhMC" +
    "RVMxETAPBgNVBAgTCEFsaWNhbnRlMQ4wDAYDVQQHEwVSYWZhbDEfMB0GA1UEChMW" +
    "YW5kcm9pZCBkZXZlbG9wZXIgcGx1czEQMA4GA1UECxMHc2VjdGlvbjESMBAGA1UE" +
    "AxMJcmFmYWxlbnNlMSIwIAYJKoZIhvcNAQkBFhNyYWZhbGVuc2VAZ21haWwuY29t" +
    "MB4XDTEzMDQyMjE4MzEyNloXDTQwMDkwNjE4MzEyNlowgZsxCzAJBgNVBAYTAkVT" +
    "MREwDwYDVQQIEwhBbGljYW50ZTEOMAwGA1UEBxMFUmFmYWwxHzAdBgNVBAoTFmFu" +
    "ZHJvaWQgZGV2ZWxvcGVyIHBsdXMxEDAOBgNVBAsTB3NlY3Rpb24xEjAQBgNVBAMT" +
    "CXJhZmFsZW5zZTEiMCAGCSqGSIb3DQEJARYTcmFmYWxlbnNlQGdtYWlsLmNvbTCB" +
    "nzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAqQ15N+7XjRR8UGR40CwK9wE2+7R3" +
    "tWPjq2qSBxfpKy3YxxNm8bOzXiyR3JECZRSNUE1OyAVrb1jvnIPT2SzfZdauAAzp" +
    "fYiuhdEseSrqOQtlup9hHeol+fZCKHQOidgZotuvUW8j3v3o4fs+8HKN4/twHpmJ" +
    "BGlgW+QrOP7fTScCAwEAATANBgkqhkiG9w0BAQUFAAOBgQBzCLfFb/xUD61z/HM+" +
    "EmTpXoE44uXTuovTd8iBhVm4G6WAaw3txLKIlnKbgsqLVlyB2QCCUj/kFCklyT0n" +
    "g4aVFsUDZQYetxKckGFZGAlyj8nAvN//dOCiRwGotx9cyiGW9JgzcmkvCMtygd2H" +
    "zD8QNQk9FL7V0YgakERbpd+xFQ=="

internal val telegramCertSeedPatch = rawResourcePatch(default = false) {
    execute {
        // Seed only the cert that belongs to the package being patched.
        // seedCert() uses last-write-wins for autoSha1, so calling both unconditionally
        // meant patching web always ended up with Plus cert SHA-1 (49EBB9) as autoSha1
        // — wrong value fed into spoofFirebaseCertHashPatch and getFingerprintHashForPackage.
        //
        // rawResourcePatch has no implicit packageName binding — read it from the manifest.
        val pkg = Regex("""package="([^"]+)"""")
            .find(get("AndroidManifest.xml").readText())
            ?.groupValues?.get(1)
            ?: "unknown"

        if (pkg == "org.telegram.plus") {
            seedCert(TELEGRAM_PLUS_CERT)
        } else {
            seedCert(TELEGRAM_CERT)  // org.telegram.messenger + org.telegram.messenger.web
        }
    }
}

val telegramBasePatch = bytecodePatch(default = false) {
    dependsOn(
        telegramCertSeedPatch,
        spoofSignatureVerificationPatch,
        spoofFirebaseCertHashPatch,
        spoofInstallSourcePatch,
    )
}

@JvmSynthetic
internal fun telegramSpoofDependency() = telegramBasePatch
