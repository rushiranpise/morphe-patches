package app.template.patches.telegram.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.telegram.MessagesControllerIsPremiumUserFingerprint
import app.template.patches.telegram.PremiumFeaturesBlockedFingerprint
import app.template.patches.telegram.SharedConfigGetDevicePerformanceClassFingerprint
import app.template.patches.telegram.StoriesControllerIsPremiumFingerprint
import app.template.patches.telegram.UserConfigGetMaxAccountCountFingerprint
import app.template.patches.telegram.UserConfigHasPremiumOnAccountsFingerprint
import app.template.patches.telegram.UserConfigIsPremiumFingerprint

@Suppress("unused")
val telegramPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Telegram Premium features for the current account.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // isPremium() for current user → true
        UserConfigIsPremiumFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // isPremiumUser(User):
        // - messenger/web: return true for all users (shows crown everywhere)
        // - Plus: return true only for self to avoid crowns on all contacts
        val isPlusBuild = PremiumFeaturesBlockedFingerprint.methodOrNull != null
        if (isPlusBuild) {
            MessagesControllerIsPremiumUserFingerprint.method.addInstructions(0, """
                if-eqz p1, :not_self
                iget-boolean v0, p1, Lorg/telegram/tgnet/TLRPC${'$'}User;->self:Z
                if-eqz v0, :not_self
                const/4 v0, 0x1
                return v0
                :not_self
                nop
            """)
            // premiumFeaturesBlocked() → false (Plus-only: suppresses "Get Premium" popups)
            PremiumFeaturesBlockedFingerprint.methodOrNull?.addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        } else {
            MessagesControllerIsPremiumUserFingerprint.method.addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """)
        }

        // StoriesController.isPremium(J) → true
        StoriesControllerIsPremiumFingerprint.methodOrNull?.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // hasPremiumOnAccounts → true (cross-account premium check)
        UserConfigHasPremiumOnAccountsFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // getMaxAccountCount → 999 (removes 3-account limit)
        UserConfigGetMaxAccountCountFingerprint.method.addInstructions(0, """
            const/16 v0, 0x3E7
            return v0
        """)

        // getDevicePerformanceClass → 2 (HIGH) for best quality video/animations
        SharedConfigGetDevicePerformanceClassFingerprint.method.addInstructions(0, """
            const/4 v0, 0x2
            return v0
        """)
    }
}
