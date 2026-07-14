package app.template.patches.shared.universal

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import org.w3c.dom.Document
import org.w3c.dom.Element

/*
 * Universal patch ports inspired by ReVanced Patches (GPL-3.0):
 * https://github.com/ReVanced/revanced-patches
 *
 * Implemented in Morphe style instead of copying ReVanced framework helpers.
 */

private const val HELPER = "Lapp/template/extension/extension/UniversalPatchHelper;"

private val UniversalApplicationOnCreateFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = emptyList(),
    custom = { method, classDef ->
        method.name == "onCreate" && classDef.superclass == "Landroid/app/Application;"
    },
)

private val UniversalActivityOnCreateFingerprint = Fingerprint(
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
val universalExportAllActivitiesPatch = resourcePatch(
    name = "Export all activities",
    description = "Makes all activities exportable.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            val nodes = doc.getElementsByTagName("activity")
            for (i in 0 until nodes.length) {
                (nodes.item(i) as? Element)?.setAttribute("android:exported", "true")
            }
        }
    }
}

@Suppress("unused")
val universalEnableAndroidDebuggingPatch = resourcePatch(
    name = "Enable Android debugging",
    description = "Sets android:debuggable=true.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:debuggable", "true")
        }
    }
}

@Suppress("unused")
val universalHideAppIconPatch = resourcePatch(
    name = "Hide app icon",
    description = "Removes launcher category from MAIN launcher filters.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            val filters = doc.getElementsByTagName("intent-filter")
            for (i in 0 until filters.length) {
                val filter = filters.item(i) as? Element ?: continue
                var hasMain = false
                var launcher: Element? = null
                val children = filter.childNodes
                for (j in 0 until children.length) {
                    val child = children.item(j) as? Element ?: continue
                    if (child.tagName == "action" && child.getAttribute("android:name") == "android.intent.action.MAIN") hasMain = true
                    if (child.tagName == "category" && child.getAttribute("android:name") == "android.intent.category.LAUNCHER") launcher = child
                }
                if (hasMain) launcher?.setAttribute("android:name", "android.intent.category.DEFAULT")
            }
        }
    }
}

@Suppress("unused")
val universalPredictiveBackGesturePatch = resourcePatch(
    name = "Predictive back gesture",
    description = "Enables Android predictive back gesture.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:enableOnBackInvokedCallback", "true")
        }
    }
}

@Suppress("unused")
val universalAllowAudioPlaybackCapturePatch = resourcePatch(
    name = "Remove screen capture restriction",
    description = "Allows audio playback capture in the manifest.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:allowAudioPlaybackCapture", "true")
        }
    }
}

@Suppress("unused")
val universalChangeVersionCodePatch = resourcePatch(
    name = "Change version code",
    description = "Changes android:versionCode.",
    default = false,
) {
    val versionCode by stringOption(
        key = "universalVersionCode",
        default = "2147483647",
        title = "Version code",
        description = "Version code to write into AndroidManifest.xml.",
        required = true,
    ) { it?.toIntOrNull() != null }

    execute {
        document("AndroidManifest.xml").use { doc ->
            doc.documentElement.setAttribute("android:versionCode", versionCode ?: "2147483647")
        }
    }
}

@Suppress("unused")
val universalSetTargetSdk34Patch = resourcePatch(
    name = "Set target SDK 34",
    description = "Sets targetSdkVersion to 34.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            doc.documentElement.setAttribute("platformBuildVersionCode", "34")
            val usesSdk = doc.getElementsByTagName("uses-sdk").item(0) as? Element
                ?: doc.createElement("uses-sdk").also { doc.documentElement.appendChild(it) }
            usesSdk.setAttribute("android:targetSdkVersion", "34")
        }
    }
}

@Suppress("unused")
val universalRemoveShareTargetsPatch = resourcePatch(
    name = "Remove share targets",
    description = "Removes chooser/direct share targets.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            listOf("share-target", "shortcut").forEach { tag ->
                val nodes = doc.getElementsByTagName(tag)
                for (i in nodes.length - 1 downTo 0) {
                    val node = nodes.item(i)
                    node.parentNode?.removeChild(node)
                }
            }
        }
    }
}

