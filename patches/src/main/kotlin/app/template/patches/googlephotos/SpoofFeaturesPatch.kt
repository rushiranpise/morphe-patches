package app.template.patches.googlephotos

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.GOOGLE_PHOTOS_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction3rc
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

// Based on RookieEnough/De-Vanced, ReVanced, PixelMask, and pixelify-google-photos-modern.
@Suppress("unused")
val googlePhotosSpoofFeaturesPatch = bytecodePatch(
    name = "Spoof features",
    description = "Spoofs selectable Pixel Photos build and feature flags.",
    default = false,
) {
    compatibleWith(GOOGLE_PHOTOS_COMPATIBILITY)

    val profileKey by stringOption(
        key = "googlePhotosSpoofProfile",
        default = DEFAULT_GOOGLE_PHOTOS_PROFILE,
        values = GOOGLE_PHOTOS_PROFILE_OPTIONS,
        title = "Pixel profile",
        description = "Device profile used for Build fields and Pixel feature levels.",
        required = true,
    )

    val androidVersionKey by stringOption(
        key = "googlePhotosAndroidVersion",
        default = GOOGLE_PHOTOS_ANDROID_VERSION_NONE,
        values = GOOGLE_PHOTOS_ANDROID_VERSION_OPTIONS,
        title = "Android version",
        description = "Optionally spoof Build.VERSION fields. Follow profile uses the selected Pixel's matching Android version.",
        required = false,
    )

    val overrideRomFeatureLevels by booleanOption(
        key = "googlePhotosOverrideRomFeatureLevels",
        default = true,
        title = "Override ROM feature levels",
        description = "Force newer Pixel feature flags off when the selected profile should not have them.",
        required = false,
    )

    execute {
        val profile = GOOGLE_PHOTOS_PROFILES[profileKey ?: DEFAULT_GOOGLE_PHOTOS_PROFILE]
            ?: GOOGLE_PHOTOS_PROFILES.getValue(DEFAULT_GOOGLE_PHOTOS_PROFILE)
        val androidVersion = androidVersionKey.resolveAndroidVersion(profile)
        val enable = profile.enabledFeatures()
        val disable = if (overrideRomFeatureLevels != false) profile.disabledFeatures() else emptySet()
        val systemPropertyOverrides = profile.systemPropertyOverrides()
        val spoofedFields = linkedMapOf<String, Map<String, Any>>(
            "Landroid/os/Build;" to profile.buildProps,
        ).apply {
            androidVersion?.buildVersionProps?.let { put("Landroid/os/Build\$VERSION;", it) }
        }

        classDefForEach { classDef ->
            if (classDef.methods.none { it.hasPhotosSpoofTarget(spoofedFields) }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!method.hasPhotosSpoofTarget(spoofedFields)) return@forEach

                if (method.hasPhotosBuildFieldRead(spoofedFields)) {
                    method.instructionsOrNull?.toList()?.forEachIndexed { index, instruction ->
                        val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                            ?: return@forEachIndexed
                        val register = (instruction as? OneRegisterInstruction)?.registerA
                            ?: return@forEachIndexed
                        val replacement = field.photosBuildReplacement(register, spoofedFields)
                            ?: return@forEachIndexed

                        method.replaceInstruction(index, replacement)
                    }
                }

                method.patchSystemPropertyReads(systemPropertyOverrides)
            }
        }

        InitializeFeaturesEnumFingerprint.method.apply {
            implementation!!.instructions
                .filter { it.opcode == Opcode.CONST_STRING }
                .forEach { instruction ->
                    val feature = ((instruction as? ReferenceInstruction)?.reference as? StringReference)?.string
                        ?: return@forEach
                    val spoofedFeature = when (feature) {
                        in enable -> "android.hardware.wifi"
                        in disable -> "dummy"
                        else -> return@forEach
                    }
                    val register = (instruction as OneRegisterInstruction).registerA
                    replaceInstruction(instruction.location.index, "const-string v$register, \"$spoofedFeature\"")
                }
        }
    }
}

