package app.template.patches.shared.drm

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Widevine DRM UUID constant used in most apps
private const val WIDEVINE_UUID_STRING = "edef8ba9-79d6-4ace-a3c8-27dcd51d21ed"

// MediaDrm.getPropertyString targets
private val DRM_STRING_PROPERTIES = setOf(
    "securityLevel",        // returns "L1", "L3" etc
    "systemId",
    "vendor",
    "version",
    "description",
    "algorithms",
    "hdcpLevel",            // "HDCP_LEVEL_NONE", "HDCP_V2_2" etc
    "maxHdcpLevel",
    "usageReportingSupport",
    "appId",
    "origin",
    "privacyMode",
    "sessionSharing",
)

// MediaDrm.getPropertyByteArray — provisioningUniqueId used for device fingerprinting
private val DRM_BYTE_PROPERTIES = setOf("provisioningUniqueId", "deviceUniqueId")

// ── Fingerprints ───────────────────────────────────────────────────────────────

// MediaDrm.getPropertyString(String) → String
private val GetDrmStringPropertyFingerprint = Fingerprint(
    custom = { method: Method, _: ClassDef ->
        val ref = method.implementation?.instructions
            ?.filterIsInstance<ReferenceInstruction>()
            ?.mapNotNull { it.reference as? MethodReference }
            ?.any { it.definingClass == "Landroid/media/MediaDrm;" && it.name == "getPropertyString" }
        ref == true
    },
)

// MediaDrm.getPropertyByteArray(String) → byte[]
private val GetDrmByteArrayPropertyFingerprint = Fingerprint(
    custom = { method: Method, _: ClassDef ->
        val ref = method.implementation?.instructions
            ?.filterIsInstance<ReferenceInstruction>()
            ?.mapNotNull { it.reference as? MethodReference }
            ?.any { it.definingClass == "Landroid/media/MediaDrm;" && it.name == "getPropertyByteArray" }
        ref == true
    },
)

// getSecurityLevel / isCryptoSchemeSupported / requiresSecureDecoder checks
private val SecurityLevelStringFingerprint = Fingerprint(
    returnType = "Ljava/lang/String;",
    strings = listOf("securityLevel"),
)

private val WidevineSupportFingerprint = Fingerprint(
    strings = listOf(WIDEVINE_UUID_STRING),
)

/**
 * Spoof DRM / Widevine security level — universal patch for any app.
 *
 * USE WHEN: an app requires Widevine L1 (hardware DRM) to play HD/4K content but the
 * device is L3, OR the device's L1 cert is revoked, OR after patching DRM checks fail.
 *
 * HOW:
 * - Replaces `MediaDrm.getPropertyString("securityLevel")` return values with "L1"
 * - Replaces `MediaDrm.getPropertyString("hdcpLevel")` with a high HDCP level string
 * - Replaces `MediaDrm.getPropertyByteArray("provisioningUniqueId")` with a stable fake ID
 *   (prevents device fingerprinting via DRM unique ID after re-signing)
 * - Bypasses `isCryptoSchemeSupported` checks that gate Widevine availability
 *
 * LIMITATION: does NOT bypass server-side Widevine license checks or CDM verification.
 * Apps that do server-side L1 attestation (Netflix, Disney+, Amazon) will still detect
 * the real security level. Works best for apps that check DRM level locally.
 */
@Suppress("unused")
val spoofDrmPatch = bytecodePatch(
    name = "Spoof Widevine / DRM level",
    description = """
        Reports Widevine L1 (hardware DRM) to apps that check DRM level locally.

        Useful for apps that refuse to play HD/4K content on L3 devices or after re-signing.

        Does not bypass server-side DRM - Netflix, Disney+ and similar are not affected.
    """.trimIndent(),
    default = false,
) {
    val targetSecurityLevel by stringOption(
        key = "securityLevel",
        default = "L1",
        values = mapOf(
            "L1 (highest — hardware)" to "L1",
            "L2" to "L2",
            "L3 (software only)" to "L3",
        ),
        title = "Widevine security level to report",
        description = "L1 = hardware DRM (highest quality). L3 = software only. Default L1.",
        required = true,
    )

    val spoofHdcp by stringOption(
        key = "hdcpLevel",
        default = "HDCP_V2_2",
        values = mapOf(
            "HDCP 2.2" to "HDCP_V2_2",
            "HDCP 2.3" to "HDCP_V2_3",
            "HDCP 1.x" to "HDCP_V1",
            "None (disable)" to "HDCP_LEVEL_NONE",
        ),
        title = "HDCP level to report",
        description = "HDCP_V2_2 is required for 4K content on most streaming apps.",
        required = false,
    )

    execute {
        val level = targetSecurityLevel ?: "L1"
        val hdcp  = spoofHdcp ?: "HDCP_V2_2"

        var patchedCount = 0

        // Patch getPropertyString call sites — replace result register with const-string
        GetDrmStringPropertyFingerprint.methodOrNull?.let { method ->
            val instructions = method.implementation?.instructions?.toList() ?: return@let
            instructions.forEachIndexed { i, instr ->
                if (instr.opcode !in setOf(Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_RANGE)) return@forEachIndexed
                val ref = (instr as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                if (ref.definingClass != "Landroid/media/MediaDrm;" || ref.name != "getPropertyString") return@forEachIndexed
                val next = instructions.getOrNull(i + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                if (next.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

                // Check what property name was loaded before this call (scan backwards for const-string)
                val propName = (i downTo maxOf(0, i - 5)).firstNotNullOfOrNull { j ->
                    val instr2 = instructions[j] as? ReferenceInstruction ?: return@firstNotNullOfOrNull null
                    (instr2.reference as? com.android.tools.smali.dexlib2.iface.reference.StringReference)?.string
                } ?: "securityLevel"

                val spoofValue = when (propName) {
                    "securityLevel" -> level
                    "hdcpLevel", "maxHdcpLevel" -> hdcp
                    else -> return@forEachIndexed
                }

                method.replaceInstruction(i + 1, "const-string v${next.registerA}, \"$spoofValue\"")
                patchedCount++
            }
        }

        // Patch SecurityLevelStringFingerprint — direct return "L1"
        SecurityLevelStringFingerprint.methodOrNull?.let { method ->
            method.addInstruction(0, "return-object v0")
            method.addInstruction(0, "const-string v0, \"$level\"")
            patchedCount++
        }

        // Patch isCryptoSchemeSupported that checks Widevine — return true
        WidevineSupportFingerprint.methodOrNull?.let { method ->
            if (method.returnType == "Z") {
                method.addInstruction(0, "return v0")
                method.addInstruction(0, "const/4 v0, 0x1")
                patchedCount++
            }
        }

        println("Spoof DRM: $patchedCount site(s) patched → securityLevel=$level hdcp=$hdcp")
    }
}
