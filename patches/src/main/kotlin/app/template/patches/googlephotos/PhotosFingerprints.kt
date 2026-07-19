package app.template.patches.googlephotos

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private fun Method.referencesString(value: String) =
    implementation?.instructions?.any {
        ((it as? ReferenceInstruction)?.reference as? StringReference)?.string == value
    } == true

private fun Method.referencesStringContaining(value: String) =
    implementation?.instructions?.any {
        ((it as? ReferenceInstruction)?.reference as? StringReference)?.string?.contains(value) == true
    } == true

private fun Method.referencesMethod(returnType: String, parameters: List<String>? = null) =
    implementation?.instructions?.any {
        ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref ->
            ref.returnType == returnType && (parameters == null || ref.parameterTypes.toList() == parameters)
        } == true
    } == true

private fun Method.referencesVoidMethodWithSingleObjectParameter() =
    implementation?.instructions?.any {
        ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref ->
            ref.returnType == "V" && ref.parameterTypes.size == 1 && ref.parameterTypes.first().startsWith("L")
        } == true
    } == true

private fun Method.referencesIntLiteral(value: Int) =
    implementation?.instructions?.any { it is NarrowLiteralInstruction && it.narrowLiteral == value } == true

private fun ClassDef.hasMethodReferencingString(value: String) = methods.any { it.referencesString(value) }

private fun ClassDef.hasMethodReferencingStringContaining(value: String) =
    methods.any { it.referencesStringContaining(value) }

internal object IsDcimFolderBackupControlDisabledFingerprint : Fingerprint(
    returnType = "Z",
    strings = listOf("/dcim", "/mars_files/"),
)

internal object LocalMediaInCameraFolderSetterFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Z"),
    custom = { method, classDef ->
        classDef.hasMethodReferencingString("Missing required properties:") &&
            classDef.hasMethodReferencingString(" inCameraFolder") &&
            method.referencesIntLiteral(32)
    },
)

internal object LegacyDcimCameraFolderFingerprint : Fingerprint(
    returnType = "Z",
    custom = { method, _ ->
        method.parameterTypes.firstOrNull() == "Ljava/lang/String;" &&
            method.referencesString("/dcim/")
    },
)

internal object InitializeFeaturesEnumFingerprint : Fingerprint(
    strings = listOf("com.google.android.apps.photos.NEXUS_PRELOAD"),
)

internal object AccountValidityMonitorCheckFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
    custom = { method, classDef ->
        classDef.hasMethodReferencingString("AccountValidityMonitor") &&
            classDef.hasMethodReferencingString("com.google.android.apps.photos.login.AccountValidityMonitor.CheckAccountTask") &&
            method.implementation?.instructions?.let { instructions ->
                instructions.any {
                    ((it as? ReferenceInstruction)?.reference as? FieldReference)?.let { ref ->
                        ref.name == "a" && ref.type == "I"
                    } == true
                } && method.referencesVoidMethodWithSingleObjectParameter()
            } == true
    },
)

internal object FrictionlessEligibilityFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    custom = { method, classDef ->
        classDef.hasMethodReferencingStringContaining("maybeStartFrictionless") &&
            classDef.hasMethodReferencingString("ProvideFrctAccountTask") &&
            method.referencesMethod("Z", emptyList()) &&
            method.referencesMethod("V", listOf("I")) &&
            method.referencesIntLiteral(-1)
    },
)