private val universalForceDarkThemeResourcePatch = resourcePatch(
    name = "Force dark theme resources",
    description = "Enables Android force-dark and switches common light theme parents to DayNight.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:forceDarkAllowed", "true")
        }

        listOf(
            "res/values/styles.xml",
            "res/values-v21/styles.xml",
            "res/values-v23/styles.xml",
            "res/values-v27/styles.xml",
            "res/values-v29/styles.xml",
            "res/values-v31/styles.xml",
        ).forEach { path ->
            runCatching {
                document(path).use { doc ->
                    val styles = doc.getElementsByTagName("style")
                    for (i in 0 until styles.length) {
                        val style = styles.item(i) as? Element ?: continue
                        style.ensureStyleItem(doc, "android:forceDarkAllowed", "true")
                        style.ensureStyleItem(doc, "android:windowLightStatusBar", "false")
                        style.ensureStyleItem(doc, "android:windowLightNavigationBar", "false")
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val universalForceDarkThemePatch = bytecodePatch(
    name = "Force dark theme",
    description = "Forces common AppCompat, UiModeManager, and Configuration dark-mode checks to night mode.",
    default = false,
) {
    dependsOn(universalForceDarkThemeResourcePatch)
    extendWith("extensions/extension.mpe")

    execute {
        UniversalApplicationOnCreateFingerprint.methodOrNull?.addInstructions(
            0,
            "invoke-static {p0}, $HELPER->forceDarkTheme(Landroid/content/Context;)V",
        )

        if (UniversalApplicationOnCreateFingerprint.methodOrNull == null) {
            UniversalActivityOnCreateFingerprint.methodOrNull?.addInstructions(
                0,
                "invoke-static {p0}, $HELPER->forceDarkTheme(Landroid/content/Context;)V",
            )
        }

        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
                    val ins = instruction as? Instruction35c
                    if (reference != null) {
                        when {
                            reference.definingClass == "Landroidx/appcompat/app/AppCompatDelegate;" &&
                                reference.name == "setDefaultNightMode" &&
                                reference.parameterTypes == listOf("I") &&
                                ins != null -> method.addInstructions(index, "const/4 v${ins.registerC}, 0x2")

                            reference.definingClass == "Landroidx/appcompat/app/AppCompatDelegate;" &&
                                reference.name == "setLocalNightMode" &&
                                reference.parameterTypes == listOf("I") &&
                                ins != null -> method.addInstructions(index, "const/4 v${ins.registerD}, 0x2")

                            reference.definingClass == "Landroid/app/UiModeManager;" &&
                                reference.name in setOf("setApplicationNightMode", "setNightMode") &&
                                reference.parameterTypes == listOf("I") &&
                                ins != null -> method.addInstructions(index, "const/4 v${ins.registerD}, 0x2")

                            reference.definingClass == "Landroidx/appcompat/app/AppCompatDelegate;" &&
                                reference.name in setOf("getDefaultNightMode", "getLocalNightMode") &&
                                reference.returnType == "I" -> {
                                val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                                if (moveResult.opcode == Opcode.MOVE_RESULT) {
                                    method.replaceInstruction(index + 1, "const/4 v${moveResult.registerA}, 0x2")
                                }
                            }

                            reference.definingClass == "Landroid/app/UiModeManager;" &&
                                reference.name in setOf("getApplicationNightMode", "getNightMode") &&
                                reference.returnType == "I" -> {
                                val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                                if (moveResult.opcode == Opcode.MOVE_RESULT) {
                                    method.replaceInstruction(index + 1, "const/4 v${moveResult.registerA}, 0x2")
                                }
                            }

                            reference.definingClass == "Landroid/webkit/WebView;" &&
                                reference.name in setOf("loadUrl", "loadData", "loadDataWithBaseURL") &&
                                ins != null -> method.addInstructions(
                                index + 1,
                                "invoke-static {v${ins.registerC}}, $HELPER->enableWebViewDarkMode(Landroid/webkit/WebView;)V",
                            )
                        }
                    }

                    // Configuration.uiMode can appear in AppCompat config-copy methods where
                    // register shapes are verifier-sensitive. AppCompat/UiModeManager hooks cover
                    // the safe universal path without rewriting those field accesses.
                }
            }
        }

        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                if (method.name != "onPageFinished" ||
                    method.returnType != "V" ||
                    method.parameterTypes != listOf("Landroid/webkit/WebView;", "Ljava/lang/String;")
                ) return@forEach

                method.addInstructions(
                    0,
                    "invoke-static {p1}, $HELPER->enableWebViewDarkMode(Landroid/webkit/WebView;)V",
                )
            }
        }
    }
}