private fun Method.hasPhotosBuildFieldRead(spoofedFields: Map<String, Map<String, Any>>) =
    instructionsOrNull?.any { instruction ->
        if (instruction.opcode !in setOf(Opcode.SGET_OBJECT, Opcode.SGET, Opcode.SGET_WIDE)) {
            return@any false
        }

        ((instruction as? ReferenceInstruction)?.reference as? FieldReference)?.let { field ->
            field.name in spoofedFields[field.definingClass].orEmpty()
        } == true
    } == true

private fun Method.hasPhotosSpoofTarget(spoofedFields: Map<String, Map<String, Any>>) =
    hasPhotosBuildFieldRead(spoofedFields) || hasSystemPropertiesGetCall()

private fun Method.hasSystemPropertiesGetCall() =
    instructionsOrNull?.any { instruction ->
        val method = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@any false
        method.isSystemPropertiesStringGetter()
    } == true

private fun FieldReference.photosBuildReplacement(
    register: Int,
    spoofedFields: Map<String, Map<String, Any>>,
): String? {
    val value = spoofedFields[definingClass]?.get(name) ?: return null

    return when (value) {
        is Int -> "const/16 v$register, $value"
        is String -> "const-string v$register, \"${value.escapeSmali()}\""
        else -> null
    }
}

private fun String.escapeSmali() = replace("\\", "\\\\").replace("\"", "\\\"")

private fun String?.resolveAndroidVersion(profile: GooglePhotosPixelProfile): GooglePhotosAndroidVersion? =
    when (this) {
        GOOGLE_PHOTOS_ANDROID_VERSION_FOLLOW_PROFILE -> profile.androidVersion
        GOOGLE_PHOTOS_ANDROID_VERSION_NONE, null -> null
        else -> GOOGLE_PHOTOS_ANDROID_VERSIONS[this]
    }

private fun MutableMethod.patchSystemPropertyReads(overrides: Map<String, String>) {
    if (overrides.isEmpty()) return

    val instructions = instructionsOrNull?.toList() ?: return
    instructions.forEachIndexed { index, instruction ->
        val methodReference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            ?: return@forEachIndexed
        if (!methodReference.isSystemPropertiesStringGetter()) return@forEachIndexed

        val keyRegister = instruction.firstInvokeRegister() ?: return@forEachIndexed
        val key = instructions.constStringValueBefore(index, keyRegister) ?: return@forEachIndexed
        val replacement = overrides[key] ?: return@forEachIndexed
        val moveResult = instructions.getOrNull(index + 1)
        if (moveResult?.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

        val resultRegister = (moveResult as? OneRegisterInstruction)?.registerA ?: return@forEachIndexed
        replaceInstruction(index, "nop")
        replaceInstruction(index + 1, "const-string v$resultRegister, \"${replacement.escapeSmali()}\"")
    }
}

private fun MethodReference.isSystemPropertiesStringGetter(): Boolean =
    definingClass == "Landroid/os/SystemProperties;" &&
        name == "get" &&
        returnType == "Ljava/lang/String;" &&
        parameterTypes.size in 1..2 &&
        parameterTypes.all { it == "Ljava/lang/String;" }

private fun ReferenceInstruction.firstInvokeRegister(): Int? =
    when (this) {
        is Instruction35c -> registerC
        is Instruction3rc -> startRegister
        else -> null
    }

private fun List<Instruction>.constStringValueBefore(
    index: Int,
    register: Int,
): String? {
    val start = maxOf(0, index - 8)
    for (previousIndex in index - 1 downTo start) {
        val instruction = this[previousIndex]
        if (instruction.opcode !in setOf(Opcode.CONST_STRING, Opcode.CONST_STRING_JUMBO)) continue
        val oneRegisterInstruction = instruction as? OneRegisterInstruction ?: continue
        if (oneRegisterInstruction.registerA != register) continue

        return ((instruction as? ReferenceInstruction)?.reference as? StringReference)?.string
    }

    return null
}
