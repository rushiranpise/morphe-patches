package app.template.patches.telegram.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency

private val sharedConfigIsAppUpdateAvailableFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/SharedConfig;",
    name = "isAppUpdateAvailable",
    returnType = "Z",
)

private val sharedConfigSetNewAppVersionAvailableFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/SharedConfig;",
    name = "setNewAppVersionAvailable",
    returnType = "Z",
    parameters = listOf("Lorg/telegram/tgnet/TLRPC\$TL_help_appUpdate;"),
)

private val messagesControllerCheckPromoInfoInternalFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessagesController;",
    name = "checkPromoInfoInternal",
    returnType = "V",
    parameters = listOf("Z"),
)

/** Telegram Plus 12.10.3.0 */
private val plusUpdateButtonUpdateFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/plus/update/UpdateButton;",
    name = "update",
    returnType = "V",
    parameters = listOf("Z"),
)

@Suppress("unused")
val telegramDisableAutoUpdatePatch = bytecodePatch(
    name = "Disable auto-update",
    description = "Disables Telegram update availability, update-version storage and proxy sponsor-channel insertion.",
) {
    compatibleWith(
        TELEGRAM_COMPATIBILITY,
        TELEGRAM_PLUS_COMPATIBILITY,
        TELEGRAM_WEB_COMPATIBILITY,
    )
    dependsOn(telegramSpoofDependency())

    execute {
        sharedConfigIsAppUpdateAvailableFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        sharedConfigSetNewAppVersionAvailableFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        messagesControllerCheckPromoInfoInternalFingerprint.method.addInstructions(0, "return-void")

        // Plus 12.10.3.0 has a dedicated update button. Its update() method
        // reads SharedConfig.isAppUpdateAvailable(); make the UI non-reactive
        // as well. Optional so Telegram/Web are unaffected.
        plusUpdateButtonUpdateFingerprint.methodOrNull?.addInstructions(0, "return-void")
    }
}