@Suppress("unused")
val universalRemoveScreenshotRestrictionPatch = bytecodePatch(
    name = "Remove screenshot restriction",
    description = "Removes FLAG_SECURE and screenshot blocking.",
    default = false,
) {
    extendWith("extensions/extension.mpe")

    execute {
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                    if (reference.definingClass == "Landroid/view/Window;" && reference.name == "addFlags" && reference.parameterTypes == listOf("I")) {
                        val ins = instruction as? Instruction35c ?: return@forEachIndexed
                        method.replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}}, $HELPER->addWindowFlags(Landroid/view/Window;I)V")
                    }
                    if (reference.definingClass == "Landroid/view/Window;" && reference.name == "setFlags" && reference.parameterTypes == listOf("I", "I")) {
                        val ins = instruction as? Instruction35c ?: return@forEachIndexed
                        method.replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}, v${ins.registerE}}, $HELPER->setWindowFlags(Landroid/view/Window;II)V")
                    }
                    if (reference.definingClass == "Landroid/app/Activity;" && reference.name in setOf("registerScreenCaptureCallback", "unregisterScreenCaptureCallback")) {
                        method.replaceInstruction(index, "nop")
                    }
                }
                instructions.forEachIndexed { index, instruction ->
                    val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference ?: return@forEachIndexed
                    if (instruction.opcode == Opcode.IPUT && field.definingClass == "Landroid/view/WindowManager\$LayoutParams;" && field.name == "flags") {
                        val register = (instruction as? TwoRegisterInstruction)?.registerA ?: return@forEachIndexed
                        method.addInstructions(index, "and-int/lit16 v$register, v$register, -0x2001")
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val universalHideMockLocationPatch = bytecodePatch(
    name = "Hide mock location",
    description = "Hides mock-location signals from app checks.",
    default = false,
) {
    val mode by stringOption(
        key = "hideMockLocationMode",
        default = "full",
        values = mapOf(
            "Full" to "full",
            "Location API only" to "location",
            "Settings only" to "settings",
            "AppOps only" to "appops",
            "Extras and provider only" to "extras_provider",
            "Accuracy only" to "accuracy",
        ),
        title = "Mode",
        description = "Which mock-location checks to hide.",
        required = true,
    )
    val provider by stringOption(
        key = "hideMockLocationProvider",
        default = "gps",
        values = mapOf(
            "GPS" to "gps",
            "Network" to "network",
            "Fused" to "fused",
            "Passive" to "passive",
            "Original" to "original",
        ),
        title = "Provider",
        description = "Provider returned when app reads an unknown/mock provider.",
        required = true,
    )
    val accuracyMeters by stringOption(
        key = "hideMockLocationAccuracy",
        default = "5.0",
        title = "Accuracy meters",
        description = "Accuracy value returned by Location.getAccuracy().",
        required = true,
    ) { it?.toFloatOrNull() != null }

    extendWith("extensions/extension.mpe")
    execute {
        val selectedMode = mode ?: "full"
        val patchLocation = selectedMode in setOf("full", "location")
        val patchSettings = selectedMode in setOf("full", "settings")
        val patchAppOps = selectedMode in setOf("full", "appops")
        val patchExtrasProvider = selectedMode in setOf("full", "extras_provider", "location")
        val patchAccuracy = selectedMode in setOf("full", "accuracy", "location")
        val providerValue = provider ?: "gps"
        val accuracyBits = java.lang.Float.floatToIntBits(accuracyMeters?.toFloatOrNull() ?: 5.0f)

        if (patchLocation) {
            replaceMoveResultAfterCalls(mapOf("Landroid/location/Location;" to setOf("isMock", "isFromMockProvider")), "const/4 {r}, 0x0")
        }
        if (patchAccuracy) {
            replaceMoveResultAfterCalls(mapOf("Landroid/location/Location;" to setOf("hasAccuracy")), "const/4 {r}, 0x1")
        }

        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                    val ins = instruction as? Instruction35c

                    if (patchLocation && reference.definingClass == "Landroid/location/Location;" && reference.name in setOf("setMock", "setIsFromMockProvider")) {
                        if (ins != null && reference.parameterTypes == listOf("Z")) {
                            method.addInstructions(index, "const/4 v${ins.registerD}, 0x0")
                        }
                    }

                    if (patchExtrasProvider && reference.definingClass == "Landroid/location/Location;" && reference.name == "setExtras" && reference.parameterTypes == listOf("Landroid/os/Bundle;")) {
                        if (ins != null) {
                            method.addInstructions(
                                index,
                                "invoke-static {v${ins.registerD}}, $HELPER->patchLocationExtras(Landroid/os/Bundle;)Landroid/os/Bundle;\nmove-result-object v${ins.registerD}",
                            )
                        }
                    }

                    if (patchExtrasProvider && reference.definingClass == "Landroid/location/Location;" && reference.name == "getExtras" && reference.returnType == "Landroid/os/Bundle;") {
                        val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                        if (moveResult.opcode == Opcode.MOVE_RESULT_OBJECT) {
                            method.addInstructions(
                                index + 2,
                                "invoke-static {v${moveResult.registerA}}, $HELPER->patchLocationExtras(Landroid/os/Bundle;)Landroid/os/Bundle;\nmove-result-object v${moveResult.registerA}",
                            )
                        }
                    }

                    if (patchAccuracy && reference.definingClass == "Landroid/location/Location;" && reference.name == "getAccuracy" && reference.returnType == "F") {
                        val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                        if (moveResult.opcode == Opcode.MOVE_RESULT) {
                            method.replaceInstruction(index + 1, "const v${moveResult.registerA}, 0x${accuracyBits.toUInt().toString(16)}")
                        }
                    }

                    if (patchExtrasProvider && providerValue != "original" && reference.definingClass == "Landroid/location/Location;" && reference.name == "getProvider" && reference.returnType == "Ljava/lang/String;") {
                        val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                        if (moveResult.opcode == Opcode.MOVE_RESULT_OBJECT) {
                            method.replaceInstruction(index + 1, "const-string v${moveResult.registerA}, \"$providerValue\"")
                        }
                    }

                    if (patchSettings && reference.definingClass == "Landroid/provider/Settings\$Secure;") {
                        method.replaceSecureSettingsCall(index, reference, ins)
                    }

                    if (patchAppOps && reference.definingClass == "Landroid/app/AppOpsManager;" && ins != null) {
                        method.replaceAppOpsCall(index, reference, ins)
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val universalDisablePlayIntegrityPatch = bytecodePatch(
    name = "Disable Play Integrity",
    description = "Blocks Play Integrity service binding.",
    default = false,
) {
    extendWith("extensions/extension.mpe")
    execute {
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                    if (reference.definingClass == "Landroid/content/Context;" && reference.name == "bindService" && reference.parameterTypes.size == 3) {
                        val ins = instruction as? Instruction35c ?: return@forEachIndexed
                        method.replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}, v${ins.registerE}, v${ins.registerF}}, $HELPER->bindService(Landroid/content/Context;Landroid/content/Intent;Landroid/content/ServiceConnection;I)Z")
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val universalHideAdbStatusPatch = bytecodePatch(
    name = "Hide ADB status",
    description = "Hides adb_enabled and development_settings_enabled.",
    default = false,
) {
    extendWith("extensions/extension.mpe")
    execute {
        classDefForEach { classDef ->
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                    if (reference.definingClass != "Landroid/provider/Settings\$Global;" || reference.name != "getInt" || reference.returnType != "I") return@forEachIndexed
                    val ins = instruction as? Instruction35c ?: return@forEachIndexed
                    if (reference.parameterTypes.size == 2) {
                        method.replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}}, $HELPER->getSettingsGlobalInt(Landroid/content/ContentResolver;Ljava/lang/String;)I")
                    } else if (reference.parameterTypes.size == 3) {
                        method.replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}, v${ins.registerE}}, $HELPER->getSettingsGlobalInt(Landroid/content/ContentResolver;Ljava/lang/String;I)I")
                    }
                }
            }
        }
    }
}

