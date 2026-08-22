package app.template.patches.telegram.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.telegram.BlockingUpdateViewShowFingerprint
import app.template.patches.telegram.CheckAppUpdateFingerprint
import app.template.patches.telegram.MessagesControllerCheckPromoInfoInternalFingerprint
import app.template.patches.telegram.PlusSettingsIsUpdateEnabledFingerprint
import app.template.patches.telegram.PlusUpdaterCheckAppUpdateFingerprint
import app.template.patches.telegram.SharedConfigIsAppUpdateAvailableFingerprint
import app.template.patches.telegram.SharedConfigSetNewAppVersionAvailableFingerprint

@Suppress("unused")
val telegramDisableAutoUpdatePatch = bytecodePatch(
    name = "Disable auto-update",
    description = "Disables automatic app update checks, the blocking update screen, " +
        "and the proxy sponsor channel insertion. On Telegram Plus also disables the " +
        "Plus-specific updater and update settings flag.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // Suppress update checks at the LaunchActivity level
        CheckAppUpdateFingerprint.method.addInstructions(0, "return-void")

        // Block the modal update screen from showing
        BlockingUpdateViewShowFingerprint.method.addInstructions(0, "return-void")

        // SharedConfig level: never report an update as available
        SharedConfigIsAppUpdateAvailableFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Suppress storing new app version info (prevents update banners)
        SharedConfigSetNewAppVersionAvailableFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Suppress proxy sponsor dialog injection into dialogs list
        MessagesControllerCheckPromoInfoInternalFingerprint.method.addInstructions(0, "return-void")

        // Plus-only: block Plus-specific update checker (no-op on messenger/web)
        PlusUpdaterCheckAppUpdateFingerprint.methodOrNull?.addInstructions(0, "return-void")

        // Plus-only: isUpdateEnabled → false (no-op on messenger/web)
        PlusSettingsIsUpdateEnabledFingerprint.methodOrNull?.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
    }
}
