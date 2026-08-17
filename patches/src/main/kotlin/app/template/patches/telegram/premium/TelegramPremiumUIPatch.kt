package app.template.patches.telegram.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.ChatActivityIsSwipeBackEnabledFingerprint
import app.template.patches.telegram.MediaDataControllerLoadPinnedMessagesFingerprint
import app.template.patches.telegram.MessagesControllerStoriesEnabledFingerprint
import app.template.patches.telegram.MessagesControllerStoryEntitiesAllowedFingerprint
import app.template.patches.telegram.MessagesControllerStoryEntitiesAllowedUserFingerprint
import app.template.patches.telegram.PeerStoriesViewAllowScreenshotsFingerprint
import app.template.patches.telegram.ProfileActivityIsSwipeBackEnabledFingerprint
import app.template.patches.telegram.StoriesControllerHasStoriesFingerprint
import app.template.patches.telegram.StoriesControllerMarkStoryAsReadFingerprint
import app.template.patches.telegram.TranslateControllerIsTranslateDialogHiddenFingerprint

@Suppress("unused")
val telegramPremiumUIPatch = bytecodePatch(
    name = "Premium UI",
    description = "Hides story strip, allows saving stories, suppresses story/translate nags, " +
        "enables swipe-back everywhere, and suppresses pinned message server fetches.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // Story strip hidden
        MessagesControllerStoriesEnabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
        StoriesControllerHasStoriesFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Story entities disabled (hides sticker packs / premium story reactions)
        MessagesControllerStoryEntitiesAllowedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
        MessagesControllerStoryEntitiesAllowedUserFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Story read receipt suppressed
        StoriesControllerMarkStoryAsReadFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Allow saving any story
        PeerStoriesViewAllowScreenshotsFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Hide translate nag prompt (true = hidden)
        TranslateControllerIsTranslateDialogHiddenFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Swipe-back: both profile AND chat activities
        ProfileActivityIsSwipeBackEnabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
        ChatActivityIsSwipeBackEnabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Suppress pinned message server fetch
        MediaDataControllerLoadPinnedMessagesFingerprint.method.addInstructions(0, "return-void")
    }
}
