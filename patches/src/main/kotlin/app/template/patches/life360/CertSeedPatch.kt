package app.template.patches.life360

import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.cert.seedCert

private const val LIFE360_CERT =
    "MIICSTCCAbKgAwIBAgIESzwL1zANBgkqhkiG9w0BAQUFADBoMQswCQYDVQQGEwJVUzELMAkGA1UECBMC" +
        "Q0ExFjAUBgNVBAcTDVNhbiBGcmFuY2lzY28xEDAOBgNVBAoTB0xpZmUzNjAxEDAOBgNVBAsTB1Vu" +
        "a25vd24xEDAOBgNVBAMTB1Vua25vd24wIBcNMDkxMjMxMDIyNjMxWhgPMjA2NDEwMDMwMjI2MzFa" +
        "MGgxCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQTEWMBQGA1UEBxMNU2FuIEZyYW5jaXNjbzEQMA4G" +
        "A1UEChMHTGlmZTM2MDEQMA4GA1UECxMHVW5rbm93bjEQMA4GA1UEAxMHVW5rbm93bjCBnzANBgkq" +
        "hkiG9w0BAQEFAAOBjQAwgYkCgYEA4ftW4hOZu42Tk19E3VZQ63R7DdJmj4KtaItZrK7hh/WuP9LB" +
        "dsoYuQV10IO4u9DPslGoB3f9OLzTT+ENolJSJDjWhQq0EtMsBC10vvwLOZb7eBGRGzIOLToxQy/8" +
        "Duxd2Ima6kDevMnePS7Ifa5wMb4Mu2NTvrqw1/hYP0MIDOMCAwEAATANBgkqhkiG9w0BAQUFAAOB" +
        "gQCx5/0NW8uXXDgXCRZPEFyuHpk+FY6GhoaXn0fsLNkNPgx7ApO3ppvuKM5g/jHQ1M8fSaGspi3g" +
        "C/rFYlmhwtxJy3rmAfM0iKj5L5j9c6PQB9aa4VgEzInyfYh7s1wbDSp3Rdm3yUmkyvWyy6q3teel" +
        "/xt5eNRbhPtde4nW82FetA=="

internal val life360CertSeedPatch = rawResourcePatch(default = false) {
    execute { seedCert(LIFE360_CERT) }
}
