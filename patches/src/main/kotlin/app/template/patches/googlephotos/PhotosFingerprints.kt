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

// Matches the method that checks whether DCIM folder backup control is disabled.
// Anchored on stable path literals that are unlikely to change.
internal object IsDcimFolderBackupControlDisabledFingerprint : Fingerprint(
    returnType = "Z",
    strings = listOf("/dcim", "/mars_files/"),
)

// Matches the builder setter that marks a LocalMedia item as belonging to a Camera folder.
// Uses class-level string predicates from the builder's toString() and its required-properties
// validator, plus a literal 32 that corresponds to the inCameraFolder bitmask.
internal object LocalMediaInCameraFolderSetterFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Z"),
    custom = { method, classDef ->
        classDef.hasMethodReferencingString("Missing required properties:") &&
            classDef.hasMethodReferencingString(" inCameraFolder") &&
            method.referencesIntLiteral(32)
    },
)

// Matches the legacy path-based camera-folder classification method.
// Used as a graceful-miss fallback (methodOrNull) for older app versions.
internal object LegacyDcimCameraFolderFingerprint : Fingerprint(
    returnType = "Z",
    custom = { method, _ ->
        method.parameterTypes.firstOrNull() == "Ljava/lang/String;" &&
            method.referencesString("/dcim/")
    },
)

// Matches the enum static initialiser that maps feature flags to Pixel generations.
// The NEXUS_PRELOAD string has been stable across all versions and is the only entry
// for the original Pixel XL tier, making it a unique and reliable anchor.
internal object InitializeFeaturesEnumFingerprint : Fingerprint(
    strings = listOf("com.google.android.apps.photos.NEXUS_PRELOAD"),
)

// Matches the AccountValidityMonitor.onResume-equivalent that enqueues CheckAccountTask.
// Under MicroG this method clears the selected account, so we suppress it entirely.
// Anchored on the two log-tag strings that appear in sibling methods of the same class
// plus the int field access pattern of the target method itself.
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

// Matches the frictionless-login eligibility check that fires on cold start.
// Under MicroG this can return false and trigger the code path that clears the
// selected account.  Using "checkPlayServices" (adopted from De-Vanced) as the
// second class-level anchor is more stable than "maybeStartFrictionless" which
// may be inlined or renamed in future R8 passes.
internal object FrictionlessEligibilityFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    custom = { method, classDef ->
        classDef.hasMethodReferencingString("checkPlayServices") &&
            classDef.hasMethodReferencingString("ProvideFrctAccountTask") &&
            method.referencesMethod("Z", emptyList()) &&
            method.referencesMethod("V", listOf("I")) &&
            method.referencesIntLiteral(-1)
    },
)
