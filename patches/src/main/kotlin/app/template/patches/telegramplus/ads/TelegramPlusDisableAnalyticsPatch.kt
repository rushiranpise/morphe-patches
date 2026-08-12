package app.template.patches.telegramplus.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.telegramplus.AnalyticsEnableFingerprint
import app.template.patches.telegramplus.AnalyticsTrackEventFingerprint
import app.template.patches.telegramplus.AnalyticsTrackEventMapFingerprint

@Suppress("unused")
val telegramPlusDisableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Blocks all Firebase analytics and event tracking in Telegram Plus.",
) {
    compatibleWith(TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // Skip analytics setup but leave FirebaseApp.initializeApp() in start() intact
        // so FCM push notifications keep working.
        AnalyticsEnableFingerprint.method.addInstructions(0, "return-void")
        AnalyticsTrackEventFingerprint.method.addInstructions(0, "return-void")
        AnalyticsTrackEventMapFingerprint.method.addInstructions(0, "return-void")
    }
}
