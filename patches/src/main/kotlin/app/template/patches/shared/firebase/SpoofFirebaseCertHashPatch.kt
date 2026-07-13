package app.template.patches.shared.firebase

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.cert.autoSha1
import app.template.patches.shared.cert.extractApkCertificatePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Fingerprint ───────────────────────────────────────────────────────────────

/**
 * Targets `FirebaseInstallationServiceClient.openHttpUrlConnection()` which sets the
 * `X-Android-Cert` HTTP header to the app's SHA-1 certificate fingerprint.
 *
 * Source: https://github.com/firebase/firebase-android-sdk/blob/c8ada3ce645798bd8bacd5c9b5cb08bdf7254a34/
 *         firebase-installations/src/main/java/com/google/firebase/installations/remote/
 *         FirebaseInstallationServiceClient.java#L495
 *
 * Fixed vs adobo: uses `Fingerprint(...)` style (not `object :`) and avoids adobo's
 * `getReference<T>()` extension — we use standard smali casting instead.
 */
internal val FirebaseOpenHttpConnectionFingerprint = Fingerprint(
    returnType = "Ljava/net/HttpURLConnection;",
    parameters = listOf("Ljava/net/URL;", "Ljava/lang/String;"),
    strings = listOf(
        "X-Android-Cert",
        "Firebase Installations Service is unavailable. Please try again later.",
    ),
)

// Main patch ────────────────────────────────────────────────────────────────

/**
 * Universal "Spoof Firebase certificate hash" patch — shows in Morphe for **any** app.
 *
 * Ported from jkennethcarino/adobo (SpoofAndroidCertPatch + BaseSpoofAndroidCertPatch).
 *
 * **What it does:**
 * Firebase Installations SDK sends the app's certificate SHA-1 fingerprint in the
 * `X-Android-Cert` HTTP header for every Firebase API call. When the APK is re-signed,
 * this hash changes and Firebase rejects the request, breaking push notifications,
 * Remote Config, Firebase Auth, and any other Firebase service.
 *
 * This patch injects a `const-string` with the original certificate's SHA-1 hash
 * immediately before the `addRequestProperty("X-Android-Cert", ...)` call, replacing
 * the runtime-computed hash before it reaches the network.
 *
 * **Why adobo's version failed in our setup:**
 * - `BaseSpoofAndroidCertPatch` used adobo's `getReference<MethodReference>()` extension
 *   (a Kotlin inline reified helper not available in our patcher). Fixed with standard cast.
 * - `stringMatches.first().index` API is identical in our patcher — no change needed.
 * - `OpenHttpUrlConnectionFingerprint` was declared as `internal object` extending `Fingerprint()`
 *   which requires adobo's secondary constructor. Rewritten as a top-level `val`.
 * - `addInstruction` took `smaliInstructions` (plural, multiline String) in adobo's fork.
 *   Our API uses `addInstruction` (singular) with a single smali line.
 *
 * **Option:**
 * - `certificateHash` — SHA-1 fingerprint (40 hex chars) of the original APK's signing cert.
 *   Find it with: `apksigner verify --print-certs original.apk | grep SHA-1`
 *   or: `keytool -printcert -jarfile original.apk`
 */
@Suppress("unused")
val spoofFirebaseCertHashPatch = bytecodePatch(
    name = "Fix Firebase after re-signing",
    description = """
        Fixes Firebase services (push notifications, Remote Config, Firebase Auth) that break after Morphe re-signs the app with a different certificate.

        Apply with Original app certificate patch no other config needed.
    """.trimIndent(),
    default = false,
) {
    dependsOn(extractApkCertificatePatch)

    execute {
        val hash = autoSha1
            ?.uppercase()
            ?: throw PatchException(
                "No certificate found in META-INF and no certificateHash supplied. "
                    + "Provide the 40-char SHA-1 hex fingerprint via the option."
            )

        val method = FirebaseOpenHttpConnectionFingerprint.methodOrNull
            ?: run {
                println("Spoof Firebase cert: Firebase Installations SDK not found — patch has no effect.")
                return@execute
            }

        // Index of the "X-Android-Cert" string match — the addRequestProperty call follows it.
        val xAndroidCertIndex = FirebaseOpenHttpConnectionFingerprint.stringMatches.first().index

        // Walk instructions from xAndroidCertIndex forward to find the addRequestProperty call.
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

        // registerE holds the value argument of addRequestProperty(key, value).
        val valueRegister = (addRequestPropertyInstr as FiveRegisterInstruction).registerE

        // Inject const-string immediately before addRequestProperty so it overwrites
        // the runtime-computed hash with our original certificate hash.
        val insertIndex = instructionList.indexOf(addRequestPropertyInstr)
        method.addInstruction(
            insertIndex,
            "const-string v$valueRegister, \"$hash\"",
        )

        println("Spoof Firebase cert: injected SHA-1=$hash at instruction index $insertIndex.")
    }
}
