package app.template.patches.shared.universal

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import org.w3c.dom.Element
import java.io.File

/*
 * Additional universal ports inspired by ReVanced Patches (GPL-3.0):
 * https://github.com/ReVanced/revanced-patches
 */

private const val EXTRA_HELPER = "Lapp/template/extension/extension/UniversalPatchHelper;"

@Suppress("unused")
val universalSpoofBuildInfoReVancedPatch = bytecodePatch(
    name = "Spoof build info",
    description = "Spoofs common android.os.Build fields with configurable values.",
    default = false,
) {
    val model by stringOption("universalBuildModel", "Pixel 10 Pro XL", title = "MODEL", required = true)
    val manufacturer by stringOption("universalBuildManufacturer", "Google", title = "MANUFACTURER", required = true)
    val brand by stringOption("universalBuildBrand", "google", title = "BRAND", required = true)
    val device by stringOption("universalBuildDevice", "mustang", title = "DEVICE", required = true)
    val product by stringOption("universalBuildProduct", "mustang", title = "PRODUCT", required = true)
    val fingerprint by stringOption(
        "universalBuildFingerprint",
        "google/mustang/mustang:17/CP2A.260605.012/15430684:user/release-keys",
        title = "FINGERPRINT",
        required = true,
    )
    val release by stringOption("universalBuildRelease", "17", title = "VERSION.RELEASE", required = true)
    val sdk by stringOption("universalBuildSdk", "37", title = "VERSION.SDK_INT", required = true) { it?.toIntOrNull() != null }

    execute {
        val fields = mapOf(
            "Landroid/os/Build;->MODEL" to (model ?: "Pixel 10 Pro XL"),
            "Landroid/os/Build;->MANUFACTURER" to (manufacturer ?: "Google"),
            "Landroid/os/Build;->SOC_MANUFACTURER" to (manufacturer ?: "Google"),
            "Landroid/os/Build;->BRAND" to (brand ?: "google"),
            "Landroid/os/Build;->DEVICE" to (device ?: "mustang"),
            "Landroid/os/Build;->BOARD" to (device ?: "mustang"),
            "Landroid/os/Build;->HARDWARE" to (device ?: "mustang"),
            "Landroid/os/Build;->PRODUCT" to (product ?: "mustang"),
            "Landroid/os/Build;->FINGERPRINT" to (fingerprint ?: ""),
            "Landroid/os/Build\$VERSION;->RELEASE" to (release ?: "17"),
        )
        val sdkInt = (sdk ?: "37").toInt()
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference ?: return@forEachIndexed
                    val register = (instruction as? OneRegisterInstruction)?.registerA ?: return@forEachIndexed
                    val key = "${field.definingClass}->${field.name}"
                    fields[key]?.let { method.replaceInstruction(index, "const-string v$register, \"${it.escapeSmali()}\"") }
                    if (field.definingClass == "Landroid/os/Build\$VERSION;" && field.name == "SDK_INT") {
                        method.replaceInstruction(index, "const/16 v$register, $sdkInt")
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val universalSpoofSimProviderPatch = bytecodePatch(
    name = "Spoof SIM provider",
    description = "Spoofs TelephonyManager SIM/network provider values.",
    default = false,
) {
    val iso by stringOption("universalSimCountryIso", "us", title = "Country ISO", required = true)
    val operator by stringOption("universalSimOperator", "310260", title = "Operator code", required = true)
    val operatorName by stringOption("universalSimOperatorName", "T-Mobile", title = "Operator name", required = true)

    execute {
        val values = mapOf(
            "getSimCountryIso" to (iso ?: "us"),
            "getNetworkCountryIso" to (iso ?: "us"),
            "getSimOperator" to (operator ?: "310260"),
            "getNetworkOperator" to (operator ?: "310260"),
            "getSimOperatorName" to (operatorName ?: "T-Mobile"),
            "getNetworkOperatorName" to (operatorName ?: "T-Mobile"),
        )
        replaceTelephonyStringResults(values)
    }
}

@Suppress("unused")
val universalSpoofWifiPatch = bytecodePatch(
    name = "Spoof Wi-Fi connection",
    description = "Forces common connectivity checks to connected/unmetered.",
    default = false,
) {
    execute {
        replaceBooleanResults(
            mapOf(
                "Landroid/net/NetworkInfo;" to setOf("isConnected", "isConnectedOrConnecting", "isAvailable"),
                "Landroid/net/NetworkCapabilities;" to setOf("hasTransport", "hasCapability"),
            ),
            true,
        )
        replaceBooleanResults(mapOf("Landroid/net/ConnectivityManager;" to setOf("isActiveNetworkMetered")), false)
        wrapObjectResults(
            mapOf("Landroid/net/NetworkInfo;" to mapOf("getState" to "connectedState", "getDetailedState" to "connectedDetailedState"))
        )
    }
}

@Suppress("unused")
val universalOverrideCertificatePinningPatch = resourcePatch(
    name = "Override certificate pinning",
    description = "Forces network security config trust anchors to override pins.",
    default = false,
) {
    execute { writeNetworkSecurityConfig(cleartext = true, overridePins = true) }
}

private val universalExportInternalDataDocumentsProviderResourcePatch = resourcePatch(
    description = "Registers an extension DocumentsProvider for the app internal data directory.",
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            val manifest = doc.documentElement
            val packageName = manifest.getAttribute("package")
            val application = doc.getElementsByTagName("application").item(0) as? Element ?: return@use
            val provider = doc.createElement("provider")
            provider.setAttribute("android:name", "app.template.extension.extension.InternalDataDocumentsProvider")
            provider.setAttribute("android:authorities", "$packageName.morphe.internaldata.documents")
            provider.setAttribute("android:exported", "true")
            provider.setAttribute("android:grantUriPermissions", "true")
            provider.setAttribute("android:permission", "android.permission.MANAGE_DOCUMENTS")
            application.appendChild(provider)
        }
    }
}

@Suppress("unused")
val universalExportInternalDataDocumentsProviderPatch = bytecodePatch(
    name = "Export internal data documents provider",
    description = "Registers an extension DocumentsProvider for the app internal data directory.",
    default = false,
) {
    dependsOn(universalExportInternalDataDocumentsProviderResourcePatch)
    extendWith("extensions/extension.mpe")
    execute {}
}

@Suppress("unused")
val universalEnableRomSignatureSpoofingPatch = resourcePatch(
    name = "Enable ROM signature spoofing",
    description = "Adds fake-signature permission and metadata.",
    default = false,
) {
    val signature by stringOption("universalFakeSignature", "", title = "Certificate hex/signature", required = false)
    execute {
        document("AndroidManifest.xml").use { doc ->
            val manifest = doc.documentElement
            doc.createElement("uses-permission").also {
                it.setAttribute("android:name", "android.permission.FAKE_PACKAGE_SIGNATURE")
                manifest.insertBefore(it, manifest.firstChild)
            }
            val application = doc.getElementsByTagName("application").item(0) as? Element ?: return@use
            doc.createElement("meta-data").also {
                it.setAttribute("android:name", "fake-signature")
                it.setAttribute("android:value", signature ?: "")
                application.appendChild(it)
            }
        }
    }
}

@Suppress("unused")
val universalSpoofKeystoreSecurityLevelPatch = bytecodePatch(
    name = "Spoof keystore security level",
    description = "Forces key/security level getters to software/trusted-environment style values.",
    default = false,
) {
    execute {
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val lower = method.name.lowercase()
                if (method.returnType == "I" && ("securitylevel" in lower || "security_level" in lower || "getsecuritylevel" in lower)) {
                    method.addInstructions(0, "const/4 v0, 0x2\nreturn v0")
                }
            }
        }
    }
}