private fun app.morphe.patcher.patch.BytecodePatchContext.replaceMoveResultAfterCalls(
    targets: Map<String, Set<String>>,
    replacementTemplate: String,
) {
    classDefForEach { classDef ->
        mutableClassDefBy(classDef).methods.forEach { method ->
            val instructions = method.instructionsOrNull?.toList() ?: return@forEach
            instructions.forEachIndexed { index, instruction ->
                val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@forEachIndexed
                if (reference.name !in (targets[reference.definingClass] ?: emptySet())) return@forEachIndexed
                val moveResult = instructions.getOrNull(index + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                if (moveResult.opcode != Opcode.MOVE_RESULT) return@forEachIndexed
                method.replaceInstruction(index + 1, replacementTemplate.replace("{r}", "v${moveResult.registerA}"))
            }
        }
    }
}

private fun MutableMethod.replaceSecureSettingsCall(index: Int, reference: MethodReference, ins: Instruction35c?) {
    if (ins == null) return
    when {
        reference.name == "getString" &&
            reference.returnType == "Ljava/lang/String;" &&
            reference.parameterTypes == listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;") ->
            replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}}, $HELPER->getSettingsSecureString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;")

        reference.name == "getInt" &&
            reference.returnType == "I" &&
            reference.parameterTypes == listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;") ->
            replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}}, $HELPER->getSettingsSecureInt(Landroid/content/ContentResolver;Ljava/lang/String;)I")

        reference.name == "getInt" &&
            reference.returnType == "I" &&
            reference.parameterTypes == listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;", "I") ->
            replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}, v${ins.registerE}}, $HELPER->getSettingsSecureInt(Landroid/content/ContentResolver;Ljava/lang/String;I)I")

        reference.name == "getFloat" &&
            reference.returnType == "F" &&
            reference.parameterTypes == listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;") ->
            replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}}, $HELPER->getSettingsSecureFloat(Landroid/content/ContentResolver;Ljava/lang/String;)F")

        reference.name == "getFloat" &&
            reference.returnType == "F" &&
            reference.parameterTypes == listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;", "F") ->
            replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}, v${ins.registerE}}, $HELPER->getSettingsSecureFloat(Landroid/content/ContentResolver;Ljava/lang/String;F)F")

        reference.name == "getLong" &&
            reference.returnType == "J" &&
            reference.parameterTypes == listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;") ->
            replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}}, $HELPER->getSettingsSecureLong(Landroid/content/ContentResolver;Ljava/lang/String;)J")

        reference.name == "getLong" &&
            reference.returnType == "J" &&
            reference.parameterTypes == listOf("Landroid/content/ContentResolver;", "Ljava/lang/String;", "J") ->
            replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}, v${ins.registerE}}, $HELPER->getSettingsSecureLong(Landroid/content/ContentResolver;Ljava/lang/String;J)J")
    }
}

