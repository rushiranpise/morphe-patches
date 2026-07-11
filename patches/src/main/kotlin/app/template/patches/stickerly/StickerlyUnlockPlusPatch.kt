package app.template.patches.stickerly

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.STICKERLY_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val stickerlyUnlockPlusPatch = bytecodePatch(
    name = "Unlock Plus",
    description = "Unlocks Sticker.ly PLUS subscription" + "Note: For Facebook Login, Uninstall Facebook App.",
    default = true,
) {
    compatibleWith(STICKERLY_COMPATIBILITY)

    execute {
        listOf(
            SubscriptionCacheReadFingerprint,
            SubscriptionResponseMapperFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        new-instance v0, Lcom/snowcorp/stickerly/android/base/domain/payment/SubscriptionModel;
                        new-instance v1, Ljava/util/Date;
                        const-wide/16 v2, 0x0
                        invoke-direct {v1, v2, v3}, Ljava/util/Date;-><init>(J)V
                        new-instance v4, Ljava/util/Date;
                        const-wide v2, 0x3bb2cc3d800L
                        invoke-direct {v4, v2, v3}, Ljava/util/Date;-><init>(J)V
                        const/4 v2, 0x1
                        const-string v3, "plus"
                        invoke-direct {v0, v2, v3, v1, v4}, Lcom/snowcorp/stickerly/android/base/domain/payment/SubscriptionModel;-><init>(ZLjava/lang/String;Ljava/util/Date;Ljava/util/Date;)V
                        return-object v0
                    """.trimIndent(),
                )
            }
        }
    }
}