@Suppress("unused")
val universalSpoofRootOfTrustPatch = bytecodePatch(
    name = "Spoof root of trust",
    description = "Spoofs common RootOfTrust verified boot getters.",
    default = false,
) {
    execute {
        classDefForEach { classDef ->
            if (!classDef.type.lowercase().contains("rootoftrust")) return@classDefForEach
            mutableClassDefBy(classDef).methods.forEach { method ->
                when {
                    method.name == "isDeviceLocked" && method.returnType == "Z" -> method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                    method.name == "getVerifiedBootState" && method.returnType == "I" -> method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                }
            }
        }
    }
}

@Suppress("unused")
val universalSpoofPlayAgeSignalsPatch = bytecodePatch(
    name = "Spoof Play age signals",
    description = "Spoofs Play age signal result getters.",
    default = false,
) {
    val lowerAge by stringOption("universalPlayAgeLower", "18", title = "Lower age", required = true) { it?.toIntOrNull() != null }
    val upperAge by stringOption("universalPlayAgeUpper", "99", title = "Upper age", required = true) { it?.toIntOrNull() != null }
    execute {
        val values = mapOf("ageLower" to (lowerAge ?: "18").toInt(), "ageUpper" to (upperAge ?: "99").toInt())
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                    val value = values[reference.name] ?: return@forEachIndexed
                    val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                    if (move.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed
                    method.addInstructions(
                        index + 2,
                        "const/16 v${move.registerA}, $value\ninvoke-static {v${move.registerA}}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;\nmove-result-object v${move.registerA}",
                    )
                }
            }
        }
    }
}

