package app.template.patches.telegram.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.telegram.AnalyticsEnableFingerprint
import app.template.patches.telegram.AnalyticsTrackEventFingerprint
import app.template.patches.telegram.AnalyticsTrackEventMapFingerprint

@Suppress("unused")
val telegramPlusDisableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Blocks Firebase analytics and event tracking in Telegram Plus. " +
        "FirebaseApp.initializeApp() is preserved so push notifications keep working.",
) {
    compatibleWith(TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // Skip analytics setup (FirebaseApp.initializeApp stays intact for FCM)
        AnalyticsEnableFingerprint.method.addInstructions(0, "return-void")
        AnalyticsTrackEventFingerprint.method.addInstructions(0, "return-void")
        AnalyticsTrackEventMapFingerprint.method.addInstructions(0, "return-void")
    }
}
