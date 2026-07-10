package app.template.patches.picturethis

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

@Suppress("unused")
val bypassNativeKeyCheckPatch = bytecodePatch {
    compatibleWith(Constants.PICTURETHIS_COMPATIBILITY)

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
                        0x38t
                        0x39t
                        0x32t
                        0x46t
                        0x32t
                        0x43t
                        0x35t
                        0x32t
                        0x33t
                        0x42t
                        0x37t
                        0x32t
                        0x42t
                        0x36t
                        0x35t
                        0x31t
                        0x35t
                        0x30t
                        0x45t
                        0x37t
                        0x42t
                        0x38t
                        0x32t
                        0x32t
                        0x41t
                        0x44t
                        0x31t
                        0x44t
                        0x45t
                        0x42t
                        0x34t
                        0x31t
                    .end array-data
                """,
            )
        }
    }
}
