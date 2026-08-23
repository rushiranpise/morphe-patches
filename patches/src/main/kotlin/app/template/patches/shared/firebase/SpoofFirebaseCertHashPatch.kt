package app.template.patches.shared.firebase

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.cert.autoSha1
import app.template.patches.shared.cert.extractApkCertificatePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// ─── Fingerprint 1: openHttpUrlConnection — sets X-Android-Cert header ───────
//
// FirebaseInstallationServiceClient.openHttpUrlConnection(URL, String) sets the
// X-Android-Cert HTTP header to the app's cert SHA-1. We overwrite the value
// register immediately before addRequestProperty is called.

internal val FirebaseOpenHttpConnectionFingerprint = Fingerprint(
    returnType = "Ljava/net/HttpURLConnection;",
    parameters = listOf("Ljava/net/URL;", "Ljava/lang/String;"),
    strings = listOf(
        "X-Android-Cert",
        "Firebase Installations Service is unavailable. Please try again later.",
    ),
)

// ─── Fingerprint 2: getFingerprintHashForPackage — the source of the SHA-1 ───
//
// This private method in FirebaseInstallationServiceClient calls
// AndroidUtilsLight.getPackageCertificateHashBytes() which uses
// PackageManagerWrapper (GMS cross-process IPC) to read the signing cert.
// Because the IPC response is read in GMS/system_server process space,
// our in-process SignatureHookApp Parcelable hook does NOT intercept it.
// The real Morphe re-signing cert SHA-1 is returned instead of the original.
// Overriding this method directly bypasses the IPC boundary entirely.

internal val FirebaseFingerprintHashFingerprint = Fingerprint(
    definingClass = "Lcom/google/firebase/installations/remote/FirebaseInstallationServiceClient;",
    name = "getFingerprintHashForPackage",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf(),
)

// ─── Patch ────────────────────────────────────────────────────────────────────

@Suppress("unused")
val spoofFirebaseCertHashPatch = bytecodePatch(
    name = "Fix Firebase after re-signing",
    description = """
        Fixes Firebase services (push notifications, Remote Config, Firebase Auth) that break after Morphe re-signs the app with a different certificate.

        Apply with Original app certificate patch — no other config needed.
    """.trimIndent(),
    default = false,
) {
    dependsOn(extractApkCertificatePatch)

    execute {
        val hash = autoSha1
            ?.uppercase()
            ?: throw PatchException(
                "No certificate found in META-INF and no certificateHash supplied. " +
                    "Provide the 40-char SHA-1 hex fingerprint via the option."
            )

        // ── Fix 1: Override getFingerprintHashForPackage() to return original SHA-1 ──
        //
        // This is the authoritative source of the cert hash inside
        // FirebaseInstallationServiceClient. Patching here means every caller
        // (the X-Android-Cert header, FID auth, etc.) gets the correct value
        // without relying on our in-process PackageManager hook reaching GMS.
        FirebaseFingerprintHashFingerprint.methodOrNull?.addInstructions(0, """
            const-string v0, "$hash"
            return-object v0
        """) ?: run {
            // Firebase Installations SDK not present — fall through to header-only fix.
        }

        // ── Fix 2: Also overwrite the header value at the call site ──
        //
        // Belt-and-suspenders: some Firebase SDK versions call addRequestProperty
        // with an inline-computed hash that bypasses getFingerprintHashForPackage.
        // Patching the call site as well covers those cases.
        val method = FirebaseOpenHttpConnectionFingerprint.methodOrNull
            ?: run {
                // Neither fingerprint matched — SDK not present, nothing to do.
                return@execute
            }

        val xAndroidCertIndex = FirebaseOpenHttpConnectionFingerprint.stringMatches.first().index

        val instructionList = method.instructions.toList()
        val addRequestPropertyInstr = instructionList
            .drop(xAndroidCertIndex)
            .firstOrNull { instr ->
                instr.opcode == Opcode.INVOKE_VIRTUAL &&
                    ((instr as? ReferenceInstruction)?.reference as? MethodReference)
                        ?.name == "addRequestProperty"
            }
            ?: throw PatchException(
                "Could not find addRequestProperty call after X-Android-Cert string."
            )

        val valueRegister = (addRequestPropertyInstr as FiveRegisterInstruction).registerE
        val insertIndex = instructionList.indexOf(addRequestPropertyInstr)
        method.addInstruction(
            insertIndex,
            "const-string v$valueRegister, \"$hash\"",
        )
    }
}