private fun MutableMethod.replaceAppOpsCall(index: Int, reference: MethodReference, ins: Instruction35c) {
    if (reference.returnType != "I") return
    if (reference.name !in setOf("checkOp", "checkOpNoThrow")) return
    when (reference.parameterTypes) {
        listOf("I", "I", "Ljava/lang/String;") ->
            replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}, v${ins.registerE}, v${ins.registerF}}, $HELPER->${reference.name}(Landroid/app/AppOpsManager;IILjava/lang/String;)I")

        listOf("Ljava/lang/String;", "I", "Ljava/lang/String;") ->
            replaceInstruction(index, "invoke-static {v${ins.registerC}, v${ins.registerD}, v${ins.registerE}, v${ins.registerF}}, $HELPER->${reference.name}(Landroid/app/AppOpsManager;Ljava/lang/String;ILjava/lang/String;)I")
    }
}

private fun Element.ensureStyleItem(document: Document, name: String, value: String) {
    val items = getElementsByTagName("item")
    for (i in 0 until items.length) {
        val item = items.item(i) as? Element ?: continue
        if (item.getAttribute("name") == name) {
            item.textContent = value
            return
        }
    }
    val item = document.createElement("item")
    item.setAttribute("name", name)
    item.textContent = value
    appendChild(item)
}
