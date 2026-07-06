package app.template.patches.picturemushroom

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

@Suppress("unused")
val bypassNativeKeyCheckPatch = bytecodePatch {
    compatibleWith(Constants.PICTUREMUSHROOM_COMPATIBILITY)

    execute {
        EncryptGetPrivateKeyFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/16 v0, 0x20
                    new-array v0, v0, [B
                    fill-array-data v0, :aes_key
                    return-object v0

                    :aes_key
                    .array-data 1
                        0x43t
                        0x53t
                        0x78t
                        0x4ft
                        0x4ct
                        0x6at
                        0x7at
                        0x74t
                        0x63t
                        0x47t
                        0x55t
                        0x53t
                        0x79t
                        0x4dt
                        0x31t
                        0x75t
                        0x4at
                        0x52t
                        0x4ct
                        0x79t
                        0x6bt
                        0x79t
                        0x49t
                        0x42t
                        0x43t
                        0x4at
                        0x53t
                        0x65t
                        0x4dt
                        0x66t
                        0x58t
                        0x42t
                    .end array-data
                """,
            )
        }
    }
}