@Suppress("unused")
val universalRemoveAdManifestEntriesPatch = resourcePatch(
    name = "Remove ad manifest entries",
    description = "Removes common ad SDK permissions, services, providers, libraries, and metadata.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            val adPermissions = setOf(
                "com.google.android.gms.permission.AD_ID",
                "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
                "android.permission.ACCESS_ADSERVICES_AD_ID",
                "android.permission.ACCESS_ADSERVICES_TOPICS",
                "android.permission.AD_SERVICES_CONFIG",
            )
            val adNames = listOf(
                "com.google.android.gms.ads",
                "com.google.android.gms.measurement",
                "com.facebook.ads",
                "com.applovin",
                "com.mbridge.msdk",
                "com.inmobi.ads",
                "com.unity3d.ads",
                "com.vungle",
                "com.ironsource",
                "com.bytedance.sdk",
                "com.anythink",
                "com.qq.e.",
                "com.baidu.mobads",
                "com.kwad.sdk",
                "com.sigmob",
                "com.tradplus",
                "com.pangle",
            )
            for (tag in listOf("uses-permission", "uses-library", "property", "meta-data", "provider", "service", "receiver", "activity")) {
                val nodes = doc.getElementsByTagName(tag)
                for (i in nodes.length - 1 downTo 0) {
                    val node = nodes.item(i) as? Element ?: continue
                    val name = node.getAttribute("android:name")
                    val value = node.getAttribute("android:value")
                    val remove = when (tag) {
                        "uses-permission" -> name in adPermissions
                        "uses-library" -> name == "android.ext.adservices"
                        "property" -> name == "android.adservices.AD_SERVICES_CONFIG"
                        else -> adNames.any { name.startsWith(it) || value.startsWith(it) }
                    }
                    if (remove) node.parentNode?.removeChild(node)
                }
            }
        }
    }
}

@Suppress("unused")
val universalDisableAdSdkCallsPatch = bytecodePatch(
    name = "Disable ad SDK calls",
    description = "No-ops common ad SDK load/show/init/fetch methods in bundled ad packages.",
    default = false,
) {
    execute {
        val adPackages = listOf(
            "Lcom/applovin/",
            "Lcom/facebook/ads/",
            "Lcom/fyber/inneractive/sdk/",
            "Lcom/google/android/gms/ads/",
            "Lcom/mbridge/msdk/",
            "Lcom/inmobi/ads/",
            "Lcom/smaato/sdk/",
            "Lcom/tradplus/ads/",
            "Lcom/unity3d/ads/",
            "Lcom/unity3d/services/",
            "Lcom/vungle/",
            "Lcom/ironsource/",
            "Lcom/bytedance/sdk/",
            "Lcom/anythink/",
            "Lcom/qq/e/",
            "Lcom/baidu/mobads/",
            "Lcom/kwad/sdk/",
            "Lcom/sigmob/",
            "Lcom/pangle/",
        )
        val voidMethodNames = setOf(
            "loadAd",
            "loadAds",
            "load",
            "show",
            "showAd",
            "fetchAd",
            "init",
            "start",
            "initSDK",
            "initialize",
            "initializeSdk",
            "loadSplashAd",
            "loadRewardVideoAd",
            "loadInterstitialAd",
            "loadBannerAd",
            "loadNativeAd",
            "loadFeedAd",
            "loadNativeExpressAd",
            "loadBannerExpressAd",
            "loadDrawFeedAd",
            "loadExpressDrawFeedAd",
            "loadSplashScreenAd",
            "showSplashView",
            "showSplashClickEyeView",
            "showSplashCardView",
            "showRewardVideoAd",
            "showFullScreenVideoAd",
            "showInterstitialAd",
            "showSplashMiniWindow",
            "showSplashMiniWindowIfNeeded",
            "showNativeAd",
            "negativeFeedback",
            "startLoadAd",
        )
        val objectMethodNames = setOf(
            "getSplashView",
            "getSplashClickEyeView",
            "getSplashCardView",
            "getBannerView",
            "getFeedView",
        )
        classDefForEach { classDef ->
            if (adPackages.none { classDef.type.startsWith(it) }) return@classDefForEach
            mutableClassDefBy(classDef).methods.forEach { method ->
                if (method.name in voidMethodNames && method.returnType == "V" && method.name !in setOf("<init>", "<clinit>")) {
                    method.addInstructions(0, "return-void")
                }
                if (method.name in objectMethodNames && method.returnType.startsWith("L")) {
                    method.addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
                }
                if (method.name in setOf("isInitSuccess", "isSdkReady") && method.returnType == "Z") {
                    method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                }
            }
        }
    }
}

