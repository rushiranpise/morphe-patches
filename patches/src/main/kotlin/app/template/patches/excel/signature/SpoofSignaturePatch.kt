package app.template.patches.excel.signature

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.cert.seedCert
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

private const val EXCEL_CERT_V3 =
    "MIIGqDCCBJCgAwIBAgITMwAAAAZErL4ZV1WrCwAAAAAABjANBgkqhkiG9w0BAQwF" +
    "ADBWMQswCQYDVQQGEwJVUzEeMBwGA1UEChMVTWljcm9zb2Z0IENvcnBvcmF0aW9u" +
    "MScwJQYDVQQDEx5NaWNyb3NvZnQgQW5kcm9pZCBQbGF5IEFwcCBQQ0EwHhcNMjIw" +
    "NTEyMjAxNjQxWhcNMzQwOTMwMjAxNjQxWjCBijELMAkGA1UEBhMCVVMxEzARBgNV" +
    "BAgTCldhc2hpbmd0b24xEDAOBgNVBAcTB1JlZG1vbmQxHjAcBgNVBAoTFU1pY3Jv" +
    "c29mdCBDb3Jwb3JhdGlvbjE0MDIGA1UEAxMrTWljcm9zb2Z0IFB1Ymxpc2hlciBm" +
    "b3IgQW5kcm9pZCBBcHAgQnVuZGxlczCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCC" +
    "AgoCggIBALn51Zj4mjRzV/QoWukcePjPQ0+SlNDsVxRrOrakXF4otiaTuctq35VR" +
    "ao49LVpB6u4w9/iX4ydvsTrgAHaqKDkYd4QgoLdX2h/Z6E+OsXf9xNRJdXRqNd1y" +
    "f4HEznGuSiTOT3IEDtZVQyPX3Oe8ysaRc49fhGtcUBcpHgclUbH4x25mfcELZmw" +
    "B7aLkBJ+JczbYYcl0Qt+DBYquf/vwCh7SS4zqM3fD3/+RVNwcfxAkKwrw0PgBmE3" +
    "wsa9aHg2Ghrotpj6VGKWjm+Y4Yv0XFduhVAFRMudtK8HfMNWNLakB5Oj6Eomr80" +
    "0yBvDjM8PWRULgszQUdpuWZp3c/orxfTf9GNbWXJU9DTtzYLpUKTdLZXub06iPgR" +
    "EhFjgnD4H+JRNVdbQXZ3dYmpvubG2S4laA8qh+sUJg5bKw9zDOpdZSLV2B8bn9cD" +
    "ICsDcLDqqwiRgeAhYgUBcn0sRxK4P7H8UNBmtNYOroc+1qsFdFtxkIB9zWPSp+P4" +
    "JAtnGc6Bu1OOagXE5IKZ1YwWEg+T9Y+H1E0VH71eKL/I+k2H6HLi6SmkGGIzK1jo" +
    "5NscINGZIbRHpOs6ozImFowa4vQZ4AltYWLHmggD7T/MNj1mvOoL+LD4YSPCLmzd" +
    "4IuNxxSWtAeSSU0UiV5bmKAY+nFaRPeC5jFIrfX6KNDMDgs6yF55GRAgMBAAGjgg" +
    "E4MIIBNDAVBgNVHSUEDjAMBgorBgEEAYI3TAIBMB0GA1UdDgQWBBTWlxPw94t0Mg" +
    "x96IP1XJFz6zV94zAfBgNVHSMEGDAWgBSVBJ3bYDVMZcPfc6Wao5sRv+ls1DBfBg" +
    "NVHR8EWDBWMFSgUqBQhk5odHRwOi8vd3d3Lm1pY3Jvc29mdC5jb20vcGtpb3BzL2" +
    "NybC9NaWNyb3NvZnQlMjBBbmRyb2lkJTIwUGxheSUyMEFwcCUyMFBDQS5jcmwwbA" +
    "YIKwYBBQUHAQEEYDBeMFwGCCsGAQUFBzAChlBodHRwOi8vd3d3Lm1pY3Jvc29mdC" +
    "5jb20vcGtpb3BzL2NlcnRzL01pY3Jvc29mdCUyMEFuZHJvaWQlMjBQbGF5JTIwQX" +
    "BwJTIwUENBLmNydDAMBgNVHRMBAf8EAjAAMA0GCSqGSIb3DQEBDAUAA4ICAQAsEC/" +
    "+t6cqphgMUTGCVUvOf/gBy1wzfIx1QnITFDW5BdwQtmRxzzsrkeIv9063vnswmw+" +
    "/lq3XLISUbvqM/r6xRpoI2A6/yK2SrYUjHzmnH0wfmsEwjPWDSFhoXyR5KITG2On" +
    "YGuzPXA2Jstk4n9XiUYWjyQWXJoquO8jPHf9lk1O/Bu+UcivwXba4QvT9LngvOrG" +
    "9x8A+3ds8uFnzryjC7k0AXbxdP/p3XCgJbUmIk1UN6lReD3zsnsNvMsuMMm35lLX" +
    "RB9DUpNMy4e6qu/ibP4VjOoNMRquAO2RX8LXRFXDEaA8QU6VN3rlG4fBfw/6V97c" +
    "gKppk6M7sjPc/JuRukLMOiC71uPQzZYQ4nF/BqyiK+hF4EpUdpoV0iRC143YrftI" +
    "+xiqX3lE3eTF17rvCaVmxIhRVvIgYbZyhTWhOb8GU1+MLwe23zM0onSLXCEgYm+m" +
    "pc5lo1q5bux+uy1NoftIHX2ZLUVpB62fOmVXCLERv9By0/hpcaaQLLCbMYWe3H68" +
    "68kHH1DDflvRAZiEcg877WVwcAmOkeKTzvlUOxpv8J2pUmsazNd04pXXOOFsanGR7" +
    "tGQIPy9FOSU58xXeSXXZpJnkC0reetcHwnBp43owAulmRSnYjLRMDTeUwFkecBZYo" +
    "WUg9AqP8yLUQlSGFunlB0LGjJEFN3906TKIIQ=="

