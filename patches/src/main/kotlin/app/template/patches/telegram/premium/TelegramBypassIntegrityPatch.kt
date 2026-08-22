package app.template.patches.telegram.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.AndroidUtilitiesGetCertFingerprintFingerprint
import app.template.patches.telegram.SafetyNetCheckFingerprint
import app.template.patches.telegram.signature.telegramSpoofDependency
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

// SHA-256 of the original signing certificate (X.509 DER) for each package variant.
// Computed as: SHA-256(CertificateFactory("X509").generateCertificate(pkcs7Stream).encoded)
// i.e. the hash of the DER-encoded X.509 cert extracted from the APK's META-INF/[name].RSA PKCS7 blob.
// This is exactly what getCertificateSHA256Fingerprint() computes at runtime.
//
// Do NOT use the raw PKCS7 SHA-256 — that's a different (larger) byte sequence.
//
// Verified against (APKMirror bundletool-signed builds):
//   org.telegram.messenger      12.9.2   versionCode 69912  (META-INF/BNDLTOOL.RSA)
//   org.telegram.messenger.web  12.9.2   versionCode 69919  (META-INF/CERT.RSA)
//   org.telegram.plus           12.9.0.1 versionCode 22437  (META-INF/BNDLTOOL.RSA)
//
// messenger + web share the same Nikolay Kudasov/VK signing key → identical hash.
private val CERT_HASHES = mapOf(
    "org.telegram.messenger"     to "49C1522548EBACD46CE322B6FD47F6092BB745D0F88082145CAF35E14DCC38E1",
    "org.telegram.messenger.web" to "49C1522548EBACD46CE322B6FD47F6092BB745D0F88082145CAF35E14DCC38E1",
    "org.telegram.plus"          to "6EBB622268AAD319DBE8A1F414837D2843A9B35856AEFB7DEE2971A3D493F276",
)

private var detectedPackageName = ""

private val readPackageNamePatch = app.morphe.patcher.patch.resourcePatch(default = false) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            detectedPackageName = (doc.getElementsByTagName("manifest").item(0)
                as org.w3c.dom.Element).getAttribute("package")
        }
    }
}

@Suppress("unused")
val telegramBypassIntegrityPatch = bytecodePatch(
    name = "Bypass integrity check",
    description = "Spoofs certificate fingerprint and SafetyNet results so login works on patched APK.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency(), readPackageNamePatch)

    execute {
        val certHash = CERT_HASHES[detectedPackageName]
            ?: error("No cert hash for package '$detectedPackageName'")

        // Return the original cert SHA-256 so server-side cert checks pass
        AndroidUtilitiesGetCertFingerprintFingerprint.method.addInstructions(0, """
            const-string v0, "$certHash"
            return-object v0
        """)

        // Force basicIntegrity and ctsProfileMatch to true
        SafetyNetCheckFingerprint.method.apply {
            implementation!!.instructions.forEachIndexed { index, instruction ->
                val str = instruction.toString()
                if (str.contains("basicIntegrity") || str.contains("ctsProfileMatch")) {
                    val patchIndex = index + 2
                    if (patchIndex < implementation!!.instructions.size) {
                        val reg = getInstruction<OneRegisterInstruction>(patchIndex).registerA
                        replaceInstruction(patchIndex, "const/4 v$reg, 0x1")
                    }
                }
            }
        }
    }
}
