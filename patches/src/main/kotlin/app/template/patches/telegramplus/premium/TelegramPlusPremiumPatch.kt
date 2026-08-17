package app.template.patches.telegramplus.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.telegramplus.PremiumFeaturesBlockedFingerprint
import app.template.patches.telegram.MessagesControllerIsPremiumUserFingerprint
import app.template.patches.telegram.UserConfigIsPremiumFingerprint

@Suppress("unused")
val telegramPlusPremiumPatch = bytecodePatch(
    name = "Plus Premium",
    description = "Unlocks premium UI and suppresses premium nag dialogs. " +
        "isPremiumUser returns true only for self to avoid showing premium badges on others.",
) {
    compatibleWith(TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // UserConfig.isPremium() → true (current user is premium)
        UserConfigIsPremiumFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // isPremiumUser(User) → true only for self, false for others.
        // This avoids showing premium crown on all contacts (which looks wrong)
        // while still unlocking premium features for the logged-in account.
        MessagesControllerIsPremiumUserFingerprint.method.addInstructions(0, """
            if-eqz p1, :not_self
            iget-boolean v0, p1, Lorg/telegram/tgnet/TLRPC${'$'}User;->self:Z
            if-eqz v0, :not_self
            const/4 v0, 0x1
            return v0
            :not_self
            nop
        """)

        // premiumFeaturesBlocked() → false (suppresses "Get Premium" popups)
        PremiumFeaturesBlockedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
    }
}