private const val EXCEL_CERT_LEGACY =
    "MIIGKjCCBBKgAwIBAgIETjM5nDANBgkqhkiG9w0BAQsFADCB1jELMAkGA1UEBhMC" +
    "VVMxEzARBgNVBAgTCldhc2hpbmd0b24xEDAOBgNVBAcTB1JlZG1vbmQxHjAcBgNV" +
    "BAoTFU1pY3Jvc29mdCBDb3Jwb3JhdGlvbjE5MDcGA1UECxMwQW5kcm9pZCBNYXJr" +
    "ZXRwbGFjZSBTaWduaW5nIGZvciBNaWNyb3NvZnQgT2ZmaWNlMUUwQwYDVQQDEzxN" +
    "aWNyb3NvZnQgQ29ycG9yYXRpb24gVGhpcmQgUGFydHkgTWFya2V0cGxhY2UgKERv" +
    "IE5vdCBUcnVzdCkwHhcNMTEwNzI5MjI1MjEyWhcNMzQwOTE5MjI1MjEyWjCB1jEL" +
    "MAkGA1UEBhMCVVMxEzARBgNVBAgTCldhc2hpbmd0b24xEDAOBgNVBAcTB1JlZG1v" +
    "bmQxHjAcBgNVBAoTFU1pY3Jvc29mdCBDb3Jwb3JhdGlvbjE5MDcGA1UECxMwQW5k" +
    "cm9pZCBNYXJrZXRwbGFjZSBTaWduaW5nIGZvciBNaWNyb3NvZnQgT2ZmaWNlMUUw" +
    "QwYDVQQDEzxNaWNyb3NvZnQgQ29ycG9yYXRpb24gVGhpcmQgUGFydHkgTWFya2V0" +
    "cGxhY2UgKERvIE5vdCBUcnVzdCkwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIK" +
    "AoICAQCRrhT3io+iRiA/0+Fe9tO+G0XS2KuvLq0W5CGU+hrXivJMKcVhZwLo3LPH" +
    "YVfgqA056IFZA8estcmaosuZKqc+gltkHWVaY/EZn39XJKLlAa77oC5XaPs0VriI" +
    "4Zz9w/HgAY16Uxarx5uUXs4g85PzKS22qdBVZYvoR3/fjs7ZDs4AvUHdFt0v80Rv" +
    "UFElkAVLbXI0+S/PObeXiAjJiEKb8aNCnGIGkCn39Q/mKO5gJIcFMmV2umgji/Ue" +
    "geJWlM5lwbkFkm3Mfo5ypPzweYQHfWfi+msdVN9c4ScrrSNaa+bxvJZ3QfTZ4wCe" +
    "G3ixuCrfk+SmqHNKiSBW4sr9vy4+vIadSxtq/cmGjYxVaBT2EHh1zfmA06FhyrFZ" +
    "x1m2UCfLkwcwf+PekkNz5GXx7UAiRprCAMtp/IW/NU3drlrcbzz0hmJ3M5+rbbB2" +
    "B8qsu2GDGTQ78oBmCqdn56c+ZcxwzVrGndY3Ul/RBe4t+pCCEBKmaOHVXsfljqoK" +
    "vHVMf8NCsvAF7n3QAVgke6ECTCnMniQBXyv2/8lg61vLGdATYkThW271EN6sITqA" +
    "uxfWuzcfYmHe0BSq8BryC0Kun0Rv/UAxDYs2jW8iDU3bUVLDGTNMu5KBfg/P7ffl" +
    "DgGC7byoKAV2izOOp96tpb3Xk6lnX1Nblgm1DoUgo81sFQ/63QIDAQABMA0GCSqG" +
    "SIb3DQEBCwUAA4ICAQBcI8qK6XA+FuBCgFW83yvkNBMMOTlPEUrATveeU6llG3YR" +
    "JvxXV7b7RzBjbim1hppDtyhCZc3G+y5aWnVqeETX0UK+k5FwGc6LOyyQVDW/iMFU" +
    "LI31TTwRyENkHNWuyW3uQJWTUl9upna++8mZRyjlmiKdj+vCVejZWm20nFfsjXt6" +
    "ILOhseXFDlHahSS81uJliRpNaR0wO/5sFZFz3LZJd3J3u1mOoSBJ18EhwonrCDgB" +
    "MhmQ7OzwUHjPEI6me6hu/tIVK1WfmfhTK6iCxDoedbcAsPJb+/3ihtSCXf+i97Jt" +
    "3dbn4GNMH1lExSIYZbS3qvkTquOOEOY2YxYz6YiFwNSn9CdiH0kKXH9TGrxvIdXI" +
    "J1pkdhKXgF9E0Lqezk327sRQztJJ47N0NQhH8Ce8soWqjLBA/ge5y2hbadOMdpC+" +
    "+M72uxGQ4wheW9edtg/NMGNnJeVIApkEQdbkCOaivOePSuhB6kAeTKmmco1b23Mm" +
    "/OKK1jE3Sh73yFlgaooLthtaDaNcnljxPtNW274mfoTIHi8jS5I43YvoTowiIanQ" +
    "D5p2mfh3UgMfjzZOxAdfvVTbjfo303UF5MF+RKi2ccUDqjJZOe972hosLPwqQF3C" +
    "NhWpdRmub1ZldE6uqXym0F5rmPCXtC7F7Nfo7cOrmDlnGPFyBbAowxQSwdsjCA=="

private val excelCertSeedPatch = rawResourcePatch(default = false) {
    execute {
        seedCert(EXCEL_CERT_V3)
        seedCert(EXCEL_CERT_LEGACY)
    }
}

private val excelSpoofSignaturePatch = bytecodePatch(
) {
    dependsOn(excelCertSeedPatch, spoofSignatureVerificationPatch)
}

@JvmSynthetic
internal fun excelSpoofSignatureDependency() = excelSpoofSignaturePatch
