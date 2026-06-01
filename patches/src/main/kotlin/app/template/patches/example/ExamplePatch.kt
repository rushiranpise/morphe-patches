package app.template.patches.example

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_EXAMPLE

private const val EXTENSION_CLASS = "Lapp/template/extension/ExamplePatch;"

@Suppress("unused")
val examplePatch = bytecodePatch(
    name = "Example Patch",
    description = "Example patch to start with.",
    default = true
) {
    compatibleWith(COMPATIBILITY_EXAMPLE)

    extendWith("extensions/extension.mpe")

    // Business logic of the patch to disable ads in the app.
    execute {
        AdLoaderFingerprint.method.addInstructions(
            0,
            """
                invoke-static {}, $EXTENSION_CLASS;->showAds()Z
                move-result v0
                return v0
            """
        )
    }
}
