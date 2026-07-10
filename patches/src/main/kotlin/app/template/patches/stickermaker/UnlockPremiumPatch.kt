package app.template.patches.stickermaker

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.STICKER_MAKER_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Sticker Maker premium and ad-free checks.",
    default = true,
) {
    compatibleWith(STICKER_MAKER_COMPATIBILITY)

    execute {
        listOf(IsPremiumFingerprint, PremiumDialogShownFingerprint).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const/4 v0, 0x1
                        return v0
                    """,
                )
            }
        }

        SetPremiumDialogShownFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    return-void
                """,
            )
        }

        SetPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 p1, 0x1
                    invoke-static {}, Lcom/marsvard/stickermakerforwhatsapp/ConstantsKt;->getHAWK_PREMIUM_PURCHASED()Ljava/lang/String;
                    move-result-object v0
                    invoke-static {p1}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                    move-result-object p1
                    invoke-static {v0, p1}, Lcom/orhanobut/hawk/Hawk;->put(Ljava/lang/String;Ljava/lang/Object;)Z
                    return-void
                """,
            )
        }
    }
}
