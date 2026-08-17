package app.template.patches.telegramplus.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.telegramplus.PlusSettingsIsUpdateEnabledFingerprint
import app.template.patches.telegramplus.PlusUpdaterCheckAppUpdateFingerprint
import app.template.patches.telegram.CheckAppUpdateFingerprint

@Suppress("unused")
val telegramPlusDisableAutoUpdatePatch = bytecodePatch(
    name = "Disable auto-update",
    description = "Disables automatic app update checks in Telegram Plus.",
) {
    compatibleWith(TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // Block Plus-specific update checker
        PlusUpdaterCheckAppUpdateFingerprint.method.addInstructions(0, "return-void")

        // Report updates as always disabled
        PlusSettingsIsUpdateEnabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Block LaunchActivity update entry point (shared with Web/regular builds)
        CheckAppUpdateFingerprint.method.addInstructions(0, "return-void")
    }
}
