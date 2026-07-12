package app.template.patches.shared.pixel

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction3rc
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val HELPER = "Lapp/template/extension/extension/PixelSpoofHelper;"

private val ApplicationOnCreateFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = emptyList(),
    custom = { method, classDef ->
        method.name == "onCreate" && classDef.superclass == "Landroid/app/Application;"
    },
)

private val ActivityOnCreateFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    custom = { method, classDef ->
        method.name == "onCreate" &&
            (classDef.superclass?.contains("Activity;") == true ||
                method.implementation?.instructions?.any {
                    val reference = (it as? ReferenceInstruction)?.reference as? MethodReference
                    reference?.name == "onCreate" && reference.parameterTypes == listOf("Landroid/os/Bundle;")
                } == true)
    },
)

@Suppress("unused")
val spoofPixelDevicePatch = bytecodePatch(
    name = "Spoof Pixel device",
    description = "Ports PixelSpoof-style Build, system property, and Pixel feature spoofing.",
    default = false,
) {
    val mode by stringOption(
        key = "pixelSpoofMode",
        default = "build_props_features",
        values = mapOf(
            "Full spoof" to "build_props_features",
            "Build fields only" to "build",
            "System properties only" to "props",
            "Pixel features only" to "features",
            "Build + system properties" to "build_props",
            "Build + Pixel features" to "build_features",
            "System properties + Pixel features" to "props_features",
            "Build + props + features + identifiers" to "build_props_features_identifiers",
            "Google Photos unlimited storage" to "photos_build_props_features",
            "Identifiers only" to "identifiers",
        ),
        title = "Spoof mode",
        description = "Pixel 10 Pro XL / mustang profile, or Pixel XL / marlin for Google Photos.",
        required = true,
    )

    extendWith("extensions/extension.mpe")

    execute {
        val selectedMode = mode ?: "build_props_features"
        val photosMode = "photos" in selectedMode

        ApplicationOnCreateFingerprint.methodOrNull?.addInstructions(
            0,
            """
                const-string v0, "$selectedMode"
                invoke-static {p0, v0}, $HELPER->init(Landroid/content/Context;Ljava/lang/String;)V
            """.trimIndent(),
        )

        if (ApplicationOnCreateFingerprint.methodOrNull == null) {
            ActivityOnCreateFingerprint.methodOrNull?.addInstructions(
                0,
                """
                    const-string v0, "$selectedMode"
                    invoke-static {p0, v0}, $HELPER->init(Landroid/content/Context;Ljava/lang/String;)V
                """.trimIndent(),
            )
        }

        if ("build" in selectedMode || "features" in selectedMode) {
            classDefForEach { classDef ->
                if (classDef.methods.none { it.hasBuildFieldRead() || it.hasFeatureCall() }) return@classDefForEach

                mutableClassDefBy(classDef).methods.forEach { method ->
                    if (!method.hasBuildFieldRead() && !method.hasFeatureCall()) return@forEach
                    val instructions = method.instructionsOrNull?.toList() ?: return@forEach

                    val insertions = mutableListOf<Pair<Int, String>>()
                    instructions.forEachIndexed { index, instruction ->
                        val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference ?: return@forEachIndexed
                        val register = (instruction as? OneRegisterInstruction)?.registerA ?: return@forEachIndexed
                        val replacement = field.pixelBuildReplacement(register, photosMode) ?: return@forEachIndexed
                        method.replaceInstruction(index, replacement)
                    }
                    if ("features" in selectedMode) {
                        instructions.forEachIndexed { index, instruction ->
                            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                            if (reference.definingClass != "Landroid/content/pm/PackageManager;" ||
                                reference.name != "hasSystemFeature" ||
                                reference.returnType != "Z"
                            ) return@forEachIndexed
                            val featureRegister = instruction.featureArgumentRegister() ?: return@forEachIndexed
                            val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                            if (moveResult.opcode != Opcode.MOVE_RESULT) return@forEachIndexed
                            insertions += index + 2 to """
                                invoke-static {v$featureRegister, v${moveResult.registerA}}, $HELPER->hasPixelSystemFeature(Ljava/lang/String;Z)Z
                                move-result v${moveResult.registerA}
                            """.trimIndent()
                        }
                    }
                    insertions.asReversed().forEach { (index, code) -> method.addInstructions(index, code) }
                }
            }
        }

        if ("props" in selectedMode) {
            classDefForEach { classDef ->
                if (classDef.methods.none { it.hasSystemPropertiesGetCall() }) return@classDefForEach

                mutableClassDefBy(classDef).methods.forEach { method ->
                    if (!method.hasSystemPropertiesGetCall()) return@forEach
                    val instructions = method.instructionsOrNull?.toList() ?: return@forEach

                    val insertions = buildList {
                        instructions.forEachIndexed { index, instruction ->
                        val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                        if (reference.definingClass != "Landroid/os/SystemProperties;" ||
                            reference.name !in setOf("get", "getInt", "getLong", "getBoolean")
                        ) return@forEachIndexed
                        val keyRegister = instruction.firstArgumentRegister() ?: return@forEachIndexed

                            val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed

                            when {
                                reference.name == "get" && reference.returnType == "Ljava/lang/String;" && moveResult.opcode == Opcode.MOVE_RESULT_OBJECT ->
                                    add(index + 2 to """
                                        invoke-static {v$keyRegister, v${moveResult.registerA}}, $HELPER->getSystemProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                                        move-result-object v${moveResult.registerA}
                                    """.trimIndent())

                                reference.name == "getInt" && reference.returnType == "I" && moveResult.opcode == Opcode.MOVE_RESULT ->
                                    add(index + 2 to """
                                        invoke-static {v$keyRegister, v${moveResult.registerA}}, $HELPER->getSystemPropertyInt(Ljava/lang/String;I)I
                                        move-result v${moveResult.registerA}
                                    """.trimIndent())

                                reference.name == "getLong" && reference.returnType == "J" && moveResult.opcode == Opcode.MOVE_RESULT_WIDE ->
                                    add(index + 2 to """
                                        invoke-static {v$keyRegister, v${moveResult.registerA}, v${moveResult.registerA + 1}}, $HELPER->getSystemPropertyLong(Ljava/lang/String;J)J
                                        move-result-wide v${moveResult.registerA}
                                    """.trimIndent())

                                reference.name == "getBoolean" && reference.returnType == "Z" && moveResult.opcode == Opcode.MOVE_RESULT ->
                                    add(index + 2 to """
                                        invoke-static {v$keyRegister, v${moveResult.registerA}}, $HELPER->getSystemPropertyBoolean(Ljava/lang/String;Z)Z
                                        move-result v${moveResult.registerA}
                                    """.trimIndent())
                            }
                        }
                    }
                    insertions.asReversed().forEach { (index, code) ->
                        method.addInstructions(index, code)
                    }
                }
            }
        }

        if ("identifiers" in selectedMode) {
            classDefForEach { classDef ->
                mutableClassDefBy(classDef).methods.forEach { method ->
                    val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                    instructions.forEachIndexed { index, instruction ->
                        val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                        val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                        if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

                        reference.pixelIdentifierReplacement()?.let { value ->
                            method.replaceInstruction(index + 1, "const-string v${moveResult.registerA}, \"${value.escapeSmali()}\"")
                        }

                        if (reference.definingClass == "Landroid/os/Build;" &&
                            reference.name == "getSerial" &&
                            reference.returnType == "Ljava/lang/String;"
                        ) {
                            method.replaceInstruction(index + 1, "const-string v${moveResult.registerA}, \"39061FDJG000Q8\"")
                        }

                        if (reference.definingClass == "Landroid/provider/Settings\$Secure;" &&
                            reference.name == "getString" &&
                            reference.returnType == "Ljava/lang/String;" &&
                            reference.parameterTypes == listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;")
                        ) {
                            val ins = instruction as? Instruction35c ?: return@forEachIndexed
                            method.addInstructions(
                                index + 2,
                                """
                                    const-string v${ins.registerC}, "a1b2c3d4e5f67890"
                                    invoke-static {v${ins.registerD}, v${moveResult.registerA}, v${ins.registerC}}, Lapp/template/extension/extension/UniversalPatchHelper;->spoofAndroidId(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                                    move-result-object v${moveResult.registerA}
                                """.trimIndent(),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Method.hasSystemPropertiesGetCall() =
    instructionsOrNull?.any { instruction ->
        if (instruction.opcode !in setOf(Opcode.INVOKE_STATIC, Opcode.INVOKE_STATIC_RANGE)) return@any false
        val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@any false
        reference.definingClass == "Landroid/os/SystemProperties;" &&
            reference.name in setOf("get", "getInt", "getLong", "getBoolean")
    } == true

private fun Method.hasBuildFieldRead() =
    instructionsOrNull?.any { instruction ->
        if (instruction.opcode !in setOf(Opcode.SGET_OBJECT, Opcode.SGET, Opcode.SGET_WIDE)) return@any false
        ((instruction as? ReferenceInstruction)?.reference as? FieldReference)?.pixelBuildReplacement(0, false) != null
    } == true

private fun Method.hasFeatureCall() =
    instructionsOrNull?.any { instruction ->
        if (instruction.opcode !in setOf(Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_RANGE)) return@any false
        val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@any false
        reference.definingClass == "Landroid/content/pm/PackageManager;" &&
            reference.name == "hasSystemFeature" &&
            reference.returnType == "Z"
    } == true

private fun FieldReference.pixelBuildReplacement(register: Int, photosMode: Boolean): String? {
    val owner = definingClass
    val value = when {
        owner == "Landroid/os/Build;" -> when (name) {
            "DISPLAY", "ID" -> if (photosMode) "QP1A.191005.007.A3" else "CP2A.260605.012"
            "BOOTLOADER" -> if (photosMode) "unknown" else "deepspace-17.2-15372054"
            "HARDWARE", "BOARD", "DEVICE", "PRODUCT" -> if (photosMode) "marlin" else "mustang"
            "BRAND" -> "google"
            "MANUFACTURER", "SOC_MANUFACTURER" -> "Google"
            "MODEL" -> if (photosMode) "Pixel XL" else "Pixel 10 Pro XL"
            "SERIAL" -> "39061FDJG000Q8"
            "SOC_MODEL" -> if (photosMode) "MSM8996" else "Tensor G5"
            "TAGS" -> "release-keys"
            "TYPE" -> "user"
            "USER" -> "android-build"
            "HOST" -> "bcb8c9bcce95"
            "FINGERPRINT" -> if (photosMode) {
                "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys"
            } else {
                "google/mustang/mustang:17/CP2A.260605.012/15430684:user/release-keys"
            }
            else -> null
        }
        owner == "Landroid/os/Build\$VERSION;" -> when (name) {
            "RELEASE" -> if (photosMode) "10" else "17"
            "SECURITY_PATCH" -> if (photosMode) "2019-10-05" else "2026-06-05"
            "INCREMENTAL" -> if (photosMode) "5972272" else "15430684"
            else -> null
        }
        else -> null
    }
    if (value != null) return "const-string v$register, \"$value\""

    if (owner == "Landroid/os/Build;" && name == "TIME") {
        return if (photosMode) "const-wide v$register, 0x16d9dc63000L" else "const-wide v$register, 0x19e69262180L"
    }
    if (owner == "Landroid/os/Build\$VERSION;" && name == "SDK_INT") {
        return if (photosMode) "const/16 v$register, 0x1d" else "const/16 v$register, 0x25"
    }
    return null
}

private fun ReferenceInstruction.firstArgumentRegister(): Int? =
    when (this) {
        is Instruction35c -> registerC
        is Instruction3rc -> startRegister
        else -> null
    }

private fun ReferenceInstruction.featureArgumentRegister(): Int? =
    when (this) {
        is Instruction35c -> registerD
        is Instruction3rc -> startRegister + 1
        else -> null
    }

private fun MethodReference.pixelIdentifierReplacement(): String? =
    when (definingClass) {
        "Landroid/telephony/TelephonyManager;" -> when (name) {
            "getDeviceId", "getImei" -> "356938035643809"
            "getMeid" -> "A000004E4F4F50"
            "getSubscriberId" -> "310260000000000"
            "getSimSerialNumber" -> "89014103211118510720"
            "getLine1Number" -> "+15551234567"
            else -> null
        }
        "Landroid/net/wifi/WifiInfo;" -> when (name) {
            "getSSID" -> "Pixel_WiFi"
            "getBSSID" -> "02:00:00:12:34:56"
            "getMacAddress" -> "02:00:00:65:43:21"
            else -> null
        }
        "Landroid/bluetooth/BluetoothAdapter;" -> when (name) {
            "getName" -> "Pixel Buds"
            "getAddress" -> "02:11:22:33:44:55"
            else -> null
        }
        else -> null
    }

private fun String.escapeSmali() = replace("\\", "\\\\").replace("\"", "\\\"")
