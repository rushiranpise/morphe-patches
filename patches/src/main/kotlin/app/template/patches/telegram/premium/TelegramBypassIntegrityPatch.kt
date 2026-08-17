package app.template.patches.telegram.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.AndroidUtilitiesGetCertFingerprintFingerprint
import app.template.patches.telegram.SafetyNetCheckFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import org.w3c.dom.Element

// Original signing certificate SHA-256 per package.
// Spoofed so getCertificateSHA256Fingerprint() returns the expected value
// and server-side login challenges pass even on a re-signed APK.
private val CERT_HASHES = mapOf(
    "org.telegram.messenger"      to "69938D64ADEF5C93DC7A13D04008881FCADD091220F04C024B351524D437C077",
    "org.telegram.messenger.web"  to "49C1522548EBACD46CE322B6FD47F6092BB745D0F88082145CAF35E14DCC38E1",
    "org.telegram.plus"           to "9A9BDE9AC584AC4C97B5ECD304158BB2E98A7C6577E8C663EC49D4A173F744FB",
)

// Read package name from AndroidManifest at patch time.
private var detectedPackageName = ""

private val readPackageNamePatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { doc ->
            detectedPackageName = (doc.getElementsByTagName("manifest").item(0) as Element)
                .getAttribute("package")
        }
    }
}

@Suppress("unused")
val telegramBypassIntegrityPatch = bytecodePatch(
    name = "Bypass integrity check",
    description = "Spoofs certificate fingerprint and SafetyNet results so login works on a patched APK.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    dependsOn(readPackageNamePatch)

    execute {
        val certHash = CERT_HASHES[detectedPackageName]
            ?: error("No cert hash registered for package '$detectedPackageName'")

        AndroidUtilitiesGetCertFingerprintFingerprint.method.addInstructions(0, """
            const-string v0, "$certHash"
            return-object v0
        """)

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