@Suppress("unused")
val universalDisableShakeAdsPatch = bytecodePatch(
    name = "Disable shake ads",
    description = "Skips SensorManager.registerListener calls that can power shake-to-ad behavior.",
    default = false,
) {
    execute {
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                    if (reference.definingClass != "Landroid/hardware/SensorManager;" ||
                        reference.name != "registerListener" ||
                        reference.returnType != "Z" ||
                        "Landroid/hardware/Sensor;" !in reference.parameterTypes
                    ) return@forEachIndexed
                    val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                    if (move.opcode == Opcode.MOVE_RESULT) {
                        method.replaceInstruction(index, "nop")
                        method.replaceInstruction(index + 1, "const/4 v${move.registerA}, 0x1")
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val universalDisableClipboardAccessPatch = bytecodePatch(
    name = "Disable clipboard access",
    description = "Blocks app clipboard reads and writes.",
    default = false,
) {
    execute {
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                    if (reference.definingClass != "Landroid/content/ClipboardManager;") return@forEachIndexed
                    when {
                        reference.name in setOf("setPrimaryClip", "setText") && reference.returnType == "V" ->
                            method.replaceInstruction(index, "nop")

                        reference.name == "hasPrimaryClip" && reference.returnType == "Z" -> {
                            val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                            if (move.opcode == Opcode.MOVE_RESULT) method.replaceInstruction(index + 1, "const/4 v${move.registerA}, 0x0")
                        }

                        reference.name in setOf("getPrimaryClip", "getText", "getPrimaryClipDescription") -> {
                            val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                            if (move.opcode == Opcode.MOVE_RESULT_OBJECT) method.replaceInstruction(index + 1, "const/4 v${move.registerA}, 0x0")
                        }
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val universalHideVpnProxyPatch = bytecodePatch(
    name = "Hide VPN and proxy",
    description = "Hides common VPN transport/interface and Java proxy property checks.",
    default = false,
) {
    extendWith("extensions/extension.mpe")
    execute {
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                    val ins = instruction as? Instruction35c

                    if (reference.definingClass == "Ljava/lang/System;" &&
                        reference.name == "getProperty" &&
                        reference.returnType == "Ljava/lang/String;" &&
                        reference.parameterTypes == listOf("Ljava/lang/String;")
                    ) {
                        val keyRegister = ins?.registerC ?: return@forEachIndexed
                        val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                        if (move.opcode == Opcode.MOVE_RESULT_OBJECT) {
                            method.addInstructions(
                                index + 2,
                                "invoke-static {v$keyRegister, v${move.registerA}}, $EXTRA_HELPER->hideProxyProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;\nmove-result-object v${move.registerA}",
                            )
                        }
                    }

                    if (reference.definingClass == "Landroid/net/NetworkCapabilities;" &&
                        reference.name in setOf("hasTransport", "hasCapability") &&
                        reference.returnType == "Z" &&
                        ins != null
                    ) {
                        method.replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}}, $EXTRA_HELPER->${reference.name}(Landroid/net/NetworkCapabilities;I)Z")
                    }

                    if (reference.definingClass == "Landroid/net/NetworkInfo;" &&
                        reference.name in setOf("getType", "getTypeName", "getSubtypeName")
                    ) {
                        val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                        when {
                            reference.returnType == "I" && move.opcode == Opcode.MOVE_RESULT ->
                                method.addInstructions(index + 2, "invoke-static {v${move.registerA}}, $EXTRA_HELPER->hideVpnNetworkType(I)I\nmove-result v${move.registerA}")
                            reference.returnType == "Ljava/lang/String;" && move.opcode == Opcode.MOVE_RESULT_OBJECT ->
                                method.addInstructions(index + 2, "invoke-static {v${move.registerA}}, $EXTRA_HELPER->hideVpnNetworkName(Ljava/lang/String;)Ljava/lang/String;\nmove-result-object v${move.registerA}")
                        }
                    }

                    if (reference.definingClass == "Ljava/net/NetworkInterface;" &&
                        reference.name in setOf("isVirtual", "isUp", "getName")
                    ) {
                        val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                        when {
                            reference.returnType == "Z" && move.opcode == Opcode.MOVE_RESULT ->
                                method.replaceInstruction(index + 1, "const/4 v${move.registerA}, 0x0")
                            reference.returnType == "Ljava/lang/String;" && move.opcode == Opcode.MOVE_RESULT_OBJECT ->
                                method.addInstructions(index + 2, "invoke-static {v${move.registerA}}, $EXTRA_HELPER->hideVpnInterfaceName(Ljava/lang/String;)Ljava/lang/String;\nmove-result-object v${move.registerA}")
                        }
                    }
                }
            }
        }
    }
}

private fun app.morphe.patcher.patch.BytecodePatchContext.replaceBooleanResults(
    targets: Map<String, Set<String>>,
    value: Boolean,
) {
    val literal = if (value) "0x1" else "0x0"
    classDefForEach { classDef ->
        mutableClassDefBy(classDef).methods.forEach { method ->
            val instructions = method.instructionsOrNull?.toList() ?: return@forEach
            instructions.forEachIndexed { index, instruction ->
                val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                if (reference.returnType != "Z" || reference.name !in (targets[reference.definingClass] ?: emptySet())) return@forEachIndexed
                val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                if (move.opcode == Opcode.MOVE_RESULT) method.replaceInstruction(index + 1, "const/4 v${move.registerA}, $literal")
            }
        }
    }
}

private fun app.morphe.patcher.patch.BytecodePatchContext.replaceTelephonyStringResults(values: Map<String, String>) {
    classDefForEach { classDef ->
        mutableClassDefBy(classDef).methods.forEach { method ->
            val instructions = method.instructionsOrNull?.toList() ?: return@forEach
            instructions.forEachIndexed { index, instruction ->
                val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                if (reference.definingClass != "Landroid/telephony/TelephonyManager;" || reference.returnType != "Ljava/lang/String;") return@forEachIndexed
                val value = values[reference.name] ?: return@forEachIndexed
                val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                if (move.opcode == Opcode.MOVE_RESULT_OBJECT) method.replaceInstruction(index + 1, "const-string v${move.registerA}, \"${value.escapeSmali()}\"")
            }
        }
    }
}

private fun app.morphe.patcher.patch.BytecodePatchContext.wrapObjectResults(targets: Map<String, Map<String, String>>) {
    classDefForEach { classDef ->
        mutableClassDefBy(classDef).methods.forEach { method ->
            val instructions = method.instructionsOrNull?.toList() ?: return@forEach
            instructions.forEachIndexed { index, instruction ->
                val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                val helper = targets[reference.definingClass]?.get(reference.name) ?: return@forEachIndexed
                val move = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                if (move.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed
                val register = move.registerA
                method.addInstructions(index + 2, "invoke-static {v$register}, $EXTRA_HELPER->$helper(${reference.returnType})${reference.returnType}\nmove-result-object v$register")
            }
        }
    }
}

private fun app.morphe.patcher.patch.ResourcePatchContext.writeNetworkSecurityConfig(cleartext: Boolean, overridePins: Boolean) {
    document("AndroidManifest.xml").use { doc ->
        val application = doc.getElementsByTagName("application").item(0) as? Element ?: return@use
        application.setAttribute("android:networkSecurityConfig", "@xml/morphe_network_security_config")
        application.setAttribute("android:usesCleartextTraffic", cleartext.toString())
    }
    val file = File(get("res/xml"), "morphe_network_security_config.xml")
    file.parentFile?.mkdirs()
    val override = if (overridePins) " overridePins=\"true\"" else ""
    file.writeText(
        """
        <?xml version="1.0" encoding="utf-8"?>
        <network-security-config>
            <base-config cleartextTrafficPermitted="$cleartext">
                <trust-anchors>
                    <certificates src="system"$override />
                    <certificates src="user"$override />
                </trust-anchors>
            </base-config>
        </network-security-config>
        """.trimIndent(),
    )
}

private fun String.escapeSmali() = replace("\\", "\\\\").replace("\"", "\\\"")
