package app.template.patches.telegram.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.MessagesControllerIsPremiumUserFingerprint
import app.template.patches.telegram.PremiumFeaturesBlockedFingerprint
import app.template.patches.telegram.SharedConfigGetDevicePerformanceClassFingerprint
import app.template.patches.telegram.StoriesControllerIsPremiumFingerprint
import app.template.patches.telegram.UserConfigGetMaxAccountCountFingerprint
import app.template.patches.telegram.UserConfigHasPremiumOnAccountsFingerprint
import app.template.patches.telegram.UserConfigIsPremiumFingerprint
import app.template.patches.telegram.signature.telegramSpoofDependency

// True Plus-only marker on the supplied 12.10.3.0 build.
// Unlike MessagesController.premiumFeaturesBlocked(), this class is absent
// from the standard Telegram/Web APKs.
private val plusUpdateButtonFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/plus/update/UpdateButton;",
    name = "update",
    returnType = "V",
    parameters = listOf("Z"),
)

@Suppress("unused")
val telegramPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Telegram Premium features for the current account.",
) {
    compatibleWith(
        TELEGRAM_COMPATIBILITY,
        TELEGRAM_WEB_COMPATIBILITY,
        TELEGRAM_PLUS_COMPATIBILITY,
    )

    dependsOn(telegramSpoofDependency())

    execute {
        UserConfigIsPremiumFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // premiumFeaturesBlocked() exists in standard Telegram/Web as well,
        // so it cannot be used to detect the Plus build.
        val isPlusBuild = plusUpdateButtonFingerprint.methodOrNull != null

        if (isPlusBuild) {
            MessagesControllerIsPremiumUserFingerprint.method.addInstructions(
                0,
                """
                    if-eqz p1, :not_self
                    iget-boolean v0, p1, Lorg/telegram/tgnet/TLRPC${'$'}User;->self:Z
                    if-eqz v0, :not_self
                    const/4 v0, 0x1
                    return v0
                    :not_self
                    nop
                """,
            )

            PremiumFeaturesBlockedFingerprint.methodOrNull?.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
        } else {
            MessagesControllerIsPremiumUserFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }

        StoriesControllerIsPremiumFingerprint.methodOrNull?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        UserConfigHasPremiumOnAccountsFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        UserConfigGetMaxAccountCountFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x3E7
                return v0
            """,
        )

        SharedConfigGetDevicePerformanceClassFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x2
                return v0
            """,
        )
    }
}
