package app.template.patches.googlephotos

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.GOOGLE_PHOTOS_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags

private const val EXTENSION_CLASS = "Lapp/template/extension/extension/PhotosDcimFolderPatch;"

// Based on RevealedSoulEven/XposedPhotosFIX v3 builder hook logic.
@Suppress("unused")
val googlePhotosDcimFolderClassificationPatch = bytecodePatch(
    name = "Fix DCIM folder classification",
    description = "Prevents non-Camera DCIM folders from being grouped as Camera.",
    default = false,
) {
    compatibleWith(GOOGLE_PHOTOS_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    execute {
        LocalMediaInCameraFolderSetterFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0, p1}, $EXTENSION_CLASS->fixInCameraFolder(Ljava/lang/Object;Z)Z
                move-result p1
            """.trimIndent(),
        )

        LegacyDcimCameraFolderFingerprint.methodOrNull?.let { method ->
            val pathRegister = if (AccessFlags.STATIC.isSet(method.accessFlags)) "p0" else "p1"
            method.addInstructions(
                0,
                """
                    invoke-static {$pathRegister}, $EXTENSION_CLASS->isCameraFolderPath(Ljava/lang/String;)Z
                    move-result $pathRegister
                    return $pathRegister
                """.trimIndent(),
            )
        }
    }
}
