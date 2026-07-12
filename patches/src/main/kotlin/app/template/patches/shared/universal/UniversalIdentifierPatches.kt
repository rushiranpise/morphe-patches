package app.template.patches.shared.universal

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference


private const val ID_HELPER = "Lapp/template/extension/extension/UniversalPatchHelper;"

@Suppress("unused")
val universalSpoofAndroidIdPatch = bytecodePatch(
    name = "Spoof Android ID",
    description = "Spoofs Settings.Secure android_id reads.",
    default = false,
) {
    val androidId by stringOption(
        key = "universalAndroidId",
        default = "a1b2c3d4e5f67890",
        title = "Android ID",
        description = "16 hex characters.",
        required = true,
    ) { it?.matches(Regex("[0-9A-Fa-f]{16}")) == true }

    extendWith("extensions/extension.mpe")

    execute {
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                    if (reference.definingClass != "Landroid/provider/Settings\$Secure;" ||
                        reference.name != "getString" ||
                        reference.returnType != "Ljava/lang/String;" ||
                        reference.parameterTypes != listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;")
                    ) return@forEachIndexed

                    val ins = instruction as? Instruction35c ?: return@forEachIndexed
                    val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                    if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

                    method.addInstructions(
                        index + 2,
                        """
                            const-string v${ins.registerC}, "${(androidId ?: "a1b2c3d4e5f67890").escapeSmali()}"
                            invoke-static {v${ins.registerD}, v${moveResult.registerA}, v${ins.registerC}}, $ID_HELPER->spoofAndroidId(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                            move-result-object v${moveResult.registerA}
                        """.trimIndent(),
                    )
                }
            }
        }
    }
}

@Suppress("unused")
val universalSpoofTelephonyIdsPatch = bytecodePatch(
    name = "Spoof telephony IDs",
    description = "Spoofs IMEI, MEID, subscriber ID, SIM serial, and line number reads.",
    default = false,
) {
    val imei by stringOption("universalImei", "356938035643809", title = "IMEI", required = true)
    val meid by stringOption("universalMeid", "A000004E4F4F50", title = "MEID", required = true)
    val subscriberId by stringOption("universalSubscriberId", "310260000000000", title = "Subscriber ID", required = true)
    val simSerial by stringOption("universalSimSerial", "89014103211118510720", title = "SIM serial", required = true)
    val lineNumber by stringOption("universalLineNumber", "+15551234567", title = "Line number", required = true)

    execute {
        replaceStringMoveResults(
            mapOf(
                "Landroid/telephony/TelephonyManager;" to mapOf(
                    "getDeviceId" to (imei ?: "356938035643809"),
                    "getImei" to (imei ?: "356938035643809"),
                    "getMeid" to (meid ?: "A000004E4F4F50"),
                    "getSubscriberId" to (subscriberId ?: "310260000000000"),
                    "getSimSerialNumber" to (simSerial ?: "89014103211118510720"),
                    "getLine1Number" to (lineNumber ?: "+15551234567"),
                ),
            ),
        )
    }
}

@Suppress("unused")
val universalSpoofWifiIdentifiersPatch = bytecodePatch(
    name = "Spoof Wi-Fi identifiers",
    description = "Spoofs Wi-Fi SSID, BSSID, and MAC address reads.",
    default = false,
) {
    val ssid by stringOption("universalWifiSsid", "Pixel_WiFi", title = "SSID", required = true)
    val bssid by stringOption("universalWifiBssid", "02:00:00:12:34:56", title = "BSSID", required = true)
    val mac by stringOption("universalWifiMac", "02:00:00:65:43:21", title = "MAC address", required = true)

    execute {
        replaceStringMoveResults(
            mapOf(
                "Landroid/net/wifi/WifiInfo;" to mapOf(
                    "getSSID" to (ssid ?: "Pixel_WiFi"),
                    "getBSSID" to (bssid ?: "02:00:00:12:34:56"),
                    "getMacAddress" to (mac ?: "02:00:00:65:43:21"),
                ),
            ),
        )
    }
}

@Suppress("unused")
val universalSpoofBluetoothIdentifiersPatch = bytecodePatch(
    name = "Spoof Bluetooth identifiers",
    description = "Spoofs Bluetooth adapter name and MAC address reads.",
    default = false,
) {
    val name by stringOption("universalBluetoothName", "Pixel Buds", title = "Bluetooth name", required = true)
    val address by stringOption("universalBluetoothAddress", "02:11:22:33:44:55", title = "Bluetooth MAC address", required = true)

    execute {
        replaceStringMoveResults(
            mapOf(
                "Landroid/bluetooth/BluetoothAdapter;" to mapOf(
                    "getName" to (name ?: "Pixel Buds"),
                    "getAddress" to (address ?: "02:11:22:33:44:55"),
                ),
            ),
        )
    }
}

private fun app.morphe.patcher.patch.BytecodePatchContext.replaceStringMoveResults(
    targets: Map<String, Map<String, String>>,
) {
    classDefForEach { classDef ->
        mutableClassDefBy(classDef).methods.forEach { method ->
            val instructions = method.instructionsOrNull?.toList() ?: return@forEach
            instructions.forEachIndexed { index, instruction ->
                val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                if (reference.returnType != "Ljava/lang/String;") return@forEachIndexed
                val value = targets[reference.definingClass]?.get(reference.name) ?: return@forEachIndexed
                val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed
                method.replaceInstruction(index + 1, "const-string v${moveResult.registerA}, \"${value.escapeSmali()}\"")
            }
        }
    }
}

private fun String.escapeSmali() = replace("\\", "\\\\").replace("\"", "\\\"")
