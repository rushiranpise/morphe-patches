package app.template.patches.telegram.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.ChatPullingDownDrawableDrawBottomPanelFingerprint
import app.template.patches.telegram.ChatPullingDownDrawableGetNextFingerprint
import app.template.patches.telegram.ChatPullingDownDrawableNeedDrawBottomPanelFingerprint

@Suppress("unused")
val telegramDisableChannelSwitchingPatch = bytecodePatch(
    name = "Disable channel switching",
    description = "Disables the pull-down gesture that switches to the next unread channel.",
    default = true,
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // getNextUnreadDialog → null (no next dialog to switch to)
        ChatPullingDownDrawableGetNextFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return-object v0
        """)

        // needDrawBottomPanel → false (hide the "swipe down" bottom bar)
        ChatPullingDownDrawableNeedDrawBottomPanelFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // drawBottomPanel → void (skip drawing even if called)
        ChatPullingDownDrawableDrawBottomPanelFingerprint.method.addInstructions(0, "return-void")
    }
}
