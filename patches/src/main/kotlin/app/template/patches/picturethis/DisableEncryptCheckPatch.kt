package app.template.patches.picturethis

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants

@Suppress("unused")
val disableEncryptCheckPatch = bytecodePatch {
    compatibleWith(Constants.PICTURETHIS_COMPATIBILITY)

    execute {
        // aes_delegate$lambda$0: new AESCrypt(), then tries encrypt("test"), shows toast on catch.
        // Replace entirely: just new-instance + init + return, no test call, no toast.
        AesDelegateLambdaFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Lcom/glority/encrypt/AESCrypt;
                invoke-static {}, Lcom/glority/utils/UtilsApp;->getApp()Landroid/app/Application;
                move-result-object v1
                check-cast v1, Landroid/content/Context;
                invoke-direct {v0, v1}, Lcom/glority/encrypt/AESCrypt;-><init>(Landroid/content/Context;)V
                return-object v0
            """,
        )
    }
}
